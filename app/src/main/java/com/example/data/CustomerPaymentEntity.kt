package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "customer_payments")
data class CustomerPaymentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val customerId: Long,
    val amount: Double,
    val date: Long = System.currentTimeMillis(),
    val paymentMode: String = "Cash",
    val referenceNo: String = "",
    val notes: String = "",
    val firmId: Long = 1L
)
