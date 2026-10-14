package com.mywallet.finance.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.input.KeyboardType
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
    onUpdateTransaction: (TransactionEntity) -> Unit,
    onDeleteTransaction: (TransactionEntity) -> Unit,
    onDeleteAllTransactions: () -> Unit,
    onAddAccount: (AccountEntity) -> Unit,
    onUpdateAccount: (AccountEntity) -> Unit,
    onDeleteAccount: (AccountEntity) -> Unit,
    onAddCategory: ((CategoryEntity) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var showQuickModal by remember { mutableStateOf(false) }
    var editingTransaction by remember { mutableStateOf<TransactionEntity?>(null) }
    var managingAccount by remember { mutableStateOf<AccountEntity?>(null) }
    var showNewAccountDialog by remember { mutableStateOf(false) }
    var showCleanDataConfirmDialog by remember { mutableStateOf(false) }

    // Filtro por Cuenta: null = Todas
    var selectedFilterAccountId by remember { mutableStateOf<Long?>(null) }

    val netWorth = remember(accounts, transactions) {
        BalanceCalculator.calculateNetWorth(accounts, transactions)
    }

    val totalIncome = remember(transactions) {
        BalanceCalculator.calculateTotalIncome(transactions)
    }

    val totalExpense = remember(transactions) {
        BalanceCalculator.calculateTotalExpenses(transactions)
    }

    val filteredTransactions = remember(transactions, selectedFilterAccountId) {
        if (selectedFilterAccountId == null) {
            transactions
        } else {
            transactions.filter {
                it.accountId == selectedFilterAccountId || it.toAccountId == selectedFilterAccountId
            }
        }
    }

    val categoriesMap = remember(categories) { categories.associateBy { it.id } }
    val accountsMap = remember(accounts) { accounts.associateBy { it.id } }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingTransaction = null
                    showQuickModal = true
                },
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

            // 2. Banner de Limpieza de Datos de Prueba (si existen transacciones)
            if (transactions.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = { showCleanDataConfirmDialog = true },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentOrange)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CleaningServices,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Empezar de cero (Limpiar pruebas)", fontSize = 12.sp)
                        }
                    }
                }
            }

            // 3. Carrusel Horizontal de Cuentas con Botón "+ Nueva Cuenta"
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tus Cuentas",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = { showNewAccountDialog = true }) {
                        Text("+ Nueva", fontWeight = FontWeight.SemiBold)
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                AccountsCarousel(
                    accounts = accounts,
                    transactions = transactions,
                    onAccountClick = { account ->
                        managingAccount = account
                    },
                    onNewAccountClick = {
                        showNewAccountDialog = true
                    }
                )
            }

            // 4. Resumen de Presupuestos Activos (con Rollover)
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

            // 5. Selector / Filtro por Cuenta
            item {
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Movimientos",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Chips de filtro por cuenta
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedFilterAccountId == null,
                        onClick = { selectedFilterAccountId = null },
                        label = { Text("Todas (${transactions.size})") }
                    )
                    for (acc in accounts) {
                        val count = transactions.count { it.accountId == acc.id || it.toAccountId == acc.id }
                        FilterChip(
                            selected = selectedFilterAccountId == acc.id,
                            onClick = { selectedFilterAccountId = acc.id },
                            label = { Text("${acc.name} ($count)") }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            // 6. Lista de Movimientos Filtrados (Clicables para editar/borrar)
            if (filteredTransactions.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (transactions.isEmpty()) {
                                "No hay transacciones registradas.\nToca el botón + para añadir una."
                            } else {
                                "No hay transacciones para esta cuenta seleccionada."
                            },
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                items(filteredTransactions, key = { it.id }) { tx ->
                    TransactionListItem(
                        transaction = tx,
                        category = categoriesMap[tx.categoryId],
                        account = accountsMap[tx.accountId],
                        toAccount = tx.toAccountId?.let { accountsMap[it] },
                        onClick = {
                            editingTransaction = tx
                            showQuickModal = true
                        }
                    )
                }
            }
        }

        // Modal de Registro o Edición de Transacción
        if (showQuickModal) {
            QuickTransactionModal(
                accounts = accounts,
                categories = categories,
                existingTransaction = editingTransaction,
                onDismiss = {
                    showQuickModal = false
                    editingTransaction = null
                },
                onSaveTransaction = { tx ->
                    if (editingTransaction != null) {
                        onUpdateTransaction(tx)
                    } else {
                        onAddTransaction(tx)
                    }
                },
                onDeleteTransaction = { tx ->
                    onDeleteTransaction(tx)
                },
                onAddCategory = onAddCategory
            )
        }

        // Diálogo para gestionar cuenta y ajustar saldo real
        managingAccount?.let { account ->
            ManageAccountDialog(
                account = account,
                transactions = transactions,
                accountsCount = accounts.size,
                onDismiss = { managingAccount = null },
                onSave = { updatedAccount, adjustmentTx ->
                    onUpdateAccount(updatedAccount)
                    adjustmentTx?.let { onAddTransaction(it) }
                    managingAccount = null
                },
                onDelete = {
                    onDeleteAccount(account)
                    managingAccount = null
                }
            )
        }

        // Diálogo para crear nueva cuenta
        if (showNewAccountDialog) {
            NewAccountDialog(
                onDismiss = { showNewAccountDialog = false },
                onSave = { newAccount ->
                    onAddAccount(newAccount)
                    showNewAccountDialog = false
                }
            )
        }

        // Diálogo de confirmación para limpiar datos de prueba
        if (showCleanDataConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showCleanDataConfirmDialog = false },
                title = { Text("Empezar de cero") },
                text = {
                    Text("¿Deseas borrar todas las transacciones actuales? Tus cuentas se mantendrán para que puedas ajustar sus saldos reales de inmediato.")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showCleanDataConfirmDialog = false
                            onDeleteAllTransactions()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentRed)
                    ) {
                        Text("Borrar todo")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCleanDataConfirmDialog = false }) {
                        Text("Cancelar")
                    }
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
                                imageVector = Icons.AutoMirrored.Filled.TrendingUp,
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
                                imageVector = Icons.AutoMirrored.Filled.TrendingDown,
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
    transactions: List<TransactionEntity>,
    onAccountClick: (AccountEntity) -> Unit,
    onNewAccountClick: () -> Unit
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
                    .width(170.dp)
                    .height(110.dp)
                    .clickable { onAccountClick(account) },
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
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            modifier = Modifier.weight(1f)
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(cardColor)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Editar cuenta",
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }

                    Column {
                        Text(
                            text = String.format(Locale.GERMANY, "%.2f %s", balance, account.currencyCode),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = account.accountType,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (!account.includeInTotal) {
                                Text(
                                    text = "Excluida",
                                    fontSize = 9.sp,
                                    color = AccentOrange,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Tarjeta para Añadir Nueva Cuenta
        Card(
            modifier = Modifier
                .width(140.dp)
                .height(110.dp)
                .clickable { onNewAccountClick() },
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Nueva Cuenta",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Nueva Cuenta",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

/**
 * Diálogo interactivo: Gestionar Cuenta y Ajustar Saldo Real en euros.
 */
@Composable
private fun ManageAccountDialog(
    account: AccountEntity,
    transactions: List<TransactionEntity>,
    accountsCount: Int,
    onDismiss: () -> Unit,
    onSave: (AccountEntity, TransactionEntity?) -> Unit,
    onDelete: () -> Unit
) {
    val currentCalculatedBalance = remember(account, transactions) {
        BalanceCalculator.calculateAccountBalance(account, transactions)
    }

    var accountName by remember { mutableStateOf(account.name) }
    var realBalanceText by remember {
        mutableStateOf(String.format(Locale.US, "%.2f", currentCalculatedBalance))
    }
    var includeInTotal by remember { mutableStateOf(account.includeInTotal) }
    var selectedColor by remember { mutableStateOf(account.colorHex) }
    var adjustmentOption by remember { mutableStateOf(0) } // 0: Ajustar saldo inicial, 1: Transacción de ajuste
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val colorsPalette = listOf("#1976D2", "#388E3C", "#7B1FA2", "#F57C00", "#E53935", "#00897B", "#3949AB", "#D81B60")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Gestionar Cuenta y Saldo",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = accountName,
                    onValueChange = { accountName = it },
                    label = { Text("Nombre de la cuenta") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Campo grande de Saldo Real Deseado
                Text(
                    text = "¿Cuánto dinero tienes ahora en esta cuenta?",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PrimaryBlue
                )
                OutlinedTextField(
                    value = realBalanceText,
                    onValueChange = { realBalanceText = it },
                    label = { Text("Saldo Actual Real (€)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                // Opciones de ajuste
                Column {
                    Text(
                        text = "Método de ajuste de saldo:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = adjustmentOption == 0, onClick = { adjustmentOption = 0 })
                        Text("Ajustar saldo inicial de forma limpia", fontSize = 12.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = adjustmentOption == 1, onClick = { adjustmentOption = 1 })
                        Text("Registrar diferencia como ajuste", fontSize = 12.sp)
                    }
                }

                // Selector de color
                Text("Color:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (hex in colorsPalette) {
                        val c = Color(android.graphics.Color.parseColor(hex))
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(c)
                                .border(
                                    width = if (selectedColor == hex) 3.dp else 0.dp,
                                    color = if (selectedColor == hex) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedColor = hex }
                        )
                    }
                }

                // Switch patrimonio
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Incluir en patrimonio total", fontSize = 13.sp)
                    Switch(checked = includeInTotal, onCheckedChange = { includeInTotal = it })
                }

                // Botón eliminar cuenta
                if (accountsCount > 1) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        onClick = { showDeleteConfirm = true },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Eliminar esta cuenta")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val desiredBalance = realBalanceText.replace(",", ".").toDoubleOrNull() ?: currentCalculatedBalance
                    if (adjustmentOption == 0) {
                        // Opción 0: Ajustar automáticamente el initialBalance para que coincida exactamente
                        val newInitialBalance = BalanceCalculator.calculateInitialBalanceForDesired(
                            desiredBalance = desiredBalance,
                            accountId = account.id,
                            transactions = transactions
                        )
                        val updated = account.copy(
                            name = accountName.trim(),
                            colorHex = selectedColor,
                            includeInTotal = includeInTotal,
                            initialBalance = newInitialBalance
                        )
                        onSave(updated, null)
                    } else {
                        // Opción 1: Crear transacción de ajuste
                        val difference = desiredBalance - currentCalculatedBalance
                        val updated = account.copy(
                            name = accountName.trim(),
                            colorHex = selectedColor,
                            includeInTotal = includeInTotal
                        )
                        val adjustmentTx = if (Math.abs(difference) > 0.001) {
                            TransactionEntity(
                                accountId = account.id,
                                amount = Math.abs(difference),
                                timestamp = System.currentTimeMillis(),
                                note = "Ajuste de saldo manual",
                                transactionType = if (difference > 0) TransactionEntity.TYPE_INCOME else TransactionEntity.TYPE_EXPENSE,
                                isAutoDetected = false
                            )
                        } else null

                        onSave(updated, adjustmentTx)
                    }
                }
            ) {
                Text("Guardar Cambios")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("¿Eliminar ${account.name}?") },
            text = { Text("Se eliminará esta cuenta. Las transacciones asociadas deberán ser reasignadas o eliminadas.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentRed)
                ) {
                    Text("Confirmar eliminación")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

/**
 * Diálogo para crear una nueva cuenta desde cero.
 */
@Composable
private fun NewAccountDialog(
    onDismiss: () -> Unit,
    onSave: (AccountEntity) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var initialBalanceText by remember { mutableStateOf("0.00") }
    var selectedType by remember { mutableStateOf("REGULAR") }
    var selectedColor by remember { mutableStateOf("#1976D2") }
    var includeInTotal by remember { mutableStateOf(true) }

    val colorsPalette = listOf("#1976D2", "#388E3C", "#7B1FA2", "#F57C00", "#E53935", "#00897B", "#3949AB", "#D81B60")
    val types = listOf("REGULAR" to "Bancaria", "CASH" to "Efectivo", "SAVINGS" to "Ahorros")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva Cuenta", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre de la cuenta (ej. Revolut, BBVA)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = initialBalanceText,
                    onValueChange = { initialBalanceText = it },
                    label = { Text("Saldo Actual Inicial (€)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Tipo de cuenta:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for ((typeKey, typeLabel) in types) {
                        FilterChip(
                            selected = selectedType == typeKey,
                            onClick = { selectedType = typeKey },
                            label = { Text(typeLabel, fontSize = 12.sp) }
                        )
                    }
                }

                Text("Color distintivo:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (hex in colorsPalette) {
                        val c = Color(android.graphics.Color.parseColor(hex))
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(c)
                                .border(
                                    width = if (selectedColor == hex) 3.dp else 0.dp,
                                    color = if (selectedColor == hex) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedColor = hex }
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Incluir en patrimonio total", fontSize = 13.sp)
                    Switch(checked = includeInTotal, onCheckedChange = { includeInTotal = it })
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val initBal = initialBalanceText.replace(",", ".").toDoubleOrNull() ?: 0.0
                        val newAcc = AccountEntity(
                            name = name.trim(),
                            initialBalance = initBal,
                            currencyCode = "EUR",
                            colorHex = selectedColor,
                            iconName = if (selectedType == "CASH") "payments" else if (selectedType == "SAVINGS") "savings" else "account_balance",
                            accountType = selectedType,
                            includeInTotal = includeInTotal
                        )
                        onSave(newAcc)
                    }
                }
            ) {
                Text("Crear Cuenta")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
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
    toAccount: AccountEntity?,
    onClick: () -> Unit
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
            .clickable { onClick() }
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
