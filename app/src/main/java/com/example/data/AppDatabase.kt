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
        InvoiceSequenceEntity::class,
        FirmEntity::class
    ],
    version = 8,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun invoiceDao(): InvoiceDao
    abstract fun customerDao(): CustomerDao
    abstract fun supplierDao(): SupplierDao
    abstract fun productDao(): ProductDao
    abstract fun firmDao(): FirmDao

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

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 1. Create firms table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `firms` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                        `name` TEXT NOT NULL, 
                        `phone` TEXT NOT NULL DEFAULT '', 
                        `email` TEXT NOT NULL DEFAULT '', 
                        `address` TEXT NOT NULL DEFAULT '', 
                        `gstin` TEXT NOT NULL DEFAULT '', 
                        `logoUri` TEXT NOT NULL DEFAULT ''
                    )
                """.trimIndent())

                // 2. Add firmId column to existing tables
                db.execSQL("ALTER TABLE `customers` ADD COLUMN `firmId` INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE `suppliers` ADD COLUMN `firmId` INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE `product_categories` ADD COLUMN `firmId` INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE `product_items` ADD COLUMN `firmId` INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE `invoices` ADD COLUMN `firmId` INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE `customer_payments` ADD COLUMN `firmId` INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE `supplier_payments` ADD COLUMN `firmId` INTEGER NOT NULL DEFAULT 1")

                // 3. Recreate invoice_sequences for composite primary key (firmId, type)
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `invoice_sequences_new` (
                        `firmId` INTEGER NOT NULL DEFAULT 1, 
                        `type` TEXT NOT NULL, 
                        `lastVal` INTEGER NOT NULL, 
                        PRIMARY KEY(`firmId`, `type`)
                    )
                """.trimIndent())
                // Copy existing sequence records
                db.execSQL("INSERT INTO `invoice_sequences_new` (`firmId`, `type`, `lastVal`) SELECT 1, `type`, `lastVal` FROM `invoice_sequences`")
                // Drop old table and rename new one
                db.execSQL("DROP TABLE `invoice_sequences`")
                db.execSQL("ALTER TABLE `invoice_sequences_new` RENAME TO `invoice_sequences`")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "billing_database"
                )
                .addMigrations(MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8)
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
