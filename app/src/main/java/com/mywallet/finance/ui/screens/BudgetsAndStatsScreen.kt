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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mywallet.finance.data.model.BudgetEntity
import com.mywallet.finance.data.model.CategoryEntity
import com.mywallet.finance.data.model.TransactionEntity
import com.mywallet.finance.domain.BudgetRolloverEngine
import com.mywallet.finance.ui.components.BarChartCanvas
import com.mywallet.finance.ui.components.MonthlyBarData
import com.mywallet.finance.ui.components.PieChartCanvas
import com.mywallet.finance.ui.components.PieSliceData
import com.mywallet.finance.ui.theme.AccentGreen
import com.mywallet.finance.ui.theme.AccentOrange
import com.mywallet.finance.ui.theme.AccentRed
import com.mywallet.finance.ui.theme.PrimaryBlue
import java.util.Locale

@Composable
fun BudgetsAndStatsScreen(
    budgets: List<BudgetEntity>,
    categories: List<CategoryEntity>,
    transactions: List<TransactionEntity>,
    onAddBudget: (BudgetEntity) -> Unit,
    onUpdateBudget: (BudgetEntity) -> Unit,
    onDeleteBudget: (BudgetEntity) -> Unit,
    onAddCategory: (CategoryEntity) -> Unit,
    onDeleteCategory: ((CategoryEntity) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var managingBudget by remember { mutableStateOf<BudgetEntity?>(null) }
    var showNewBudgetDialog by remember { mutableStateOf(false) }
    var showNewCategoryDialog by remember { mutableStateOf(false) }

    val categoriesMap = remember(categories) { categories.associateBy { it.id } }

    // Calcular datos del Donut Chart (Gastos por Categoría)
    val pieSlices = remember(transactions, categoriesMap) {
        val expenses = transactions.filter { it.transactionType == TransactionEntity.TYPE_EXPENSE }
        val grouped = expenses.groupBy { it.categoryId ?: 0L }

        grouped.map { (catId, txList) ->
            val cat = categoriesMap[catId]
            val catColor = try {
                Color(android.graphics.Color.parseColor(cat?.colorHex ?: "#1E88E5"))
            } catch (e: Exception) {
                PrimaryBlue
            }
            PieSliceData(
                label = cat?.name ?: "Otros",
                value = txList.sumOf { it.amount },
                color = catColor
            )
        }.sortedByDescending { it.value }
    }

    val totalExpenses = remember(pieSlices) { pieSlices.sumOf { it.value } }

    // Datos comparativos mensuales simulados a partir de los datos registrados
    val monthlyData = remember(transactions) {
        listOf(
            MonthlyBarData("Jul", 1700.0, 1100.0),
            MonthlyBarData("Ago", 1900.0, 1450.0),
            MonthlyBarData("Sep", 1850.0, 1300.0),
            MonthlyBarData(
                "Oct",
                transactions.filter { it.transactionType == TransactionEntity.TYPE_INCOME }.sumOf { it.amount }.coerceAtLeast(1850.0),
                totalExpenses
            )
        )
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Título
        item {
            Text(
                text = "Presupuestos y Estadísticas",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // 2. Gráfico Donut de Gastos por Categoría
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Distribución de Gastos",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    PieChartCanvas(
                        slices = pieSlices,
                        centerTitle = "Total Gastado",
                        centerSubtitle = String.format(Locale.GERMANY, "%.0f €", totalExpenses)
                    )
                }
            }
        }

        // 3. Gráfico de Barras Ingresos vs Gastos
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Flujo Mensual (Ingresos vs Gastos)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    BarChartCanvas(dataPoints = monthlyData)
                }
            }
        }

        // 4. Cabecera de Control de Presupuestos
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Control de Presupuestos",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = { showNewBudgetDialog = true }) {
                    Text("+ Nuevo Presupuesto", fontWeight = FontWeight.SemiBold)
                }
            }
        }

        if (budgets.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("No hay presupuestos activos.")
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { showNewBudgetDialog = true }) {
                            Text("Crear mi primer presupuesto")
                        }
                    }
                }
            }
        }

        items(budgets, key = { it.id }) { budget ->
            val status = BudgetRolloverEngine.evaluateBudget(budget, transactions)

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { managingBudget = budget }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = budget.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(
                                text = "Periodo: ${budget.period}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (budget.rollover) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = AccentGreen.copy(alpha = 0.15f)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "Rollover activo",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AccentGreen,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Editar",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LinearProgressIndicator(
                        progress = { status.progressPercent },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = when {
                            status.isOverBudget -> AccentRed
                            status.progressPercent > 0.8f -> AccentOrange
                            else -> AccentGreen
                        },
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = String.format(Locale.GERMANY, "Gastado: %.2f €", status.spentAmount),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                            if (budget.rollover && status.rolloverAmount != 0.0) {
                                Text(
                                    text = "Remanente previo: ${if (status.rolloverAmount > 0) "+" else ""}${String.format(Locale.GERMANY, "%.2f €", status.rolloverAmount)}",
                                    fontSize = 11.sp,
                                    color = if (status.rolloverAmount > 0) AccentGreen else AccentRed
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = if (status.isOverBudget) {
                                    String.format(Locale.GERMANY, "Excedido por %.2f €", -status.remainingAmount)
                                } else {
                                    String.format(Locale.GERMANY, "Restante: %.2f €", status.remainingAmount)
                                },
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (status.isOverBudget) AccentRed else AccentGreen
                            )
                            Text(
                                text = String.format(Locale.GERMANY, "Límite: %.2f €", status.effectiveLimit),
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // 5. Sección de Gestión de Categorías
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Categorías (${categories.size})",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = { showNewCategoryDialog = true }) {
                    Text("+ Nueva Categoría", fontWeight = FontWeight.SemiBold)
                }
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (cat in categories) {
                    val catColor = try {
                        Color(android.graphics.Color.parseColor(cat.colorHex))
                    } catch (e: Exception) {
                        PrimaryBlue
                    }
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(catColor)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = cat.name,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (cat.isExpense) "• Gasto" else "• Ingreso",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (onDeleteCategory != null && cat.id > 10) {
                                Spacer(modifier = Modifier.width(4.dp))
                                IconButton(
                                    onClick = { onDeleteCategory(cat) },
                                    modifier = Modifier.size(18.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Borrar categoría",
                                        tint = AccentRed,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Diálogo para gestionar presupuesto existente
    managingBudget?.let { budget ->
        ManageBudgetDialog(
            budget = budget,
            categories = categories,
            onDismiss = { managingBudget = null },
            onSave = { updated ->
                onUpdateBudget(updated)
                managingBudget = null
            },
            onDelete = {
                onDeleteBudget(budget)
                managingBudget = null
            }
        )
    }

    // Diálogo para crear nuevo presupuesto
    if (showNewBudgetDialog) {
        NewBudgetDialog(
            categories = categories,
            onDismiss = { showNewBudgetDialog = false },
            onSave = { newBudget ->
                onAddBudget(newBudget)
                showNewBudgetDialog = false
            }
        )
    }

    // Diálogo para crear nueva categoría
    if (showNewCategoryDialog) {
        NewCategoryDialog(
            onDismiss = { showNewCategoryDialog = false },
            onSave = { newCategory ->
                onAddCategory(newCategory)
                showNewCategoryDialog = false
            }
        )
    }
}

/**
 * Diálogo interactivo: Gestionar y editar Presupuesto existente.
 */
@Composable
private fun ManageBudgetDialog(
    budget: BudgetEntity,
    categories: List<CategoryEntity>,
    onDismiss: () -> Unit,
    onSave: (BudgetEntity) -> Unit,
    onDelete: () -> Unit
) {
    var name by remember { mutableStateOf(budget.name) }
    var limitText by remember { mutableStateOf(String.format(Locale.US, "%.2f", budget.limitAmount)) }
    var rollover by remember { mutableStateOf(budget.rollover) }
    var selectedCategoryId by remember {
        mutableStateOf(budget.categoryIds.split(",").firstOrNull()?.trim()?.toLongOrNull() ?: 1L)
    }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val expenseCategories = categories.filter { it.isExpense }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Gestionar Presupuesto", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre del presupuesto") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = limitText,
                    onValueChange = { limitText = it },
                    label = { Text("Límite mensual (€)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Categoría asociada:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (cat in expenseCategories) {
                        FilterChip(
                            selected = selectedCategoryId == cat.id,
                            onClick = { selectedCategoryId = cat.id },
                            label = { Text(cat.name, fontSize = 12.sp) }
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Rollover (Acumular sobrante)", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                        Text("Suma lo no gastado al mes siguiente", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = rollover, onCheckedChange = { rollover = it })
                }

                Spacer(modifier = Modifier.height(4.dp))
                Button(
                    onClick = { showDeleteConfirm = true },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Eliminar Presupuesto")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsedLimit = limitText.replace(",", ".").toDoubleOrNull() ?: budget.limitAmount
                    if (name.isNotBlank() && parsedLimit > 0) {
                        onSave(
                            budget.copy(
                                name = name.trim(),
                                limitAmount = parsedLimit,
                                categoryIds = selectedCategoryId.toString(),
                                rollover = rollover
                            )
                        )
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
            title = { Text("¿Eliminar presupuesto?") },
            text = { Text("Se eliminará este presupuesto. Tus transacciones no se verán afectadas.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentRed)
                ) {
                    Text("Eliminar")
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
 * Diálogo para crear un nuevo presupuesto.
 */
@Composable
private fun NewBudgetDialog(
    categories: List<CategoryEntity>,
    onDismiss: () -> Unit,
    onSave: (BudgetEntity) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var limitText by remember { mutableStateOf("300.00") }
    var rollover by remember { mutableStateOf(true) }
    val expenseCategories = categories.filter { it.isExpense }
    var selectedCategoryId by remember {
        mutableStateOf(expenseCategories.firstOrNull()?.id ?: 1L)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo Presupuesto", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre (ej. Supermercado, Salidas)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = limitText,
                    onValueChange = { limitText = it },
                    label = { Text("Límite mensual (€)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Categoría:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (cat in expenseCategories) {
                        FilterChip(
                            selected = selectedCategoryId == cat.id,
                            onClick = { selectedCategoryId = cat.id },
                            label = { Text(cat.name, fontSize = 12.sp) }
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Rollover automático", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                        Text("Acumula el ahorro para el mes siguiente", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = rollover, onCheckedChange = { rollover = it })
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsedLimit = limitText.replace(",", ".").toDoubleOrNull() ?: 0.0
                    if (name.isNotBlank() && parsedLimit > 0) {
                        val newB = BudgetEntity(
                            name = name.trim(),
                            categoryIds = selectedCategoryId.toString(),
                            limitAmount = parsedLimit,
                            period = BudgetEntity.PERIOD_MONTHLY,
                            rollover = rollover
                        )
                        onSave(newB)
                    }
                }
            ) {
                Text("Crear")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

/**
 * Diálogo para crear una nueva categoría personalizada.
 */
@Composable
private fun NewCategoryDialog(
    onDismiss: () -> Unit,
    onSave: (CategoryEntity) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var isExpense by remember { mutableStateOf(true) }
    var selectedColor by remember { mutableStateOf("#FF5722") }

    val colorsPalette = listOf("#FF5722", "#03A9F4", "#9C27B0", "#FF9800", "#E91E63", "#4CAF50", "#00BCD4", "#673AB7")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva Categoría", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre de la categoría") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = isExpense,
                        onClick = { isExpense = true },
                        label = { Text("Gasto") }
                    )
                    FilterChip(
                        selected = !isExpense,
                        onClick = { isExpense = false },
                        label = { Text("Ingreso") }
                    )
                }

                Text("Color:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
                                    width = if (selectedColor == hex) 3.dp else 0.dp,
                                    color = if (selectedColor == hex) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedColor = hex }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val newCat = CategoryEntity(
                            name = name.trim(),
                            iconName = "category",
                            colorHex = selectedColor,
                            isExpense = isExpense
                        )
                        onSave(newCat)
                    }
                }
            ) {
                Text("Crear Categoría")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
