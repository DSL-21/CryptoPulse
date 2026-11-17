package com.dsl21.cryptopulse.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AlertEngineTest {

    @Test
    fun aboveAlertTriggersAtOrAboveTarget() {
        val rules = listOf(
            PriceAlertRule("a", "BTC", AlertDirection.ABOVE, 100_000.0)
        )
        val hits = AlertEngine.evaluate(rules, listOf(quote("BTC", 100_000.0)))
        assertEquals(1, hits.size)
        assertEquals("BTC", hits.first().rule.symbol)
    }

    @Test
    fun belowAlertTriggersAtOrBelowTarget() {
        val rules = listOf(
            PriceAlertRule("b", "ETH", AlertDirection.BELOW, 3_000.0)
        )
        val hits = AlertEngine.evaluate(rules, listOf(quote("ETH", 2_950.0)))
        assertEquals(1, hits.size)
    }

    @Test
    fun disabledRuleDoesNotTrigger() {
        val rules = listOf(
            PriceAlertRule("c", "BTC", AlertDirection.ABOVE, 90_000.0, enabled = false)
        )
        assertTrue(AlertEngine.evaluate(rules, listOf(quote("BTC", 100_000.0))).isEmpty())
    }

    @Test
    fun missingAssetDoesNotTrigger() {
        val rules = listOf(
            PriceAlertRule("d", "DOGE", AlertDirection.ABOVE, 1.0)
        )
        assertTrue(AlertEngine.evaluate(rules, listOf(quote("BTC", 100_000.0))).isEmpty())
    }

    private fun quote(symbol: String, price: Double) = AssetQuote(
        asset = AssetInfo(symbol, symbol, symbol + "USDT"),
        exchanges = listOf(
            ExchangeQuote(
                exchange = "Test",
                lastPrice = price,
                quoteCurrency = "USDT",
                changePercent24h = 0.0,
                high24h = price,
                low24h = price,
                quoteVolume24h = 0.0
            )
        )
    )
}
