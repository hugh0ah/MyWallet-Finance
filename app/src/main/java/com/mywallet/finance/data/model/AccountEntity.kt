package com.mywallet.finance.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad de Cuenta inspirada en el modelo Account de Wallet by BudgetBakers.
 */
@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val initialBalance: Double = 0.0,
    val currencyCode: String = "EUR",
    val colorHex: String = "#4285F4",
    val iconName: String = "account_balance",
    val accountType: String = "REGULAR", // REGULAR, SAVINGS, DEBIT_CARD, CREDIT_CARD, CASH
    val includeInTotal: Boolean = true
)
