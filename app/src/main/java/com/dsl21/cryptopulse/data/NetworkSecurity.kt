package com.dsl21.cryptopulse.data

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.URI
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.Locale
import javax.net.ssl.HttpsURLConnection

/**
 * Small, dependency-free HTTP boundary for public JSON APIs.
 *
 * Callers must provide a fixed host allowlist. Redirects are intentionally disabled so a
 * trusted API cannot silently move a request to a different host or protocol.
 */
internal object SecureHttp {
    const val MAX_RESPONSE_BYTES = 2 * 1024 * 1024
    private const val CONNECT_TIMEOUT_MS = 10_000
    private const val READ_TIMEOUT_MS = 10_000

    fun getJson(url: String, allowedHosts: Set<String>): String {
        if (!isAllowedHttpsApiUrl(url, allowedHosts)) {
            throw IOException("Request URL failed the HTTPS host allowlist")
        }

        val connection = URL(url).openConnection() as? HttpsURLConnection
            ?: throw IOException("Only HTTPS connections are permitted")

        connection.requestMethod = "GET"
        connection.connectTimeout = CONNECT_TIMEOUT_MS
        connection.readTimeout = READ_TIMEOUT_MS
        connection.instanceFollowRedirects = false
        connection.useCaches = false
        connection.doInput = true
        connection.setRequestProperty("Accept", "application/json")
        connection.setRequestProperty("Cache-Control", "no-cache")
        connection.setRequestProperty("User-Agent", "CryptoPulse-Android/1.0.1")

        try {
            val status = connection.responseCode
            if (status !in 200..299) {
                // Do not reflect an untrusted response body into the UI or logs.
                throw IOException("HTTP $status from ${connection.url.host}")
            }

            val contentType = connection.contentType
                ?.substringBefore(';')
                ?.trim()
                ?.lowercase(Locale.ROOT)

            if (contentType != null &&
                contentType != "application/json" &&
                !contentType.endsWith("+json")
            ) {
                throw IOException("Unexpected content type from ${connection.url.host}")
            }

            val contentLength = connection.contentLengthLong
            if (contentLength > MAX_RESPONSE_BYTES) {
                throw IOException("API response exceeded the configured size limit")
            }

            return connection.inputStream.use { input ->
                val output = ByteArrayOutputStream()
                val buffer = ByteArray(8 * 1024)

                while (true) {
                    val remaining = MAX_RESPONSE_BYTES - output.size()
                    // Read at most one byte beyond the limit so unknown-length responses
                    // are rejected without buffering an unbounded response.
                    val requested = minOf(buffer.size, remaining + 1)
                    val count = input.read(buffer, 0, requested)
                    if (count < 0) break
                    if (count > remaining) {
                        throw IOException("API response exceeded the configured size limit")
                    }
                    output.write(buffer, 0, count)
                }

                String(output.toByteArray(), StandardCharsets.UTF_8)
            }
        } finally {
            connection.disconnect()
        }
    }
}

/** Accept only HTTPS URLs whose exact hostname is explicitly allowlisted. */
internal fun isAllowedHttpsApiUrl(rawUrl: String, allowedHosts: Set<String>): Boolean {
    val uri = runCatching { URI(rawUrl) }.getOrNull() ?: return false
    val host = uri.host?.lowercase(Locale.ROOT) ?: return false
    val normalizedAllowlist = allowedHosts.mapTo(mutableSetOf()) {
        it.lowercase(Locale.ROOT)
    }

    return uri.scheme.equals("https", ignoreCase = true) &&
        host in normalizedAllowlist &&
        uri.userInfo == null &&
        (uri.port == -1 || uri.port == 443) &&
        uri.rawFragment == null
}

/** DEX API path components must be plain ASCII identifiers, never URL syntax. */
internal fun isValidDexChainId(value: String): Boolean =
    value.length in 1..64 && value.all {
        it in 'a'..'z' || it in 'A'..'Z' || it in '0'..'9' || it == '-' || it == '_'
    }

internal fun isValidDexTokenAddress(value: String): Boolean =
    value.length in 16..128 && value.all {
        it in 'a'..'z' || it in 'A'..'Z' || it in '0'..'9'
    }

/**
 * DEX Screener supplies this URL as untrusted display data. Only open its canonical HTTPS
 * hostname; reject credentials, non-standard ports, fragments and lookalike domains.
 */
internal fun isSafeDexScreenerUrl(rawUrl: String): Boolean {
    val uri = runCatching { URI(rawUrl) }.getOrNull() ?: return false
    return uri.scheme.equals("https", ignoreCase = true) &&
        uri.host.equals("dexscreener.com", ignoreCase = true) &&
        uri.userInfo == null &&
        (uri.port == -1 || uri.port == 443) &&
        uri.rawFragment == null &&
        uri.rawPath?.startsWith("/") == true &&
        rawUrl.length <= 2_048
}
