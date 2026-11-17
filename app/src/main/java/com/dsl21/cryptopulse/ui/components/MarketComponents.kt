package com.dsl21.cryptopulse.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dsl21.cryptopulse.data.AssetQuote
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

@Composable
fun SectionTitle(title: String, trailing: String? = null) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        if (trailing != null) {
            Text(trailing, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun MetricCard(label: String, value: String, detail: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111E30))
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(detail, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
        }
    }
}

@Composable
fun AssetRow(
    quote: AssetQuote,
    isWatched: Boolean,
    onClick: () -> Unit,
    onToggleWatch: () -> Unit
) {
    val change = quote.primary.changePercent24h
    val movementColor = if (change >= 0.0) Color(0xFF62E6B5) else Color(0xFFFF7185)

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(bottom = 9.dp),
        shape = RoundedCornerShape(17.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF101C2D))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 14.dp, end = 6.dp, top = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .background(Color(0xFF1B2C42), RoundedCornerShape(13.dp))
                    .padding(horizontal = 10.dp, vertical = 9.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(quote.asset.symbol.take(1), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
            Column(Modifier.weight(1f).padding(start = 12.dp)) {
                Text(quote.asset.symbol, fontWeight = FontWeight.Bold)
                Text(
                    quote.asset.name + " · " + quote.primary.exchange,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    formatPrice(quote.primary.lastPrice) + " " + quote.primary.quoteCurrency,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    String.format(Locale.US, "%+.2f%%", change),
                    color = movementColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            IconButton(onClick = onToggleWatch) {
                Text(
                    if (isWatched) "★" else "☆",
                    color = if (isWatched) Color(0xFFF3B96C) else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 21.sp
                )
            }
        }
    }
}

@Composable
fun MiniLineChart(
    values: List<Double>,
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.primary
) {
    Canvas(modifier = modifier) {
        if (values.size < 2) return@Canvas
        val minimum = values.minOrNull() ?: return@Canvas
        val maximum = values.maxOrNull() ?: return@Canvas
        val range = (maximum - minimum).takeIf { it > 0.0 } ?: 1.0
        val path = Path()
        values.forEachIndexed { index, value ->
            val x = size.width * index.toFloat() / (values.size - 1).toFloat()
            val y = size.height - (((value - minimum) / range).toFloat() * size.height)
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(path, lineColor, style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round))
        drawLine(
            color = lineColor.copy(alpha = 0.14f),
            start = Offset(0f, size.height - 1f),
            end = Offset(size.width, size.height - 1f),
            strokeWidth = 1.dp.toPx()
        )
    }
}

fun formatPrice(value: Double): String {
    val pattern = when {
        value >= 1000.0 -> "#,##0.00"
        value >= 1.0 -> "#,##0.000"
        value >= 0.01 -> "#,##0.0000"
        else -> "#,##0.000000"
    }
    return DecimalFormat(pattern, DecimalFormatSymbols(Locale.US)).format(value)
}

fun formatVolume(value: Double): String = when {
    value >= 1_000_000_000.0 -> String.format(Locale.US, "%.2fB", value / 1_000_000_000.0)
    value >= 1_000_000.0 -> String.format(Locale.US, "%.1fM", value / 1_000_000.0)
    value >= 1_000.0 -> String.format(Locale.US, "%.1fK", value / 1_000.0)
    else -> String.format(Locale.US, "%.0f", value)
}
