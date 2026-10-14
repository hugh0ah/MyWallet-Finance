package com.mywallet.finance.service

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.mywallet.finance.data.AppDatabase
import com.mywallet.finance.data.model.TransactionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.regex.Pattern

/**
 * Servicio de escucha de notificaciones de pago (Google Wallet, Santander, BBVA, CaixaBank, Revolut, etc.)
 * Procesa en tiempo real el texto, extrae importe y comercio, y auto-registra la transacción.
 */
class PaymentNotificationListenerService : NotificationListenerService() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return

        val packageName = sbn.packageName
        if (!isPaymentApp(packageName)) return

        val extras = sbn.notification.extras ?: return
        val title = extras.getString(Notification.EXTRA_TITLE) ?: ""
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
        val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString() ?: ""
        val fullMessage = "$title $text $bigText".trim()

        Log.d(TAG, "Notificación detectada de [$packageName]: $fullMessage")

        serviceScope.launch {
            processPaymentMessage(fullMessage, applicationContext)
        }
    }

    companion object {
        private const val TAG = "PaymentNotificationListener"

        // Lista de paquetes bancarios y billeteras compatibles
        private val TARGET_PACKAGES = setOf(
            "com.google.android.apps.walletnfcrel",      // Google Wallet
            "com.bbva.bbvacontigo",                      // BBVA España
            "es.bancosantander.apps",                    // Banco Santander
            "com.lacaixa.mobile.android.newExterior",    // CaixaBank
            "com.bancsabadell.bsmobile",                 // Banco Sabadell
            "com.bankinter.launcher",                    // Bankinter
            "es.ing.mobile.app",                         // ING Direct
            "com.revolut.revolut",                       // Revolut
            "de.number26.android",                       // N26
            "es.openbank.mobile",                        // Openbank
            "com.imaginbank.app"                         // Imagin
        )

        fun isPaymentApp(packageName: String): Boolean {
            return TARGET_PACKAGES.contains(packageName)
        }

        /**
         * Extrae el importe y el nombre del comercio mediante expresiones regulares
         * y almacena la transacción en la base de datos aplicando reglas de categorización.
         */
        suspend fun processPaymentMessage(
            message: String,
            context: android.content.Context
        ): Boolean {
            val amount = parseAmount(message) ?: return false
            val merchant = parseMerchant(message) ?: "Comercio no identificado"

            val database = AppDatabase.getInstance(context)

            // Buscar regla de comercio coincidente
            val matchedRule = database.merchantRuleDao().findMatchingRule(merchant)
            val categoryId = matchedRule?.targetCategoryId ?: 1L // Por defecto Alimentación (ID 1)
            val accountId = matchedRule?.targetAccountId ?: 1L   // Por defecto Cuenta Principal (ID 1)

            val transaction = TransactionEntity(
                accountId = accountId,
                categoryId = categoryId,
                amount = amount,
                timestamp = System.currentTimeMillis(),
                note = "Pago en $merchant (Auto-detectado)",
                transactionType = TransactionEntity.TYPE_EXPENSE,
                isAutoDetected = true
            )

            val id = database.transactionDao().insertTransaction(transaction)
            Log.i(TAG, "Transacción auto-registrada con ID #$id: $amount € en $merchant")
            return true
        }

        /**
         * Detecta importes en euros: ej. "24,50 €", "12.30€", "15 EUR", "45,00 euros"
         */
        fun parseAmount(text: String): Double? {
            // Regex que busca números seguidos o precedidos de símbolo monetario
            val pattern = Pattern.compile(
                """(?:[€$]\s*(\d+[.,]\d{1,2})|(\d+[.,]\d{1,2})\s*(?:€|EUR|eur|euros?))"""
            )
            val matcher = pattern.matcher(text)
            if (matcher.find()) {
                val rawValue = matcher.group(1) ?: matcher.group(2) ?: return null
                val normalized = rawValue.replace(",", ".")
                return normalized.toDoubleOrNull()
            }
            return null
        }

        /**
         * Detecta el comercio o beneficiario a partir de patrones habituales en notificaciones bancarias.
         */
        fun parseMerchant(text: String): String? {
            val patterns = listOf(
                Pattern.compile("""(?:en|en el comercio|a favor de)\s+([A-Za-z0-9\sÁÉÍÓÚáéíóúÑñ\.\-]+?)(?:\s+con tarjeta|\s+por|\s+el|\s+a las|\.|\,|$)""", Pattern.CASE_INSENSITIVE),
                Pattern.compile("""Pago en\s+([A-Za-z0-9\sÁÉÍÓÚáéíóúÑñ\.\-]+?)(?:\s+por|\.|\,|$)""", Pattern.CASE_INSENSITIVE),
                Pattern.compile("""Compra en\s+([A-Za-z0-9\sÁÉÍÓÚáéíóúÑñ\.\-]+?)(?:\s+por|\.|\,|$)""", Pattern.CASE_INSENSITIVE)
            )

            for (p in patterns) {
                val matcher = p.matcher(text)
                if (matcher.find()) {
                    val candidate = matcher.group(1)?.trim()
                    if (!candidate.isNullOrBlank() && candidate.length > 2) {
                        return candidate
                    }
                }
            }
            return null
        }
    }
}
