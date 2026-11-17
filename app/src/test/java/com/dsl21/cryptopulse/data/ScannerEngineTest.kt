package com.dsl21.cryptopulse.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ScannerEngineTest {

    @Test
    fun strongMoveCreatesMomentumObservation() {
        val quote = sampleQuote(change = 5.2, volume = 10_000_000.0, high = 105.0, low = 99.0)
        val signals = ScannerEngine.buildSignals(listOf(quote))
        assertTrue(signals.any { it.title == "Strong positive momentum" })
    }

    @Test
    fun highQuoteVolumeCreatesActivityObservation() {
        val quote = sampleQuote(change = 0.4, volume = 150_000_000.0, high = 101.0, low = 99.5)
        val signals = ScannerEngine.buildSignals(listOf(quote))
        assertTrue(signals.any { it.title == "High 24h trading activity" })
    }

    @Test
    fun emptyMarketProducesNoSignals() {
        assertEquals(0, ScannerEngine.buildSignals(emptyList()).size)
    }

    private fun sampleQuote(change: Double, volume: Double, high: Double, low: Double) =
        AssetQuote(
            asset = AssetInfo("BTC", "Bitcoin", "BTCUSDT"),
            exchanges = listOf(
                ExchangeQuote(
                    exchange = "Test",
                    lastPrice = 100.0,
                    quoteCurrency = "USDT",
                    changePercent24h = change,
                    high24h = high,
                    low24h = low,
                    quoteVolume24h = volume
                )
            )
        )
}
