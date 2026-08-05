package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "product_items")
data class ProductItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val categoryName: String,
    val name: String,
    val hsnCode: String? = null,
    val defaultSellingRate: Double? = null,
    val defaultDiscountValue: Double? = null,
    val defaultDiscountType: String? = null
)
