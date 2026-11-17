package com.dsl21.cryptopulse.data

object ScannerEngine {
    /** Transparent observations, not forecasts or recommendations to buy or sell. */
    fun buildSignals(quotes: List<AssetQuote>): List<ScannerSignal> {
        val signals = mutableListOf<ScannerSignal>()
        quotes.forEach { quote ->
            val primary = quote.primary
            val change = primary.changePercent24h
            if (change >= 4.0) {
                signals += ScannerSignal(
                    quote.asset.symbol, quote.asset.name, "Strong positive momentum",
                    "24h price change is " + formatPercent(change) + " on " + primary.exchange + ".",
                    SignalDirection.BULLISH
                )
            } else if (change <= -4.0) {
                signals += ScannerSignal(
                    quote.asset.symbol, quote.asset.name, "Strong negative momentum",
                    "24h price change is " + formatPercent(change) + " on " + primary.exchange + ".",
                    SignalDirection.BEARISH
                )
            }
            if (primary.quoteVolume24h >= 100_000_000.0) {
                signals += ScannerSignal(
                    quote.asset.symbol, quote.asset.name, "High 24h trading activity",
                    "Reported quote volume is " + formatCompact(primary.quoteVolume24h) + " " + primary.quoteCurrency + ".",
                    SignalDirection.NEUTRAL
                )
            }
            if (primary.lastPrice > 0.0) {
                val rangePercent = ((primary.high24h - primary.low24h) / primary.lastPrice) * 100.0
                if (rangePercent >= 7.0) {
                    signals += ScannerSignal(
                        quote.asset.symbol, quote.asset.name, "Wide 24h trading range",
                        "The reported high-to-low range is " + formatPercent(rangePercent) + " of the latest price.",
                        SignalDirection.NEUTRAL
                    )
                }
            }
        }
        return signals.sortedWith(
            compareBy<ScannerSignal> {
                when (it.direction) {
                    SignalDirection.BULLISH -> 0
                    SignalDirection.BEARISH -> 1
                    SignalDirection.NEUTRAL -> 2
                }
            }.thenBy { it.symbol }
        )
    }

    private fun formatPercent(value: Double) = String.format(java.util.Locale.US, "%+.2f%%", value)

    private fun formatCompact(value: Double): String = when {
        value >= 1_000_000_000.0 -> String.format(java.util.Locale.US, "%.2fB", value / 1_000_000_000.0)
        value >= 1_000_000.0 -> String.format(java.util.Locale.US, "%.1fM", value / 1_000_000.0)
        else -> String.format(java.util.Locale.US, "%.0f", value)
    }
}
