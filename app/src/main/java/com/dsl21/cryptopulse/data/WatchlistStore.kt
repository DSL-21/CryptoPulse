package com.dsl21.cryptopulse.data

import android.content.Context

class WatchlistStore(context: Context) {
    private val preferences = context.getSharedPreferences("cryptopulse_watchlist", Context.MODE_PRIVATE)

    fun load(): Set<String> =
        preferences.getStringSet(KEY_SYMBOLS, emptySet())?.toSet() ?: emptySet()

    fun toggle(symbol: String): Set<String> {
        val updated = load().toMutableSet()
        if (!updated.add(symbol)) updated.remove(symbol)
        preferences.edit().putStringSet(KEY_SYMBOLS, updated).apply()
        return updated
    }

    private companion object {
        const val KEY_SYMBOLS = "symbols"
    }
}
