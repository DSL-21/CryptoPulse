package com.dsl21.cryptopulse

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.dsl21.cryptopulse.ui.CryptoPulseApp
import com.dsl21.cryptopulse.ui.theme.CryptoPulseTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.rgb(7, 17, 31)
        window.navigationBarColor = android.graphics.Color.rgb(7, 17, 31)
        setContent {
            CryptoPulseTheme {
                CryptoPulseApp()
            }
        }
    }
}
