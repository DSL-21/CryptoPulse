package com.dsl21.cryptopulse.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ScannerEngineTest {

    @Test
    fun strongMoveCreatesMomentumObservation() {
        val quote = sampleQuote(change = 5.2, volume = 10_000_000.0, high = 105.0, low = 99.0)
        val signals = ScannerEngine.buildSignals(listOf(quote), emptyList())
        assertTrue(signals.any { it.title == "Strong positive momentum" })
    }

    @Test
    fun highQuoteVolumeCreatesActivityObservation() {
        val quote = sampleQuote(change = 0.4, volume = 150_000_000.0, high = 101.0, low = 99.5)
        val signals = ScannerEngine.buildSignals(listOf(quote), emptyList())
        assertTrue(signals.any { it.title == "High reported trading volume" })
    }

    @Test
    fun completedHourlyVolumeSpikeCreatesObservation() {
        val quote = sampleQuote(change = 0.4, volume = 10_000_000.0, high = 101.0, low = 99.5)
        val pulse = HourlyVolumePulse(
            symbol = "BTC",
            lastClosedHourQuoteVolume = 300_000.0,
            averagePrevious24Hours = 100_000.0,
            relativeVolume = 3.0,
            completedHours = 24
        )
        val signals = ScannerEngine.buildSignals(listOf(quote), listOf(pulse))
        assertTrue(signals.any { it.title == "Hourly volume spike" })
    }

    @Test
    fun hourlyVolumeBelowThresholdDoesNotCreateSpike() {
        val quote = sampleQuote(change = 0.4, volume = 10_000_000.0, high = 101.0, low = 99.5)
        val pulse = HourlyVolumePulse(
            symbol = "BTC",
            lastClosedHourQuoteVolume = 200_000.0,
            averagePrevious24Hours = 100_000.0,
            relativeVolume = 2.0,
            completedHours = 24
        )
        val signals = ScannerEngine.buildSignals(listOf(quote), listOf(pulse))
        assertTrue(signals.none { it.title == "Hourly volume spike" })
    }

    @Test
    fun emptyMarketProducesNoSignals() {
        assertEquals(0, ScannerEngine.buildSignals(emptyList(), emptyList()).size)
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
