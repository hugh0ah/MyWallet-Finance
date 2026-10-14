package com.mywallet.finance.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mywallet.finance.data.model.AccountEntity
import com.mywallet.finance.data.model.BudgetEntity
import com.mywallet.finance.data.model.CategoryEntity
import com.mywallet.finance.data.model.TransactionEntity
import com.mywallet.finance.domain.BalanceCalculator
import com.mywallet.finance.domain.BudgetRolloverEngine
import com.mywallet.finance.ui.components.QuickTransactionModal
import com.mywallet.finance.ui.theme.AccentGreen
import com.mywallet.finance.ui.theme.AccentOrange
import com.mywallet.finance.ui.theme.AccentRed
import com.mywallet.finance.ui.theme.PrimaryBlue
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    accounts: List<AccountEntity>,
    categories: List<CategoryEntity>,
    transactions: List<TransactionEntity>,
    budgets: List<BudgetEntity>,
    onAddTransaction: (TransactionEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var showQuickModal by remember { mutableStateOf(false) }

    val netWorth = remember(accounts, transactions) {
        BalanceCalculator.calculateNetWorth(accounts, transactions)
    }

    val totalIncome = remember(transactions) {
        BalanceCalculator.calculateTotalIncome(transactions)
    }

    val totalExpense = remember(transactions) {
        BalanceCalculator.calculateTotalExpenses(transactions)
    }

    val categoriesMap = remember(categories) { categories.associateBy { it.id } }
    val accountsMap = remember(accounts) { accounts.associateBy { it.id } }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showQuickModal = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Nueva Transacción")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // 1. Tarjeta de Patrimonio Neto (Net Worth Card)
            item {
                NetWorthCard(
                    netWorth = netWorth,
                    totalIncome = totalIncome,
                    totalExpense = totalExpense
                )
            }

            // 2. Carrusel Horizontal de Cuentas
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Tus Cuentas",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                AccountsCarousel(
                    accounts = accounts,
                    transactions = transactions
                )
            }

            // 3. Resumen de Presupuestos Activos (con Rollover)
            if (budgets.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "Presupuestos Activos",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    BudgetsRow(
                        budgets = budgets,
                        transactions = transactions
                    )
                }
            }

            // 4. Lista de Movimientos Recientes Agrupados
            item {
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "Movimientos Recientes",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (transactions.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No hay transacciones registradas.\nToca el botón + para añadir una.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                items(transactions) { tx ->
                    TransactionListItem(
                        transaction = tx,
                        category = categoriesMap[tx.categoryId],
                        account = accountsMap[tx.accountId],
                        toAccount = tx.toAccountId?.let { accountsMap[it] }
                    )
                }
            }
        }

        if (showQuickModal) {
            QuickTransactionModal(
                accounts = accounts,
                categories = categories,
                onDismiss = { showQuickModal = false },
                onSaveTransaction = { newTx ->
                    onAddTransaction(newTx)
                }
            )
        }
    }
}

@Composable
private fun NetWorthCard(
    netWorth: Double,
    totalIncome: Double,
    totalExpense: Double
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(PrimaryBlue, Color(0xFF1565C0))
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Text(
                    text = "Patrimonio Neto Total",
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.85f),
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = String.format(Locale.GERMANY, "%.2f €", netWorth),
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Ingresos
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = Color(0xFFB9F6CA),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(text = "Ingresos", fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
                            Text(
                                text = String.format(Locale.GERMANY, "+%.2f €", totalIncome),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }

                    // Gastos
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.TrendingDown,
                                contentDescription = null,
                                tint = Color(0xFFFF8A80),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(text = "Gastos", fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
                            Text(
                                text = String.format(Locale.GERMANY, "-%.2f €", totalExpense),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AccountsCarousel(
    accounts: List<AccountEntity>,
    transactions: List<TransactionEntity>
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        for (account in accounts) {
            val balance = BalanceCalculator.calculateAccountBalance(account, transactions)
            val cardColor = try {
                Color(android.graphics.Color.parseColor(account.colorHex))
            } catch (e: Exception) {
                PrimaryBlue
            }

            Card(
                modifier = Modifier
                    .width(160.dp)
                    .height(100.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = account.name,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1
                        )
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(cardColor)
                        )
                    }

                    Column {
                        Text(
                            text = String.format(Locale.GERMANY, "%.2f %s", balance, account.currencyCode),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = account.accountType,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BudgetsRow(
    budgets: List<BudgetEntity>,
    transactions: List<TransactionEntity>
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        for (budget in budgets) {
            val status = BudgetRolloverEngine.evaluateBudget(budget, transactions)

            Card(
                modifier = Modifier.width(220.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = budget.name, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        if (budget.rollover) {
                            Text(text = "Rollover", fontSize = 10.sp, color = AccentGreen, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LinearProgressIndicator(
                        progress = { status.progressPercent },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (status.isOverBudget) AccentRed else AccentOrange,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = String.format("%.0f€ / %.0f€", status.spentAmount, status.effectiveLimit),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (status.isOverBudget) "Excedido" else String.format("%.0f€ libre", status.remainingAmount),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (status.isOverBudget) AccentRed else AccentGreen
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TransactionListItem(
    transaction: TransactionEntity,
    category: CategoryEntity?,
    account: AccountEntity?,
    toAccount: AccountEntity?
) {
    val dateStr = remember(transaction.timestamp) {
        SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(Date(transaction.timestamp))
    }

    val isExpense = transaction.transactionType == TransactionEntity.TYPE_EXPENSE
    val isTransfer = transaction.transactionType == TransactionEntity.TYPE_TRANSFER

    val amountColor = when {
        isExpense -> AccentRed
        isTransfer -> PrimaryBlue
        else -> AccentGreen
    }

    val amountPrefix = when {
        isExpense -> "-"
        isTransfer -> ""
        else -> "+"
    }

    val catColor = try {
        Color(android.graphics.Color.parseColor(category?.colorHex ?: "#1E88E5"))
    } catch (e: Exception) {
        PrimaryBlue
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(if (isTransfer) PrimaryBlue.copy(alpha = 0.15f) else catColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = when {
                    isTransfer -> Icons.Default.SwapHoriz
                    isExpense -> Icons.Default.ArrowDownward
                    else -> Icons.Default.ArrowUpward
                },
                contentDescription = null,
                tint = if (isTransfer) PrimaryBlue else catColor,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (isTransfer) {
                        "${account?.name ?: ""} → ${toAccount?.name ?: ""}"
                    } else {
                        transaction.note ?: category?.name ?: "Sin categoría"
                    },
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
                if (transaction.isAutoDetected) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.FlashOn,
                        contentDescription = "Auto-detectado",
                        tint = AccentOrange,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
            Text(
                text = "${account?.name ?: ""} • $dateStr",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Text(
            text = "$amountPrefix${String.format(Locale.GERMANY, "%.2f €", transaction.amount)}",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = amountColor
        )
    }
}
