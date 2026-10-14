package com.mywallet.finance.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mywallet.finance.data.model.MerchantRuleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MerchantRuleDao {
    @Query("SELECT * FROM merchant_rules ORDER BY keyword ASC")
    fun getAllRulesFlow(): Flow<List<MerchantRuleEntity>>

    @Query("SELECT * FROM merchant_rules")
    suspend fun getAllRules(): List<MerchantRuleEntity>

    @Query("SELECT * FROM merchant_rules WHERE :merchantName LIKE '%' || keyword || '%' LIMIT 1")
    suspend fun findMatchingRule(merchantName: String): MerchantRuleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRule(rule: MerchantRuleEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(rules: List<MerchantRuleEntity>): List<Long>

    @Update
    suspend fun updateRule(rule: MerchantRuleEntity)

    @Delete
    suspend fun deleteRule(rule: MerchantRuleEntity)

    @Query("SELECT COUNT(*) FROM merchant_rules")
    suspend fun countRules(): Int
}
