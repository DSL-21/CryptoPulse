# CryptoPulse

**Open-source Android cryptocurrency market intelligence.**

CryptoPulse brings public exchange market data, transparent market scanners, an exchange-specific trade tape and a local watchlist into one Android app.

The project is built around one rule: **the user should not need to create an exchange account, register for a data provider, or supply an API key just to view public market information.**

## Current development status

**Version:** 0.1.0 · **Status:** Initial development build

The first development slice includes:

- Dark Android interface built with Kotlin and Jetpack Compose.
- Binance Spot public 24-hour market statistics for a curated list of USDT pairs.
- Kraken public ticker snapshots for selected USD markets.
- Revolut X public recent-trades feed and BTC-USD top-of-book summary.
- Rule-based scanner observations for large 24-hour moves, high reported volume and wide trading ranges.
- Local watchlist storage (no account or cloud sync).
- 48 hourly Binance candles on the asset detail screen.
- Per-source availability status and a 60-second refresh interval.

Some feeds provide different types of data. Revolut X's public endpoints do not provide an unauthenticated all-market ticker: CryptoPulse uses only the documented public recent-trades and order-book endpoints. Read [DATA_SOURCES.md](docs/DATA_SOURCES.md) for details.

## Build

The project uses Android Gradle Plugin 8.13.2, Kotlin 2.4.21, Gradle 8.13 and Jetpack Compose BOM 2026.06.01.

You need:

- JDK 17 or newer
- Android SDK Platform 36
- Android build tools 36.0.0
- Gradle 8.13

Build and test from the repository root:

\`\`\`bash
gradle --no-daemon :app:testDebugUnitTest
gradle --no-daemon :app:assembleDebug
\`\`\`

The APK is created at:

\`\`\`
app/build/outputs/apk/debug/app-debug.apk
\`\`\`

GitHub Actions runs unit tests and builds the debug APK on pushes and pull requests. Build artifacts are attached to the workflow run. Pushing a version tag such as \`v0.1.0\` creates a GitHub Release with the APK.

## Data sources

- [Binance Spot public market data](https://github.com/binance/binance-spot-api-docs/blob/master/faqs/market_data_only.md)
- [Kraken public REST API](https://docs.kraken.com/api/docs/rest-api/get-ticker-information/)
- [Revolut X public market data](https://developer.revolut.com/docs/guides/crypto-exchange/get-started/make-your-first-request)

No user API keys, accounts, or paid market-data subscriptions are required by the current app.

## Important

CryptoPulse is a market-monitoring and educational tool. Scanner observations are descriptive rule-based signals, not financial advice or trading recommendations. Exchange prices, volumes, market coverage and update frequency can differ.

## License

MIT. See [LICENSE](LICENSE).
