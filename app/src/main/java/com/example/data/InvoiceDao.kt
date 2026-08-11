package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface InvoiceDao {
    @Transaction
    @Query("SELECT * FROM invoices WHERE firmId = :firmId ORDER BY date DESC")
    fun getAllInvoices(firmId: Long): Flow<List<InvoiceWithItems>>

    @Transaction
    @Query("SELECT * FROM invoices WHERE id = :id")
    fun getInvoiceById(id: Long): Flow<InvoiceWithItems?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoice(invoice: InvoiceEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoiceItems(items: List<InvoiceItemEntity>)

    @Query("DELETE FROM invoices WHERE id = :id")
    suspend fun deleteInvoiceById(id: Long)

    @Query("UPDATE invoices SET outstandingAmount = :outstandingAmount WHERE id = :invoiceId")
    suspend fun updateOutstandingAmount(invoiceId: Long, outstandingAmount: Double)

    @Query("SELECT DISTINCT ii.name FROM invoice_items ii INNER JOIN invoices i ON ii.invoiceId = i.id WHERE i.firmId = :firmId AND ii.name != '' ORDER BY ii.name ASC")
    fun getDistinctItemNames(firmId: Long): Flow<List<String>>

    @Query("SELECT DISTINCT partyName FROM invoices WHERE firmId = :firmId AND partyName != '' ORDER BY partyName ASC")
    fun getDistinctPartyNames(firmId: Long): Flow<List<String>>

    @Query("SELECT MAX(id) FROM invoices")
    suspend fun getMaxInvoiceId(): Long?

    @Query("SELECT seq FROM sqlite_sequence WHERE name = 'invoices'")
    suspend fun getInvoiceSequenceValue(): Long?

    @Query("UPDATE invoices SET invoiceNumber = :invoiceNumber WHERE id = :invoiceId")
    suspend fun updateInvoiceNumber(invoiceId: Long, invoiceNumber: String)

    @Query("SELECT lastVal FROM invoice_sequences WHERE firmId = :firmId AND type = :type")
    suspend fun getLastSequenceValue(firmId: Long, type: String): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSequenceValue(seq: InvoiceSequenceEntity)

    @Query("SELECT invoiceNumber FROM invoices WHERE type = :type AND firmId = :firmId")
    suspend fun getInvoiceNumbersByType(firmId: Long, type: String): List<String>

    @Query("SELECT * FROM invoices WHERE firmId = :firmId")
    suspend fun getAllInvoicesList(firmId: Long): List<InvoiceEntity>

    @Query("SELECT * FROM invoices")
    suspend fun getAllInvoicesListUnfiltered(): List<InvoiceEntity>

    @Query("SELECT * FROM invoice_items")
    suspend fun getAllInvoiceItemsList(): List<InvoiceItemEntity>

    @Query("SELECT * FROM invoice_sequences WHERE firmId = :firmId")
    suspend fun getAllInvoiceSequencesList(firmId: Long): List<InvoiceSequenceEntity>

    @Query("SELECT * FROM invoice_sequences")
    suspend fun getAllInvoiceSequencesListUnfiltered(): List<InvoiceSequenceEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoices(invoices: List<InvoiceEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoiceSequences(sequences: List<InvoiceSequenceEntity>)
}
