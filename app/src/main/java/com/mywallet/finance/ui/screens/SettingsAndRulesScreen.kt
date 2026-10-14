package com.mywallet.finance.ui.screens

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mywallet.finance.data.AppDatabase
import com.mywallet.finance.data.model.AccountEntity
import com.mywallet.finance.data.model.CategoryEntity
import com.mywallet.finance.data.model.MerchantRuleEntity
import com.mywallet.finance.domain.DataBackupManager
import com.mywallet.finance.service.PaymentNotificationListenerService
import com.mywallet.finance.ui.theme.AccentGreen
import com.mywallet.finance.ui.theme.AccentOrange
import com.mywallet.finance.ui.theme.AccentRed
import com.mywallet.finance.ui.theme.PrimaryBlue
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsAndRulesScreen(
    accounts: List<AccountEntity>,
    categories: List<CategoryEntity>,
    merchantRules: List<MerchantRuleEntity>,
    onAddMerchantRule: (MerchantRuleEntity) -> Unit,
    onDeleteMerchantRule: (MerchantRuleEntity) -> Unit,
    onDeleteAllTransactions: () -> Unit,
    onResetDatabaseToFactory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val database = remember { AppDatabase.getInstance(context) }
    val backupManager = remember { DataBackupManager(database) }

    var testNotificationText by remember {
        mutableStateOf("Has pagado 24,50 € en Mercadona con tarjeta")
    }

    var showAddRuleDialog by remember { mutableStateOf(false) }
    var showClearTransactionsDialog by remember { mutableStateOf(false) }
    var showResetFactoryDialog by remember { mutableStateOf(false) }

    val categoriesMap = remember(categories) { categories.associateBy { it.id } }
    val accountsMap = remember(accounts) { accounts.associateBy { it.id } }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Título
        item {
            Text(
                text = "Ajustes y Automatización",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // 2. Tarjeta Destacada: Gestión de Datos Reales
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.CleaningServices, contentDescription = null, tint = PrimaryBlue)
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            text = "Gestión de Datos Reales",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Configura la app para tu uso diario real eliminando movimientos de prueba o restableciendo la configuración inicial.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    // Botón 1: Borrar todas las transacciones
                    Button(
                        onClick = { showClearTransactionsDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentOrange),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.size(6.dp))
                        Text("Borrar todas las transacciones de prueba")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Botón 2: Restablecer aplicación de fábrica
                    OutlinedButton(
                        onClick = { showResetFactoryDialog = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentRed),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.size(6.dp))
                        Text("Restablecer aplicación de fábrica")
                    }
                }
            }
        }

        // 3. Permiso del Sistema para Notificaciones
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Notifications, contentDescription = null, tint = PrimaryBlue)
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(text = "Acceso a Notificaciones de Pago", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Permite a MyWallet escuchar notificaciones de Google Wallet y bancos compatibles (BBVA, Santander, CaixaBank, Sabadell, Revolut, etc.) para registrar tus gastos en tiempo real.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                            context.startActivity(intent)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Text("Abrir Ajustes de Permisos de Android")
                    }
                }
            }
        }

        // 4. Simulador de Notificación de Pago (Pruebas en Vivo)
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.FlashOn, contentDescription = null, tint = AccentOrange)
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(text = "Simulador de Notificación Bancaria", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Prueba el motor de parsing Regex y la auto-asignación de categorías simulando un mensaje de notificación de tu banco:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = testNotificationText,
                        onValueChange = { testNotificationText = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Texto simulado de notificación") },
                        singleLine = false,
                        maxLines = 3
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            scope.launch {
                                val success = PaymentNotificationListenerService.processPaymentMessage(testNotificationText, context)
                                if (success) {
                                    Toast.makeText(context, "¡Transacción auto-detectada y guardada con éxito!", Toast.LENGTH_LONG).show()
                                } else {
                                    Toast.makeText(context, "No se pudo extraer importe o comercio del texto.", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentOrange)
                    ) {
                        Text("Simular Notificación de Pago")
                    }
                }
            }
        }

        // 5. Gestor de Reglas de Comercios (Auto-Categorización)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Reglas de Comercios (${merchantRules.size})",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Button(
                    onClick = { showAddRuleDialog = true },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.size(4.dp))
                    Text("Añadir", fontSize = 12.sp)
                }
            }
        }

        items(merchantRules, key = { it.id }) { rule ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = rule.keyword, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text(
                            text = "Categoría: ${categoriesMap[rule.targetCategoryId]?.name ?: "Alimentación"}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Cuenta: ${accountsMap[rule.targetAccountId]?.name ?: "Principal"}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { onDeleteMerchantRule(rule) }) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Eliminar", tint = AccentRed)
                    }
                }
            }
        }

        // 6. Copias de Seguridad (Exportar / Importar)
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Copias de Seguridad y Datos",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Tus datos financieros son 100% privados y se almacenan únicamente en tu dispositivo. Puedes exportarlos en formato JSON para guardarlos o compartirlos.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                scope.launch {
                                    val json = backupManager.exportToJson()
                                    val sendIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, json)
                                        type = "application/json"
                                    }
                                    context.startActivity(Intent.createChooser(sendIntent, "Exportar Backup JSON"))
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentGreen)
                        ) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.size(4.dp))
                            Text("Exportar", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }

    // Diálogo de Confirmación: Borrar todas las transacciones
    if (showClearTransactionsDialog) {
        AlertDialog(
            onDismissRequest = { showClearTransactionsDialog = false },
            title = { Text("¿Borrar todas las transacciones?") },
            text = {
                Text("Esta acción eliminará todas las transacciones registradas. Los saldos de las cuentas volverán a su saldo inicial limpio.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showClearTransactionsDialog = false
                        onDeleteAllTransactions()
                        Toast.makeText(context, "Transacciones eliminadas correctamente", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentRed)
                ) {
                    Text("Borrar todo")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearTransactionsDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Diálogo de Confirmación: Restablecer aplicación de fábrica
    if (showResetFactoryDialog) {
        AlertDialog(
            onDismissRequest = { showResetFactoryDialog = false },
            title = { Text("¿Restablecer de fábrica?") },
            text = {
                Text("Se eliminarán todas tus transacciones, cuentas personalizadas y reglas. La aplicación volverá a su estado inicial de instalación.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showResetFactoryDialog = false
                        onResetDatabaseToFactory()
                        Toast.makeText(context, "Aplicación restablecida de fábrica", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentRed)
                ) {
                    Text("Restablecer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetFactoryDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Diálogo para añadir nueva regla de comercio
    if (showAddRuleDialog) {
        AddMerchantRuleDialog(
            accounts = accounts,
            categories = categories,
            onDismiss = { showAddRuleDialog = false },
            onSave = { rule ->
                onAddMerchantRule(rule)
                showAddRuleDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddMerchantRuleDialog(
    accounts: List<AccountEntity>,
    categories: List<CategoryEntity>,
    onDismiss: () -> Unit,
    onSave: (MerchantRuleEntity) -> Unit
) {
    var keyword by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf(categories.firstOrNull()?.id ?: 1L) }
    var selectedAccountId by remember { mutableStateOf(accounts.firstOrNull()?.id ?: 1L) }

    var categoryExpanded by remember { mutableStateOf(false) }
    var accountExpanded by remember { mutableStateOf(false) }

    val categoriesMap = remember(categories) { categories.associateBy { it.id } }
    val accountsMap = remember(accounts) { accounts.associateBy { it.id } }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva Regla de Comercio") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = keyword,
                    onValueChange = { keyword = it },
                    label = { Text("Palabra clave (ej. Mercadona, Zara)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Dropdown Categoría
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = it }
                ) {
                    OutlinedTextField(
                        value = categoriesMap[selectedCategoryId]?.name ?: "Seleccionar",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Asignar Categoría") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.name) },
                                onClick = {
                                    selectedCategoryId = cat.id
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }

                // Dropdown Cuenta
                ExposedDropdownMenuBox(
                    expanded = accountExpanded,
                    onExpandedChange = { accountExpanded = it }
                ) {
                    OutlinedTextField(
                        value = accountsMap[selectedAccountId]?.name ?: "Seleccionar",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Asignar a Cuenta") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = accountExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = accountExpanded,
                        onDismissRequest = { accountExpanded = false }
                    ) {
                        accounts.forEach { acc ->
                            DropdownMenuItem(
                                text = { Text(acc.name) },
                                onClick = {
                                    selectedAccountId = acc.id
                                    accountExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (keyword.isNotBlank()) {
                        val newRule = MerchantRuleEntity(
                            keyword = keyword.trim(),
                            targetCategoryId = selectedCategoryId,
                            targetAccountId = selectedAccountId
                        )
                        onSave(newRule)
                    }
                }
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
