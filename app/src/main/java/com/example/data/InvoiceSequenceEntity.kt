package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "invoice_sequences", primaryKeys = ["firmId", "type"])
data class InvoiceSequenceEntity(
    val firmId: Long = 1L,
    val type: String, // "SALE" or "PURCHASE"
    val lastVal: Int
)
