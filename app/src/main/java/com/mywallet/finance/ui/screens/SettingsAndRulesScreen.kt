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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Notifications
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

        // 2. Permiso del Sistema para Notificaciones
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Notifications, contentDescription = null, tint = PrimaryBlue)
                        Spacer(modifier = Modifier.padding(4.dp))
                        Text(text = "Acceso a Notificaciones de Pago", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Permite a MyWallet escuchar notificaciones de Google Wallet y bancos compatibles para registrar tus gastos al instante en segundo plano sin pulsar nada.",
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

        // 3. Simulador de Notificación de Pago (Pruebas en Vivo)
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.FlashOn, contentDescription = null, tint = AccentOrange)
                        Spacer(modifier = Modifier.padding(4.dp))
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

        // 4. Gestor de Reglas de Comercios (Auto-Categorización)
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
                    Spacer(modifier = Modifier.padding(2.dp))
                    Text("Añadir", fontSize = 12.sp)
                }
            }
        }

        items(merchantRules) { rule ->
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
                    Column {
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
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.Gray)
                    }
                }
            }
        }

        // 5. Copias de Seguridad (Exportar / Importar)
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
                    Text(text = "Exportación e Importación Offline-First", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tus datos nunca salen de tu dispositivo salvo que decidas exportar una copia en JSON o CSV.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                scope.launch {
                                    val json = backupManager.exportToJson()
                                    shareText(context, json, "backup_mywallet.json", "application/json")
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentGreen)
                        ) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = null)
                            Spacer(modifier = Modifier.padding(2.dp))
                            Text("Exportar JSON", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    val csv = backupManager.exportTransactionsToCsv()
                                    shareText(context, csv, "transacciones_mywallet.csv", "text/csv")
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.Download, contentDescription = null)
                            Spacer(modifier = Modifier.padding(2.dp))
                            Text("Exportar CSV", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }

    // Diálogo para añadir regla de comercio
    if (showAddRuleDialog) {
        var keywordInput by remember { mutableStateOf("") }
        var selectedCatId by remember { mutableStateOf(categories.firstOrNull()?.id ?: 1L) }
        var selectedAccId by remember { mutableStateOf(accounts.firstOrNull()?.id ?: 1L) }

        AlertDialog(
            onDismissRequest = { showAddRuleDialog = false },
            title = { Text("Nueva Regla de Comercio") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = keywordInput,
                        onValueChange = { keywordInput = it },
                        label = { Text("Palabra clave (ej. Mercadona, Repsol)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(text = "Asignar a Categoría:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    // Selector simple de categorías
                    for (cat in categories.take(4)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                        ) {
                            androidx.compose.material3.RadioButton(
                                selected = selectedCatId == cat.id,
                                onClick = { selectedCatId = cat.id }
                            )
                            Text(text = cat.name, fontSize = 13.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (keywordInput.isNotBlank()) {
                            onAddMerchantRule(
                                MerchantRuleEntity(
                                    keyword = keywordInput.trim(),
                                    targetCategoryId = selectedCatId,
                                    targetAccountId = selectedAccId
                                )
                            )
                            showAddRuleDialog = false
                        }
                    }
                ) {
                    Text("Guardar Regla")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddRuleDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

private fun shareText(context: Context, content: String, title: String, mimeType: String) {
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, content)
        type = mimeType
    }
    val shareIntent = Intent.createChooser(sendIntent, title)
    context.startActivity(shareIntent)
}
