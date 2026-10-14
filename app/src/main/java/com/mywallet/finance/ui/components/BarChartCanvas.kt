package com.mywallet.finance.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mywallet.finance.ui.theme.AccentGreen
import com.mywallet.finance.ui.theme.AccentRed

data class MonthlyBarData(
    val label: String, // ej. "Ago", "Sep", "Oct"
    val income: Double,
    val expense: Double
)

@Composable
fun BarChartCanvas(
    dataPoints: List<MonthlyBarData>,
    modifier: Modifier = Modifier
) {
    if (dataPoints.isEmpty()) return

    val maxAmount = dataPoints.maxOfOrNull { maxOf(it.income, it.expense) }?.coerceAtLeast(100.0) ?: 100.0

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .padding(horizontal = 8.dp)
        ) {
            val totalWidth = size.width
            val totalHeight = size.height
            val groupWidth = totalWidth / dataPoints.size
            val barWidth = 12.dp.toPx()
            val cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())

            for ((index, item) in dataPoints.withIndex()) {
                val groupCenterX = groupWidth * index + groupWidth / 2

                // Altura de barras normalizadas
                val incomeHeight = ((item.income / maxAmount) * (totalHeight - 20f)).toFloat()
                val expenseHeight = ((item.expense / maxAmount) * (totalHeight - 20f)).toFloat()

                // Barra Ingreso (Verde)
                val incomeLeft = groupCenterX - barWidth - 2.dp.toPx()
                val incomeTop = totalHeight - incomeHeight
                drawRoundRect(
                    color = AccentGreen,
                    topLeft = Offset(incomeLeft, incomeTop),
                    size = Size(barWidth, incomeHeight),
                    cornerRadius = cornerRadius
                )

                // Barra Gasto (Rojo)
                val expenseLeft = groupCenterX + 2.dp.toPx()
                val expenseTop = totalHeight - expenseHeight
                drawRoundRect(
                    color = AccentRed,
                    topLeft = Offset(expenseLeft, expenseTop),
                    size = Size(barWidth, expenseHeight),
                    cornerRadius = cornerRadius
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Etiquetas inferiores de los meses
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            for (item in dataPoints) {
                Text(
                    text = item.label,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Indicador de colores
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Spacer(modifier = Modifier.width(10.dp))
                Canvas(modifier = Modifier.width(10.dp).height(10.dp)) {
                    drawCircle(AccentGreen)
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "Ingresos", fontSize = 11.sp, fontWeight = FontWeight.Medium)
            }
            Spacer(modifier = Modifier.width(20.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Canvas(modifier = Modifier.width(10.dp).height(10.dp)) {
                    drawCircle(AccentRed)
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "Gastos", fontSize = 11.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}
