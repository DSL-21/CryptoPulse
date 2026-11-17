package com.dsl21.cryptopulse.data

data class AssetInfo(
    val symbol: String,
    val name: String,
    val binanceSymbol: String
)

object AssetCatalog {
    val assets = listOf(
        AssetInfo("BTC", "Bitcoin", "BTCUSDT"),
        AssetInfo("ETH", "Ethereum", "ETHUSDT"),
        AssetInfo("BNB", "BNB", "BNBUSDT"),
        AssetInfo("SOL", "Solana", "SOLUSDT"),
        AssetInfo("XRP", "XRP", "XRPUSDT"),
        AssetInfo("ADA", "Cardano", "ADAUSDT"),
        AssetInfo("DOGE", "Dogecoin", "DOGEUSDT"),
        AssetInfo("LINK", "Chainlink", "LINKUSDT"),
        AssetInfo("AVAX", "Avalanche", "AVAXUSDT"),
        AssetInfo("LTC", "Litecoin", "LTCUSDT")
    )
}

data class ExchangeQuote(
    val exchange: String,
    val lastPrice: Double,
    val quoteCurrency: String,
    val changePercent24h: Double,
    val high24h: Double,
    val low24h: Double,
    val quoteVolume24h: Double,
    val changeWindowLabel: String = "24h",
    val quoteVolumeIsEstimate: Boolean = false
)

data class AssetQuote(val asset: AssetInfo, val exchanges: List<ExchangeQuote>) {
    val primary: ExchangeQuote get() = exchanges.first()
}

data class HourlyVolumePulse(
    val symbol: String,
    val lastClosedHourQuoteVolume: Double,
    val averagePrevious24Hours: Double,
    val relativeVolume: Double,
    val completedHours: Int
)

data class RecentTrade(
    val assetSymbol: String,
    val assetName: String,
    val price: Double,
    val priceCurrency: String,
    val quantity: Double,
    val quantityCurrency: String,
    val timestamp: String
)

data class OrderBookSummary(
    val bidPrice: Double,
    val askPrice: Double,
    val spreadPercent: Double,
    val timestamp: String
)

data class SourceStatus(
    val name: String,
    val available: Boolean,
    val detail: String
)

data class MarketSnapshot(
    val quotes: List<AssetQuote>,
    val hourlyVolumePulses: List<HourlyVolumePulse>,
    val revolutTrades: List<RecentTrade>,
    val revolutBtcOrderBook: OrderBookSummary?,
    val sources: List<SourceStatus>,
    val updatedAt: Long
) {
    companion object {
        fun empty() = MarketSnapshot(
            quotes = emptyList(),
            hourlyVolumePulses = emptyList(),
            revolutTrades = emptyList(),
            revolutBtcOrderBook = null,
            sources = emptyList(),
            updatedAt = 0L
        )
    }
}

data class ScannerSignal(
    val symbol: String,
    val name: String,
    val title: String,
    val detail: String,
    val direction: SignalDirection,
    val observedValue: Double? = null
)

enum class SignalDirection { BULLISH, BEARISH, NEUTRAL }

data class DexPair(
    val chainId: String,
    val dexId: String,
    val pairAddress: String,
    val tokenName: String,
    val tokenSymbol: String,
    val quoteSymbol: String,
    val priceUsd: Double,
    val changeM5: Double?,
    val changeH1: Double?,
    val changeH6: Double?,
    val changeH24: Double?,
    val volumeM5: Double?,
    val volumeH1: Double?,
    val volumeH24: Double?,
    val liquidityUsd: Double?,
    val fdvUsd: Double?,
    val pairCreatedAt: Long?,
    val dexUrl: String,
    val boostsActive: Int
) {
    val pairAgeHours: Double?
        get() = pairCreatedAt?.let {
            ((System.currentTimeMillis() - it).coerceAtLeast(0L)) / 3_600_000.0
        }
}

enum class AlertDirection { ABOVE, BELOW }

data class PriceAlertRule(
    val id: String,
    val symbol: String,
    val direction: AlertDirection,
    val targetPrice: Double,
    val enabled: Boolean = true,
    val triggeredAt: Long? = null
)
