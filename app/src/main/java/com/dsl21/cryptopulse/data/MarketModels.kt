package com.dsl21.cryptopulse.data

data class AssetInfo(val symbol: String, val name: String, val binanceSymbol: String)

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
    val quoteVolume24h: Double
)

data class AssetQuote(val asset: AssetInfo, val exchanges: List<ExchangeQuote>) {
    val primary: ExchangeQuote get() = exchanges.first()
}

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

data class SourceStatus(val name: String, val available: Boolean, val detail: String)

data class MarketSnapshot(
    val quotes: List<AssetQuote>,
    val revolutTrades: List<RecentTrade>,
    val revolutBtcOrderBook: OrderBookSummary?,
    val sources: List<SourceStatus>,
    val updatedAt: Long
) {
    companion object {
        fun empty() = MarketSnapshot(emptyList(), emptyList(), null, emptyList(), 0L)
    }
}

data class ScannerSignal(
    val symbol: String,
    val name: String,
    val title: String,
    val detail: String,
    val direction: SignalDirection
)

enum class SignalDirection { BULLISH, BEARISH, NEUTRAL }
