package com.mywallet.finance.domain

import com.mywallet.finance.data.model.AccountEntity
import com.mywallet.finance.data.model.TransactionEntity

/**
 * Motor contable de cálculo de saldos en tiempo real basado en Wallet by BudgetBakers.
 */
object BalanceCalculator {

    /**
     * Calcula el saldo actual de una cuenta específica:
     * Saldo = InitialBalance + Ingresos - Gastos - Transferencias Salientes + Transferencias Entrantes.
     */
    fun calculateAccountBalance(
        account: AccountEntity,
        transactions: List<TransactionEntity>
    ): Double {
        var balance = account.initialBalance

        for (tx in transactions) {
            when (tx.transactionType) {
                TransactionEntity.TYPE_INCOME -> {
                    if (tx.accountId == account.id) {
                        balance += tx.amount
                    }
                }
                TransactionEntity.TYPE_EXPENSE -> {
                    if (tx.accountId == account.id) {
                        balance -= tx.amount
                    }
                }
                TransactionEntity.TYPE_TRANSFER -> {
                    if (tx.accountId == account.id) {
                        balance -= tx.amount // Saliente
                    }
                    if (tx.toAccountId == account.id) {
                        balance += tx.amount // Entrante
                    }
                }
            }
        }
        return balance
    }

    /**
     * Calcula el patrimonio neto consolidado sumando los saldos de todas las cuentas
     * que tienen activada la bandera `includeInTotal`.
     */
    fun calculateNetWorth(
        accounts: List<AccountEntity>,
        transactions: List<TransactionEntity>
    ): Double {
        return accounts
            .filter { it.includeInTotal }
            .sumOf { account ->
                calculateAccountBalance(account, transactions)
            }
    }

    /**
     * Calcula el total de gastos acumulados en un conjunto de transacciones.
     */
    fun calculateTotalExpenses(transactions: List<TransactionEntity>): Double {
        return transactions
            .filter { it.transactionType == TransactionEntity.TYPE_EXPENSE }
            .sumOf { it.amount }
    }

    /**
     * Calcula el total de ingresos acumulados en un conjunto de transacciones.
     */
    fun calculateTotalIncome(transactions: List<TransactionEntity>): Double {
        return transactions
            .filter { it.transactionType == TransactionEntity.TYPE_INCOME }
            .sumOf { it.amount }
    }
}
