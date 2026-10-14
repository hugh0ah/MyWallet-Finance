package com.mywallet.finance.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidad de Transacción que modela tanto gastos como ingresos y transferencias entre cuentas.
 */
@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["accountId"]),
        Index(value = ["categoryId"]),
        Index(value = ["timestamp"])
    ]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val accountId: Long,             // Cuenta origen
    val toAccountId: Long? = null,   // Cuenta destino (si transactionType == "TRANSFER")
    val categoryId: Long? = null,    // Categoría asignada
    val amount: Double,              // Importe positivo siempre
    val timestamp: Long = System.currentTimeMillis(),
    val note: String? = null,
    val transactionType: String = TYPE_EXPENSE, // EXPENSE, INCOME, TRANSFER
    val isAutoDetected: Boolean = false // Marcada si fue leída por NotificationListenerService
) {
    companion object {
        const val TYPE_EXPENSE = "EXPENSE"
        const val TYPE_INCOME = "INCOME"
        const val TYPE_TRANSFER = "TRANSFER"
    }
}
