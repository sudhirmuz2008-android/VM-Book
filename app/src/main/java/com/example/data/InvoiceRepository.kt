package com.example.data

import kotlinx.coroutines.flow.Flow

class InvoiceRepository(
    private val invoiceDao: InvoiceDao,
    private val customerDao: CustomerDao,
    private val supplierDao: SupplierDao,
    private val productDao: ProductDao,
    private val firmDao: FirmDao
) {
    fun getAllInvoices(firmId: Long): Flow<List<InvoiceWithItems>> = invoiceDao.getAllInvoices(firmId)
    fun getDistinctItemNames(firmId: Long): Flow<List<String>> = invoiceDao.getDistinctItemNames(firmId)
    fun getDistinctPartyNames(firmId: Long): Flow<List<String>> = invoiceDao.getDistinctPartyNames(firmId)

    // Product Category operations
    fun getAllCategoriesFlow(firmId: Long): Flow<List<ProductCategoryEntity>> = productDao.getAllCategoriesFlow(firmId)
    suspend fun getAllCategories(firmId: Long): List<ProductCategoryEntity> = productDao.getAllCategories(firmId)
    suspend fun getAllCategoriesUnfiltered(): List<ProductCategoryEntity> = productDao.getAllCategoriesUnfiltered()
    suspend fun insertCategory(category: ProductCategoryEntity) = productDao.insertCategory(category)
    suspend fun updateCategory(category: ProductCategoryEntity) = productDao.updateCategory(category)
    suspend fun deleteCategory(category: ProductCategoryEntity) = productDao.deleteCategory(category)

    // Product Item operations
    fun getAllProductItemsFlow(firmId: Long): Flow<List<ProductItemEntity>> = productDao.getAllItemsFlow(firmId)
    suspend fun getAllProductItems(firmId: Long): List<ProductItemEntity> = productDao.getAllItems(firmId)
    suspend fun getAllProductItemsUnfiltered(): List<ProductItemEntity> = productDao.getAllItemsUnfiltered()
    suspend fun insertProductItem(item: ProductItemEntity) = productDao.insertItem(item)
    suspend fun updateProductItem(item: ProductItemEntity) = productDao.updateItem(item)
    suspend fun deleteProductItem(item: ProductItemEntity) = productDao.deleteItem(item)

    // Customer operations
    fun getAllCustomers(firmId: Long): Flow<List<CustomerEntity>> = customerDao.getAllCustomers(firmId)
    fun getCustomerById(id: Long): Flow<CustomerEntity?> = customerDao.getCustomerById(id)
    suspend fun insertCustomer(customer: CustomerEntity): Long = customerDao.insertCustomer(customer)
    suspend fun deleteCustomerById(id: Long) = customerDao.deleteCustomerById(id)

    fun getAllCustomerPayments(firmId: Long): Flow<List<CustomerPaymentEntity>> = customerDao.getAllCustomerPayments(firmId)
    fun getPaymentsForCustomer(customerId: Long): Flow<List<CustomerPaymentEntity>> = customerDao.getPaymentsForCustomer(customerId)
    suspend fun insertCustomerPayment(payment: CustomerPaymentEntity): Long = customerDao.insertCustomerPayment(payment)
    suspend fun deleteCustomerPaymentById(id: Long) = customerDao.deleteCustomerPaymentById(id)

    // Supplier operations
    fun getAllSuppliers(firmId: Long): Flow<List<SupplierEntity>> = supplierDao.getAllSuppliers(firmId)
    fun getSupplierById(id: Long): Flow<SupplierEntity?> = supplierDao.getSupplierById(id)
    suspend fun insertSupplier(supplier: SupplierEntity): Long = supplierDao.insertSupplier(supplier)
    suspend fun deleteSupplierById(id: Long) = supplierDao.deleteSupplierById(id)

    fun getAllSupplierPayments(firmId: Long): Flow<List<SupplierPaymentEntity>> = supplierDao.getAllSupplierPayments(firmId)
    fun getPaymentsForSupplier(supplierId: Long): Flow<List<SupplierPaymentEntity>> = supplierDao.getPaymentsForSupplier(supplierId)
    suspend fun insertSupplierPayment(payment: SupplierPaymentEntity): Long = supplierDao.insertSupplierPayment(payment)
    suspend fun deleteSupplierPaymentById(id: Long) = supplierDao.deleteSupplierPaymentById(id)

    // Firm operations
    val allFirms: Flow<List<FirmEntity>> = firmDao.getAllFirms()
    suspend fun getFirmById(id: Long): FirmEntity? = firmDao.getFirmById(id)
    suspend fun insertFirm(firm: FirmEntity): Long = firmDao.insertFirm(firm)
    suspend fun deleteFirm(firm: FirmEntity) = firmDao.deleteFirm(firm)
    suspend fun getAllFirmsList(): List<FirmEntity> = firmDao.getAllFirmsList()
    suspend fun insertFirms(firms: List<FirmEntity>) = firmDao.insertFirms(firms)

    fun getInvoiceById(id: Long): Flow<InvoiceWithItems?> = invoiceDao.getInvoiceById(id)

    suspend fun insertInvoiceWithItems(invoice: InvoiceEntity, items: List<InvoiceItemEntity>): Long {
        val isNew = invoice.id == 0L
        var finalInvoice = invoice
        val currentFirmId = invoice.firmId
        if (isNew && invoice.invoiceNumber.isBlank()) {
            val prefix = if (invoice.type == "SALE") "INV-" else "PUR-"
            val existingNumbers = invoiceDao.getInvoiceNumbersByType(currentFirmId, invoice.type)
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
            val storedLastVal = invoiceDao.getLastSequenceValue(currentFirmId, invoice.type) ?: 0
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
                val currentStored = invoiceDao.getLastSequenceValue(currentFirmId, finalInvoice.type) ?: 0
                if (parsed > currentStored) {
                    invoiceDao.insertSequenceValue(InvoiceSequenceEntity(currentFirmId, finalInvoice.type, parsed))
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

    suspend fun getLastSequenceValue(firmId: Long, type: String): Int? = invoiceDao.getLastSequenceValue(firmId, type)
    suspend fun insertSequenceValue(seq: InvoiceSequenceEntity) = invoiceDao.insertSequenceValue(seq)
    suspend fun getInvoiceNumbersByType(firmId: Long, type: String): List<String> = invoiceDao.getInvoiceNumbersByType(firmId, type)

    // Sync operations
    suspend fun getAllCustomersList(firmId: Long): List<CustomerEntity> = customerDao.getAllCustomersList(firmId)
    suspend fun getAllCustomersListUnfiltered(): List<CustomerEntity> = customerDao.getAllCustomersListUnfiltered()
    suspend fun getAllCustomerPaymentsList(firmId: Long): List<CustomerPaymentEntity> = customerDao.getAllCustomerPaymentsList(firmId)
    suspend fun getAllCustomerPaymentsListUnfiltered(): List<CustomerPaymentEntity> = customerDao.getAllCustomerPaymentsListUnfiltered()
    suspend fun getAllSuppliersList(firmId: Long): List<SupplierEntity> = supplierDao.getAllSuppliersList(firmId)
    suspend fun getAllSuppliersListUnfiltered(): List<SupplierEntity> = supplierDao.getAllSuppliersListUnfiltered()
    suspend fun getAllSupplierPaymentsList(firmId: Long): List<SupplierPaymentEntity> = supplierDao.getAllSupplierPaymentsList(firmId)
    suspend fun getAllSupplierPaymentsListUnfiltered(): List<SupplierPaymentEntity> = supplierDao.getAllSupplierPaymentsListUnfiltered()
    suspend fun getAllInvoicesList(firmId: Long): List<InvoiceEntity> = invoiceDao.getAllInvoicesList(firmId)
    suspend fun getAllInvoicesListUnfiltered(): List<InvoiceEntity> = invoiceDao.getAllInvoicesListUnfiltered()
    suspend fun getAllInvoiceItemsList(): List<InvoiceItemEntity> = invoiceDao.getAllInvoiceItemsList()
    suspend fun getAllInvoiceSequencesList(firmId: Long): List<InvoiceSequenceEntity> = invoiceDao.getAllInvoiceSequencesList(firmId)
    suspend fun getAllInvoiceSequencesListUnfiltered(): List<InvoiceSequenceEntity> = invoiceDao.getAllInvoiceSequencesListUnfiltered()

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
