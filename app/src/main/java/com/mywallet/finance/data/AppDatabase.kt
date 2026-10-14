package com.mywallet.finance.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mywallet.finance.data.dao.AccountDao
import com.mywallet.finance.data.dao.BudgetDao
import com.mywallet.finance.data.dao.CategoryDao
import com.mywallet.finance.data.dao.MerchantRuleDao
import com.mywallet.finance.data.dao.TransactionDao
import com.mywallet.finance.data.model.AccountEntity
import com.mywallet.finance.data.model.BudgetEntity
import com.mywallet.finance.data.model.CategoryEntity
import com.mywallet.finance.data.model.MerchantRuleEntity
import com.mywallet.finance.data.model.TransactionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        AccountEntity::class,
        CategoryEntity::class,
        TransactionEntity::class,
        BudgetEntity::class,
        MerchantRuleEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun accountDao(): AccountDao
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
    abstract fun budgetDao(): BudgetDao
    abstract fun merchantRuleDao(): MerchantRuleDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mywallet_database.db"
                )
                    .addCallback(DatabasePrepopulateCallback())
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabasePrepopulateCallback : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            CoroutineScope(Dispatchers.IO).launch {
                val database = INSTANCE ?: return@launch

                // 1. Pre-poblar Cuentas Iniciales
                val defaultAccounts = listOf(
                    AccountEntity(
                        name = "Cuenta Principal",
                        initialBalance = 1250.0,
                        currencyCode = "EUR",
                        colorHex = "#1976D2",
                        iconName = "account_balance",
                        accountType = "REGULAR",
                        includeInTotal = true
                    ),
                    AccountEntity(
                        name = "Efectivo",
                        initialBalance = 140.0,
                        currencyCode = "EUR",
                        colorHex = "#388E3C",
                        iconName = "payments",
                        accountType = "CASH",
                        includeInTotal = true
                    ),
                    AccountEntity(
                        name = "Fondo de Ahorros",
                        initialBalance = 4500.0,
                        currencyCode = "EUR",
                        colorHex = "#7B1FA2",
                        iconName = "savings",
                        accountType = "SAVINGS",
                        includeInTotal = true
                    )
                )
                database.accountDao().insertAll(defaultAccounts)

                // 2. Pre-poblar Categorías (Basadas en ingeniería inversa de Wallet y Cashew)
                val defaultCategories = listOf(
                    CategoryEntity(id = 1, name = "Alimentación", iconName = "restaurant", colorHex = "#FF5722", isExpense = true),
                    CategoryEntity(id = 2, name = "Transporte", iconName = "directions_car", colorHex = "#03A9F4", isExpense = true),
                    CategoryEntity(id = 3, name = "Vivienda & Facturas", iconName = "home", colorHex = "#9C27B0", isExpense = true),
                    CategoryEntity(id = 4, name = "Ocio & Cafeterías", iconName = "local_cafe", colorHex = "#FF9800", isExpense = true),
                    CategoryEntity(id = 5, name = "Salud & Farmacia", iconName = "medical_services", colorHex = "#E91E63", isExpense = true),
                    CategoryEntity(id = 6, name = "Compras & Ropa", iconName = "shopping_bag", colorHex = "#673AB7", isExpense = true),
                    CategoryEntity(id = 7, name = "Suscripciones", iconName = "subscriptions", colorHex = "#009688", isExpense = true),
                    CategoryEntity(id = 8, name = "Nómina & Salario", iconName = "payments", colorHex = "#4CAF50", isExpense = false),
                    CategoryEntity(id = 9, name = "Inversiones", iconName = "trending_up", colorHex = "#00BCD4", isExpense = false),
                    CategoryEntity(id = 10, name = "Otros Ingresos", iconName = "add_circle", colorHex = "#8BC34A", isExpense = false)
                )
                database.categoryDao().insertAll(defaultCategories)

                // 3. Pre-poblar Reglas de Comercios (Auto-categorización de notificaciones)
                val defaultRules = listOf(
                    MerchantRuleEntity(keyword = "Mercadona", targetCategoryId = 1, targetAccountId = 1),
                    MerchantRuleEntity(keyword = "Carrefour", targetCategoryId = 1, targetAccountId = 1),
                    MerchantRuleEntity(keyword = "Lidl", targetCategoryId = 1, targetAccountId = 1),
                    MerchantRuleEntity(keyword = "Dia", targetCategoryId = 1, targetAccountId = 1),
                    MerchantRuleEntity(keyword = "Repsol", targetCategoryId = 2, targetAccountId = 1),
                    MerchantRuleEntity(keyword = "Cepsa", targetCategoryId = 2, targetAccountId = 1),
                    MerchantRuleEntity(keyword = "Uber", targetCategoryId = 2, targetAccountId = 1),
                    MerchantRuleEntity(keyword = "Amazon", targetCategoryId = 6, targetAccountId = 1),
                    MerchantRuleEntity(keyword = "Netflix", targetCategoryId = 7, targetAccountId = 1),
                    MerchantRuleEntity(keyword = "Spotify", targetCategoryId = 7, targetAccountId = 1)
                )
                database.merchantRuleDao().insertAll(defaultRules)

                // 4. Pre-poblar Presupuestos Iniciales con Rollover
                val defaultBudgets = listOf(
                    BudgetEntity(
                        name = "Alimentación Mensual",
                        categoryIds = "1",
                        limitAmount = 400.0,
                        period = BudgetEntity.PERIOD_MONTHLY,
                        rollover = true
                    ),
                    BudgetEntity(
                        name = "Ocio y Salidas",
                        categoryIds = "4",
                        limitAmount = 150.0,
                        period = BudgetEntity.PERIOD_MONTHLY,
                        rollover = false
                    )
                )
                database.budgetDao().insertAll(defaultBudgets)

                // 5. Un par de transacciones de ejemplo para visualizar el dashboard al instante
                val now = System.currentTimeMillis()
                val oneDayMs = 24 * 60 * 60 * 1000L
                val sampleTransactions = listOf(
                    TransactionEntity(
                        accountId = 1,
                        categoryId = 8,
                        amount = 1850.0,
                        timestamp = now - (oneDayMs * 3),
                        note = "Nómina Mensual",
                        transactionType = TransactionEntity.TYPE_INCOME
                    ),
                    TransactionEntity(
                        accountId = 1,
                        categoryId = 1,
                        amount = 45.20,
                        timestamp = now - (oneDayMs * 2),
                        note = "Mercadona compra semanal",
                        transactionType = TransactionEntity.TYPE_EXPENSE
                    ),
                    TransactionEntity(
                        accountId = 1,
                        categoryId = 2,
                        amount = 55.00,
                        timestamp = now - oneDayMs,
                        note = "Gasolinera Repsol",
                        transactionType = TransactionEntity.TYPE_EXPENSE
                    ),
                    TransactionEntity(
                        accountId = 1,
                        toAccountId = 3,
                        amount = 200.0,
                        timestamp = now - 3600000L,
                        note = "Ahorro mensual transferido",
                        transactionType = TransactionEntity.TYPE_TRANSFER
                    )
                )
                database.transactionDao().insertAll(sampleTransactions)
            }
        }
    }
}
