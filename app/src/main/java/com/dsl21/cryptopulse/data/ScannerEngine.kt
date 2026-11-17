package com.dsl21.cryptopulse.data

object ScannerEngine {

    /**
     * Deterministic market observations. Signals describe measured exchange data;
     * they are not forecasts, trading recommendations, or evidence that a token is safe.
     */
    fun buildSignals(
        quotes: List<AssetQuote>,
        volumePulses: List<HourlyVolumePulse>
    ): List<ScannerSignal> {
        val signals = mutableListOf<ScannerSignal>()
        val pulseBySymbol = volumePulses.associateBy { it.symbol }

        quotes.forEach { quote ->
            val primary = quote.primary
            val change = primary.changePercent24h

            if (change >= 4.0) {
                signals += ScannerSignal(
                    quote.asset.symbol,
                    quote.asset.name,
                    "Strong positive momentum",
                    primary.changeWindowLabel + " price change is " + formatPercent(change) +
                        " on " + primary.exchange + ".",
                    SignalDirection.BULLISH,
                    change
                )
            } else if (change <= -4.0) {
                signals += ScannerSignal(
                    quote.asset.symbol,
                    quote.asset.name,
                    "Strong negative momentum",
                    primary.changeWindowLabel + " price change is " + formatPercent(change) +
                        " on " + primary.exchange + ".",
                    SignalDirection.BEARISH,
                    change
                )
            }

            if (primary.quoteVolume24h >= 100_000_000.0) {
                val estimatePrefix = if (primary.quoteVolumeIsEstimate) "Estimated " else ""
                signals += ScannerSignal(
                    quote.asset.symbol,
                    quote.asset.name,
                    "High reported trading volume",
                    estimatePrefix + "quote volume is " + formatCompact(primary.quoteVolume24h) +
                        " " + primary.quoteCurrency + " on " + primary.exchange + ".",
                    SignalDirection.NEUTRAL,
                    primary.quoteVolume24h
                )
            }

            if (primary.lastPrice > 0.0) {
                val rangePercent = ((primary.high24h - primary.low24h) / primary.lastPrice) * 100.0
                if (rangePercent >= 7.0) {
                    signals += ScannerSignal(
                        quote.asset.symbol,
                        quote.asset.name,
                        "Wide 24h trading range",
                        "The reported high-to-low range is " + formatPercent(rangePercent) +
                            " of the latest price on " + primary.exchange + ".",
                        SignalDirection.NEUTRAL,
                        rangePercent
                    )
                }
            }

            val pulse = pulseBySymbol[quote.asset.symbol]
            if (pulse != null &&
                pulse.relativeVolume >= 2.5 &&
                pulse.lastClosedHourQuoteVolume >= 100_000.0
            ) {
                signals += ScannerSignal(
                    quote.asset.symbol,
                    quote.asset.name,
                    "Hourly volume spike",
                    "The last completed Binance 1h candle traded " +
                        String.format(java.util.Locale.US, "%.2fx", pulse.relativeVolume) +
                        " its previous-24h hourly average.",
                    SignalDirection.NEUTRAL,
                    pulse.relativeVolume
                )
            }
        }

        return signals.sortedWith(
            compareBy<ScannerSignal> {
                when (it.direction) {
                    SignalDirection.BULLISH -> 0
                    SignalDirection.BEARISH -> 1
                    SignalDirection.NEUTRAL -> 2
                }
            }.thenByDescending { it.observedValue ?: 0.0 }
                .thenBy { it.symbol }
        )
    }

    private fun formatPercent(value: Double): String =
        String.format(java.util.Locale.US, "%+.2f%%", value)

    private fun formatCompact(value: Double): String = when {
        value >= 1_000_000_000.0 ->
            String.format(java.util.Locale.US, "%.2fB", value / 1_000_000_000.0)
        value >= 1_000_000.0 ->
            String.format(java.util.Locale.US, "%.1fM", value / 1_000_000.0)
        else -> String.format(java.util.Locale.US, "%.0f", value)
    }
}
