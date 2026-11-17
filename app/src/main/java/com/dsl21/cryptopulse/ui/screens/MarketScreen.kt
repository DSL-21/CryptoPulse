package com.dsl21.cryptopulse.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dsl21.cryptopulse.data.AssetQuote
import com.dsl21.cryptopulse.data.MarketSnapshot
import com.dsl21.cryptopulse.ui.components.AssetRow
import com.dsl21.cryptopulse.ui.components.MetricCard
import com.dsl21.cryptopulse.ui.components.SectionTitle

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
    val filtered = snapshot.quotes.filter {
        it.asset.symbol.contains(query, true) || it.asset.name.contains(query, true)
    }
    val gainers = snapshot.quotes.count { it.primary.changePercent24h > 0.0 }
    val losers = snapshot.quotes.count { it.primary.changePercent24h < 0.0 }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 22.dp)
    ) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricCard("TRACKED ASSETS", snapshot.quotes.size.toString(), "Public spot markets", Modifier.weight(1f))
                MetricCard("MARKET BREADTH", "$gainers / $losers", "Gainers / losers", Modifier.weight(1f))
            }
            Spacer(Modifier.height(15.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF101C2D))
            ) {
                Column(Modifier.padding(13.dp)) {
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("DATA SOURCES", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        Text(
                            if (isLoading) "SYNCING" else "↻ REFRESH",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(4.dp)
                        )
                    }
                    snapshot.sources.forEach { source ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(source.name, style = MaterialTheme.typography.bodySmall)
                                Text(source.detail, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                            }
                            Text(
                                if (source.available) "●" else "○",
                                color = if (source.available) Color(0xFF62E6B5) else MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("Market watch", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(if (isLoading) "Updating…" else "Auto refresh · 60s", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
            }
            Spacer(Modifier.height(8.dp))
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
            Spacer(Modifier.height(12.dp))
        }

        if (isLoading && snapshot.quotes.isEmpty()) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 36.dp),
                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
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
                    else "No assets match your search.",
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
                "Prices are exchange-specific. Binance quotes are shown in USDT; Kraken quotes are shown in USD.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}
