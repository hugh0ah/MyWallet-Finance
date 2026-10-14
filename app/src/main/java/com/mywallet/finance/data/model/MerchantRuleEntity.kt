package com.mywallet.finance.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Regla de categorización automática para notificaciones de pago basada en Cashew AssociatedTitles.
 */
@Entity(tableName = "merchant_rules")
data class MerchantRuleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val keyword: String,           // Ej: "Mercadona", "Repsol", "Amazon", "Uber"
    val targetCategoryId: Long,    // ID de la categoría a la que se asignará
    val targetAccountId: Long      // ID de la cuenta en la que se cargará
)
