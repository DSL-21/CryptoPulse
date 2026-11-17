package com.dsl21.cryptopulse.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dsl21.cryptopulse.data.AssetQuote
import com.dsl21.cryptopulse.data.MarketSnapshot
import com.dsl21.cryptopulse.data.PublicMarketRepository
import com.dsl21.cryptopulse.data.WatchlistStore
import com.dsl21.cryptopulse.ui.screens.AssetDetailScreen
import com.dsl21.cryptopulse.ui.screens.MarketScreen
import com.dsl21.cryptopulse.ui.screens.ScannerScreen
import com.dsl21.cryptopulse.ui.screens.TradeTapeScreen
import com.dsl21.cryptopulse.ui.screens.WatchlistScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private enum class AppTab(val label: String, val glyph: String) {
    MARKET("Market", "◈"),
    SCANNER("Scanner", "⌕"),
    TAPE("Tape", "≋"),
    WATCHLIST("Saved", "☆")
}

@Composable
fun CryptoPulseApp() {
    val context = LocalContext.current
    val repository = remember { PublicMarketRepository() }
    val watchlistStore = remember(context) { WatchlistStore(context) }
    val scope = rememberCoroutineScope()

    var snapshot by remember { mutableStateOf(MarketSnapshot.empty()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedTab by remember { mutableStateOf(AppTab.MARKET) }
    var selectedSymbol by remember { mutableStateOf<String?>(null) }
    var history by remember { mutableStateOf(emptyList<Double>()) }
    var isLoadingHistory by remember { mutableStateOf(false) }
    var watchlist by remember { mutableStateOf(watchlistStore.load()) }

    val refreshMarket: () -> Unit = {
        scope.launch {
            if (!isLoading) {
                isLoading = true
                errorMessage = null
                try {
                    snapshot = repository.refresh()
                    if (snapshot.quotes.isEmpty()) {
                        errorMessage = "Market feeds did not return tracked assets. Check your connection."
                    }
                } catch (exception: Exception) {
                    errorMessage = exception.message ?: "Unable to refresh market data."
                } finally {
                    isLoading = false
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        refreshMarket()
        while (true) {
            delay(60_000)
            refreshMarket()
        }
    }

    LaunchedEffect(selectedSymbol) {
        val symbol = selectedSymbol ?: return@LaunchedEffect
        val asset = snapshot.quotes.firstOrNull { it.asset.symbol == symbol }?.asset
        if (asset != null) {
            isLoadingHistory = true
            history = runCatching { repository.fetchHourlyCloses(asset) }.getOrDefault(emptyList())
            isLoadingHistory = false
        }
    }

    fun toggleWatch(symbol: String) {
        watchlist = watchlistStore.toggle(symbol)
    }

    Scaffold(
        containerColor = Color(0xFF07111F),
        bottomBar = {
            if (selectedSymbol == null) {
                NavigationBar(containerColor = Color(0xFF0C1727)) {
                    AppTab.values().forEach { tab ->
                        NavigationBarItem(
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab },
                            icon = { Text(tab.glyph, fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                            label = { Text(tab.label, fontSize = 10.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF07111F),
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(Modifier.fillMaxSize().padding(innerPadding)) {
            AppHeader(
                isLoading = isLoading,
                isDetail = selectedSymbol != null,
                onRefresh = refreshMarket,
                onBack = { selectedSymbol = null }
            )

            if (errorMessage != null) {
                Text(
                    errorMessage.orEmpty(),
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Box(Modifier.weight(1f).fillMaxWidth()) {
                if (selectedSymbol != null) {
                    val selectedQuote = snapshot.quotes.firstOrNull { it.asset.symbol == selectedSymbol }
                    if (selectedQuote != null) {
                        AssetDetailScreen(
                            quote = selectedQuote,
                            history = history,
                            isLoadingHistory = isLoadingHistory,
                            isWatched = selectedSymbol in watchlist,
                            onBack = { selectedSymbol = null },
                            onToggleWatch = { selectedSymbol?.let(::toggleWatch) }
                        )
                    } else {
                        Text("This asset is no longer in the current feed.", modifier = Modifier.padding(16.dp))
                    }
                } else {
                    when (selectedTab) {
                        AppTab.MARKET -> MarketScreen(
                            snapshot = snapshot,
                            isLoading = isLoading,
                            watchlist = watchlist,
                            onRefresh = refreshMarket,
                            onSelectAsset = { quote: AssetQuote -> selectedSymbol = quote.asset.symbol },
                            onToggleWatch = ::toggleWatch
                        )
                        AppTab.SCANNER -> ScannerScreen(snapshot)
                        AppTab.TAPE -> TradeTapeScreen(snapshot)
                        AppTab.WATCHLIST -> WatchlistScreen(
                            quotes = snapshot.quotes,
                            watchlist = watchlist,
                            onSelectAsset = { quote -> selectedSymbol = quote.asset.symbol },
                            onToggleWatch = ::toggleWatch
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AppHeader(
    isLoading: Boolean,
    isDetail: Boolean,
    onRefresh: () -> Unit,
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(modifier = Modifier.size(37.dp), shape = CircleShape, color = Color(0xFF153B36)) {
                Box(contentAlignment = Alignment.Center) {
                    Text("CP", color = Color(0xFF62E6B5), fontWeight = FontWeight.Black, fontSize = 13.sp)
                }
            }
            Column(Modifier.padding(start = 10.dp)) {
                Text(
                    if (isDetail) "ASSET INTELLIGENCE" else "CRYPTOPULSE",
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.1.sp,
                    fontSize = 13.sp
                )
                Text(
                    if (isDetail) "Market detail" else "Multi-exchange market radar",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp
                )
            }
        }
        Surface(
            modifier = Modifier.clickable { if (isDetail) onBack() else onRefresh() },
            shape = CircleShape,
            color = Color(0xFF142338)
        ) {
            Text(
                if (isDetail) "‹" else if (isLoading) "…" else "↻",
                modifier = Modifier.padding(horizontal = 13.dp, vertical = 7.dp),
                color = MaterialTheme.colorScheme.primary,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
