package com.mywallet.finance

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.mywallet.finance.ui.navigation.AppNavigation
import com.mywallet.finance.ui.theme.MyWalletTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as MyWalletApp
        val database = app.database

        setContent {
            MyWalletTheme {
                AppNavigation(database = database)
            }
        }
    }
}
