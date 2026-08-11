package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "product_categories")
data class ProductCategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val firmId: Long = 1L
)
