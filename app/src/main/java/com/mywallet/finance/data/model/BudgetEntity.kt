package com.mywallet.finance.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad de Presupuesto con soporte de periodos y acumulación (rollover).
 */
@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val categoryIds: String, // Comma-separated category IDs e.g. "1,2,4"
    val limitAmount: Double,
    val period: String = PERIOD_MONTHLY, // WEEKLY, MONTHLY
    val rollover: Boolean = false // Si es true, acumula el sobrante/déficit del periodo previo
) {
    companion object {
        const val PERIOD_WEEKLY = "WEEKLY"
        const val PERIOD_MONTHLY = "MONTHLY"
    }

    fun getCategoryIdsList(): List<Long> {
        return if (categoryIds.isBlank()) {
            emptyList()
        } else {
            categoryIds.split(",").mapNotNull { it.trim().toLongOrNull() }
        }
    }
}
