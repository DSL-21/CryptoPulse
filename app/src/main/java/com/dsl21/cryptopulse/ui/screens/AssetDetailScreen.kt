package com.dsl21.cryptopulse.ui.screens

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dsl21.cryptopulse.data.AssetQuote
import com.dsl21.cryptopulse.ui.components.MiniLineChart
import com.dsl21.cryptopulse.ui.components.SectionTitle
import com.dsl21.cryptopulse.ui.components.formatPrice
import com.dsl21.cryptopulse.ui.components.formatVolume
import java.util.Locale

@Composable
fun AssetDetailScreen(
    quote: AssetQuote,
    history: List<Double>,
    isLoadingHistory: Boolean,
    isWatched: Boolean,
    onBack: () -> Unit,
    onToggleWatch: () -> Unit
) {
    val primary = quote.primary
    val movementColor = if (primary.changePercent24h >= 0.0) Color(0xFF62E6B5) else Color(0xFFFF7185)

    LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "‹  Back to market",
                    modifier = Modifier.clickable(onClick = onBack).padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    if (isWatched) "★ Saved" else "☆ Save",
                    modifier = Modifier.clickable(onClick = onToggleWatch).padding(8.dp),
                    color = if (isWatched) Color(0xFFF3B96C) else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Text(quote.asset.name, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(quote.asset.symbol, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Text(
                    formatPrice(primary.lastPrice) + " " + primary.quoteCurrency,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    String.format(Locale.US, "%+.2f%%", primary.changePercent24h),
                    color = movementColor,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 3.dp)
                )
            }
            Text(
                "Primary source: " + primary.exchange + " · " + primary.changeWindowLabel + " change",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(top = 4.dp)
            )
            Spacer(Modifier.height(18.dp))
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF101C2D))
            ) {
                Column(Modifier.fillMaxWidth().padding(15.dp)) {
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("48-hour price history", fontWeight = FontWeight.SemiBold)
                        Text(
                            if (isLoadingHistory) "Loading…" else "48 × 1h · Binance",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                    MiniLineChart(
                        values = history,
                        modifier = Modifier.fillMaxWidth().height(150.dp),
                        lineColor = movementColor
                    )
                    if (history.isEmpty() && !isLoadingHistory) {
                        Text(
                            "Hourly history is not available right now.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
            SectionTitle("Reported 24-hour range", primary.exchange)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatTile("HIGH", formatPrice(primary.high24h) + " " + primary.quoteCurrency, Modifier.weight(1f))
                StatTile("LOW", formatPrice(primary.low24h) + " " + primary.quoteCurrency, Modifier.weight(1f))
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                val prefix = if (primary.quoteVolumeIsEstimate) "≈" else ""
                StatTile(
                    "QUOTE VOLUME",
                    prefix + formatVolume(primary.quoteVolume24h) + " " + primary.quoteCurrency,
                    Modifier.weight(1f)
                )
                StatTile("CHANGE WINDOW", primary.changeWindowLabel, Modifier.weight(1f))
            }
            SectionTitle("Exchange comparison", quote.exchanges.size.toString() + " source(s)")
        }

        items(quote.exchanges) { exchange ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                shape = RoundedCornerShape(15.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF101C2D))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(exchange.exchange, fontWeight = FontWeight.SemiBold)
                        Text(
                            exchange.quoteCurrency + " · " + exchange.changeWindowLabel,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(formatPrice(exchange.lastPrice) + " " + exchange.quoteCurrency, fontWeight = FontWeight.Bold)
                        Text(
                            String.format(Locale.US, "%+.2f%%", exchange.changePercent24h),
                            color = if (exchange.changePercent24h >= 0) Color(0xFF62E6B5) else Color(0xFFFF7185),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }

        item {
            Text(
                "Prices and volumes are exchange-specific, not a consolidated index. USD and USDT quotes are intentionally displayed separately. Kraken's percent change is since UTC day open.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(vertical = 12.dp)
            )
        }
    }
}

@Composable
private fun StatTile(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF101C2D))
    ) {
        Column(Modifier.padding(13.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, fontWeight = FontWeight.SemiBold)
        }
    }
}
