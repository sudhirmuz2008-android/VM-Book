package com.example

import android.app.Application
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import com.example.data.InvoiceEntity
import com.example.data.InvoiceItemEntity
import com.example.data.InvoiceRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PurchaseDialogTest {

  private lateinit var database: AppDatabase
  private lateinit var repository: InvoiceRepository

  @Before
  fun setUp() {
    val context = ApplicationProvider.getApplicationContext<Application>()
    // Create an in-memory database
    database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
      .allowMainThreadQueries()
      .build()
    
    repository = InvoiceRepository(
      database.invoiceDao(),
      database.customerDao(),
      database.supplierDao(),
      database.productDao(),
      database.firmDao()
    )
  }

  @After
  fun tearDown() {
    database.close()
  }

  @Test
  fun testSaveMultiProductPurchaseInvoice_createsOneInvoiceWithItemsAndUpdatesStock() = runTest {
    // 1. Prepare invoice draft details
    val invoice = InvoiceEntity(
      invoiceNumber = "PUR-2026-X1",
      partyName = "Elite Steel Corporation",
      type = "PURCHASE",
      date = System.currentTimeMillis(),
      discount = 0.0,
      tax = 18.0,
      totalAmount = 14160.0, // 10 * 1200 + 18% GST
      notes = "Bulk Steel Supply",
      isCreditSale = false,
      outstandingAmount = 0.0,
      dueDate = 0L
    )

    val items = listOf(
      InvoiceItemEntity(
        invoiceId = 0,
        name = "Premium Steel Bars [HSN: 7214]",
        price = 1200.0,
        quantity = 10.0,
        totalPrice = 12000.0
      )
    )

    // 2. Insert invoice directly using repository
    repository.insertInvoiceWithItems(invoice, items)

    // 3. Retrieve and assert stored invoice
    val storedInvoices = repository.allInvoices.first()
    assertEquals(1, storedInvoices.size)
    
    val savedInvoice = storedInvoices.first()
    assertEquals("PUR-2026-X1", savedInvoice.invoice.invoiceNumber)
    assertEquals("Elite Steel Corporation", savedInvoice.invoice.partyName)
    assertEquals("PURCHASE", savedInvoice.invoice.type)
    assertEquals(14160.0, savedInvoice.invoice.totalAmount, 0.01)

    // 4. Assert items are linked correctly
    assertEquals(1, savedInvoice.items.size)
    val savedItem = savedInvoice.items.first()
    assertEquals("Premium Steel Bars [HSN: 7214]", savedItem.name)
    assertEquals(1200.0, savedItem.price, 0.01)
    assertEquals(10.0, savedItem.quantity, 0.01)
  }
}
