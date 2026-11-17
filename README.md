# CryptoPulse

**Open-source Android cryptocurrency market intelligence.**

CryptoPulse combines public centralized-exchange market data, a transparent market scanner, DEX pair discovery, a local watchlist and one-shot price alerts in a native Android app.

## Stable release

**Version:** 1.0.1  
**Application ID:** `com.dsl21.cryptopulse`  
**Minimum Android:** 8.0 (API 26)  
**Target Android:** API 36

Download the optimized APK and SHA-256 checksum from [GitHub Releases](https://github.com/DSL-21/CryptoPulse/releases/latest).

> The release is a non-debuggable, minified Android release variant. It is currently signed with the CI runner's temporary Android debug certificate because no persistent production keystore is configured. This is for sideload testing, not Play Store distribution. A later CI build may require uninstalling the current copy before updating because the signing key is ephemeral.

## Security hardening in 1.0.1

- HTTPS-only public API requests restricted to exact host allowlists.
- HTTP redirects are rejected rather than followed.
- Network connections have bounded connect/read timeouts and JSON response bodies are limited to 2 MiB.
- DEX chain identifiers and token addresses are validated before becoming URL path segments.
- External DEX pair links must use the canonical `https://dexscreener.com` hostname.
- The release variant enables R8 minification and resource shrinking.
- CI verifies that the packaged APK is signed and not debuggable.
- GitHub Actions are pinned to full commit SHAs; Dependabot and CodeQL checks are configured.
- Build tooling is aligned to Android Gradle Plugin 9.3.1 and Gradle 9.7.0.

## Features

### Market
- Binance Spot rolling 24-hour ticker snapshots for ten tracked USDT pairs.
- Kraken public ticker snapshots for selected USD pairs.
- Exchange-specific quote-currency display; USD and USDT stay separate.
- Market heatmap, asset search, gainers/losers filters and local watchlist.

### Scanner
- Positive/negative price-momentum observations.
- High reported exchange-volume observations.
- Wide 24-hour range observations.
- Relative hourly-volume spikes using completed Binance 1-hour candles.
- Transparent thresholds and explanations.

### DEX Radar
- Recent token profiles from DEX Screener's public API.
- Best available pair per profile, selected by reported liquidity.
- Chain, DEX, price changes, volume, liquidity, pair age and provider-reported active boosts.
- Sort by hourly volume, hourly price change or liquidity.
- Low-liquidity and very-new-pair cautions.

DEX Radar is a limited discovery sample, not a complete on-chain token index or a token-safety assessment.

### Revolut X public tape
- Latest public trades across pairs.
- Public BTC/USD top-of-book bid, ask and spread.
- No Revolut login or personal API key.

### Local price alerts
- One-shot above/below price conditions for tracked assets.
- Local storage; no account or cloud sync.
- Android notifications when permission is granted.

Price alerts are checked on the market refresh cycle while CryptoPulse is open. Background monitoring after the app is closed is not included.

## Public data sources

- Binance Spot public market-data REST API.
- Kraken public REST API.
- Revolut X public recent-trades and order-book endpoints.
- DEX Screener public token-profile and token-pair endpoints.

See [docs/DATA_SOURCES.md](docs/DATA_SOURCES.md) for endpoint details, limits and interpretation differences.

## Build from source

Requirements: JDK 17, Android SDK Platform 36 and Build Tools 36.0.0. Android Gradle Plugin 9.3.1 and Gradle 9.7.0 are used by CI.

Build:
```bash
gradle --no-daemon lintDebug lintRelease testDebugUnitTest assembleDebug assembleRelease
```

APK output: `app/build/outputs/apk/release/app-release.apk`.

## Security

See [SECURITY.md](SECURITY.md) for the reporting policy and security scope. CI checks include Android lint, unit tests and CodeQL; they are not a substitute for an independent mobile application penetration test.

## Known limitations
- Kraken's percentage is change since UTC day open, not a rolling 24-hour return; its quote-notional volume is estimated.
- USD and USDT stay separate; exchange volumes are venue-specific.
- DEX discovery is limited to recent profiles.
- Price alerts are checked while the app is open.
- The current sideload APK does not use a persistent Play Store signing key.

MIT License.
