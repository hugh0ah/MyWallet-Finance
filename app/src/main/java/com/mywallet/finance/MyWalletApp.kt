package com.mywallet.finance

import android.app.Application
import com.mywallet.finance.data.AppDatabase

class MyWalletApp : Application() {

    val database: AppDatabase by lazy {
        AppDatabase.getInstance(this)
    }

    override fun onCreate() {
        super.onCreate()
        // Inicializar instancia de base de datos
        database
    }
}
