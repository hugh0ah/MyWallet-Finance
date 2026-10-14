package com.mywallet.finance.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.mywallet.finance.data.AppDatabase
import com.mywallet.finance.ui.screens.BudgetsAndStatsScreen
import com.mywallet.finance.ui.screens.HomeScreen
import com.mywallet.finance.ui.screens.SettingsAndRulesScreen
import kotlinx.coroutines.launch

sealed class Screen(val title: String, val icon: ImageVector) {
    object Home : Screen("Inicio", Icons.Default.Home)
    object Stats : Screen("Estadísticas", Icons.Default.PieChart)
    object Settings : Screen("Ajustes", Icons.Default.Settings)
}

@Composable
fun AppNavigation(database: AppDatabase) {
    var selectedScreenIndex by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()

    val accounts by database.accountDao().getAllAccountsFlow().collectAsState(initial = emptyList())
    val categories by database.categoryDao().getAllCategoriesFlow().collectAsState(initial = emptyList())
    val transactions by database.transactionDao().getAllTransactionsFlow().collectAsState(initial = emptyList())
    val budgets by database.budgetDao().getAllBudgetsFlow().collectAsState(initial = emptyList())
    val merchantRules by database.merchantRuleDao().getAllRulesFlow().collectAsState(initial = emptyList())

    val screens = listOf(Screen.Home, Screen.Stats, Screen.Settings)

    Scaffold(
        bottomBar = {
            NavigationBar {
                screens.forEachIndexed { index, screen ->
                    NavigationBarItem(
                        selected = selectedScreenIndex == index,
                        onClick = { selectedScreenIndex = index },
                        icon = { Icon(imageVector = screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) }
                    )
                }
            }
        }
    ) { innerPadding ->
        when (selectedScreenIndex) {
            0 -> {
                HomeScreen(
                    accounts = accounts,
                    categories = categories,
                    transactions = transactions,
                    budgets = budgets,
                    onAddTransaction = { newTx ->
                        scope.launch {
                            database.transactionDao().insertTransaction(newTx)
                        }
                    },
                    onUpdateTransaction = { tx ->
                        scope.launch {
                            database.transactionDao().updateTransaction(tx)
                        }
                    },
                    onDeleteTransaction = { tx ->
                        scope.launch {
                            database.transactionDao().deleteTransaction(tx)
                        }
                    },
                    onDeleteAllTransactions = {
                        scope.launch {
                            database.transactionDao().deleteAllTransactions()
                        }
                    },
                    onAddAccount = { newAccount ->
                        scope.launch {
                            database.accountDao().insertAccount(newAccount)
                        }
                    },
                    onUpdateAccount = { account ->
                        scope.launch {
                            database.accountDao().updateAccount(account)
                        }
                    },
                    onDeleteAccount = { account ->
                        scope.launch {
                            database.accountDao().deleteAccount(account)
                        }
                    },
                    onAddCategory = { cat ->
                        scope.launch {
                            database.categoryDao().insertCategory(cat)
                        }
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            1 -> {
                BudgetsAndStatsScreen(
                    budgets = budgets,
                    categories = categories,
                    transactions = transactions,
                    onAddBudget = { newBudget ->
                        scope.launch {
                            database.budgetDao().insertBudget(newBudget)
                        }
                    },
                    onUpdateBudget = { budget ->
                        scope.launch {
                            database.budgetDao().updateBudget(budget)
                        }
                    },
                    onDeleteBudget = { budget ->
                        scope.launch {
                            database.budgetDao().deleteBudget(budget)
                        }
                    },
                    onAddCategory = { newCategory ->
                        scope.launch {
                            database.categoryDao().insertCategory(newCategory)
                        }
                    },
                    onDeleteCategory = { category ->
                        scope.launch {
                            database.categoryDao().deleteCategory(category)
                        }
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            2 -> {
                SettingsAndRulesScreen(
                    accounts = accounts,
                    categories = categories,
                    merchantRules = merchantRules,
                    onAddMerchantRule = { rule ->
                        scope.launch {
                            database.merchantRuleDao().insertRule(rule)
                        }
                    },
                    onDeleteMerchantRule = { rule ->
                        scope.launch {
                            database.merchantRuleDao().deleteRule(rule)
                        }
                    },
                    onDeleteAllTransactions = {
                        scope.launch {
                            database.transactionDao().deleteAllTransactions()
                        }
                    },
                    onResetDatabaseToFactory = {
                        scope.launch {
                            database.resetToFactory()
                        }
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}
