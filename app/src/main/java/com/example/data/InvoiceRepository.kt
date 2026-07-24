package com.example.data

import kotlinx.coroutines.flow.Flow

class InvoiceRepository(
    private val invoiceDao: InvoiceDao,
    private val customerDao: CustomerDao,
    private val supplierDao: SupplierDao
) {
    val allInvoices: Flow<List<InvoiceWithItems>> = invoiceDao.getAllInvoices()
    val distinctItemNames: Flow<List<String>> = invoiceDao.getDistinctItemNames()
    val distinctPartyNames: Flow<List<String>> = invoiceDao.getDistinctPartyNames()

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

    suspend fun insertInvoiceWithItems(invoice: InvoiceEntity, items: List<InvoiceItemEntity>) {
        val invoiceId = invoiceDao.insertInvoice(invoice)
        val itemsWithId = items.map { it.copy(invoiceId = invoiceId) }
        invoiceDao.insertInvoiceItems(itemsWithId)
    }

    suspend fun deleteInvoice(id: Long) {
        invoiceDao.deleteInvoiceById(id)
    }

    suspend fun updateOutstandingAmount(invoiceId: Long, outstandingAmount: Double) {
        invoiceDao.updateOutstandingAmount(invoiceId, outstandingAmount)
    }
}
