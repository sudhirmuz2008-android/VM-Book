package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "supplier_payments")
data class SupplierPaymentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val supplierId: Long,
    val amount: Double,
    val date: Long = System.currentTimeMillis(),
    val paymentMode: String = "Cash",
    val referenceNo: String = "",
    val notes: String = ""
)
