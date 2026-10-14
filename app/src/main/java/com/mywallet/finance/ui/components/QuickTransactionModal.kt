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
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.mywallet.finance.ui.theme.AccentRed
import com.mywallet.finance.ui.theme.PrimaryBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickTransactionModal(
    accounts: List<AccountEntity>,
    categories: List<CategoryEntity>,
    onDismiss: () -> Unit,
    onSaveTransaction: (TransactionEntity) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var selectedTab by remember { mutableStateOf(0) } // 0: Gasto, 1: Ingreso, 2: Transferencia
    var amountExpression by remember { mutableStateOf("0") }
    var selectedAccountId by remember { mutableStateOf(accounts.firstOrNull()?.id ?: 1L) }
    var selectedToAccountId by remember { mutableStateOf(accounts.getOrNull(1)?.id ?: 2L) }
    var selectedCategoryId by remember { mutableStateOf(categories.firstOrNull { it.isExpense }?.id ?: 1L) }
    var note by remember { mutableStateOf("") }

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
            // Header con Pestañas
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
                                // Actualizar categoría por defecto según pestaña
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
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Cerrar")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Visor de Importe
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .padding(vertical = 12.dp, horizontal = 16.dp),
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

            Spacer(modifier = Modifier.height(10.dp))

            // Selector de Cuenta Origen (y Destino si es transferencia)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (selectedTab == 2) {
                    Text(
                        text = "De:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.CenterVertically)
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
                Spacer(modifier = Modifier.height(6.dp))
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
                Spacer(modifier = Modifier.height(8.dp))
                val relevantCategories = categories.filter {
                    if (selectedTab == 0) it.isExpense else !it.isExpense
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
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
                                    .size(44.dp)
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

            Spacer(modifier = Modifier.height(10.dp))

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
                            accountId = selectedAccountId,
                            toAccountId = if (txType == TransactionEntity.TYPE_TRANSFER) selectedToAccountId else null,
                            categoryId = if (txType != TransactionEntity.TYPE_TRANSFER) selectedCategoryId else null,
                            amount = evaluatedAmount,
                            timestamp = System.currentTimeMillis(),
                            note = if (note.isBlank()) null else note,
                            transactionType = txType,
                            isAutoDetected = false
                        )
                        onSaveTransaction(transaction)
                        onDismiss()
                    }
                }
            )
        }
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
            if (res % 1.0 == 0.0) res.toLong().toString() else String.format("%.2f", res)
        } else {
            val direct = sanitized.toDoubleOrNull() ?: 0.0
            if (direct % 1.0 == 0.0) direct.toLong().toString() else String.format("%.2f", direct)
        }
    } catch (e: Exception) {
        expr
    }
}
