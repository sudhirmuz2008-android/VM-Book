package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Delete
import kotlinx.coroutines.flow.Flow

@Dao
interface FirmDao {
    @Query("SELECT * FROM firms ORDER BY name ASC")
    fun getAllFirms(): Flow<List<FirmEntity>>

    @Query("SELECT * FROM firms WHERE id = :id")
    suspend fun getFirmById(id: Long): FirmEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFirm(firm: FirmEntity): Long

    @Delete
    suspend fun deleteFirm(firm: FirmEntity)

    @Query("SELECT * FROM firms")
    suspend fun getAllFirmsList(): List<FirmEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFirms(firms: List<FirmEntity>)
}
