package com.dsl21.cryptopulse.ui.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dsl21.cryptopulse.data.AssetQuote
import com.dsl21.cryptopulse.ui.components.AssetRow
import com.dsl21.cryptopulse.ui.components.SectionTitle

@Composable
fun WatchlistScreen(
    quotes: List<AssetQuote>,
    watchlist: Set<String>,
    onSelectAsset: (AssetQuote) -> Unit,
    onToggleWatch: (String) -> Unit
) {
    val watchedQuotes = quotes.filter { it.asset.symbol in watchlist }
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        item { SectionTitle("Your watchlist", watchedQuotes.size.toString() + " assets") }
        if (watchedQuotes.isEmpty()) {
            item {
                Text(
                    "Your watchlist is empty. Open Market and tap the star beside an asset to save it here.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 20.dp)
                )
            }
        } else {
            items(watchedQuotes, key = { it.asset.symbol }) { quote ->
                AssetRow(quote, true, { onSelectAsset(quote) }, { onToggleWatch(quote.asset.symbol) })
            }
        }
    }
}
