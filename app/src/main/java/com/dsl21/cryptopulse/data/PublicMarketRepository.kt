package com.dsl21.cryptopulse.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class PublicMarketRepository {

    suspend fun refresh(): MarketSnapshot = coroutineScope {
        val binanceTask = async(Dispatchers.IO) { runCatching { fetchBinanceQuotes() } }
        val krakenTask = async(Dispatchers.IO) { runCatching { fetchKrakenQuotes() } }
        val revolutTradesTask = async(Dispatchers.IO) { runCatching { fetchRevolutTrades() } }
        val revolutBookTask = async(Dispatchers.IO) { runCatching { fetchRevolutBtcOrderBook() } }

        val binance = binanceTask.await()
        val kraken = krakenTask.await()
        val revolutTrades = revolutTradesTask.await()
        val revolutBook = revolutBookTask.await()

        val quotes = AssetCatalog.assets.mapNotNull { asset ->
            val exchangeQuotes = listOfNotNull(
                binance.getOrNull()?.get(asset.symbol),
                kraken.getOrNull()?.get(asset.symbol)
            )
            if (exchangeQuotes.isEmpty()) null else AssetQuote(asset, exchangeQuotes)
        }.sortedByDescending { it.primary.quoteVolume24h }

        MarketSnapshot(
            quotes = quotes,
            revolutTrades = revolutTrades.getOrDefault(emptyList()),
            revolutBtcOrderBook = revolutBook.getOrNull(),
            sources = listOf(
                SourceStatus("Binance Spot", binance.isSuccess, binance.fold(
                    { it.size.toString() + " tracked markets · 24h statistics" },
                    { "Unavailable: " + (it.message ?: "request failed") }
                )),
                SourceStatus("Kraken", kraken.isSuccess, kraken.fold(
                    { it.size.toString() + " tracked markets · public ticker" },
                    { "Unavailable: " + (it.message ?: "request failed") }
                )),
                SourceStatus(
                    "Revolut X",
                    revolutTrades.isSuccess || revolutBook.isSuccess,
                    if (revolutTrades.isSuccess || revolutBook.isSuccess)
                        "Public recent trades and BTC-USD order book"
                    else "Unavailable: public market-data request failed"
                )
            ),
            updatedAt = System.currentTimeMillis()
        )
    }

    suspend fun fetchHourlyCloses(asset: AssetInfo): List<Double> = withContext(Dispatchers.IO) {
        val url = "https://data-api.binance.vision/api/v3/klines" +
            "?symbol=" + asset.binanceSymbol + "&interval=1h&limit=48"
        val candles = JSONArray(requestText(url))
        buildList {
            for (index in 0 until candles.length()) {
                val candle = candles.optJSONArray(index) ?: continue
                candle.optString(4).toDoubleOrNull()?.let(::add)
            }
        }
    }

    private fun fetchBinanceQuotes(): Map<String, ExchangeQuote> {
        val symbolsJson = AssetCatalog.assets.joinToString(prefix = "[", postfix = "]") {
            "\"" + it.binanceSymbol + "\""
        }
        val encodedSymbols = URLEncoder.encode(symbolsJson, StandardCharsets.UTF_8.name())
        val json = JSONArray(requestText(
            "https://data-api.binance.vision/api/v3/ticker/24hr?symbols=" + encodedSymbols
        ))
        val assetByPair = AssetCatalog.assets.associateBy { it.binanceSymbol }
        val result = mutableMapOf<String, ExchangeQuote>()

        for (index in 0 until json.length()) {
            val item = json.optJSONObject(index) ?: continue
            val asset = assetByPair[item.optString("symbol")] ?: continue
            val price = item.number("lastPrice") ?: continue
            result[asset.symbol] = ExchangeQuote(
                exchange = "Binance",
                lastPrice = price,
                quoteCurrency = "USDT",
                changePercent24h = item.number("priceChangePercent") ?: 0.0,
                high24h = item.number("highPrice") ?: price,
                low24h = item.number("lowPrice") ?: price,
                quoteVolume24h = item.number("quoteVolume") ?: 0.0
            )
        }
        if (result.isEmpty()) throw IOException("Binance returned no tracked markets")
        return result
    }

    private fun fetchKrakenQuotes(): Map<String, ExchangeQuote> {
        val root = JSONObject(requestText(
            "https://api.kraken.com/0/public/Ticker?pair=XBTUSD,ETHUSD,SOLUSD"
        ))
        val errors = root.optJSONArray("error")
        if (errors != null && errors.length() > 0) throw IOException(errors.optString(0))

        val resultObject = root.optJSONObject("result")
            ?: throw IOException("Kraken response did not contain result data")
        val result = mutableMapOf<String, ExchangeQuote>()
        val keys = resultObject.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            val symbol = when {
                key.contains("XBT", ignoreCase = true) -> "BTC"
                key.contains("ETH", ignoreCase = true) -> "ETH"
                key.contains("SOL", ignoreCase = true) -> "SOL"
                else -> continue
            }
            val ticker = resultObject.optJSONObject(key) ?: continue
            val last = ticker.optJSONArray("c")?.optString(0)?.toDoubleOrNull() ?: continue
            val open = ticker.number("o") ?: last
            val baseVolume = ticker.optJSONArray("v")?.optString(1)?.toDoubleOrNull() ?: 0.0
            val high = ticker.optJSONArray("h")?.optString(1)?.toDoubleOrNull() ?: last
            val low = ticker.optJSONArray("l")?.optString(1)?.toDoubleOrNull() ?: last
            val change = if (open > 0.0) ((last / open) - 1.0) * 100.0 else 0.0
            result[symbol] = ExchangeQuote(
                exchange = "Kraken",
                lastPrice = last,
                quoteCurrency = "USD",
                changePercent24h = change,
                high24h = high,
                low24h = low,
                quoteVolume24h = baseVolume * last
            )
        }
        if (result.isEmpty()) throw IOException("Kraken returned no supported markets")
        return result
    }

    private fun fetchRevolutTrades(): List<RecentTrade> {
        val root = JSONObject(requestText("https://revx.revolut.com/api/2.0/public/last-trades"))
        val data = root.optJSONArray("data") ?: JSONArray()
        return buildList {
            for (index in 0 until data.length()) {
                val item = data.optJSONObject(index) ?: continue
                val priceCurrency = item.optString("pc")
                if (priceCurrency !in setOf("USD", "USDC", "USDT", "EUR")) continue
                val price = item.number("p") ?: continue
                add(RecentTrade(
                    assetSymbol = item.optString("aid", "—"),
                    assetName = item.optString("anm", item.optString("aid", "Unknown")),
                    price = price,
                    priceCurrency = priceCurrency,
                    quantity = item.number("q") ?: 0.0,
                    quantityCurrency = item.optString("qc", item.optString("aid")),
                    timestamp = item.optString("tdt")
                ))
            }
        }.take(40)
    }

    private fun fetchRevolutBtcOrderBook(): OrderBookSummary {
        val root = JSONObject(requestText(
            "https://revx.revolut.com/api/2.0/public/order-book/BTC-USD"
        ))
        val data = root.optJSONObject("data") ?: throw IOException("Missing order-book data")
        val bids = data.optJSONArray("bids") ?: JSONArray()
        val asks = data.optJSONArray("asks") ?: JSONArray()
        val bid = bids.optJSONObject(0)?.number("p") ?: throw IOException("No BTC bid level returned")
        val ask = asks.optJSONObject(0)?.number("p") ?: throw IOException("No BTC ask level returned")
        val midpoint = (bid + ask) / 2.0
        val spread = if (midpoint > 0.0) ((ask - bid) / midpoint) * 100.0 else 0.0
        return OrderBookSummary(
            bidPrice = bid,
            askPrice = ask,
            spreadPercent = spread,
            timestamp = root.optJSONObject("metadata")?.optString("timestamp").orEmpty()
        )
    }

    private fun requestText(url: String): String {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 12_000
        connection.readTimeout = 12_000
        connection.instanceFollowRedirects = true
        connection.setRequestProperty("Accept", "application/json")
        connection.setRequestProperty("User-Agent", "CryptoPulse-Android/0.1.0")
        try {
            val status = connection.responseCode
            val stream = if (status in 200..299) connection.inputStream else connection.errorStream
            val body = stream?.bufferedReader(StandardCharsets.UTF_8)?.use { it.readText() }.orEmpty()
            if (status !in 200..299) {
                throw IOException("HTTP " + status + " from " + URL(url).host + ": " + body.take(160))
            }
            return body
        } finally {
            connection.disconnect()
        }
    }

    private fun JSONObject.number(key: String): Double? =
        optString(key).toDoubleOrNull()?.takeIf { it.isFinite() }
}
