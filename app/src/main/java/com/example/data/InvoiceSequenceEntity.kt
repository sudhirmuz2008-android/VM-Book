package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "invoice_sequences")
data class InvoiceSequenceEntity(
    @PrimaryKey val type: String, // "SALE" or "PURCHASE"
    val lastVal: Int
)
