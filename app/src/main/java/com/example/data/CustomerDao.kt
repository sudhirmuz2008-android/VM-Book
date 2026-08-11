package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {
    @Query("SELECT * FROM customers WHERE firmId = :firmId ORDER BY name ASC")
    fun getAllCustomers(firmId: Long): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE id = :id")
    fun getCustomerById(id: Long): Flow<CustomerEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: CustomerEntity): Long

    @Query("SELECT * FROM customers WHERE firmId = :firmId")
    suspend fun getAllCustomersList(firmId: Long): List<CustomerEntity>

    @Query("SELECT * FROM customers")
    suspend fun getAllCustomersListUnfiltered(): List<CustomerEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomers(customers: List<CustomerEntity>)

    @Query("DELETE FROM customers WHERE id = :id")
    suspend fun deleteCustomerById(id: Long)

    @Query("SELECT * FROM customer_payments WHERE firmId = :firmId ORDER BY date DESC")
    fun getAllCustomerPayments(firmId: Long): Flow<List<CustomerPaymentEntity>>

    @Query("SELECT * FROM customer_payments WHERE firmId = :firmId")
    suspend fun getAllCustomerPaymentsList(firmId: Long): List<CustomerPaymentEntity>

    @Query("SELECT * FROM customer_payments")
    suspend fun getAllCustomerPaymentsListUnfiltered(): List<CustomerPaymentEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomerPayments(payments: List<CustomerPaymentEntity>)

    @Query("SELECT * FROM customer_payments WHERE customerId = :customerId ORDER BY date DESC")
    fun getPaymentsForCustomer(customerId: Long): Flow<List<CustomerPaymentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomerPayment(payment: CustomerPaymentEntity): Long

    @Query("DELETE FROM customer_payments WHERE id = :id")
    suspend fun deleteCustomerPaymentById(id: Long)
}
