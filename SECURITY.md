# Security Policy

## Supported release

Security fixes are developed against the latest stable release and the current `main` branch.

## Reporting a vulnerability

Please do not publish exploit details in a public issue.

Use GitHub's private vulnerability reporting feature on the repository's **Security** tab ("Report a vulnerability") where available. Include the affected version, Android version/device, impact, reproduction steps and any relevant sanitized logs. Do not include personal data, wallet secrets, API keys or other credentials.

## Security design notes

- CryptoPulse is a read-only market-data client. It does not request wallet access, seed phrases, private keys, exchange credentials, contacts, location or file access.
- Network requests must use HTTPS and the exact API hostname allowlist in the application.
- Redirects are rejected, and response bodies are size-limited.
- Third-party DEX metadata is untrusted display data. Outbound pair links are restricted to the canonical DEX Screener HTTPS host.
- The publicly distributed APK is currently sideload/test signed with an ephemeral CI debug certificate. It is not a Play Store production-signed build.

## Verification scope

CI runs Android lint, unit tests, debug and release builds, plus CodeQL analysis. These controls reduce risk but do not replace a device-level penetration test, runtime instrumentation, API fuzzing or independent security assessment.
