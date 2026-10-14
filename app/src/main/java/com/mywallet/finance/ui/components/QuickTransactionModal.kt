package com.mywallet.finance.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mywallet.finance.data.model.AccountEntity
import com.mywallet.finance.data.model.CategoryEntity
import com.mywallet.finance.data.model.TransactionEntity
import com.mywallet.finance.ui.theme.AccentGreen
import com.mywallet.finance.ui.theme.AccentOrange
import com.mywallet.finance.ui.theme.AccentRed
import com.mywallet.finance.ui.theme.PrimaryBlue
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickTransactionModal(
    accounts: List<AccountEntity>,
    categories: List<CategoryEntity>,
    existingTransaction: TransactionEntity? = null,
    onDismiss: () -> Unit,
    onSaveTransaction: (TransactionEntity) -> Unit,
    onDeleteTransaction: ((TransactionEntity) -> Unit)? = null,
    onAddCategory: ((CategoryEntity) -> Unit)? = null
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val initialTab = when (existingTransaction?.transactionType) {
        TransactionEntity.TYPE_INCOME -> 1
        TransactionEntity.TYPE_TRANSFER -> 2
        else -> 0
    }

    val initialAmount = existingTransaction?.let {
        if (it.amount % 1.0 == 0.0) it.amount.toLong().toString() else String.format(Locale.US, "%.2f", it.amount)
    } ?: "0"

    var selectedTab by remember { mutableIntStateOf(initialTab) } // 0: Gasto, 1: Ingreso, 2: Transferencia
    var amountExpression by remember { mutableStateOf(initialAmount) }
    var selectedAccountId by remember {
        mutableStateOf(existingTransaction?.accountId ?: accounts.firstOrNull()?.id ?: 1L)
    }
    var selectedToAccountId by remember {
        mutableStateOf(existingTransaction?.toAccountId ?: accounts.getOrNull(1)?.id ?: 2L)
    }
    var selectedCategoryId by remember {
        mutableStateOf(existingTransaction?.categoryId ?: categories.firstOrNull { it.isExpense }?.id ?: 1L)
    }
    var note by remember { mutableStateOf(existingTransaction?.note ?: "") }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showNewCategoryDialog by remember { mutableStateOf(false) }

    val tabs = listOf("Gasto", "Ingreso", "Transferencia")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            // Header con Pestañas y título/acciones
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TabRow(
                    selectedTabIndex = selectedTab,
                    modifier = Modifier.weight(1f)
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = {
                                selectedTab = index
                                if (index == 0) {
                                    categories.firstOrNull { it.isExpense }?.let { selectedCategoryId = it.id }
                                } else if (index == 1) {
                                    categories.firstOrNull { !it.isExpense }?.let { selectedCategoryId = it.id }
                                }
                            },
                            text = {
                                Text(
                                    text = title,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                    color = when {
                                        selectedTab == index && index == 0 -> AccentRed
                                        selectedTab == index && index == 1 -> AccentGreen
                                        selectedTab == index && index == 2 -> PrimaryBlue
                                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            }
                        )
                    }
                }

                if (existingTransaction != null && onDeleteTransaction != null) {
                    IconButton(onClick = { showDeleteConfirmDialog = true }) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Eliminar", tint = AccentRed)
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Cerrar")
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Visor de Importe
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .padding(vertical = 10.dp, horizontal = 16.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = "$amountExpression €",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = when (selectedTab) {
                        0 -> AccentRed
                        1 -> AccentGreen
                        else -> PrimaryBlue
                    }
                )
            }

            // Campo de Nota / Concepto
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Nota / Concepto (opcional)", fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Selector de Cuenta Origen (y Destino si es transferencia)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (selectedTab == 2) {
                    Text(
                        text = "De:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                for (account in accounts) {
                    val isSelected = account.id == selectedAccountId
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedAccountId = account.id },
                        label = { Text(account.name) },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.AccountBalance, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    )
                }
            }

            if (selectedTab == 2) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "A: ",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    for (account in accounts.filter { it.id != selectedAccountId }) {
                        val isSelected = account.id == selectedToAccountId
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedToAccountId = account.id },
                            label = { Text(account.name) },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        )
                    }
                }
            }

            // Selector de Categorías (solo si no es transferencia)
            if (selectedTab != 2) {
                Spacer(modifier = Modifier.height(6.dp))
                val relevantCategories = categories.filter {
                    if (selectedTab == 0) it.isExpense else !it.isExpense
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Botón de crear nueva categoría directamente
                    if (onAddCategory != null) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { showNewCategoryDialog = true }
                                .padding(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Nueva Categoría",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "+ Nueva",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1
                            )
                        }
                    }

                    for (cat in relevantCategories) {
                        val isSelected = cat.id == selectedCategoryId
                        val catColor = try {
                            Color(android.graphics.Color.parseColor(cat.colorHex))
                        } catch (e: Exception) {
                            PrimaryBlue
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { selectedCategoryId = cat.id }
                                .padding(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(catColor.copy(alpha = if (isSelected) 1f else 0.25f))
                                    .border(
                                        width = if (isSelected) 2.dp else 0.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Category,
                                    contentDescription = cat.name,
                                    tint = if (isSelected) Color.White else catColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = cat.name,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Teclado Numérico Embebido
            CustomNumpad(
                onDigitClick = { digit ->
                    if (amountExpression == "0" && digit != ".") {
                        amountExpression = digit
                    } else {
                        amountExpression += digit
                    }
                },
                onOperationClick = { op ->
                    if (!amountExpression.endsWith("+") && !amountExpression.endsWith("-")) {
                        amountExpression += " $op "
                    }
                },
                onDeleteClick = {
                    if (amountExpression.length > 1) {
                        amountExpression = amountExpression.trimEnd()
                        amountExpression = amountExpression.substring(0, amountExpression.length - 1).trimEnd()
                        if (amountExpression.isEmpty()) amountExpression = "0"
                    } else {
                        amountExpression = "0"
                    }
                },
                onEvaluateClick = {
                    amountExpression = evaluateExpression(amountExpression)
                },
                onConfirmClick = {
                    val evaluatedAmount = evaluateExpression(amountExpression).toDoubleOrNull() ?: 0.0
                    if (evaluatedAmount > 0.0) {
                        val txType = when (selectedTab) {
                            0 -> TransactionEntity.TYPE_EXPENSE
                            1 -> TransactionEntity.TYPE_INCOME
                            else -> TransactionEntity.TYPE_TRANSFER
                        }

                        val transaction = TransactionEntity(
                            id = existingTransaction?.id ?: 0,
                            accountId = selectedAccountId,
                            toAccountId = if (txType == TransactionEntity.TYPE_TRANSFER) selectedToAccountId else null,
                            categoryId = if (txType != TransactionEntity.TYPE_TRANSFER) selectedCategoryId else null,
                            amount = evaluatedAmount,
                            timestamp = existingTransaction?.timestamp ?: System.currentTimeMillis(),
                            note = if (note.isBlank()) null else note,
                            transactionType = txType,
                            isAutoDetected = existingTransaction?.isAutoDetected ?: false
                        )
                        onSaveTransaction(transaction)
                        onDismiss()
                    }
                }
            )
        }
    }

    // Diálogo de confirmación de eliminación
    if (showDeleteConfirmDialog && existingTransaction != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Eliminar movimiento") },
            text = { Text("¿Deseas eliminar este movimiento permanentemente? Los saldos de las cuentas se recalcularán automáticamente.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmDialog = false
                        onDeleteTransaction?.invoke(existingTransaction)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentRed)
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Diálogo rápido para crear nueva categoría
    if (showNewCategoryDialog && onAddCategory != null) {
        var catName by remember { mutableStateOf("") }
        var catColor by remember { mutableStateOf("#FF5722") }
        val colorsPalette = listOf("#FF5722", "#03A9F4", "#9C27B0", "#FF9800", "#E91E63", "#4CAF50", "#00BCD4", "#673AB7")

        AlertDialog(
            onDismissRequest = { showNewCategoryDialog = false },
            title = { Text("Nueva Categoría (${if (selectedTab == 0) "Gasto" else "Ingreso"})") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = catName,
                        onValueChange = { catName = it },
                        label = { Text("Nombre de la categoría") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("Elige un color:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(c)
                                    .border(
                                        width = if (catColor == hex) 3.dp else 0.dp,
                                        color = if (catColor == hex) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable { catColor = hex }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (catName.isNotBlank()) {
                            val newCat = CategoryEntity(
                                name = catName.trim(),
                                iconName = "category",
                                colorHex = catColor,
                                isExpense = (selectedTab == 0)
                            )
                            onAddCategory(newCat)
                            showNewCategoryDialog = false
                        }
                    }
                ) {
                    Text("Crear")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewCategoryDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

/**
 * Evalúa expresiones simples de calculadora como "15 + 4.5" o "50 - 12"
 */
fun evaluateExpression(expr: String): String {
    return try {
        val sanitized = expr.replace(",", ".").trim()
        val parts = sanitized.split(" ")
        if (parts.size == 3) {
            val a = parts[0].toDouble()
            val op = parts[1]
            val b = parts[2].toDouble()
            val res = when (op) {
                "+" -> a + b
                "-" -> a - b
                else -> a
            }
            if (res % 1.0 == 0.0) res.toLong().toString() else String.format(Locale.US, "%.2f", res)
        } else {
            val direct = sanitized.toDoubleOrNull() ?: 0.0
            if (direct % 1.0 == 0.0) direct.toLong().toString() else String.format(Locale.US, "%.2f", direct)
        }
    } catch (e: Exception) {
        expr
    }
}
