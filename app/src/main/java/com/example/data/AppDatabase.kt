package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        InvoiceEntity::class, 
        InvoiceItemEntity::class,
        CustomerEntity::class,
        SupplierEntity::class,
        CustomerPaymentEntity::class,
        SupplierPaymentEntity::class,
        ProductCategoryEntity::class,
        ProductItemEntity::class,
        InvoiceSequenceEntity::class
    ],
    version = 7,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun invoiceDao(): InvoiceDao
    abstract fun customerDao(): CustomerDao
    abstract fun supplierDao(): SupplierDao
    abstract fun productDao(): ProductDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE invoice_items ADD COLUMN discount REAL NOT NULL DEFAULT 0.0")
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE product_items ADD COLUMN defaultSellingRate REAL")
                db.execSQL("ALTER TABLE product_items ADD COLUMN defaultDiscountValue REAL")
                db.execSQL("ALTER TABLE product_items ADD COLUMN defaultDiscountType TEXT")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "billing_database"
                )
                .addMigrations(MIGRATION_5_6, MIGRATION_6_7)
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }

        fun closeDatabase() {
            synchronized(this) {
                INSTANCE?.let { db ->
                    if (db.isOpen) {
                        db.close()
                    }
                    INSTANCE = null
                }
            }
        }
    }
}
