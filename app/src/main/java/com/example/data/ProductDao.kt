package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    // Categories
    @Query("SELECT * FROM product_categories WHERE firmId = :firmId ORDER BY name ASC")
    fun getAllCategoriesFlow(firmId: Long): Flow<List<ProductCategoryEntity>>

    @Query("SELECT * FROM product_categories WHERE firmId = :firmId ORDER BY name ASC")
    suspend fun getAllCategories(firmId: Long): List<ProductCategoryEntity>

    @Query("SELECT * FROM product_categories")
    suspend fun getAllCategoriesUnfiltered(): List<ProductCategoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: ProductCategoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<ProductCategoryEntity>)

    @Update
    suspend fun updateCategory(category: ProductCategoryEntity)

    @Delete
    suspend fun deleteCategory(category: ProductCategoryEntity)

    // Items
    @Query("SELECT * FROM product_items WHERE firmId = :firmId ORDER BY name ASC")
    fun getAllItemsFlow(firmId: Long): Flow<List<ProductItemEntity>>

    @Query("SELECT * FROM product_items WHERE firmId = :firmId ORDER BY name ASC")
    suspend fun getAllItems(firmId: Long): List<ProductItemEntity>

    @Query("SELECT * FROM product_items")
    suspend fun getAllItemsUnfiltered(): List<ProductItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ProductItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<ProductItemEntity>)

    @Update
    suspend fun updateItem(item: ProductItemEntity)

    @Delete
    suspend fun deleteItem(item: ProductItemEntity)
}
