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
import com.dsl21.cryptopulse.ui.components.MetricCard
import com.dsl21.cryptopulse.ui.components.SectionTitle

@Composable
fun ScannerScreen(snapshot: MarketSnapshot) {
    val signals = ScannerEngine.buildSignals(snapshot.quotes, snapshot.hourlyVolumePulses)
    val bullish = signals.count { it.direction == SignalDirection.BULLISH }
    val bearish = signals.count { it.direction == SignalDirection.BEARISH }
    val neutral = signals.count { it.direction == SignalDirection.NEUTRAL }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            SectionTitle("Market scanner", signals.size.toString() + " observations")
            Text(
                "Transparent, rule-based market observations from public exchange data. Signals are not predictions, buy/sell recommendations, or proof that a token is safe.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 12.dp)) {
                MetricCard("BULLISH", bullish.toString(), "Strong price moves", Modifier.weight(1f))
                MetricCard("BEARISH", bearish.toString(), "Strong negative moves", Modifier.weight(1f))
                MetricCard("ACTIVITY", neutral.toString(), "Volume and range", Modifier.weight(1f))
            }
            SectionTitle("Detected conditions", "Live snapshot rules")
        }

        if (signals.isEmpty()) {
            item {
                Text(
                    "No conditions currently match the scanner thresholds.",
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
            SectionTitle("Scanner rules")
            RuleInfo("Momentum", "At least +4% or -4% over the reporting window supplied by the primary exchange.")
            RuleInfo("Reported volume", "At least 100 million quote-currency units in the exchange's reported 24-hour volume.")
            RuleInfo("Wide range", "Reported 24-hour high-to-low range is at least 7% of the current price.")
            RuleInfo("Hourly volume spike", "Last completed Binance 1h candle is at least 2.5× the average of the previous 24 completed hourly candles, with at least 100,000 USDT traded.")
            Text(
                "Volume spikes update approximately every five minutes. Kraken's percentage is explicitly labelled UTC-day change; the main tracked USDT sample is sourced from Binance's rolling 24h ticker.",
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
            Text(
                signal.detail,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun RuleInfo(title: String, detail: String) {
    Column(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Text(title, fontWeight = FontWeight.SemiBold)
        Text(detail, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
    }
}
