package com.dsl21.cryptopulse.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dsl21.cryptopulse.data.DexPair
import com.dsl21.cryptopulse.data.isSafeDexScreenerUrl
import com.dsl21.cryptopulse.ui.components.MetricCard
import com.dsl21.cryptopulse.ui.components.SectionTitle
import com.dsl21.cryptopulse.ui.components.formatPrice
import com.dsl21.cryptopulse.ui.components.formatVolume
import java.util.Locale

private enum class DexSort { VOLUME, CHANGE, LIQUIDITY }

@Composable
fun DexRadarScreen(
    pairs: List<DexPair>,
    isLoading: Boolean,
    errorMessage: String?,
    onRefresh: () -> Unit
) {
    var sort by remember { mutableStateOf(DexSort.VOLUME) }
    val sorted = when (sort) {
        DexSort.VOLUME -> pairs.sortedByDescending { it.volumeH1 ?: 0.0 }
        DexSort.CHANGE -> pairs.sortedByDescending { it.changeH1 ?: Double.NEGATIVE_INFINITY }
        DexSort.LIQUIDITY -> pairs.sortedByDescending { it.liquidityUsd ?: 0.0 }
    }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            SectionTitle("DEX radar", if (isLoading) "Updating…" else "Latest token profiles")
            Text(
                "Recent profiles from DEX Screener, enriched with the highest-liquidity pair found for each profile. This is a discovery feed, not a complete list of every on-chain token.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 12.dp)) {
                MetricCard("PROFILES", pairs.size.toString(), "Pairs with market data", Modifier.weight(1f))
                MetricCard("CHAINS", pairs.map { it.chainId }.distinct().size.toString(), "Reported networks", Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 12.dp)) {
                FilterChip(selected = sort == DexSort.VOLUME, onClick = { sort = DexSort.VOLUME }, label = { Text("1h volume") })
                FilterChip(selected = sort == DexSort.CHANGE, onClick = { sort = DexSort.CHANGE }, label = { Text("1h change") })
                FilterChip(selected = sort == DexSort.LIQUIDITY, onClick = { sort = DexSort.LIQUIDITY }, label = { Text("Liquidity") })
            }
            Text(
                "Tap a pair to open its DEX Screener page. Low liquidity, a young pair, or a paid boost is not a safety or legitimacy rating.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(top = 6.dp, bottom = 4.dp)
            )
        }

        if (isLoading && pairs.isEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 28.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator()
                    Text("Loading public DEX profiles…", modifier = Modifier.padding(start = 12.dp))
                }
            }
        } else if (sorted.isEmpty()) {
            item {
                Text(
                    errorMessage ?: "No DEX pairs are available right now. Try refreshing later.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 24.dp)
                )
            }
        } else {
            items(sorted, key = { it.chainId + ":" + it.pairAddress }) { pair ->
                DexPairCard(pair)
            }
        }

        item {
            Text(
                "Data provider: DEX Screener public API. Latest token profiles are rate-limited by the provider; CryptoPulse refreshes this feed every five minutes and does not require an API key.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(top = 8.dp, bottom = 18.dp)
            )
        }
    }
}

@Composable
private fun DexPairCard(pair: DexPair) {
    val uriHandler = LocalUriHandler.current
    val liquidity = pair.liquidityUsd ?: 0.0
    val ageHours = pair.pairAgeHours
    val caution = when {
        liquidity < 10_000.0 -> "Very low liquidity"
        liquidity < 50_000.0 -> "Low liquidity"
        ageHours != null && ageHours < 24.0 -> "New pair"
        else -> null
    }
    val change = pair.changeH1
    val changeColor = if ((change ?: 0.0) >= 0.0) Color(0xFF62E6B5) else Color(0xFFFF7185)
    val safeExternalUrl = isSafeDexScreenerUrl(pair.dexUrl)

    Card(
        modifier = Modifier.fillMaxWidth().clickable(enabled = safeExternalUrl) {
            runCatching { uriHandler.openUri(pair.dexUrl) }
        },
        shape = RoundedCornerShape(17.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF101C2D))
    ) {
        Column(Modifier.fillMaxWidth().padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(Modifier.weight(1f)) {
                    Text(pair.tokenSymbol, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text(pair.tokenName, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                    Text(
                        pair.chainId + " · " + pair.dexId,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("$" + formatPrice(pair.priceUsd), fontWeight = FontWeight.SemiBold)
                    if (change != null) {
                        Text(
                            String.format(Locale.US, "%+.2f%%", change) + " · 1h",
                            color = changeColor,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                DexMetric("LIQUIDITY", "$" + formatVolume(liquidity))
                DexMetric("1H VOLUME", "$" + formatVolume(pair.volumeH1 ?: 0.0))
                DexMetric("24H CHANGE", pair.changeH24?.let { String.format(Locale.US, "%+.1f%%", it) } ?: "—")
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val age = when {
                    ageHours == null -> "Pair age unknown"
                    ageHours < 24.0 -> "Pair age " + String.format(Locale.US, "%.1fh", ageHours)
                    ageHours < 24.0 * 30.0 -> "Pair age " + String.format(Locale.US, "%.1fd", ageHours / 24.0)
                    else -> "Pair age " + String.format(Locale.US, "%.1fmo", ageHours / (24.0 * 30.0))
                }
                Text(age, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                if (pair.boostsActive > 0) {
                    Text("Boosted profile", color = Color(0xFFF3B96C), style = MaterialTheme.typography.labelSmall)
                } else if (caution != null) {
                    Text(caution, color = Color(0xFFFFB76B), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                } else {
                    Text("Open pair ↗", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall)
                }
            }
            if (caution != null && pair.boostsActive > 0) {
                Text(caution, color = Color(0xFFFFB76B), style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(top = 4.dp))
            }
        }
    }
}

@Composable
private fun DexMetric(label: String, value: String) {
    Column {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
        Text(value, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)
    }
}
