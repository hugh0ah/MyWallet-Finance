package com.mywallet.finance.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad de Categoría con soporte para subcategorías jerárquicas (parentId).
 */
@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val iconName: String = "category",
    val colorHex: String = "#FF7043",
    val isExpense: Boolean = true,
    val parentCategoryId: Long? = null
)
