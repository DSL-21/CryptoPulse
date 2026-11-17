package com.dsl21.cryptopulse.data

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NetworkSecurityTest {

    @Test
    fun apiUrlsRequireHttpsAndAnExactAllowlistedHost() {
        val allowed = setOf("api.kraken.com")

        assertTrue(isAllowedHttpsApiUrl("https://api.kraken.com/0/public/Ticker", allowed))
        assertFalse(isAllowedHttpsApiUrl("http://api.kraken.com/0/public/Ticker", allowed))
        assertFalse(isAllowedHttpsApiUrl("https://api.kraken.com.attacker.invalid/data", allowed))
        assertFalse(isAllowedHttpsApiUrl("https://attacker.invalid@api.kraken.com/data", allowed))
        assertFalse(isAllowedHttpsApiUrl("https://api.kraken.com:8443/data", allowed))
        assertFalse(isAllowedHttpsApiUrl("https://api.kraken.com/data#fragment", allowed))
        assertFalse(isAllowedHttpsApiUrl("https://api.binance.com/data", allowed))
    }

    @Test
    fun dexPathSegmentsRejectUrlSyntax() {
        assertTrue(isValidDexChainId("ethereum"))
        assertTrue(isValidDexChainId("arbitrum-one_1"))
        assertFalse(isValidDexChainId("../ethereum"))
        assertFalse(isValidDexChainId("ethereum/other"))
        assertFalse(isValidDexChainId(""))

        assertTrue(isValidDexTokenAddress("0x1234567890abcdef1234567890abcdef12345678"))
        assertTrue(isValidDexTokenAddress("So11111111111111111111111111111111111111112"))
        assertFalse(isValidDexTokenAddress("../../private"))
        assertFalse(isValidDexTokenAddress("0x1234?redirect=attacker"))
        assertFalse(isValidDexTokenAddress("short"))
    }

    @Test
    fun dexLinksMustUseTheCanonicalHttpsHost() {
        assertTrue(isSafeDexScreenerUrl("https://dexscreener.com/ethereum/0x1234"))
        assertTrue(isSafeDexScreenerUrl("https://dexscreener.com/solana/abc?embed=1"))
        assertFalse(isSafeDexScreenerUrl("http://dexscreener.com/ethereum/pair"))
        assertFalse(isSafeDexScreenerUrl("https://dexscreener.com.attacker.invalid/ethereum/pair"))
        assertFalse(isSafeDexScreenerUrl("https://dexscreener.com@attacker.invalid/ethereum/pair"))
        assertFalse(isSafeDexScreenerUrl("https://dexscreener.com:8443/ethereum/pair"))
        assertFalse(isSafeDexScreenerUrl("https://dexscreener.com/ethereum/pair#fragment"))
    }
}
