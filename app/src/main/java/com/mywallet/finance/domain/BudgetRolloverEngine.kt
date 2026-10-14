package com.mywallet.finance.domain

import com.mywallet.finance.data.model.BudgetEntity
import com.mywallet.finance.data.model.TransactionEntity
import java.util.Calendar

data class BudgetStatus(
    val budget: BudgetEntity,
    val effectiveLimit: Double,
    val spentAmount: Double,
    val remainingAmount: Double,
    val rolloverAmount: Double,
    val progressPercent: Float,
    val isOverBudget: Boolean
)

/**
 * Motor de cálculo de presupuestos con soporte para acumulación/remanente (Rollover)
 * extraído de la arquitectura de Wallet by BudgetBakers.
 */
object BudgetRolloverEngine {

    /**
     * Calcula el estado de un presupuesto para el mes en curso, incluyendo el sobrante
     * acumulado del mes anterior si el flag `rollover` está activado.
     */
    fun evaluateBudget(
        budget: BudgetEntity,
        allTransactions: List<TransactionEntity>,
        referenceTimestamp: Long = System.currentTimeMillis()
    ): BudgetStatus {
        val categoryIds = budget.getCategoryIdsList()

        // 1. Delimitar el periodo actual (Mes actual)
        val cal = Calendar.getInstance().apply { timeInMillis = referenceTimestamp }
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val currentMonthStart = cal.timeInMillis

        cal.add(Calendar.MONTH, 1)
        val currentMonthEnd = cal.timeInMillis - 1

        // 2. Delimitar el periodo anterior (Mes previo para calcular rollover)
        cal.timeInMillis = currentMonthStart
        cal.add(Calendar.MONTH, -1)
        val prevMonthStart = cal.timeInMillis
        val prevMonthEnd = currentMonthStart - 1

        // 3. Gastos del periodo actual
        val currentPeriodExpenses = allTransactions.filter { tx ->
            tx.transactionType == TransactionEntity.TYPE_EXPENSE &&
                    tx.timestamp in currentMonthStart..currentMonthEnd &&
                    (categoryIds.isEmpty() || (tx.categoryId != null && categoryIds.contains(tx.categoryId)))
        }.sumOf { it.amount }

        // 4. Calcular el remanente (rollover) del mes anterior si aplica
        var rolloverAmount = 0.0
        if (budget.rollover) {
            val prevPeriodExpenses = allTransactions.filter { tx ->
                tx.transactionType == TransactionEntity.TYPE_EXPENSE &&
                        tx.timestamp in prevMonthStart..prevMonthEnd &&
                        (categoryIds.isEmpty() || (tx.categoryId != null && categoryIds.contains(tx.categoryId)))
            }.sumOf { it.amount }

            // Si gastó menos del límite, el remanente es positivo (se suma); si se pasó, el remanente es negativo
            rolloverAmount = budget.limitAmount - prevPeriodExpenses
        }

        // 5. Límite efectivo = Límite Base + Rollover
        val effectiveLimit = (budget.limitAmount + rolloverAmount).coerceAtLeast(0.0)
        val remainingAmount = effectiveLimit - currentPeriodExpenses
        val progress = if (effectiveLimit > 0) {
            (currentPeriodExpenses / effectiveLimit).toFloat().coerceIn(0f, 1f)
        } else {
            1f
        }
        val isOverBudget = currentPeriodExpenses > effectiveLimit

        return BudgetStatus(
            budget = budget,
            effectiveLimit = effectiveLimit,
            spentAmount = currentPeriodExpenses,
            remainingAmount = remainingAmount,
            rolloverAmount = rolloverAmount,
            progressPercent = progress,
            isOverBudget = isOverBudget
        )
    }
}
