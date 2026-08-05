package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SupplierDao {
    @Query("SELECT * FROM suppliers ORDER BY name ASC")
    fun getAllSuppliers(): Flow<List<SupplierEntity>>

    @Query("SELECT * FROM suppliers WHERE id = :id")
    fun getSupplierById(id: Long): Flow<SupplierEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupplier(supplier: SupplierEntity): Long

    @Query("SELECT * FROM suppliers")
    suspend fun getAllSuppliersList(): List<SupplierEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSuppliers(suppliers: List<SupplierEntity>)

    @Query("DELETE FROM suppliers WHERE id = :id")
    suspend fun deleteSupplierById(id: Long)

    @Query("SELECT * FROM supplier_payments ORDER BY date DESC")
    fun getAllSupplierPayments(): Flow<List<SupplierPaymentEntity>>

    @Query("SELECT * FROM supplier_payments")
    suspend fun getAllSupplierPaymentsList(): List<SupplierPaymentEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupplierPayments(payments: List<SupplierPaymentEntity>)

    @Query("SELECT * FROM supplier_payments WHERE supplierId = :supplierId ORDER BY date DESC")
    fun getPaymentsForSupplier(supplierId: Long): Flow<List<SupplierPaymentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupplierPayment(payment: SupplierPaymentEntity): Long

    @Query("DELETE FROM supplier_payments WHERE id = :id")
    suspend fun deleteSupplierPaymentById(id: Long)
}
