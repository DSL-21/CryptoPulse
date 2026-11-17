# CryptoPulse public data sources

CryptoPulse accesses public market-data endpoints directly from the Android device. No user registration, personal API key, paid API subscription or app-owned server is required for the endpoints used in this release.

## Binance Spot
Base URL: `https://data-api.binance.vision`
- `GET /api/v3/ticker/24hr`: rolling 24-hour ticker for tracked USDT pairs.
- `GET /api/v3/klines`: hourly candles for charts and relative-volume scanner.

Official documentation:
- https://github.com/binance/binance-spot-api-docs/blob/master/faqs/market_data_only.md
- https://github.com/binance/binance-spot-api-docs/blob/master/rest-api.md

## Kraken
Base URL: `https://api.kraken.com`
- `GET /0/public/Ticker?pair=XBTUSD,ETHUSD,SOLUSD`

This public endpoint requires no authentication. The `o` field is the opening price since UTC day start, while `h`, `l` and `v[1]` describe the last 24 hours. CryptoPulse labels percentage move as UTC-day change, not rolling 24h. Quote-notional volume is estimated from base volume multiplied by latest price.

Official documentation:
- https://docs.kraken.com/api/docs/rest-api/get-ticker-information/

## Revolut X
Base URL: `https://revx.revolut.com/api/2.0`
- `GET /public/last-trades`: latest 100 public trades.
- `GET /public/order-book/BTC-USD`: public BTC/USD order book, up to five levels per side.

The documented public rate limit is 20 requests per 10 seconds. CryptoPulse does not call private account, portfolio or authenticated ticker endpoints.

Official documentation:
- https://developer.revolut.com/docs/guides/crypto-exchange/get-started/make-your-first-request
- https://github.com/revolut-engineering/revolut-x-api/blob/master/revolut-x-api-for-llm.md

## DEX Screener
Base URL: `https://api.dexscreener.com`
- `GET /token-profiles/latest/v1`: recent token profiles.
- `GET /token-pairs/v1/{chainId}/{tokenAddress}`: pairs for a profile token.

CryptoPulse samples a limited number of recent profiles, loads their pairs and selects the pair with the highest provider-reported USD liquidity. The feed refreshes every five minutes. This is not a full-chain index or token-safety assessment.

Official documentation:
- https://docs.dexscreener.com/api/reference

## Limitations
USD and USDT quotes stay separate. Exchange volumes are venue-specific. Kraken quote-notional volume is an estimate. A DEX profile, paid boost or high FDV is not evidence a token is safe or legitimate. Public APIs may be temporarily unavailable.
