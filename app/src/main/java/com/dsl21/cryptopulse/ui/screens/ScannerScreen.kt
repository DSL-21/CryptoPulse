package com.dsl21.cryptopulse.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dsl21.cryptopulse.data.MarketSnapshot
import com.dsl21.cryptopulse.data.ScannerEngine
import com.dsl21.cryptopulse.data.ScannerSignal
import com.dsl21.cryptopulse.data.SignalDirection
import com.dsl21.cryptopulse.ui.components.SectionTitle

@Composable
fun ScannerScreen(snapshot: MarketSnapshot) {
    val signals = ScannerEngine.buildSignals(snapshot.quotes)
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            SectionTitle("Smart scanner", signals.size.toString() + " observations")
            Text(
                "Rule-based market filters using public 24h exchange statistics. Signals describe market conditions; they are not trading advice.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        if (signals.isEmpty()) {
            item {
                Text(
                    "No scanner observations yet. Market data will appear here after the first successful refresh.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 20.dp)
                )
            }
        } else {
            items(signals, key = { it.symbol + "-" + it.title }) { signal ->
                SignalCard(signal)
            }
        }
        item {
            Text(
                "Current v0.1 rules: 24h move ≥ 4%, 24h quote volume ≥ 100M, or a 24h high-to-low range ≥ 7%. Thresholds are intentionally transparent.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(top = 8.dp, bottom = 18.dp)
            )
        }
    }
}

@Composable
private fun SignalCard(signal: ScannerSignal) {
    val accent = when (signal.direction) {
        SignalDirection.BULLISH -> Color(0xFF62E6B5)
        SignalDirection.BEARISH -> Color(0xFFFF7185)
        SignalDirection.NEUTRAL -> Color(0xFF8BAEFF)
    }
    Card(
        shape = RoundedCornerShape(17.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF101C2D))
    ) {
        Column(Modifier.fillMaxWidth().padding(15.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(signal.symbol + " · " + signal.name, fontWeight = FontWeight.Bold)
                Text(
                    when (signal.direction) {
                        SignalDirection.BULLISH -> "UP"
                        SignalDirection.BEARISH -> "DOWN"
                        SignalDirection.NEUTRAL -> "INFO"
                    },
                    color = accent,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(signal.title, color = accent, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 7.dp))
            Text(signal.detail, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
        }
    }
}
