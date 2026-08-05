package com.example.data

import kotlinx.coroutines.flow.Flow

class InvoiceRepository(
    private val invoiceDao: InvoiceDao,
    private val customerDao: CustomerDao,
    private val supplierDao: SupplierDao,
    private val productDao: ProductDao
) {
    val allInvoices: Flow<List<InvoiceWithItems>> = invoiceDao.getAllInvoices()
    val distinctItemNames: Flow<List<String>> = invoiceDao.getDistinctItemNames()
    val distinctPartyNames: Flow<List<String>> = invoiceDao.getDistinctPartyNames()

    // Product Category operations
    val allCategories: Flow<List<ProductCategoryEntity>> = productDao.getAllCategoriesFlow()
    suspend fun getAllCategories(): List<ProductCategoryEntity> = productDao.getAllCategories()
    suspend fun insertCategory(category: ProductCategoryEntity) = productDao.insertCategory(category)
    suspend fun updateCategory(category: ProductCategoryEntity) = productDao.updateCategory(category)
    suspend fun deleteCategory(category: ProductCategoryEntity) = productDao.deleteCategory(category)

    // Product Item operations
    val allProductItems: Flow<List<ProductItemEntity>> = productDao.getAllItemsFlow()
    suspend fun getAllProductItems(): List<ProductItemEntity> = productDao.getAllItems()
    suspend fun insertProductItem(item: ProductItemEntity) = productDao.insertItem(item)
    suspend fun updateProductItem(item: ProductItemEntity) = productDao.updateItem(item)
    suspend fun deleteProductItem(item: ProductItemEntity) = productDao.deleteItem(item)

    // Customer operations
    val allCustomers: Flow<List<CustomerEntity>> = customerDao.getAllCustomers()
    fun getCustomerById(id: Long): Flow<CustomerEntity?> = customerDao.getCustomerById(id)
    suspend fun insertCustomer(customer: CustomerEntity): Long = customerDao.insertCustomer(customer)
    suspend fun deleteCustomerById(id: Long) = customerDao.deleteCustomerById(id)

    val allCustomerPayments: Flow<List<CustomerPaymentEntity>> = customerDao.getAllCustomerPayments()
    fun getPaymentsForCustomer(customerId: Long): Flow<List<CustomerPaymentEntity>> = customerDao.getPaymentsForCustomer(customerId)
    suspend fun insertCustomerPayment(payment: CustomerPaymentEntity): Long = customerDao.insertCustomerPayment(payment)
    suspend fun deleteCustomerPaymentById(id: Long) = customerDao.deleteCustomerPaymentById(id)

    // Supplier operations
    val allSuppliers: Flow<List<SupplierEntity>> = supplierDao.getAllSuppliers()
    fun getSupplierById(id: Long): Flow<SupplierEntity?> = supplierDao.getSupplierById(id)
    suspend fun insertSupplier(supplier: SupplierEntity): Long = supplierDao.insertSupplier(supplier)
    suspend fun deleteSupplierById(id: Long) = supplierDao.deleteSupplierById(id)

    val allSupplierPayments: Flow<List<SupplierPaymentEntity>> = supplierDao.getAllSupplierPayments()
    fun getPaymentsForSupplier(supplierId: Long): Flow<List<SupplierPaymentEntity>> = supplierDao.getPaymentsForSupplier(supplierId)
    suspend fun insertSupplierPayment(payment: SupplierPaymentEntity): Long = supplierDao.insertSupplierPayment(payment)
    suspend fun deleteSupplierPaymentById(id: Long) = supplierDao.deleteSupplierPaymentById(id)

    fun getInvoiceById(id: Long): Flow<InvoiceWithItems?> = invoiceDao.getInvoiceById(id)

    suspend fun insertInvoiceWithItems(invoice: InvoiceEntity, items: List<InvoiceItemEntity>): Long {
        val isNew = invoice.id == 0L
        var finalInvoice = invoice
        if (isNew && invoice.invoiceNumber.isBlank()) {
            val prefix = if (invoice.type == "SALE") "INV-" else "PUR-"
            val existingNumbers = invoiceDao.getInvoiceNumbersByType(invoice.type)
            var maxInTable = 0
            for (num in existingNumbers) {
                if (num.startsWith(prefix, ignoreCase = true)) {
                    val suffixStr = num.substring(prefix.length)
                    val parsed = suffixStr.toIntOrNull()
                    if (parsed != null && parsed > maxInTable) {
                        maxInTable = parsed
                    }
                }
            }
            val storedLastVal = invoiceDao.getLastSequenceValue(invoice.type) ?: 0
            val nextVal = maxOf(maxInTable, storedLastVal) + 1
            val generatedNo = "$prefix${String.format(java.util.Locale.US, "%04d", nextVal)}"
            finalInvoice = invoice.copy(invoiceNumber = generatedNo)
        }

        val invoiceId = invoiceDao.insertInvoice(finalInvoice)

        val prefix = if (finalInvoice.type == "SALE") "INV-" else "PUR-"
        if (finalInvoice.invoiceNumber.startsWith(prefix, ignoreCase = true)) {
            val suffixStr = finalInvoice.invoiceNumber.substring(prefix.length)
            val parsed = suffixStr.toIntOrNull()
            if (parsed != null) {
                val currentStored = invoiceDao.getLastSequenceValue(finalInvoice.type) ?: 0
                if (parsed > currentStored) {
                    invoiceDao.insertSequenceValue(InvoiceSequenceEntity(finalInvoice.type, parsed))
                }
            }
        }

        val itemsWithId = items.map { it.copy(invoiceId = invoiceId) }
        invoiceDao.insertInvoiceItems(itemsWithId)
        return invoiceId
    }

    suspend fun deleteInvoice(id: Long) {
        invoiceDao.deleteInvoiceById(id)
    }

    suspend fun updateOutstandingAmount(invoiceId: Long, outstandingAmount: Double) {
        invoiceDao.updateOutstandingAmount(invoiceId, outstandingAmount)
    }

    suspend fun getMaxInvoiceId(): Long? = invoiceDao.getMaxInvoiceId()
    suspend fun getInvoiceSequenceValue(): Long? = invoiceDao.getInvoiceSequenceValue()
    suspend fun updateInvoiceNumber(invoiceId: Long, invoiceNumber: String) = invoiceDao.updateInvoiceNumber(invoiceId, invoiceNumber)

    suspend fun getLastSequenceValue(type: String): Int? = invoiceDao.getLastSequenceValue(type)
    suspend fun insertSequenceValue(seq: InvoiceSequenceEntity) = invoiceDao.insertSequenceValue(seq)
    suspend fun getInvoiceNumbersByType(type: String): List<String> = invoiceDao.getInvoiceNumbersByType(type)

    // Sync operations
    suspend fun getAllCustomersList(): List<CustomerEntity> = customerDao.getAllCustomersList()
    suspend fun getAllCustomerPaymentsList(): List<CustomerPaymentEntity> = customerDao.getAllCustomerPaymentsList()
    suspend fun getAllSuppliersList(): List<SupplierEntity> = supplierDao.getAllSuppliersList()
    suspend fun getAllSupplierPaymentsList(): List<SupplierPaymentEntity> = supplierDao.getAllSupplierPaymentsList()
    suspend fun getAllInvoicesList(): List<InvoiceEntity> = invoiceDao.getAllInvoicesList()
    suspend fun getAllInvoiceItemsList(): List<InvoiceItemEntity> = invoiceDao.getAllInvoiceItemsList()
    suspend fun getAllInvoiceSequencesList(): List<InvoiceSequenceEntity> = invoiceDao.getAllInvoiceSequencesList()

    suspend fun insertCustomers(customers: List<CustomerEntity>) = customerDao.insertCustomers(customers)
    suspend fun insertCustomerPayments(payments: List<CustomerPaymentEntity>) = customerDao.insertCustomerPayments(payments)
    suspend fun insertSuppliers(suppliers: List<SupplierEntity>) = supplierDao.insertSuppliers(suppliers)
    suspend fun insertSupplierPayments(payments: List<SupplierPaymentEntity>) = supplierDao.insertSupplierPayments(payments)
    suspend fun insertCategories(categories: List<ProductCategoryEntity>) = productDao.insertCategories(categories)
    suspend fun insertProductItems(items: List<ProductItemEntity>) = productDao.insertItems(items)
    suspend fun insertInvoices(invoices: List<InvoiceEntity>) = invoiceDao.insertInvoices(invoices)
    suspend fun insertInvoiceItemsDirect(items: List<InvoiceItemEntity>) = invoiceDao.insertInvoiceItems(items)
    suspend fun insertInvoiceSequences(sequences: List<InvoiceSequenceEntity>) = invoiceDao.insertInvoiceSequences(sequences)
}
