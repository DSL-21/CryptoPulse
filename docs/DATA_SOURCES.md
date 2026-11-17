# CryptoPulse public market data sources

CryptoPulse is designed to work without user accounts, personal API keys, or a paid backend. It calls public market-data endpoints directly from the Android app.

## Binance Spot

- Endpoint: \`https://data-api.binance.vision/api/v3/ticker/24hr\`
- Purpose: 24-hour price change, last price, high, low and quote volume for a tracked set of USDT spot pairs.
- Authentication: none for market-data-only endpoints.
- Documentation: https://github.com/binance/binance-spot-api-docs/blob/master/faqs/market_data_only.md

## Kraken

- Endpoint: \`https://api.kraken.com/0/public/Ticker\`
- Purpose: public ticker snapshots for BTC/USD, ETH/USD and SOL/USD where those pairs are returned by the exchange.
- Authentication: none for public market-data endpoints.
- Documentation: https://docs.kraken.com/api/docs/rest-api/get-ticker-information/

## Revolut X

- Recent public trades: \`https://revx.revolut.com/api/2.0/public/last-trades\`
- BTC order book: \`https://revx.revolut.com/api/2.0/public/order-book/BTC-USD\`
- Purpose: recent public trades and up to five price levels per side for the requested public order book.
- Authentication: none for these documented public endpoints.
- Important limitation: Revolut X's all-pairs \`/tickers\` endpoint is authenticated. CryptoPulse does not call it and does not claim that the public trade tape is a complete trade history or a full-market ticker feed.
- Public rate limit documented by Revolut X: 20 requests per 10 seconds for public recent-trades and order-book endpoints.
- Documentation: https://developer.revolut.com/docs/guides/crypto-exchange/get-started/make-your-first-request
- API reference: https://github.com/revolut-engineering/revolut-x-api/blob/master/revolut-x-api-for-llm.md

## Data interpretation

- Binance tracked spot prices are quoted in USDT. Kraken prices are quoted in USD. The app keeps the quote currency visible and does not pretend these are identical instruments or a consolidated global price.
- Kraken's public ticker reports base-asset volume. The initial implementation estimates a USD notional by multiplying the reported base volume by the last price. That estimate is not an exchange-reported quote-volume field.
- Exchange volume is venue-specific. The app does not add the same asset's volume across venues and label it as global market volume.
- Scanner observations are transparent rules based on reported 24-hour statistics. They are not price predictions, financial advice, or buy/sell recommendations.
- Public endpoints can change, become unavailable, or enforce stricter rate limits. The UI reports source availability and the app uses bounded refresh intervals.
