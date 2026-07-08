package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "invoices")
data class InvoiceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val invoiceNumber: String,
    val partyName: String,
    val type: String, // "SALE" or "PURCHASE"
    val date: Long = System.currentTimeMillis(),
    val discount: Double = 0.0, // flat discount amount
    val tax: Double = 0.0, // percentage (e.g., 18.0)
    val totalAmount: Double = 0.0,
    val notes: String = ""
)
