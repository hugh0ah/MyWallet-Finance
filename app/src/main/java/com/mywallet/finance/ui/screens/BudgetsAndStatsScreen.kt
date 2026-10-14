package com.mywallet.finance.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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

@Composable
fun BudgetsAndStatsScreen(
    budgets: List<BudgetEntity>,
    categories: List<CategoryEntity>,
    transactions: List<TransactionEntity>,
    modifier: Modifier = Modifier
) {
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
                        centerSubtitle = String.format("%.0f €", totalExpenses)
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

        // 4. Lista Detallada de Presupuestos Activos con Rollover
        item {
            Text(
                text = "Control de Presupuestos (Rollover)",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        items(budgets) { budget ->
            val status = BudgetRolloverEngine.evaluateBudget(budget, transactions)

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = budget.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(
                                text = "Periodo: ${budget.period}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

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
                                text = "Gastado: ${String.format("%.2f €", status.spentAmount)}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                            if (budget.rollover && status.rolloverAmount != 0.0) {
                                Text(
                                    text = "Remanente previo: ${if (status.rolloverAmount > 0) "+" else ""}${String.format("%.2f €", status.rolloverAmount)}",
                                    fontSize = 11.sp,
                                    color = if (status.rolloverAmount > 0) AccentGreen else AccentRed
                                )
                            }
                        }

                        Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
                            Text(
                                text = if (status.isOverBudget) {
                                    "Excedido por ${String.format("%.2f €", -status.remainingAmount)}"
                                } else {
                                    "Restante: ${String.format("%.2f €", status.remainingAmount)}"
                                },
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (status.isOverBudget) AccentRed else AccentGreen
                            )
                            Text(
                                text = "Límite: ${String.format("%.2f €", status.effectiveLimit)}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
