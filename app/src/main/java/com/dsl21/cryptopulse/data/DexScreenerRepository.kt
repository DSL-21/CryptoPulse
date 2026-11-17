package com.dsl21.cryptopulse.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class DexScreenerRepository {

    private var cachedPairs: List<DexPair> = emptyList()
    private var lastRefreshAt: Long = 0L

    suspend fun refresh(force: Boolean = false): List<DexPair> {
        val now = System.currentTimeMillis()
        if (!force && cachedPairs.isNotEmpty() && now - lastRefreshAt < CACHE_MS) {
            return cachedPairs
        }

        val profiles = withContext(Dispatchers.IO) { fetchLatestProfiles() }
        val pairsByProfile = coroutineScope {
            profiles.take(MAX_PROFILES).map { profile ->
                async(Dispatchers.IO) {
                    runCatching { fetchBestPair(profile) }.getOrNull()
                }
            }.awaitAll()
        }

        val refreshed = pairsByProfile.filterNotNull()
            .distinctBy { it.chainId + ":" + it.pairAddress }
            .sortedWith(
                compareByDescending<DexPair> { it.volumeH1 ?: 0.0 }
                    .thenByDescending { it.liquidityUsd ?: 0.0 }
            )

        if (refreshed.isEmpty()) {
            throw IOException("DEX Screener returned no token pairs with usable market data")
        }

        cachedPairs = refreshed
        lastRefreshAt = now
        return cachedPairs
    }

    private fun fetchLatestProfiles(): List<TokenProfile> {
        val json = JSONArray(
            requestText("https://api.dexscreener.com/token-profiles/latest/v1")
        )
        val seen = mutableSetOf<String>()
        val profiles = mutableListOf<TokenProfile>()

        for (index in 0 until json.length()) {
            val item = json.optJSONObject(index) ?: continue
            val chainId = item.optString("chainId").trim()
            val tokenAddress = item.optString("tokenAddress").trim()

            // These values become URL path segments below. Reject URL syntax before it
            // can influence the request path; do not try to sanitize it after interpolation.
            if (!isValidDexChainId(chainId) || !isValidDexTokenAddress(tokenAddress)) continue

            val key = chainId + ":" + tokenAddress.lowercase()
            if (seen.add(key)) {
                profiles += TokenProfile(chainId, tokenAddress)
            }
            if (profiles.size >= MAX_PROFILES) break
        }

        if (profiles.isEmpty()) throw IOException("DEX Screener returned no valid recent token profiles")
        return profiles
    }

    private fun fetchBestPair(profile: TokenProfile): DexPair? {
        if (!isValidDexChainId(profile.chainId) ||
            !isValidDexTokenAddress(profile.tokenAddress)
        ) return null

        val url = "https://api.dexscreener.com/token-pairs/v1/" +
            profile.chainId + "/" + profile.tokenAddress
        val json = JSONArray(requestText(url))
        val candidates = mutableListOf<DexPair>()

        for (index in 0 until json.length()) {
            val pair = json.optJSONObject(index) ?: continue
            val base = pair.optJSONObject("baseToken") ?: continue
            val quote = pair.optJSONObject("quoteToken") ?: continue
            val baseAddress = base.optString("address")
            val quoteAddress = quote.optString("address")
            val tokenMatches = baseAddress.equals(profile.tokenAddress, ignoreCase = true) ||
                quoteAddress.equals(profile.tokenAddress, ignoreCase = true)
            if (!tokenMatches) continue

            val priceUsd = pair.number("priceUsd") ?: continue
            if (priceUsd <= 0.0) continue

            val selectedToken = if (baseAddress.equals(profile.tokenAddress, ignoreCase = true)) base else quote
            val volume = pair.optJSONObject("volume")
            val change = pair.optJSONObject("priceChange")
            val liquidity = pair.optJSONObject("liquidity")
            val boosts = pair.optJSONObject("boosts")

            candidates += DexPair(
                chainId = pair.optString("chainId", profile.chainId),
                dexId = pair.optString("dexId", "Unknown DEX"),
                pairAddress = pair.optString("pairAddress"),
                tokenName = selectedToken.optString("name", "Unknown token"),
                tokenSymbol = selectedToken.optString("symbol", "???"),
                quoteSymbol = if (selectedToken === base) quote.optString("symbol", "") else base.optString("symbol", ""),
                priceUsd = priceUsd,
                changeM5 = change?.number("m5"),
                changeH1 = change?.number("h1"),
                changeH6 = change?.number("h6"),
                changeH24 = change?.number("h24"),
                volumeM5 = volume?.number("m5"),
                volumeH1 = volume?.number("h1"),
                volumeH24 = volume?.number("h24"),
                liquidityUsd = liquidity?.number("usd"),
                fdvUsd = pair.number("fdv"),
                pairCreatedAt = pair.optLong("pairCreatedAt").takeIf { it > 0L },
                dexUrl = pair.optString("url"),
                boostsActive = boosts?.optInt("active", 0) ?: 0
            )
        }

        return candidates.maxByOrNull { it.liquidityUsd ?: 0.0 }
    }

    private fun requestText(url: String): String =
        SecureHttp.getJson(url, setOf("api.dexscreener.com"))

    private fun JSONObject.number(key: String): Double? =
        optString(key).toDoubleOrNull()?.takeIf { it.isFinite() }

    private data class TokenProfile(
        val chainId: String,
        val tokenAddress: String
    )

    private companion object {
        const val MAX_PROFILES = 12
        const val CACHE_MS = 5 * 60 * 1000L
    }
}
