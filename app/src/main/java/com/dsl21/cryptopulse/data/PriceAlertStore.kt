package com.dsl21.cryptopulse.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class PriceAlertStore(context: Context) {
    private val preferences = context.getSharedPreferences("cryptopulse_alerts", Context.MODE_PRIVATE)

    fun load(): List<PriceAlertRule> {
        val raw = preferences.getString(KEY_ALERTS, "[]") ?: "[]"
        return runCatching {
            val json = JSONArray(raw)
            buildList {
                for (index in 0 until json.length()) {
                    val item = json.optJSONObject(index) ?: continue
                    val symbol = item.optString("symbol")
                    val target = item.optDouble("targetPrice", Double.NaN)
                    if (symbol.isBlank() || !target.isFinite() || target <= 0.0) continue
                    val direction = runCatching {
                        AlertDirection.valueOf(item.optString("direction"))
                    }.getOrDefault(AlertDirection.ABOVE)
                    add(
                        PriceAlertRule(
                            id = item.optString("id"),
                            symbol = symbol,
                            direction = direction,
                            targetPrice = target,
                            enabled = item.optBoolean("enabled", true),
                            triggeredAt = item.optLong("triggeredAt").takeIf { it > 0L }
                        )
                    )
                }
            }
        }.getOrDefault(emptyList())
    }

    fun add(rule: PriceAlertRule): List<PriceAlertRule> {
        val updated = load().filterNot { it.id == rule.id } + rule
        save(updated)
        return updated
    }

    fun remove(id: String): List<PriceAlertRule> {
        val updated = load().filterNot { it.id == id }
        save(updated)
        return updated
    }

    fun markTriggered(ids: Set<String>): List<PriceAlertRule> {
        val now = System.currentTimeMillis()
        val updated = load().map { rule ->
            if (rule.id in ids && rule.enabled) {
                rule.copy(enabled = false, triggeredAt = now)
            } else rule
        }
        save(updated)
        return updated
    }

    private fun save(rules: List<PriceAlertRule>) {
        val json = JSONArray()
        rules.forEach { rule ->
            json.put(
                JSONObject()
                    .put("id", rule.id)
                    .put("symbol", rule.symbol)
                    .put("direction", rule.direction.name)
                    .put("targetPrice", rule.targetPrice)
                    .put("enabled", rule.enabled)
                    .put("triggeredAt", rule.triggeredAt ?: 0L)
            )
        }
        preferences.edit().putString(KEY_ALERTS, json.toString()).apply()
    }

    private companion object {
        const val KEY_ALERTS = "rules"
    }
}

data class PriceAlertHit(
    val rule: PriceAlertRule,
    val currentPrice: Double,
    val quoteCurrency: String
)

object AlertEngine {
    fun evaluate(
        rules: List<PriceAlertRule>,
        quotes: List<AssetQuote>
    ): List<PriceAlertHit> {
        val bySymbol = quotes.associateBy { it.asset.symbol }
        return rules.filter { it.enabled }.mapNotNull { rule ->
            val quote = bySymbol[rule.symbol] ?: return@mapNotNull null
            val current = quote.primary.lastPrice
            val triggered = when (rule.direction) {
                AlertDirection.ABOVE -> current >= rule.targetPrice
                AlertDirection.BELOW -> current <= rule.targetPrice
            }
            if (triggered) {
                PriceAlertHit(rule, current, quote.primary.quoteCurrency)
            } else null
        }
    }
}
