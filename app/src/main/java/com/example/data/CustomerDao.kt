package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {
    @Query("SELECT * FROM customers ORDER BY name ASC")
    fun getAllCustomers(): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE id = :id")
    fun getCustomerById(id: Long): Flow<CustomerEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: CustomerEntity): Long

    @Query("DELETE FROM customers WHERE id = :id")
    suspend fun deleteCustomerById(id: Long)

    @Query("SELECT * FROM customer_payments ORDER BY date DESC")
    fun getAllCustomerPayments(): Flow<List<CustomerPaymentEntity>>

    @Query("SELECT * FROM customer_payments WHERE customerId = :customerId ORDER BY date DESC")
    fun getPaymentsForCustomer(customerId: Long): Flow<List<CustomerPaymentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomerPayment(payment: CustomerPaymentEntity): Long

    @Query("DELETE FROM customer_payments WHERE id = :id")
    suspend fun deleteCustomerPaymentById(id: Long)
}
