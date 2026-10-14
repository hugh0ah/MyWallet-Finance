package com.mywallet.finance.domain

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.mywallet.finance.data.AppDatabase
import com.mywallet.finance.data.model.AccountEntity
import com.mywallet.finance.data.model.BudgetEntity
import com.mywallet.finance.data.model.CategoryEntity
import com.mywallet.finance.data.model.MerchantRuleEntity
import com.mywallet.finance.data.model.TransactionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

data class BackupPayload(
    val accounts: List<AccountEntity>,
    val categories: List<CategoryEntity>,
    val transactions: List<TransactionEntity>,
    val budgets: List<BudgetEntity>,
    val merchantRules: List<MerchantRuleEntity>,
    val exportedAt: Long = System.currentTimeMillis()
)

/**
 * Gestor de exportación e importación de datos en formatos CSV y JSON sin pérdida de integridad.
 */
class DataBackupManager(private val database: AppDatabase) {

    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    suspend fun exportToJson(): String = withContext(Dispatchers.IO) {
        val payload = BackupPayload(
            accounts = database.accountDao().getAllAccountsFlow().first(),
            categories = database.categoryDao().getAllCategoriesFlow().first(),
            transactions = database.transactionDao().getAllTransactionsFlow().first(),
            budgets = database.budgetDao().getAllBudgetsFlow().first(),
            merchantRules = database.merchantRuleDao().getAllRules()
        )
        gson.toJson(payload)
    }

    suspend fun importFromJson(jsonString: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val payload = gson.fromJson(jsonString, BackupPayload::class.java) ?: return@withContext false
            if (payload.accounts.isNotEmpty()) database.accountDao().insertAll(payload.accounts)
            if (payload.categories.isNotEmpty()) database.categoryDao().insertAll(payload.categories)
            if (payload.transactions.isNotEmpty()) database.transactionDao().insertAll(payload.transactions)
            if (payload.budgets.isNotEmpty()) database.budgetDao().insertAll(payload.budgets)
            if (payload.merchantRules.isNotEmpty()) database.merchantRuleDao().insertAll(payload.merchantRules)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun exportTransactionsToCsv(): String = withContext(Dispatchers.IO) {
        val transactions = database.transactionDao().getAllTransactionsFlow().first()
        val accounts = database.accountDao().getAllAccountsFlow().first().associateBy { it.id }
        val categories = database.categoryDao().getAllCategoriesFlow().first().associateBy { it.id }

        val sb = StringBuilder()
        sb.append("id,timestamp,tipo,cuenta_origen,cuenta_destino,categoria,importe,nota,autodetectado\n")

        for (tx in transactions) {
            val accountName = accounts[tx.accountId]?.name ?: tx.accountId.toString()
            val toAccountName = tx.toAccountId?.let { accounts[it]?.name ?: it.toString() } ?: ""
            val categoryName = tx.categoryId?.let { categories[it]?.name ?: it.toString() } ?: ""

            sb.append("${tx.id},")
            sb.append("${tx.timestamp},")
            sb.append("${tx.transactionType},")
            sb.append("\"$accountName\",")
            sb.append("\"$toAccountName\",")
            sb.append("\"$categoryName\",")
            sb.append("${tx.amount},")
            sb.append("\"${tx.note?.replace("\"", "\"\"") ?: ""}\",")
            sb.append("${tx.isAutoDetected}\n")
        }
        sb.toString()
    }
}
