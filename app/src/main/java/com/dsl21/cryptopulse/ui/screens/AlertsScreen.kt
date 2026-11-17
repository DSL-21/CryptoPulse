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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.dsl21.cryptopulse.data.AlertDirection
import com.dsl21.cryptopulse.data.PriceAlertRule
import com.dsl21.cryptopulse.data.AssetQuote
import com.dsl21.cryptopulse.ui.components.SectionTitle
import com.dsl21.cryptopulse.ui.components.formatPrice
import java.util.Locale

@Composable
fun AlertsScreen(
    quotes: List<AssetQuote>,
    alerts: List<PriceAlertRule>,
    onAddAlert: (String, AlertDirection, Double) -> Unit,
    onRemoveAlert: (String) -> Unit
) {
    var symbol by remember { mutableStateOf(quotes.firstOrNull()?.asset?.symbol ?: "") }
    var targetText by remember { mutableStateOf("") }
    var direction by remember { mutableStateOf(AlertDirection.ABOVE) }
    var errorText by remember { mutableStateOf("") }

    val selectedQuote = quotes.firstOrNull { it.asset.symbol.equals(symbol.trim(), ignoreCase = true) }
    val enabledCount = alerts.count { it.enabled }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            SectionTitle("Price alerts", enabledCount.toString() + " active")
            Text(
                "Create one-shot price conditions for the public Binance/ Kraken tracked assets. Rules are stored locally on this phone; no cloud account is used.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )
            Card(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                shape = RoundedCornerShape(17.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(Modifier.padding(14.dp)) {
                    Text("New price alert", fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = symbol,
                        onValueChange = { symbol = it.uppercase(Locale.US); errorText = "" },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("Asset symbol") },
                        placeholder = { Text("BTC") }
                    )
                    if (selectedQuote != null) {
                        Text(
                            "Primary price source: " + selectedQuote.primary.exchange + " · " + selectedQuote.primary.quoteCurrency,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 10.dp)) {
                        FilterChip(
                            selected = direction == AlertDirection.ABOVE,
                            onClick = { direction = AlertDirection.ABOVE },
                            label = { Text("Rises above") }
                        )
                        FilterChip(
                            selected = direction == AlertDirection.BELOW,
                            onClick = { direction = AlertDirection.BELOW },
                            label = { Text("Falls below") }
                        )
                    }
                    OutlinedTextField(
                        value = targetText,
                        onValueChange = { targetText = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("Target price") },
                        placeholder = { Text("e.g. 100000") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    if (errorText.isNotBlank()) {
                        Text(errorText, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 6.dp))
                    }
                    Button(
                        onClick = {
                            val target = targetText.trim().replace(',', '.').toDoubleOrNull()
                            when {
                                selectedQuote == null -> errorText = "Enter a symbol currently tracked by CryptoPulse."
                                target == null || !target.isFinite() || target <= 0.0 -> errorText = "Enter a valid target price greater than zero."
                                else -> {
                                    onAddAlert(selectedQuote.asset.symbol, direction, target)
                                    targetText = ""
                                    errorText = ""
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                        enabled = quotes.isNotEmpty()
                    ) {
                        Text("Save price alert")
                    }
                }
            }

            Text(
                "Price checks run on the app's refresh cycle while CryptoPulse is open. Background checks are not enabled in this version. Android notification permission is needed for system notifications.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(top = 8.dp)
            )
            SectionTitle("Your alerts")
        }

        if (alerts.isEmpty()) {
            item {
                Text("No price alerts yet.", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 18.dp))
            }
        } else {
            items(alerts.sortedByDescending { it.enabled }, key = { it.id }) { rule ->
                val current = quotes.firstOrNull { it.asset.symbol == rule.symbol }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(15.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(rule.symbol, fontWeight = FontWeight.Bold)
                            Text(
                                (if (rule.direction == AlertDirection.ABOVE) "Rises above " else "Falls below ") +
                                    formatPrice(rule.targetPrice) + " " + (current?.primary?.quoteCurrency ?: ""),
                                style = MaterialTheme.typography.bodySmall
                            )
                            val status = when {
                                rule.enabled -> "ACTIVE"
                                rule.triggeredAt != null -> "TRIGGERED · ONE-SHOT"
                                else -> "DISABLED"
                            }
                            Text(status, color = if (rule.enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(top = 4.dp))
                        }
                        Button(
                            onClick = { onRemoveAlert(rule.id) },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text("Remove")
                        }
                    }
                }
            }
        }
    }
}
