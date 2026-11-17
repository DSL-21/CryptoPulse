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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dsl21.cryptopulse.data.MarketSnapshot
import com.dsl21.cryptopulse.data.RecentTrade
import com.dsl21.cryptopulse.ui.components.SectionTitle
import com.dsl21.cryptopulse.ui.components.formatPrice
import java.util.Locale

@Composable
fun TradeTapeScreen(snapshot: MarketSnapshot) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        item {
            SectionTitle("Revolut X public tape", snapshot.revolutTrades.size.toString() + " recent trades")
            Text(
                "Latest public trades returned by Revolut X. This is not a complete historical trade feed.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(Modifier.height(14.dp))
        }

        val book = snapshot.revolutBtcOrderBook
        if (book != null) {
            item {
                Card(
                    shape = RoundedCornerShape(17.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(Modifier.fillMaxWidth().padding(15.dp)) {
                        Text("BTC / USD · top of book", fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Column {
                                Text("BEST BID", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                                Text(formatPrice(book.bidPrice), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            }
                            Column {
                                Text("BEST ASK", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                                Text(formatPrice(book.askPrice), color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                            }
                            Column {
                                Text("SPREAD", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                                Text(String.format(Locale.US, "%.4f%%", book.spreadPercent), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(18.dp))
                SectionTitle("Recent public trades")
            }
        } else {
            item {
                Text("Revolut X order book is currently unavailable.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                SectionTitle("Recent public trades")
            }
        }

        if (snapshot.revolutTrades.isEmpty()) {
            item {
                Text("No recent trades were returned by the public endpoint.", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 16.dp))
            }
        } else {
            items(snapshot.revolutTrades) { trade -> TradeRow(trade) }
        }
    }
}

@Composable
private fun TradeRow(trade: RecentTrade) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 11.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(Modifier.weight(1f)) {
            Text(trade.assetSymbol + " / " + trade.priceCurrency, fontWeight = FontWeight.SemiBold)
            Text(
                formatPrice(trade.quantity) + " " + trade.quantityCurrency,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelSmall
            )
        }
        Text(formatPrice(trade.price) + " " + trade.priceCurrency, fontWeight = FontWeight.Medium)
    }
}
