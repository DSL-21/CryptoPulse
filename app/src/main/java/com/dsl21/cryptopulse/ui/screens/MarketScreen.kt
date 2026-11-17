package com.dsl21.cryptopulse.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dsl21.cryptopulse.data.AssetQuote
import com.dsl21.cryptopulse.data.MarketSnapshot
import com.dsl21.cryptopulse.ui.components.AssetRow
import com.dsl21.cryptopulse.ui.components.MetricCard
import com.dsl21.cryptopulse.ui.components.SectionTitle

private enum class MarketFilter { ALL, GAINERS, LOSERS }

@Composable
fun MarketScreen(
    snapshot: MarketSnapshot,
    isLoading: Boolean,
    watchlist: Set<String>,
    onRefresh: () -> Unit,
    onSelectAsset: (AssetQuote) -> Unit,
    onToggleWatch: (String) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf(MarketFilter.ALL) }

    val filtered = snapshot.quotes
        .filter {
            it.asset.symbol.contains(query, true) || it.asset.name.contains(query, true)
        }
        .filter {
            when (filter) {
                MarketFilter.ALL -> true
                MarketFilter.GAINERS -> it.primary.changePercent24h > 0.0
                MarketFilter.LOSERS -> it.primary.changePercent24h < 0.0
            }
        }
    val gainers = snapshot.quotes.count { it.primary.changePercent24h > 0.0 }
    val losers = snapshot.quotes.count { it.primary.changePercent24h < 0.0 }
    val averageMove = snapshot.quotes.map { it.primary.changePercent24h }.average().takeIf { it.isFinite() } ?: 0.0

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 22.dp)
    ) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricCard(
                    label = "TRACKED ASSETS",
                    value = snapshot.quotes.size.toString(),
                    detail = "Public spot markets",
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    label = "MARKET BREADTH",
                    value = gainers.toString() + " / " + losers,
                    detail = "Gainers / losers",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(10.dp))
            MetricCard(
                label = "AVERAGE TRACKED MOVE",
                value = String.format(java.util.Locale.US, "%+.2f%%", averageMove),
                detail = "Equal-weighted sample · not total-market breadth",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(14.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF101C2D))
            ) {
                Column(Modifier.padding(13.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("DATA SOURCES", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        Button(onClick = onRefresh, enabled = !isLoading, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 3.dp)) {
                            Text(if (isLoading) "Syncing…" else "Refresh", fontSize = 11.sp)
                        }
                    }
                    snapshot.sources.forEach { source ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(source.name, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                                Text(source.detail, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                            }
                            Spacer(Modifier.width(8.dp))
                            Text(
                                if (source.available) "●" else "○",
                                color = if (source.available) Color(0xFF62E6B5) else MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    if (snapshot.updatedAt > 0L) {
                        Text(
                            "Last successful refresh: " + java.text.SimpleDateFormat("HH:mm:ss 'UTC'", java.util.Locale.US)
                                .apply { timeZone = java.util.TimeZone.getTimeZone("UTC") }
                                .format(java.util.Date(snapshot.updatedAt)),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(top = 10.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            SectionTitle("Market heatmap", "Tracked Binance USDT pairs")
            HeatmapGrid(snapshot.quotes)

            Spacer(Modifier.height(16.dp))
            SectionTitle("Market watch", if (isLoading) "Updating…" else "Auto refresh · 60s")
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                placeholder = { Text("Search an asset or symbol") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color(0xFF35465D)
                )
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = filter == MarketFilter.ALL, onClick = { filter = MarketFilter.ALL }, label = { Text("All") })
                FilterChip(selected = filter == MarketFilter.GAINERS, onClick = { filter = MarketFilter.GAINERS }, label = { Text("Gainers") })
                FilterChip(selected = filter == MarketFilter.LOSERS, onClick = { filter = MarketFilter.LOSERS }, label = { Text("Losers") })
            }
            Spacer(Modifier.height(8.dp))
        }

        if (isLoading && snapshot.quotes.isEmpty()) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 36.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Text(
                        "Connecting to public market feeds…",
                        modifier = Modifier.padding(top = 14.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else if (filtered.isEmpty()) {
            item {
                Text(
                    if (snapshot.quotes.isEmpty()) "No market data is available yet. Check your connection and refresh."
                    else "No assets match this search or filter.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 24.dp)
                )
            }
        } else {
            items(filtered, key = { it.asset.symbol }) { quote ->
                AssetRow(
                    quote = quote,
                    isWatched = quote.asset.symbol in watchlist,
                    onClick = { onSelectAsset(quote) },
                    onToggleWatch = { onToggleWatch(quote.asset.symbol) }
                )
            }
        }

        item {
            Spacer(Modifier.height(8.dp))
            Text(
                "Binance prices are quoted in USDT. Kraken prices are quoted in USD and are kept separate. Kraken's displayed percentage is change since UTC day open, not a rolling 24-hour return.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
private fun HeatmapGrid(quotes: List<AssetQuote>) {
    if (quotes.isEmpty()) {
        Text("Heatmap appears after market data loads.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        return
    }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        quotes.sortedByDescending { it.primary.changePercent24h.absoluteValue }
            .chunked(3)
            .forEach { rowQuotes ->
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                    rowQuotes.forEach { quote ->
                        val move = quote.primary.changePercent24h
                        val strength = (kotlin.math.abs(move) / 10.0).coerceIn(0.12, 0.85).toFloat()
                        val tileColor = if (move >= 0.0) {
                            Color(0xFF0E8B68).copy(alpha = strength)
                        } else {
                            Color(0xFFB83A55).copy(alpha = strength)
                        }
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .height(68.dp)
                                .background(tileColor, RoundedCornerShape(12.dp))
                                .padding(9.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(quote.asset.symbol, fontWeight = FontWeight.Bold, color = Color.White)
                            Text(
                                String.format(java.util.Locale.US, "%+.2f%%", move),
                                color = Color.White,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    repeat(3 - rowQuotes.size) { Spacer(Modifier.weight(1f)) }
                }
            }
    }
}
