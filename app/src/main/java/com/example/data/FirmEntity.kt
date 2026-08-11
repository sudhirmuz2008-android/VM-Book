package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "firms")
data class FirmEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String = "",
    val email: String = "",
    val address: String = "",
    val gstin: String = "",
    val logoUri: String = ""
)
