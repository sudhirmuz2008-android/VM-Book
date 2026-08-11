package com.example.ui

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.InvoiceEntity
import com.example.data.InvoiceItemEntity
import com.example.data.InvoiceRepository
import com.example.data.InvoiceWithItems
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class BillingScreen {
    SPLASH,
    LOCK_SCREEN,
    DASHBOARD,
    PROFIT_LOSS,
    REPORTS,
    ADD_INVOICE,
    INVOICE_DETAIL,
    CUSTOMERS,
    SUPPLIERS,
    PARTIES,
    CUSTOMER_LEDGER,
    SUPPLIER_LEDGER,
    BUSINESS_PROFILE,
    CREDIT_REMINDERS,
    MORE,
    PRODUCTS,
    SETTINGS,
    HELP_SUPPORT,
    ABOUT_VM_BOOK,
    BACKUP_RESTORE,
    EXPORT_DATA
}

data class BusinessProfile(
    val firmName: String,
    val mobileNumber: String,
    val address: String,
    val gstin: String = "",
    val email: String = "",
    val businessLogoUri: String = ""
)

data class ProductSummary(
    val name: String,
    val totalQtyPurchased: Double,
    val totalQtySold: Double,
    val avgPurchasePrice: Double,
    val avgSalePrice: Double,
    val stockBalance: Double,
    val stockValue: Double
)

data class CustomerWithBalance(
    val customer: com.example.data.CustomerEntity,
    val totalSales: Double,
    val totalPayments: Double,
    val outstandingBalance: Double
)

data class SupplierWithBalance(
    val supplier: com.example.data.SupplierEntity,
    val totalPurchases: Double,
    val totalPayments: Double,
    val outstandingBalance: Double
)

data class CustomerLedgerEntry(
    val id: Long,
    val type: String, // "SALE" or "PAYMENT"
    val date: Long,
    val reference: String,
    val debit: Double,
    val credit: Double,
    val runningBalance: Double,
    val notes: String = ""
)

data class SupplierLedgerEntry(
    val id: Long,
    val type: String, // "PURCHASE" or "PAYMENT"
    val date: Long,
    val reference: String,
    val debit: Double,
    val credit: Double,
    val runningBalance: Double,
    val notes: String = ""
)

data class ProfitLossLedgerEntry(
    val invoiceWithItems: InvoiceWithItems,
    val saleAmount: Double?,
    val purchaseAmount: Double?,
    val rowProfitLoss: Double,
    val runningBalance: Double
)

data class StockItem(
    val name: String,
    val quantityPurchased: Double,
    val quantitySold: Double,
    val currentStock: Double,
    val averagePurchasePrice: Double,
    val currentStockValue: Double
)

data class InvoiceItemDraft(
    val categoryName: String = "",
    val name: String = "",
    val price: String = "",
    val quantity: String = "1",
    val hsnCode: String = "",
    val discount: String = ""
)

@OptIn(ExperimentalCoroutinesApi::class)
class BillingViewModel(
    private val repository: InvoiceRepository,
    private val application: android.app.Application
) : ViewModel() {

    private val sharedPrefs = application.getSharedPreferences("business_profile_prefs", android.content.Context.MODE_PRIVATE)

    val currentFirmId = MutableStateFlow<Long>(1L)
    val allFirms: StateFlow<List<com.example.data.FirmEntity>> = repository.allFirms
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCustomersFlow: Flow<List<com.example.data.CustomerEntity>> = currentFirmId
        .flatMapLatest { firmId -> repository.getAllCustomers(firmId) }
    val allCustomerPaymentsFlow: Flow<List<com.example.data.CustomerPaymentEntity>> = currentFirmId
        .flatMapLatest { firmId -> repository.getAllCustomerPayments(firmId) }
    val allSuppliersFlow: Flow<List<com.example.data.SupplierEntity>> = currentFirmId
        .flatMapLatest { firmId -> repository.getAllSuppliers(firmId) }
    val allSupplierPaymentsFlow: Flow<List<com.example.data.SupplierPaymentEntity>> = currentFirmId
        .flatMapLatest { firmId -> repository.getAllSupplierPayments(firmId) }

    val updateJsonUrl = MutableStateFlow(sharedPrefs.getString("update_json_url", "https://raw.githubusercontent.com/sudhir-muz/vmbook/main/update.json") ?: "https://raw.githubusercontent.com/sudhir-muz/vmbook/main/update.json")
    val updateStatus = MutableStateFlow<UpdateStatus>(UpdateStatus.Idle)

    val businessProfile = MutableStateFlow<BusinessProfile?>(null)

    // Tally-style Category & Item Master flows
    val productCategories: StateFlow<List<com.example.data.ProductCategoryEntity>> = currentFirmId
        .flatMapLatest { firmId -> repository.getAllCategoriesFlow(firmId) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val productItems: StateFlow<List<com.example.data.ProductItemEntity>> = currentFirmId
        .flatMapLatest { firmId -> repository.getAllProductItemsFlow(firmId) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // CRUD operations for Categories
    fun addCategory(name: String) {
        viewModelScope.launch {
            repository.insertCategory(com.example.data.ProductCategoryEntity(name = name, firmId = currentFirmId.value))
        }
    }

    fun editCategory(category: com.example.data.ProductCategoryEntity, newName: String) {
        viewModelScope.launch {
            repository.updateCategory(category.copy(name = newName))
        }
    }

    fun deleteCategory(category: com.example.data.ProductCategoryEntity) {
        viewModelScope.launch {
            repository.deleteCategory(category)
        }
    }

    // CRUD operations for Product Items
    fun addProductItem(
        categoryName: String,
        name: String,
        hsnCode: String?,
        defaultSellingRate: Double? = null,
        defaultDiscountValue: Double? = null,
        defaultDiscountType: String? = null
    ) {
        viewModelScope.launch {
            repository.insertProductItem(
                com.example.data.ProductItemEntity(
                    categoryName = categoryName,
                    name = name,
                    hsnCode = hsnCode,
                    defaultSellingRate = defaultSellingRate,
                    defaultDiscountValue = defaultDiscountValue,
                    defaultDiscountType = defaultDiscountType,
                    firmId = currentFirmId.value
                )
            )
        }
    }

    fun editProductItem(
        item: com.example.data.ProductItemEntity,
        categoryName: String,
        name: String,
        hsnCode: String?,
        defaultSellingRate: Double?,
        defaultDiscountValue: Double?,
        defaultDiscountType: String?
    ) {
        viewModelScope.launch {
            repository.updateProductItem(
                item.copy(
                    categoryName = categoryName,
                    name = name,
                    hsnCode = hsnCode,
                    defaultSellingRate = defaultSellingRate,
                    defaultDiscountValue = defaultDiscountValue,
                    defaultDiscountType = defaultDiscountType
                )
            )
        }
    }

    fun deleteProductItem(item: com.example.data.ProductItemEntity) {
        viewModelScope.launch {
            repository.deleteProductItem(item)
        }
    }

    init {
        viewModelScope.launch {
            // 1. Seed/ensure default firm exists
            val allFirmsList = repository.getAllFirmsList()
            if (allFirmsList.isEmpty()) {
                val legacyName = sharedPrefs.getString("firm_name", "")?.takeIf { it.isNotEmpty() } ?: "Vishwakarma Motor"
                val legacyPhone = sharedPrefs.getString("mobile_number", "") ?: ""
                val legacyAddress = sharedPrefs.getString("address", "") ?: ""
                val legacyGstin = sharedPrefs.getString("gstin", "") ?: ""
                val legacyEmail = sharedPrefs.getString("email", "") ?: ""
                val legacyLogoUri = sharedPrefs.getString("business_logo_uri", "") ?: ""
                
                val defaultFirm = com.example.data.FirmEntity(
                    id = 1L,
                    name = legacyName,
                    phone = legacyPhone,
                    address = legacyAddress,
                    gstin = legacyGstin,
                    email = legacyEmail,
                    logoUri = legacyLogoUri
                )
                repository.insertFirm(defaultFirm)
            }
            
            // 2. Load stored active firm ID
            val storedFirmId = sharedPrefs.getLong("current_firm_id", 1L)
            val activeId = if (repository.getFirmById(storedFirmId) != null) storedFirmId else 1L
            selectFirm(activeId)
            
            // 3. Seed default categories if needed
            seedDefaultCategories()
        }
        checkForUpdates(manual = false)
        initNetworkAutoSync()
    }

    private fun seedDefaultCategories() {
        viewModelScope.launch {
            val existing = repository.getAllCategories(currentFirmId.value)
            if (existing.isEmpty()) {
                val defaults = listOf("Battery", "Lubricant", "Tyre", "Oil", "Spare Parts", "Accessories")
                defaults.forEach {
                    repository.insertCategory(com.example.data.ProductCategoryEntity(name = it, firmId = currentFirmId.value))
                }
            }
        }
    }

    fun selectFirm(firmId: Long) {
        viewModelScope.launch {
            currentFirmId.value = firmId
            sharedPrefs.edit().putLong("current_firm_id", firmId).apply()
            
            // Load the firm's profile and update businessProfile
            val firm = repository.getFirmById(firmId)
            if (firm != null) {
                val profile = BusinessProfile(
                    firmName = firm.name,
                    mobileNumber = firm.phone,
                    address = firm.address,
                    gstin = firm.gstin,
                    email = firm.email,
                    businessLogoUri = firm.logoUri
                )
                businessProfile.value = profile
                
                // Keep SharedPreferences in sync for any legacy components
                sharedPrefs.edit().apply {
                    putString("firm_name", firm.name)
                    putString("mobile_number", firm.phone)
                    putString("address", firm.address)
                    putString("gstin", firm.gstin)
                    putString("email", firm.email)
                    putString("business_logo_uri", firm.logoUri)
                    apply()
                }
            } else {
                businessProfile.value = null
            }
        }
    }

    fun createNewFirm(
        name: String,
        phone: String = "",
        email: String = "",
        address: String = "",
        gstin: String = "",
        logoUri: String = ""
    ) {
        viewModelScope.launch {
            val newFirm = com.example.data.FirmEntity(
                name = name,
                phone = phone,
                email = email,
                address = address,
                gstin = gstin,
                logoUri = logoUri
            )
            val newId = repository.insertFirm(newFirm)
            selectFirm(newId)
        }
    }

    fun loadBusinessProfile() {
        viewModelScope.launch {
            val firm = repository.getFirmById(currentFirmId.value)
            if (firm != null) {
                businessProfile.value = BusinessProfile(
                    firmName = firm.name,
                    mobileNumber = firm.phone,
                    address = firm.address,
                    gstin = firm.gstin,
                    email = firm.email,
                    businessLogoUri = firm.logoUri
                )
            } else {
                val firmName = sharedPrefs.getString("firm_name", "") ?: ""
                if (firmName.isNotEmpty()) {
                    val mobileNumber = sharedPrefs.getString("mobile_number", "") ?: ""
                    val address = sharedPrefs.getString("address", "") ?: ""
                    val gstin = sharedPrefs.getString("gstin", "") ?: ""
                    val email = sharedPrefs.getString("email", "") ?: ""
                    val businessLogoUri = sharedPrefs.getString("business_logo_uri", "") ?: ""
                    businessProfile.value = BusinessProfile(
                        firmName = firmName,
                        mobileNumber = mobileNumber,
                        address = address,
                        gstin = gstin,
                        email = email,
                        businessLogoUri = businessLogoUri
                    )
                } else {
                    businessProfile.value = null
                }
            }
        }
    }

    fun saveBusinessProfile(profile: BusinessProfile) {
        viewModelScope.launch {
            sharedPrefs.edit().apply {
                putString("firm_name", profile.firmName)
                putString("mobile_number", profile.mobileNumber)
                putString("address", profile.address)
                putString("gstin", profile.gstin)
                putString("email", profile.email)
                putString("business_logo_uri", profile.businessLogoUri)
                apply()
            }
            businessProfile.value = profile
            
            val activeId = currentFirmId.value
            val existingFirm = repository.getFirmById(activeId)
            val updatedFirm = if (existingFirm != null) {
                existingFirm.copy(
                    name = profile.firmName,
                    phone = profile.mobileNumber,
                    address = profile.address,
                    gstin = profile.gstin,
                    email = profile.email,
                    logoUri = profile.businessLogoUri
                )
            } else {
                com.example.data.FirmEntity(
                    id = activeId,
                    name = profile.firmName,
                    phone = profile.mobileNumber,
                    address = profile.address,
                    gstin = profile.gstin,
                    email = profile.email,
                    logoUri = profile.businessLogoUri
                )
            }
            repository.insertFirm(updatedFirm)
        }
    }

    // Screen State
    val currentScreen = MutableStateFlow(BillingScreen.SPLASH)

    // Security & Privacy States
    val isPrivacyHidden = MutableStateFlow(sharedPrefs.getBoolean("privacy_mode_enabled", false))
    private var lastBackgroundTime: Long = 0

    fun togglePrivacyHidden() {
        isPrivacyHidden.value = !isPrivacyHidden.value
    }

    fun onAppBackgrounded() {
        if (isAppLockEnabled()) {
            lastBackgroundTime = System.currentTimeMillis()
        }
    }

    fun onAppResumed() {
        if (isAppLockEnabled()) {
            val now = System.currentTimeMillis()
            val elapsed = now - lastBackgroundTime
            val duration = getAutoLockDuration()
            val shouldLock = when (duration) {
                "immediate" -> true
                "1" -> elapsed >= 60 * 1000L
                "5" -> elapsed >= 5 * 60 * 1000L
                else -> true
            }
            if (shouldLock && currentScreen.value != BillingScreen.SPLASH && currentScreen.value != BillingScreen.LOCK_SCREEN) {
                currentScreen.value = BillingScreen.LOCK_SCREEN
            }
        }
    }

    // Filter and Search States
    val searchQuery = MutableStateFlow("")
    val typeFilter = MutableStateFlow("ALL") // "ALL", "SALE", "PURCHASE"

    // Raw invoices flow from repository
    val rawInvoices = currentFirmId
        .flatMapLatest { firmId -> repository.getAllInvoices(firmId) }
    val allInvoices: StateFlow<List<InvoiceWithItems>> = currentFirmId
        .flatMapLatest { firmId -> repository.getAllInvoices(firmId) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Product calculations and summaries
    val productSummaries: StateFlow<List<ProductSummary>> = allInvoices.map { list ->
        val summaries = mutableMapOf<String, Triple<Double, Double, Double>>() // name -> (qtyPurchased, qtySold, totalPurchaseVal)
        val salesVal = mutableMapOf<String, Double>() // name -> totalSalesVal
        
        list.forEach { invWithItems ->
            val type = invWithItems.invoice.type
            val invSubTotal = invWithItems.items.sumOf { it.totalPrice }
            val factor = if (invSubTotal > 0.0) invWithItems.invoice.totalAmount / invSubTotal else 1.0

            invWithItems.items.forEach { item ->
                val name = item.name.trim()
                if (name.isNotEmpty()) {
                    val current = summaries[name] ?: Triple(0.0, 0.0, 0.0)
                    val effectiveTotalPrice = item.totalPrice * factor
                    if (type == "PURCHASE") {
                        summaries[name] = Triple(
                            current.first + item.quantity,
                            current.second,
                            current.third + effectiveTotalPrice
                        )
                    } else {
                        summaries[name] = Triple(
                            current.first,
                            current.second + item.quantity,
                            current.third
                        )
                        salesVal[name] = (salesVal[name] ?: 0.0) + effectiveTotalPrice
                    }
                }
            }
        }
        
        summaries.map { (name, stats) ->
            val qtyPurchased = stats.first
            val qtySold = stats.second
            val totalPurchaseVal = stats.third
            val totalSalesVal = salesVal[name] ?: 0.0
            
            val avgPurchasePrice = if (qtyPurchased > 0) totalPurchaseVal / qtyPurchased else 0.0
            val avgSalePrice = if (qtySold > 0) totalSalesVal / qtySold else 0.0
            val stockBalance = qtyPurchased - qtySold
            val stockValue = stockBalance * avgPurchasePrice
            
            ProductSummary(
                name = name,
                totalQtyPurchased = qtyPurchased,
                totalQtySold = qtySold,
                avgPurchasePrice = avgPurchasePrice,
                avgSalePrice = avgSalePrice,
                stockBalance = stockBalance,
                stockValue = if (stockValue > 0) stockValue else 0.0
            )
        }.sortedBy { it.name }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Suggestions flows
    val distinctItemNames: StateFlow<List<String>> = currentFirmId
        .flatMapLatest { firmId -> repository.getDistinctItemNames(firmId) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val distinctPartyNames: StateFlow<List<String>> = currentFirmId
        .flatMapLatest { firmId -> repository.getDistinctPartyNames(firmId) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Separate Suggestions Flows
    val customerSuggestions: StateFlow<List<String>> = currentFirmId
        .flatMapLatest { firmId -> repository.getAllCustomers(firmId).map { list -> list.map { it.name } } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val supplierSuggestions: StateFlow<List<String>> = currentFirmId
        .flatMapLatest { firmId -> repository.getAllSuppliers(firmId).map { list -> list.map { it.name } } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Search and Selection States for Customers/Suppliers
    val customerSearchQuery = MutableStateFlow("")
    val supplierSearchQuery = MutableStateFlow("")
    val selectedCustomerId = MutableStateFlow<Long?>(null)
    val selectedSupplierId = MutableStateFlow<Long?>(null)

    // Customer / Supplier with balances
    val customersWithBalance: StateFlow<List<CustomerWithBalance>> = combine(
        allCustomersFlow,
        rawInvoices,
        allCustomerPaymentsFlow,
        customerSearchQuery
    ) { customerList, invoiceList, paymentList, query ->
        customerList.map { customer ->
            val customerSales = invoiceList
                .filter { it.invoice.type == "SALE" && it.invoice.partyName.trim().equals(customer.name.trim(), ignoreCase = true) }
                .sumOf { it.invoice.totalAmount }
            val customerPayments = paymentList
                .filter { it.customerId == customer.id }
                .sumOf { it.amount }
            CustomerWithBalance(
                customer = customer,
                totalSales = customerSales,
                totalPayments = customerPayments,
                outstandingBalance = customerSales - customerPayments
            )
        }.filter {
            query.isEmpty() || it.customer.name.contains(query, ignoreCase = true) || it.customer.phone.contains(query, ignoreCase = true)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val suppliersWithBalance: StateFlow<List<SupplierWithBalance>> = combine(
        allSuppliersFlow,
        rawInvoices,
        allSupplierPaymentsFlow,
        supplierSearchQuery
    ) { supplierList, invoiceList, paymentList, query ->
        supplierList.map { supplier ->
            val supplierPurchases = invoiceList
                .filter { it.invoice.type == "PURCHASE" && it.invoice.partyName.trim().equals(supplier.name.trim(), ignoreCase = true) }
                .sumOf { it.invoice.totalAmount }
            val supplierPayments = paymentList
                .filter { it.supplierId == supplier.id }
                .sumOf { it.amount }
            SupplierWithBalance(
                supplier = supplier,
                totalPurchases = supplierPurchases,
                totalPayments = supplierPayments,
                outstandingBalance = supplierPurchases - supplierPayments
            )
        }.filter {
            query.isEmpty() || it.supplier.name.contains(query, ignoreCase = true) || it.supplier.phone.contains(query, ignoreCase = true)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selected Customer Ledger & Payments
    val selectedCustomer: StateFlow<com.example.data.CustomerEntity?> = combine(
        selectedCustomerId,
        allCustomersFlow
    ) { id, customers ->
        customers.find { it.id == id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val selectedCustomerLedger: StateFlow<List<CustomerLedgerEntry>> = combine(
        selectedCustomer,
        rawInvoices,
        allCustomerPaymentsFlow
    ) { customer, invoices, payments ->
        if (customer == null) return@combine emptyList()
        val customerInvoices = invoices.filter {
            it.invoice.type == "SALE" && it.invoice.partyName.trim().equals(customer.name.trim(), ignoreCase = true)
        }
        val customerPayments = payments.filter { it.customerId == customer.id }

        val entries = mutableListOf<CustomerLedgerEntry>()
        customerInvoices.forEach { inv ->
            entries.add(
                CustomerLedgerEntry(
                    id = inv.invoice.id,
                    type = "SALE",
                    date = inv.invoice.date,
                    reference = inv.invoice.invoiceNumber,
                    debit = inv.invoice.totalAmount,
                    credit = 0.0,
                    runningBalance = 0.0,
                    notes = inv.invoice.notes
                )
            )
        }
        customerPayments.forEach { pm ->
            entries.add(
                CustomerLedgerEntry(
                    id = pm.id,
                    type = "PAYMENT",
                    date = pm.date,
                    reference = "Payment (${pm.paymentMode})" + if (pm.referenceNo.isNotEmpty()) " Ref: ${pm.referenceNo}" else "",
                    debit = 0.0,
                    credit = pm.amount,
                    runningBalance = 0.0,
                    notes = pm.notes
                )
            )
        }

        entries.sortBy { it.date }

        var running = 0.0
        entries.map { entry ->
            running += (entry.debit - entry.credit)
            entry.copy(runningBalance = running)
        }.reversed()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selected Supplier Ledger & Payments
    val selectedSupplier: StateFlow<com.example.data.SupplierEntity?> = combine(
        selectedSupplierId,
        allSuppliersFlow
    ) { id, suppliers ->
        suppliers.find { it.id == id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val selectedSupplierLedger: StateFlow<List<SupplierLedgerEntry>> = combine(
        selectedSupplier,
        rawInvoices,
        allSupplierPaymentsFlow
    ) { supplier, invoices, payments ->
        if (supplier == null) return@combine emptyList()
        val supplierInvoices = invoices.filter {
            it.invoice.type == "PURCHASE" && it.invoice.partyName.trim().equals(supplier.name.trim(), ignoreCase = true)
        }
        val supplierPayments = payments.filter { it.supplierId == supplier.id }

        val entries = mutableListOf<SupplierLedgerEntry>()
        supplierInvoices.forEach { inv ->
            entries.add(
                SupplierLedgerEntry(
                    id = inv.invoice.id,
                    type = "PURCHASE",
                    date = inv.invoice.date,
                    reference = inv.invoice.invoiceNumber,
                    debit = 0.0,
                    credit = inv.invoice.totalAmount,
                    runningBalance = 0.0,
                    notes = inv.invoice.notes
                )
            )
        }
        supplierPayments.forEach { pm ->
            entries.add(
                SupplierLedgerEntry(
                    id = pm.id,
                    type = "PAYMENT",
                    date = pm.date,
                    reference = "Payment (${pm.paymentMode})" + if (pm.referenceNo.isNotEmpty()) " Ref: ${pm.referenceNo}" else "",
                    debit = pm.amount,
                    credit = 0.0,
                    runningBalance = 0.0,
                    notes = pm.notes
                )
            )
        }

        entries.sortBy { it.date }

        var running = 0.0
        entries.map { entry ->
            running += (entry.credit - entry.debit)
            entry.copy(runningBalance = running)
        }.reversed()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Operations for Customer / Supplier
    fun addCustomer(customer: com.example.data.CustomerEntity) {
        viewModelScope.launch {
            repository.insertCustomer(customer.copy(firmId = currentFirmId.value))
        }
    }

    fun deleteCustomer(id: Long) {
        viewModelScope.launch {
            repository.deleteCustomerById(id)
        }
    }

    fun addCustomerPayment(payment: com.example.data.CustomerPaymentEntity) {
        viewModelScope.launch {
            repository.insertCustomerPayment(payment.copy(firmId = currentFirmId.value))
        }
    }

    fun addCustomerSale(customerId: Long, partyName: String, amount: Double, notes: String, date: Long) {
        viewModelScope.launch {
            val invoiceNo = getNextInvoiceNumber("SALE")
            val invoice = com.example.data.InvoiceEntity(
                invoiceNumber = invoiceNo,
                partyName = partyName,
                type = "SALE",
                date = date,
                discount = 0.0,
                tax = 0.0,
                totalAmount = amount,
                notes = notes,
                isCreditSale = true,
                outstandingAmount = amount,
                dueDate = 0L,
                firmId = currentFirmId.value
            )
            val items = listOf(
                com.example.data.InvoiceItemEntity(
                    invoiceId = 0,
                    name = if (notes.isNotEmpty()) notes else "Given / Credit Sale",
                    price = amount,
                    quantity = 1.0,
                    totalPrice = amount
                )
            )
            repository.insertInvoiceWithItems(invoice, items)
        }
    }

    fun addSupplierPurchase(supplierId: Long, partyName: String, amount: Double, notes: String, date: Long) {
        viewModelScope.launch {
            val invoiceNo = getNextInvoiceNumber("PURCHASE")
            val invoice = com.example.data.InvoiceEntity(
                invoiceNumber = invoiceNo,
                partyName = partyName,
                type = "PURCHASE",
                date = date,
                discount = 0.0,
                tax = 0.0,
                totalAmount = amount,
                notes = notes,
                isCreditSale = true,
                outstandingAmount = amount,
                dueDate = 0L,
                firmId = currentFirmId.value
            )
            val items = listOf(
                com.example.data.InvoiceItemEntity(
                    invoiceId = 0,
                    name = if (notes.isNotEmpty()) notes else "Purchase Entry / Supplier Invoice",
                    price = amount,
                    quantity = 1.0,
                    totalPrice = amount
                )
            )
            repository.insertInvoiceWithItems(invoice, items)
        }
    }

    fun deleteCustomerPayment(id: Long) {
        viewModelScope.launch {
            repository.deleteCustomerPaymentById(id)
        }
    }

    fun addSupplier(supplier: com.example.data.SupplierEntity) {
        viewModelScope.launch {
            repository.insertSupplier(supplier.copy(firmId = currentFirmId.value))
        }
    }

    fun deleteSupplier(id: Long) {
        viewModelScope.launch {
            repository.deleteSupplierById(id)
        }
    }

    fun addSupplierPayment(payment: com.example.data.SupplierPaymentEntity) {
        viewModelScope.launch {
            repository.insertSupplierPayment(payment.copy(firmId = currentFirmId.value))
        }
    }

    fun deleteSupplierPayment(id: Long) {
        viewModelScope.launch {
            repository.deleteSupplierPaymentById(id)
        }
    }

    // Filtered Invoices flow for display
    val invoices: StateFlow<List<InvoiceWithItems>> = combine(
        rawInvoices,
        searchQuery,
        typeFilter
    ) { list, query, filter ->
        list.filter { item ->
            // Apply type filter
            val matchesType = when (filter) {
                "SALE" -> item.invoice.type == "SALE"
                "PURCHASE" -> item.invoice.type == "PURCHASE"
                else -> true
            }

            // Apply search query (matches party name, invoice number, or item names)
            val matchesQuery = if (query.isEmpty()) true else {
                item.invoice.partyName.contains(query, ignoreCase = true) ||
                item.invoice.invoiceNumber.contains(query, ignoreCase = true) ||
                item.items.any { it.name.contains(query, ignoreCase = true) }
            }

            matchesType && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Financial calculations for Dashboard
    val dashboardStats: StateFlow<DashboardStats> = rawInvoices.combine(MutableStateFlow(0)) { list, _ ->
        var totalSales = 0.0
        var totalPurchases = 0.0
        var salesCount = 0
        var purchasesCount = 0

        val chronologicalList = list.sortedBy { it.invoice.date }

        // Compute average purchase price of each product name
        val productPurchaseQty = mutableMapOf<String, Double>()
        val productPurchaseVal = mutableMapOf<String, Double>()
        val productAvgPurchasePrice = mutableMapOf<String, Double>()

        chronologicalList.forEach { invoiceWithItems ->
            if (invoiceWithItems.invoice.type == "PURCHASE") {
                val invSubTotal = invoiceWithItems.items.sumOf { it.totalPrice }
                val factor = if (invSubTotal > 0.0) invoiceWithItems.invoice.totalAmount / invSubTotal else 1.0

                invoiceWithItems.items.forEach { item ->
                    val name = item.name.trim()
                    if (name.isNotEmpty()) {
                        val effectiveTotalPrice = item.totalPrice * factor
                        val qty = (productPurchaseQty[name] ?: 0.0) + item.quantity
                        val totalVal = (productPurchaseVal[name] ?: 0.0) + effectiveTotalPrice
                        productPurchaseQty[name] = qty
                        productPurchaseVal[name] = totalVal
                        productAvgPurchasePrice[name] = totalVal / qty
                    }
                }
            }
        }

        var totalRealizedProfit = 0.0
        var totalOutstandingCredit = 0.0
        var overdueCreditCount = 0
        var todaySales = 0.0
        var todayProfit = 0.0
        val now = System.currentTimeMillis()
        val todayCal = java.util.Calendar.getInstance()
        val itemCal = java.util.Calendar.getInstance()

        list.forEach { item ->
            if (item.invoice.type == "SALE") {
                totalSales += item.invoice.totalAmount
                salesCount++

                // Calculate profit for this sale
                val cogs = item.items.sumOf { it.quantity * (productAvgPurchasePrice[it.name.trim()] ?: 0.0) }
                val profit = item.invoice.totalAmount - cogs
                totalRealizedProfit += profit

                // Calculate today's sales and profit
                itemCal.timeInMillis = item.invoice.date
                if (itemCal.get(java.util.Calendar.YEAR) == todayCal.get(java.util.Calendar.YEAR) &&
                    itemCal.get(java.util.Calendar.DAY_OF_YEAR) == todayCal.get(java.util.Calendar.DAY_OF_YEAR)) {
                    todaySales += item.invoice.totalAmount
                    todayProfit += profit
                }

                if (item.invoice.isCreditSale && item.invoice.outstandingAmount > 0) {
                    totalOutstandingCredit += item.invoice.outstandingAmount
                    if (item.invoice.dueDate > 0L && item.invoice.dueDate < now) {
                        overdueCreditCount++
                    }
                }
            } else {
                totalPurchases += item.invoice.totalAmount
                purchasesCount++
            }
        }

        // Calculate remaining stock value using actual stock balance logic
        data class TempHistoryItem(val type: String, val quantity: Double, val effectiveTotalPrice: Double)
        
        val productGroups = mutableMapOf<String, MutableList<TempHistoryItem>>()
        list.forEach { invoiceWithItems ->
            val type = invoiceWithItems.invoice.type
            val invSubTotal = invoiceWithItems.items.sumOf { it.totalPrice }
            val factor = if (invSubTotal > 0.0) invoiceWithItems.invoice.totalAmount / invSubTotal else 1.0
            
            invoiceWithItems.items.forEach { item ->
                val normalizedName = item.name.trim()
                if (normalizedName.isNotEmpty()) {
                    val effectiveTotalPrice = item.totalPrice * factor
                    productGroups.getOrPut(normalizedName) { mutableListOf() }.add(
                        TempHistoryItem(type, item.quantity, effectiveTotalPrice)
                    )
                }
            }
        }

        var remainingStockValue = 0.0
        productGroups.forEach { (name, history) ->
            val purchases = history.filter { it.type == "PURCHASE" }
            val sales = history.filter { it.type == "SALE" }

            val qtyPurchased = purchases.sumOf { it.quantity }
            val totalPurchaseValue = purchases.sumOf { it.effectiveTotalPrice }
            val avgPurchasePrice = if (qtyPurchased > 0) totalPurchaseValue / qtyPurchased else {
                val qtySold = sales.sumOf { it.quantity }
                if (qtySold > 0) sales.sumOf { it.effectiveTotalPrice } / qtySold else 0.0
            }

            val qtySold = sales.sumOf { it.quantity }
            val currentStock = qtyPurchased - qtySold
            if (currentStock > 0) {
                remainingStockValue += currentStock * avgPurchasePrice
            }
        }

        DashboardStats(
            totalSales = totalSales,
            totalPurchases = totalPurchases,
            netBalance = totalRealizedProfit, // Set net balance to total profit for compatibility
            totalProfit = totalRealizedProfit,
            salesCount = salesCount,
            purchasesCount = purchasesCount,
            remainingStockValue = remainingStockValue,
            totalOutstandingCredit = totalOutstandingCredit,
            overdueCreditInvoicesCount = overdueCreditCount,
            todaySales = todaySales,
            todayProfit = todayProfit
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardStats())

    // Profit and Loss Ledger Flow
    val profitLossLedger: StateFlow<List<ProfitLossLedgerEntry>> = rawInvoices.combine(MutableStateFlow(0)) { list, _ ->
        val chronologicalList = list.sortedBy { it.invoice.date }

        // Compute average purchase price of each product name
        val productPurchaseQty = mutableMapOf<String, Double>()
        val productPurchaseVal = mutableMapOf<String, Double>()
        val productAvgPurchasePrice = mutableMapOf<String, Double>()

        chronologicalList.forEach { invoiceWithItems ->
            if (invoiceWithItems.invoice.type == "PURCHASE") {
                val invSubTotal = invoiceWithItems.items.sumOf { it.totalPrice }
                val factor = if (invSubTotal > 0.0) invoiceWithItems.invoice.totalAmount / invSubTotal else 1.0

                invoiceWithItems.items.forEach { item ->
                    val name = item.name.trim()
                    if (name.isNotEmpty()) {
                        val effectiveTotalPrice = item.totalPrice * factor
                        val qty = (productPurchaseQty[name] ?: 0.0) + item.quantity
                        val totalVal = (productPurchaseVal[name] ?: 0.0) + effectiveTotalPrice
                        productPurchaseQty[name] = qty
                        productPurchaseVal[name] = totalVal
                        productAvgPurchasePrice[name] = totalVal / qty
                    }
                }
            }
        }

        var runningProfitSum = 0.0
        val entries = chronologicalList.map { item ->
            val isSale = item.invoice.type == "SALE"
            val saleAmount = if (isSale) item.invoice.totalAmount else null
            val purchaseAmount = if (!isSale) item.invoice.totalAmount else null

            val rowProfitLoss = if (isSale) {
                val cogs = item.items.sumOf { it.quantity * (productAvgPurchasePrice[it.name.trim()] ?: 0.0) }
                item.invoice.totalAmount - cogs
            } else {
                0.0
            }

            runningProfitSum += rowProfitLoss

            ProfitLossLedgerEntry(
                invoiceWithItems = item,
                saleAmount = saleAmount,
                purchaseAmount = purchaseAmount,
                rowProfitLoss = rowProfitLoss,
                runningBalance = runningProfitSum
            )
        }
        entries.reversed()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Stock Balance Flow
    val stockBalances: StateFlow<List<StockItem>> = rawInvoices.combine(MutableStateFlow(0)) { list, _ ->
        data class TempHistoryItem(val type: String, val quantity: Double, val effectiveTotalPrice: Double)
        
        val productGroups = mutableMapOf<String, MutableList<TempHistoryItem>>()
        list.forEach { invoiceWithItems ->
            val type = invoiceWithItems.invoice.type
            val invSubTotal = invoiceWithItems.items.sumOf { it.totalPrice }
            val factor = if (invSubTotal > 0.0) invoiceWithItems.invoice.totalAmount / invSubTotal else 1.0
            
            invoiceWithItems.items.forEach { item ->
                val normalizedName = item.name.trim()
                if (normalizedName.isNotEmpty()) {
                    val effectiveTotalPrice = item.totalPrice * factor
                    productGroups.getOrPut(normalizedName) { mutableListOf() }.add(
                        TempHistoryItem(type, item.quantity, effectiveTotalPrice)
                    )
                }
            }
        }

        productGroups.map { (name, history) ->
            val purchases = history.filter { it.type == "PURCHASE" }
            val sales = history.filter { it.type == "SALE" }

            val qtyPurchased = purchases.sumOf { it.quantity }
            val totalPurchaseValue = purchases.sumOf { it.effectiveTotalPrice }
            val avgPurchasePrice = if (qtyPurchased > 0) totalPurchaseValue / qtyPurchased else {
                val qtySold = sales.sumOf { it.quantity }
                if (qtySold > 0) sales.sumOf { it.effectiveTotalPrice } / qtySold else 0.0
            }

            val qtySold = sales.sumOf { it.quantity }
            val currentStock = qtyPurchased - qtySold
            val currentStockValue = currentStock * avgPurchasePrice

            StockItem(
                name = name,
                quantityPurchased = qtyPurchased,
                quantitySold = qtySold,
                currentStock = currentStock,
                averagePurchasePrice = avgPurchasePrice,
                currentStockValue = currentStockValue
            )
        }.sortedBy { it.name }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Detail Screen state
    val selectedInvoice = MutableStateFlow<InvoiceWithItems?>(null)

    // Form inputs for Add Invoice Screen
    val formPartyName = mutableStateOf("")
    val formInvoiceNumber = mutableStateOf("")
    val formType = mutableStateOf("SALE") // "SALE" or "PURCHASE"
    val formDate = mutableStateOf(System.currentTimeMillis())
    val formDiscount = mutableStateOf("")
    val formTax = mutableStateOf("") // tax percentage e.g. "18"
    val formNotes = mutableStateOf("")
    val formItems = mutableStateListOf(InvoiceItemDraft())

    // Credit Sales fields
    val formIsCreditSale = mutableStateOf(false)
    val formOutstandingAmount = mutableStateOf("")
    val formDueDate = mutableStateOf(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000L)

    // Form validation message
    val formErrorMessage = mutableStateOf<String?>(null)

    private val screenStack = mutableListOf<BillingScreen>()

    fun setScreen(screen: BillingScreen) {
        val current = currentScreen.value
        val mainTabs = listOf(
            BillingScreen.DASHBOARD,
            BillingScreen.PARTIES,
            BillingScreen.REPORTS,
            BillingScreen.MORE
        )
        if (screen in mainTabs) {
            screenStack.clear()
        } else if (current != screen) {
            if (screenStack.isEmpty() || screenStack.last() != current) {
                screenStack.add(current)
            }
        }
        currentScreen.value = screen
    }

    fun goBack() {
        if (screenStack.isNotEmpty()) {
            val prev = screenStack.removeAt(screenStack.size - 1)
            currentScreen.value = prev
        } else {
            currentScreen.value = BillingScreen.DASHBOARD
        }
    }

    fun viewInvoiceDetail(invoice: InvoiceWithItems) {
        selectedInvoice.value = invoice
        setScreen(BillingScreen.INVOICE_DETAIL)
    }

    suspend fun getNextInvoiceNumber(type: String): String {
        val prefix = if (type == "SALE") "INV-" else "PUR-"
        val existingNumbers = repository.getInvoiceNumbersByType(currentFirmId.value, type)
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
        val storedLastVal = repository.getLastSequenceValue(currentFirmId.value, type) ?: 0
        val nextId = maxOf(maxInTable, storedLastVal) + 1
        return "$prefix${String.format(Locale.US, "%04d", nextId)}"
    }

    fun updateFormType(type: String) {
        formType.value = type
        viewModelScope.launch {
            formInvoiceNumber.value = if (type == "SALE") getNextInvoiceNumber(type) else ""
        }
    }

    fun prepareNewInvoiceForm() {
        formPartyName.value = ""
        formInvoiceNumber.value = ""
        formDate.value = System.currentTimeMillis()
        formDiscount.value = ""
        formTax.value = ""
        formNotes.value = ""
        formItems.clear()
        formItems.add(InvoiceItemDraft())
        formIsCreditSale.value = false
        formOutstandingAmount.value = ""
        formDueDate.value = System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000L
        formErrorMessage.value = null
        viewModelScope.launch {
            formInvoiceNumber.value = if (formType.value == "SALE") getNextInvoiceNumber(formType.value) else ""
        }
        setScreen(BillingScreen.ADD_INVOICE)
    }

    fun addDraftItem() {
        formItems.add(InvoiceItemDraft())
    }

    fun removeDraftItem(index: Int) {
        if (formItems.size > 1) {
            formItems.removeAt(index)
        } else {
            formItems[0] = InvoiceItemDraft()
        }
    }

    fun updateDraftItem(index: Int, updated: InvoiceItemDraft) {
        formItems[index] = updated
    }

    fun saveInvoice(): Boolean {
        val party = formPartyName.value.trim()
        val invNo = formInvoiceNumber.value.trim()
        val type = formType.value
        val discountVal = formDiscount.value.toDoubleOrNull() ?: 0.0
        val taxPercentage = formTax.value.toDoubleOrNull() ?: 0.0
        val notes = formNotes.value.trim()

        if (party.isEmpty()) {
            formErrorMessage.value = "Please enter Customer / Vendor name"
            return false
        }
        if (invNo.isEmpty()) {
            formErrorMessage.value = "Please enter Invoice/Receipt number"
            return false
        }

        // Validate future date (No future dated entries allowed)
        val todayCal = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        val inputCal = java.util.Calendar.getInstance().apply {
            timeInMillis = formDate.value
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        if (inputCal.after(todayCal)) {
            formErrorMessage.value = "Future-dated bills are not allowed! Please select today's date or a past date."
            return false
        }

        // Parse lines
        val finalItems = mutableListOf<InvoiceItemEntity>()
        var totalAmountBeforeDiscount = 0.0
        var totalDiscount = 0.0

        for (draft in formItems) {
            val itemName = draft.name.trim()
            if (itemName.isEmpty()) continue

            val priceVal = draft.price.toDoubleOrNull()
            if (priceVal == null || priceVal < 0) {
                formErrorMessage.value = "Invalid price for item '$itemName'"
                return false
            }

            val qtyVal = draft.quantity.toDoubleOrNull()
            if (qtyVal == null || qtyVal <= 0) {
                formErrorMessage.value = "Quantity must be greater than 0 for '$itemName'"
                return false
            }

            val itemDiscountVal = draft.discount.toDoubleOrNull() ?: 0.0
            if (itemDiscountVal < 0) {
                formErrorMessage.value = "Discount cannot be negative for '$itemName'"
                return false
            }

            val itemTotalBeforeDiscount = priceVal * qtyVal
            if (itemDiscountVal > itemTotalBeforeDiscount) {
                formErrorMessage.value = "Discount exceeds item amount for '$itemName'"
                return false
            }

            val taxableAmount = itemTotalBeforeDiscount - itemDiscountVal
            totalAmountBeforeDiscount += itemTotalBeforeDiscount
            totalDiscount += itemDiscountVal

            finalItems.add(
                InvoiceItemEntity(
                    invoiceId = 0, // set by repo during save
                    name = itemName,
                    price = priceVal,
                    quantity = qtyVal,
                    totalPrice = taxableAmount,
                    hsnCode = draft.hsnCode.ifBlank { null },
                    discount = itemDiscountVal
                )
            )
        }

        if (finalItems.isEmpty()) {
            formErrorMessage.value = "Please add at least one item line"
            return false
        }

        // Calculations
        val taxableAmountSum = totalAmountBeforeDiscount - totalDiscount
        val taxAmount = taxableAmountSum * (taxPercentage / 100.0)
        val grandTotal = taxableAmountSum + taxAmount

        if (grandTotal < 0) {
            formErrorMessage.value = "Discount exceeds total value of items"
            return false
        }

        val isCredit = type == "SALE" && formIsCreditSale.value
        val outstandingAmount = if (isCredit) {
            formOutstandingAmount.value.toDoubleOrNull() ?: grandTotal
        } else {
            0.0
        }
        val dueDateVal = if (isCredit) formDueDate.value else 0L

        val invoiceEntity = InvoiceEntity(
            invoiceNumber = invNo,
            partyName = party,
            type = type,
            date = formDate.value,
            discount = totalDiscount,
            tax = taxPercentage,
            totalAmount = grandTotal,
            notes = notes,
            isCreditSale = isCredit,
            outstandingAmount = outstandingAmount,
            dueDate = dueDateVal,
            firmId = currentFirmId.value
        )

        viewModelScope.launch {
            repository.insertInvoiceWithItems(invoiceEntity, finalItems)
        }

        formErrorMessage.value = null
        setScreen(BillingScreen.REPORTS)
        return true
    }

    fun deleteInvoice(id: Long) {
        viewModelScope.launch {
            repository.deleteInvoice(id)
            if (selectedInvoice.value?.invoice?.id == id) {
                selectedInvoice.value = null
                setScreen(BillingScreen.REPORTS)
            }
        }
    }

    fun updateInvoiceOutstandingAmount(invoiceId: Long, outstandingAmount: Double) {
        viewModelScope.launch {
            repository.updateOutstandingAmount(invoiceId, outstandingAmount)
        }
    }

    fun addCustomerPaymentWithInvoiceUpdate(
        partyName: String,
        paymentAmount: Double,
        invoiceId: Long,
        newOutstandingAmount: Double,
        date: Long,
        paymentMode: String,
        referenceNo: String,
        notes: String
    ) {
        viewModelScope.launch {
            // Find or create customer
            val existingCust = allCustomersFlow.first().find { 
                it.name.trim().equals(partyName.trim(), ignoreCase = true) 
            }
            val customerId = existingCust?.id ?: repository.insertCustomer(
                com.example.data.CustomerEntity(
                    name = partyName.trim(),
                    phone = "",
                    address = "",
                    firmId = currentFirmId.value
                )
            )
            
            // Insert customer payment
            repository.insertCustomerPayment(
                com.example.data.CustomerPaymentEntity(
                    customerId = customerId,
                    amount = paymentAmount,
                    date = date,
                    paymentMode = paymentMode,
                    referenceNo = referenceNo,
                    notes = notes,
                    firmId = currentFirmId.value
                )
            )
            
            // Update invoice outstanding amount
            repository.updateOutstandingAmount(invoiceId, newOutstandingAmount)
        }
    }

    fun saveDirectInvoice(invoice: com.example.data.InvoiceEntity, items: List<com.example.data.InvoiceItemEntity>) {
        viewModelScope.launch {
            repository.insertInvoiceWithItems(invoice, items)
        }
    }

    fun getShareableBillText(item: InvoiceWithItems): String {
        val inv = item.invoice
        val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        val typeLabel = if (inv.type == "SALE") "SALE INVOICE" else "PURCHASE BILL"
        
        val sb = StringBuilder()
        
        // Add Business Profile details if saved
        val profile = businessProfile.value
        if (profile != null) {
            sb.append("=== ${profile.firmName.uppercase()} ===\n")
            sb.append("Ph: ${profile.mobileNumber}\n")
            sb.append("Address: ${profile.address}\n")
            if (profile.email.isNotEmpty()) {
                sb.append("Email: ${profile.email}\n")
            }
            if (profile.gstin.isNotEmpty()) {
                sb.append("GSTIN: ${profile.gstin}\n")
            }
            sb.append("=============================\n")
        }
        
        sb.append("--- ${typeLabel} ---\n")
        sb.append("Inv No: ${inv.invoiceNumber}\n")
        sb.append("Date: ${sdf.format(Date(inv.date))}\n")
        sb.append("Party: ${inv.partyName}\n")
        sb.append("-----------------------------\n")
        
        var itemsTotal = 0.0
        item.items.forEach { itemLine ->
            val qtyStr = if (itemLine.quantity % 1 == 0.0) itemLine.quantity.toInt().toString() else itemLine.quantity.toString()
            sb.append("${itemLine.name}\n  ${qtyStr} x ₹${String.format(Locale.US, "%.2f", itemLine.price)} = ₹${String.format(Locale.US, "%.2f", itemLine.totalPrice)}\n")
            itemsTotal += itemLine.totalPrice
        }
        
        sb.append("-----------------------------\n")
        sb.append("Subtotal: ₹${String.format(Locale.US, "%.2f", itemsTotal)}\n")
        if (inv.tax > 0) {
            val taxAmount = itemsTotal * (inv.tax / 100.0)
            sb.append("Tax (${inv.tax}%): ₹${String.format(Locale.US, "%.2f", taxAmount)}\n")
        }
        if (inv.discount > 0) {
            sb.append("Discount: -₹${String.format(Locale.US, "%.2f", inv.discount)}\n")
        }
        sb.append("GRAND TOTAL: ₹${String.format(Locale.US, "%.2f", inv.totalAmount)}\n")
        if (inv.notes.isNotEmpty()) {
            sb.append("Notes: ${inv.notes}\n")
        }
        sb.append("-----------------------------\n")
        sb.append("Generated via VM BOOK App")
        
        return sb.toString()
    }

    fun checkAndSendDueReminders() {
        viewModelScope.launch {
            try {
                createNotificationChannel()
                val list = repository.getAllInvoices(currentFirmId.value).first()
                
                val now = System.currentTimeMillis()
                val calendarToday = java.util.Calendar.getInstance()
                val todayYear = calendarToday.get(java.util.Calendar.YEAR)
                val todayDay = calendarToday.get(java.util.Calendar.DAY_OF_YEAR)
                
                val activeCreditInvoices = list.filter { 
                    it.invoice.type == "SALE" && it.invoice.isCreditSale && it.invoice.outstandingAmount > 0
                }
                
                var dueTodayCount = 0
                var overdueCount = 0
                var upcomingCount = 0
                var totalOutstanding = 0.0
                
                activeCreditInvoices.forEach { item ->
                    val dueDate = item.invoice.dueDate
                    totalOutstanding += item.invoice.outstandingAmount
                    if (dueDate > 0L) {
                        val calDue = java.util.Calendar.getInstance().apply { timeInMillis = dueDate }
                        val dueYear = calDue.get(java.util.Calendar.YEAR)
                        val dueDay = calDue.get(java.util.Calendar.DAY_OF_YEAR)
                        
                        if (dueYear == todayYear && dueDay == todayDay) {
                            dueTodayCount++
                            sendNotification(
                                id = item.invoice.id.toInt() * 10 + 1,
                                title = "Payment Due Today! 💰",
                                content = "Invoice #${item.invoice.invoiceNumber} for ${item.invoice.partyName} of ₹${String.format(Locale.US, "%.2f", item.invoice.outstandingAmount)} is due today."
                            )
                        } else if (dueDate < now) {
                            overdueCount++
                            sendNotification(
                                id = item.invoice.id.toInt() * 10 + 2,
                                title = "Overdue Payment! ⚠️",
                                content = "Invoice #${item.invoice.invoiceNumber} for ${item.invoice.partyName} of ₹${String.format(Locale.US, "%.2f", item.invoice.outstandingAmount)} is OVERDUE!"
                            )
                        } else {
                            val diff = dueDate - now
                            if (diff <= 86400000L) {
                                upcomingCount++
                                sendNotification(
                                    id = item.invoice.id.toInt() * 10 + 3,
                                    title = "Upcoming Payment Due 🗓️",
                                    content = "Invoice #${item.invoice.invoiceNumber} for ${item.invoice.partyName} of ₹${String.format(Locale.US, "%.2f", item.invoice.outstandingAmount)} is due tomorrow."
                                )
                            }
                        }
                    }
                }
                
                if (dueTodayCount > 0 || overdueCount > 0) {
                    sendNotification(
                        id = 9999,
                        title = "VM Book Credit Dues Alert",
                        content = "You have $dueTodayCount bills due today and $overdueCount bills overdue. Total outstanding: ₹${String.format(Locale.US, "%.2f", totalOutstanding)}"
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val name = "Payment Reminders"
            val descriptionText = "Notifications for credit sale dues and payments"
            val importance = android.app.NotificationManager.IMPORTANCE_DEFAULT
            val channel = android.app.NotificationChannel("PAYMENT_REMINDERS_CHANNEL", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: android.app.NotificationManager =
                application.getSystemService(android.content.Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun sendNotification(id: Int, title: String, content: String) {
        try {
            val builder = androidx.core.app.NotificationCompat.Builder(application, "PAYMENT_REMINDERS_CHANNEL")
                .setSmallIcon(com.example.R.drawable.ic_vm_book_logo)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(androidx.core.app.NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)

            val notificationManager: android.app.NotificationManager =
                application.getSystemService(android.content.Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            notificationManager.notify(id, builder.build())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Settings persistence
    fun getDefaultInvoicePrefix(): String = sharedPrefs.getString("default_invoice_prefix", "INV") ?: "INV"
    fun setDefaultInvoicePrefix(prefix: String) {
        sharedPrefs.edit().putString("default_invoice_prefix", prefix).apply()
    }
    
    fun getDefaultInvoiceTerms(): String = sharedPrefs.getString("default_invoice_terms", "Thank you for your business!") ?: "Thank you for your business!"
    fun setDefaultInvoiceTerms(terms: String) {
        sharedPrefs.edit().putString("default_invoice_terms", terms).apply()
    }

    fun isAppLockEnabled(): Boolean = sharedPrefs.getBoolean("app_lock_enabled", false)
    fun setAppLockEnabled(enabled: Boolean) {
        sharedPrefs.edit().putBoolean("app_lock_enabled", enabled).apply()
    }

    fun getAppLockPin(): String = sharedPrefs.getString("app_lock_pin", "1234") ?: "1234"
    fun setAppLockPin(pin: String) {
        sharedPrefs.edit().putString("app_lock_pin", pin).apply()
    }

    fun isFingerprintEnabled(): Boolean = sharedPrefs.getBoolean("fingerprint_enabled", false)
    fun setFingerprintEnabled(enabled: Boolean) {
        sharedPrefs.edit().putBoolean("fingerprint_enabled", enabled).apply()
    }

    fun getAutoLockDuration(): String = sharedPrefs.getString("auto_lock_duration", "immediate") ?: "immediate"
    fun setAutoLockDuration(duration: String) {
        sharedPrefs.edit().putString("auto_lock_duration", duration).apply()
    }

    fun isPrivacyModeEnabled(): Boolean = sharedPrefs.getBoolean("privacy_mode_enabled", false)
    fun setPrivacyModeEnabled(enabled: Boolean) {
        sharedPrefs.edit().putBoolean("privacy_mode_enabled", enabled).apply()
        isPrivacyHidden.value = enabled
    }

    fun isAutoBackupReminderEnabled(): Boolean = sharedPrefs.getBoolean("auto_backup_reminder_enabled", false)
    fun setAutoBackupReminderEnabled(enabled: Boolean) {
        sharedPrefs.edit().putBoolean("auto_backup_reminder_enabled", enabled).apply()
    }

    fun isBackupBeforeResetEnabled(): Boolean = sharedPrefs.getBoolean("backup_before_reset_enabled", false)
    fun setBackupBeforeResetEnabled(enabled: Boolean) {
        sharedPrefs.edit().putBoolean("backup_before_reset_enabled", enabled).apply()
    }

    fun isTransactionSecurityEnabled(): Boolean = sharedPrefs.getBoolean("transaction_security_enabled", false)
    fun setTransactionSecurityEnabled(enabled: Boolean) {
        sharedPrefs.edit().putBoolean("transaction_security_enabled", enabled).apply()
    }

    fun getTransactionPassword(): String = sharedPrefs.getString("transaction_password", "") ?: ""
    fun setTransactionPassword(password: String) {
        sharedPrefs.edit().putString("transaction_password", password).apply()
    }

    fun isTransactionFingerprintEnabled(): Boolean = sharedPrefs.getBoolean("transaction_fingerprint_enabled", false)
    fun setTransactionFingerprintEnabled(enabled: Boolean) {
        sharedPrefs.edit().putBoolean("transaction_fingerprint_enabled", enabled).apply()
    }

    // --- ADDITIONAL SETTINGS SUPPORT ---
    val themeMode = MutableStateFlow(sharedPrefs.getString("theme_mode", "system") ?: "system")
    fun getThemeMode(): String = themeMode.value
    fun setThemeMode(mode: String) {
        sharedPrefs.edit().putString("theme_mode", mode).apply()
        themeMode.value = mode
    }

    val dateFormatPref = MutableStateFlow(sharedPrefs.getString("date_format_pref", "dd MMM yyyy") ?: "dd MMM yyyy")
    fun getDateFormatPref(): String = dateFormatPref.value
    fun setDateFormatPref(format: String) {
        sharedPrefs.edit().putString("date_format_pref", format).apply()
        dateFormatPref.value = format
    }

    val currencyPref = MutableStateFlow(sharedPrefs.getString("currency_pref", "₹") ?: "₹")
    fun getCurrencyPref(): String = currencyPref.value
    fun setCurrencyPref(currency: String) {
        sharedPrefs.edit().putString("currency_pref", currency).apply()
        currencyPref.value = currency
    }

    val languagePref = MutableStateFlow(sharedPrefs.getString("language_pref", "en") ?: "en")
    fun getLanguagePref(): String = languagePref.value
    fun setLanguagePref(language: String) {
        sharedPrefs.edit().putString("language_pref", language).apply()
        languagePref.value = language
    }

    val paymentReminderEnabled = MutableStateFlow(sharedPrefs.getBoolean("payment_reminder_enabled", true))
    fun isPaymentReminderEnabled(): Boolean = paymentReminderEnabled.value
    fun setPaymentReminderEnabled(enabled: Boolean) {
        sharedPrefs.edit().putBoolean("payment_reminder_enabled", enabled).apply()
        paymentReminderEnabled.value = enabled
    }

    val creditReminderEnabled = MutableStateFlow(sharedPrefs.getBoolean("credit_reminder_enabled", true))
    fun isCreditReminderEnabled(): Boolean = creditReminderEnabled.value
    fun setCreditReminderEnabled(enabled: Boolean) {
        sharedPrefs.edit().putBoolean("credit_reminder_enabled", enabled).apply()
        creditReminderEnabled.value = enabled
    }

    val dueDateReminderEnabled = MutableStateFlow(sharedPrefs.getBoolean("due_date_reminder_enabled", true))
    fun isDueDateReminderEnabled(): Boolean = dueDateReminderEnabled.value
    fun setDueDateReminderEnabled(enabled: Boolean) {
        sharedPrefs.edit().putBoolean("due_date_reminder_enabled", enabled).apply()
        dueDateReminderEnabled.value = enabled
    }

    val defaultGstRate = MutableStateFlow(sharedPrefs.getFloat("default_gst_rate", 18f))
    fun getDefaultGstRate(): Float = defaultGstRate.value
    fun setDefaultGstRate(rate: Float) {
        sharedPrefs.edit().putFloat("default_gst_rate", rate).apply()
        defaultGstRate.value = rate
    }

    val gstBillingEnabled = MutableStateFlow(sharedPrefs.getBoolean("gst_billing_enabled", true))
    fun isGstBillingEnabled(): Boolean = gstBillingEnabled.value
    fun setGstBillingEnabled(enabled: Boolean) {
        sharedPrefs.edit().putBoolean("gst_billing_enabled", enabled).apply()
        gstBillingEnabled.value = enabled
    }

    fun adjustStock(productName: String, qtyChange: Double, reason: String) {
        viewModelScope.launch {
            val rand = (1000..9999).random()
            val adjNum = "ADJ-$rand"
            val isIncrease = qtyChange > 0
            val type = if (isIncrease) "PURCHASE" else "SALE"
            val absQty = kotlin.math.abs(qtyChange)
            
            // Find existing average price for this item
            val stockList = stockBalances.value
            val existingItem = stockList.firstOrNull { it.name.trim().equals(productName.trim(), ignoreCase = true) }
            val price = existingItem?.averagePurchasePrice ?: 1.0
            
            val invoice = com.example.data.InvoiceEntity(
                invoiceNumber = adjNum,
                partyName = "Stock Adjustment",
                type = type,
                date = System.currentTimeMillis(),
                discount = 0.0,
                tax = 0.0,
                totalAmount = price * absQty,
                notes = if (reason.trim().isNotEmpty()) reason.trim() else "Manual Stock Adjustment",
                isCreditSale = false,
                outstandingAmount = 0.0,
                dueDate = 0L,
                firmId = currentFirmId.value
            )
            val items = listOf(
                com.example.data.InvoiceItemEntity(
                    invoiceId = 0,
                    name = productName.trim(),
                    price = price,
                    quantity = absQty,
                    totalPrice = price * absQty
                )
            )
            repository.insertInvoiceWithItems(invoice, items)
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            try {
                // Room built-in clear tables
                com.example.data.AppDatabase.getDatabase(application).clearAllTables()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getUpdateJsonUrl(): String = updateJsonUrl.value
    fun setUpdateJsonUrl(url: String) {
        sharedPrefs.edit().putString("update_json_url", url).apply()
        updateJsonUrl.value = url
    }

    fun checkForUpdates(manual: Boolean = false) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            updateStatus.value = UpdateStatus.Checking
            try {
                val urlString = updateJsonUrl.value
                if (urlString.isBlank()) {
                    if (manual) {
                        updateStatus.value = UpdateStatus.Error("Update check URL is empty")
                    } else {
                        updateStatus.value = UpdateStatus.Idle
                    }
                    return@launch
                }
                
                val connection = java.net.URL(urlString).openConnection() as java.net.HttpURLConnection
                connection.connectTimeout = 10000
                connection.readTimeout = 10000
                connection.requestMethod = "GET"
                connection.connect()
                
                if (connection.responseCode != 200) {
                    if (manual) {
                        updateStatus.value = UpdateStatus.Error("Server returned code ${connection.responseCode}")
                    } else {
                        updateStatus.value = UpdateStatus.Idle
                    }
                    return@launch
                }
                
                val text = connection.inputStream.bufferedReader().use { it.readText() }
                val json = org.json.JSONObject(text)
                
                val remoteVersionCode = json.optLong("versionCode", 0L)
                val remoteVersionName = json.optString("versionName", "")
                val forceUpdate = json.optBoolean("forceUpdate", false)
                val apkUrl = json.optString("apkUrl", "")
                val whatsNew = json.optString("whatsNew", "")
                
                val packageInfo = application.packageManager.getPackageInfo(application.packageName, 0)
                val currentVersionName = packageInfo.versionName ?: "1.0.0"
                val currentVersionCode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                    packageInfo.longVersionCode
                } else {
                    @Suppress("DEPRECATION")
                    packageInfo.versionCode.toLong()
                }
                
                val isUpdateAvailable = if (remoteVersionCode > 0L) {
                    remoteVersionCode > currentVersionCode
                } else {
                    compareVersions(remoteVersionName, currentVersionName) > 0
                }
                
                if (isUpdateAvailable) {
                    updateStatus.value = UpdateStatus.NewUpdateAvailable(
                        currentVersion = currentVersionName,
                        latestVersion = remoteVersionName,
                        whatsNew = whatsNew,
                        apkUrl = apkUrl,
                        forceUpdate = forceUpdate
                    )
                } else {
                    if (manual) {
                        updateStatus.value = UpdateStatus.Error("App is up-to-date")
                    } else {
                        updateStatus.value = UpdateStatus.Idle
                    }
                }
            } catch (e: Exception) {
                if (manual) {
                    updateStatus.value = UpdateStatus.Error(e.localizedMessage ?: "Unknown network error")
                } else {
                    updateStatus.value = UpdateStatus.Idle
                }
            }
        }
    }

    private fun compareVersions(v1: String, v2: String): Int {
        val parts1 = v1.split(".").mapNotNull { it.toIntOrNull() }
        val parts2 = v2.split(".").mapNotNull { it.toIntOrNull() }
        val maxLength = maxOf(parts1.size, parts2.size)
        for (i in 0 until maxLength) {
            val p1 = parts1.getOrElse(i) { 0 }
            val p2 = parts2.getOrElse(i) { 0 }
            if (p1 != p2) {
                return p1.compareTo(p2)
            }
        }
        return 0
    }

    fun dismissUpdateDialog() {
        updateStatus.value = UpdateStatus.Idle
    }

    fun downloadAndInstallApk(apkUrl: String) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            updateStatus.value = UpdateStatus.Downloading(0f)
            try {
                val connection = java.net.URL(apkUrl).openConnection() as java.net.HttpURLConnection
                connection.connectTimeout = 15000
                connection.readTimeout = 15000
                connection.connect()
                
                if (connection.responseCode != 200) {
                    updateStatus.value = UpdateStatus.Error("Failed to download APK. Server returned code ${connection.responseCode}")
                    return@launch
                }
                
                val fileLength = connection.contentLength
                val input = connection.inputStream
                
                val apkFile = java.io.File(application.getExternalFilesDir(null) ?: application.cacheDir, "update.apk")
                if (apkFile.exists()) {
                    apkFile.delete()
                }
                val output = java.io.FileOutputStream(apkFile)
                
                val data = ByteArray(4096)
                var total: Long = 0
                var count: Int
                while (input.read(data).also { count = it } != -1) {
                    total += count
                    if (fileLength > 0) {
                        val progress = total.toFloat() / fileLength
                        updateStatus.value = UpdateStatus.Downloading(progress)
                    }
                    output.write(data, 0, count)
                }
                
                output.flush()
                output.close()
                input.close()
                
                updateStatus.value = UpdateStatus.DownloadCompleted(apkFile)
            } catch (e: Exception) {
                updateStatus.value = UpdateStatus.Error("Download failed: ${e.localizedMessage}")
            }
        }
    }

    // --- CLOUD SYNC & MULTI-DEVICE SUPPORT ---
    val cloudUserMobile = MutableStateFlow(sharedPrefs.getString("cloud_user_mobile", "") ?: "")
    val cloudServerUrl = MutableStateFlow(sharedPrefs.getString("cloud_server_url", "https://api.vmbook.app") ?: "https://api.vmbook.app")
    val isCloudSandboxEnabled = MutableStateFlow(sharedPrefs.getBoolean("cloud_sandbox_enabled", true))
    val isSyncing = MutableStateFlow(false)
    val lastSyncTime = MutableStateFlow(sharedPrefs.getLong("last_sync_time", 0L))
    val syncStatusMessage = MutableStateFlow<String?>(null)

    // Login screen states
    val loginStep = MutableStateFlow(0) // 0 = Enter Mobile, 1 = Enter OTP
    val loginMobileInput = MutableStateFlow("")
    val loginOtpInput = MutableStateFlow("")
    val loginError = MutableStateFlow<String?>(null)
    private var generatedOtp = ""

    fun sendCloudOtp(mobile: String) {
        if (mobile.trim().length < 10) {
            loginError.value = "Please enter a valid 10-digit mobile number"
            return
        }
        loginError.value = null
        val code = (100000..999999).random().toString()
        generatedOtp = code
        loginStep.value = 1
        android.widget.Toast.makeText(application, "SECURE OTP: $code (Simulation)", android.widget.Toast.LENGTH_LONG).show()
    }

    fun verifyCloudOtp(otp: String) {
        if (otp != generatedOtp) {
            loginError.value = "Incorrect OTP. Please try again."
            return
        }
        loginError.value = null
        val mobile = loginMobileInput.value.trim()
        sharedPrefs.edit().putString("cloud_user_mobile", mobile).apply()
        cloudUserMobile.value = mobile
        loginStep.value = 0
        loginMobileInput.value = ""
        loginOtpInput.value = ""

        // Automatic initial sync to pull remote data on new device
        performCloudRestore(isAutoSync = true)
    }

    fun logoutCloud() {
        sharedPrefs.edit().remove("cloud_user_mobile").apply()
        cloudUserMobile.value = ""
        loginStep.value = 0
        loginMobileInput.value = ""
        loginOtpInput.value = ""
        loginError.value = null
    }

    fun setCloudSandboxEnabled(enabled: Boolean) {
        sharedPrefs.edit().putBoolean("cloud_sandbox_enabled", enabled).apply()
        isCloudSandboxEnabled.value = enabled
    }

    fun setCloudServerUrl(url: String) {
        sharedPrefs.edit().putString("cloud_server_url", url).apply()
        cloudServerUrl.value = url
    }

    // Automatically check connectivity and trigger sync
    fun initNetworkAutoSync() {
        val cm = application.getSystemService(android.content.Context.CONNECTIVITY_SERVICE) as? android.net.ConnectivityManager
        if (cm != null) {
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    cm.registerDefaultNetworkCallback(object : android.net.ConnectivityManager.NetworkCallback() {
                        override fun onAvailable(network: android.net.Network) {
                            if (cloudUserMobile.value.isNotEmpty()) {
                                performCloudBackup(isAutoSync = true)
                            }
                        }
                    })
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun performCloudBackup(isAutoSync: Boolean = false) {
        val mobile = cloudUserMobile.value
        if (mobile.isEmpty()) return

        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            isSyncing.value = true
            syncStatusMessage.value = if (isAutoSync) "Auto-syncing..." else "Creating cloud backup..."
            try {
                // Fetch all data from database tables
                val customers = repository.getAllCustomersListUnfiltered()
                val customerPayments = repository.getAllCustomerPaymentsListUnfiltered()
                val suppliers = repository.getAllSuppliersListUnfiltered()
                val supplierPayments = repository.getAllSupplierPaymentsListUnfiltered()
                val categories = repository.getAllCategoriesUnfiltered()
                val items = repository.getAllProductItemsUnfiltered()
                val invoices = repository.getAllInvoicesListUnfiltered()
                val invoiceItems = repository.getAllInvoiceItemsList()
                val sequences = repository.getAllInvoiceSequencesListUnfiltered()
                val firms = repository.getAllFirmsList()

                // Package settings from SharedPreferences
                val settingsKeys = listOf(
                    "firm_name", "mobile_number", "address", "gstin", "email",
                    "default_invoice_prefix", "default_invoice_terms", "theme_mode",
                    "date_format_pref", "currency_pref", "language_pref", "default_gst_rate",
                    "gst_billing_enabled", "payment_reminder_enabled", "credit_reminder_enabled", "due_date_reminder_enabled"
                )
                val settingsMap = settingsKeys.associateWith { key ->
                    sharedPrefs.all[key]
                }

                val payload = com.example.data.CloudSyncService.serializePayload(
                    customers, customerPayments, suppliers, supplierPayments,
                    categories, items, invoices, invoiceItems, sequences, settingsMap, firms
                )

                val success = if (isCloudSandboxEnabled.value) {
                    com.example.data.CloudSyncService.saveToSandbox(application, mobile, payload)
                    true
                } else {
                    com.example.data.CloudSyncService.uploadToCloud(cloudServerUrl.value, mobile, payload)
                }

                if (success) {
                    val time = System.currentTimeMillis()
                    sharedPrefs.edit().putLong("last_sync_time", time).apply()
                    lastSyncTime.value = time
                    syncStatusMessage.value = "Backup sync completed successfully"
                } else {
                    syncStatusMessage.value = "Failed to sync: Cloud Server unreachable"
                }
            } catch (e: Exception) {
                e.printStackTrace()
                syncStatusMessage.value = "Sync failed: ${e.localizedMessage}"
            } finally {
                isSyncing.value = false
            }
        }
    }

    fun performCloudRestore(isAutoSync: Boolean = false) {
        val mobile = cloudUserMobile.value
        if (mobile.isEmpty()) return

        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            isSyncing.value = true
            syncStatusMessage.value = "Restoring data from cloud..."
            try {
                val payloadText = if (isCloudSandboxEnabled.value) {
                    com.example.data.CloudSyncService.loadFromSandbox(application, mobile)
                } else {
                    com.example.data.CloudSyncService.downloadFromCloud(cloudServerUrl.value, mobile)
                }

                if (payloadText == null || payloadText.trim().isEmpty()) {
                    syncStatusMessage.value = "No existing cloud backup found for this account"
                    isSyncing.value = false
                    return@launch
                }

                val root = org.json.JSONObject(payloadText)

                // 0. Restore Firms
                val firmsArray = root.optJSONArray("firms") ?: org.json.JSONArray()
                val localFirms = repository.getAllFirmsList()
                val localFirmsMap = localFirms.associateBy { it.id }
                for (i in 0 until firmsArray.length()) {
                    val obj = firmsArray.getJSONObject(i)
                    val id = obj.getLong("id")
                    val name = obj.getString("name")
                    val phone = obj.optString("phone", "")
                    val email = obj.optString("email", "")
                    val address = obj.optString("address", "")
                    val gstin = obj.optString("gstin", "")
                    val logoUri = obj.optString("logoUri", "")
                    
                    if (!localFirmsMap.containsKey(id)) {
                        repository.insertFirm(com.example.data.FirmEntity(
                            id = id,
                            name = name,
                            phone = phone,
                            email = email,
                            address = address,
                            gstin = gstin,
                            logoUri = logoUri
                        ))
                    }
                }

                // 1. Restore Categories & Products
                val catArray = root.optJSONArray("categories") ?: org.json.JSONArray()
                val localCats = repository.getAllCategoriesUnfiltered()
                val catNameToIdMap = localCats.associate { "${it.name}_${it.firmId}" to it.id }.toMutableMap()
                for (i in 0 until catArray.length()) {
                    val obj = catArray.getJSONObject(i)
                    val name = obj.getString("name")
                    val firmId = obj.optLong("firmId", 1L)
                    val key = "${name}_$firmId"
                    if (!catNameToIdMap.containsKey(key)) {
                        repository.insertCategory(com.example.data.ProductCategoryEntity(name = name, firmId = firmId))
                    }
                }
                // Refresh categories map
                repository.getAllCategoriesUnfiltered().forEach { catNameToIdMap["${it.name}_${it.firmId}"] = it.id }

                val itemArray = root.optJSONArray("items") ?: org.json.JSONArray()
                val localItems = repository.getAllProductItemsUnfiltered()
                val itemKeys = localItems.map { "${it.name}_${it.categoryName}_${it.firmId}" }.toSet()
                for (i in 0 until itemArray.length()) {
                    val obj = itemArray.getJSONObject(i)
                    val name = obj.getString("name")
                    val catName = obj.getString("categoryName")
                    val firmId = obj.optLong("firmId", 1L)
                    val hsnCode = obj.optString("hsnCode", "")
                    val defaultSellingRate = if (obj.has("defaultSellingRate")) obj.getDouble("defaultSellingRate") else null
                    val defaultDiscountValue = if (obj.has("defaultDiscountValue")) obj.getDouble("defaultDiscountValue") else null
                    val defaultDiscountType = if (obj.has("defaultDiscountType")) obj.getString("defaultDiscountType") else null
                    val key = "${name}_${catName}_$firmId"
                    if (!itemKeys.contains(key)) {
                        repository.insertProductItem(com.example.data.ProductItemEntity(
                            name = name,
                            categoryName = catName,
                            hsnCode = if (hsnCode.isEmpty()) null else hsnCode,
                            defaultSellingRate = defaultSellingRate,
                            defaultDiscountValue = defaultDiscountValue,
                            defaultDiscountType = defaultDiscountType,
                            firmId = firmId
                        ))
                    }
                }

                // 2. Restore Customers & Supplier profiles
                val custArray = root.optJSONArray("customers") ?: org.json.JSONArray()
                val localCusts = repository.getAllCustomersListUnfiltered()
                val custToIdMap = localCusts.associateBy { "${it.name.trim().lowercase()}_${it.phone.trim()}_${it.firmId}" }.mapValues { it.value.id }.toMutableMap()
                val remoteToLocalCustIdMap = mutableMapOf<Long, Long>()

                for (i in 0 until custArray.length()) {
                    val obj = custArray.getJSONObject(i)
                    val rId = obj.getLong("id")
                    val name = obj.getString("name")
                    val phone = obj.optString("phone", "")
                    val email = obj.optString("email", "")
                    val address = obj.optString("address", "")
                    val notes = obj.optString("notes", "")
                    val firmId = obj.optLong("firmId", 1L)

                    val key = "${name.trim().lowercase()}_${phone.trim()}_$firmId"
                    var lId = custToIdMap[key]
                    if (lId == null) {
                        lId = repository.insertCustomer(com.example.data.CustomerEntity(
                            name = name,
                            phone = phone,
                            email = email,
                            address = address,
                            notes = notes,
                            firmId = firmId
                        ))
                        custToIdMap[key] = lId
                    }
                    remoteToLocalCustIdMap[rId] = lId
                }

                val suppArray = root.optJSONArray("suppliers") ?: org.json.JSONArray()
                val localSupps = repository.getAllSuppliersListUnfiltered()
                val suppToIdMap = localSupps.associateBy { "${it.name.trim().lowercase()}_${it.phone.trim()}_${it.firmId}" }.mapValues { it.value.id }.toMutableMap()
                val remoteToLocalSuppIdMap = mutableMapOf<Long, Long>()

                for (i in 0 until suppArray.length()) {
                    val obj = suppArray.getJSONObject(i)
                    val rId = obj.getLong("id")
                    val name = obj.getString("name")
                    val phone = obj.optString("phone", "")
                    val email = obj.optString("email", "")
                    val address = obj.optString("address", "")
                    val notes = obj.optString("notes", "")
                    val firmId = obj.optLong("firmId", 1L)

                    val key = "${name.trim().lowercase()}_${phone.trim()}_$firmId"
                    var lId = suppToIdMap[key]
                    if (lId == null) {
                        lId = repository.insertSupplier(com.example.data.SupplierEntity(
                            name = name,
                            phone = phone,
                            email = email,
                            address = address,
                            notes = notes,
                            firmId = firmId
                        ))
                        suppToIdMap[key] = lId
                    }
                    remoteToLocalSuppIdMap[rId] = lId
                }

                // 3. Restore Customer and Supplier Payments
                val custPayArray = root.optJSONArray("customerPayments") ?: org.json.JSONArray()
                val localCustPayments = repository.getAllCustomerPaymentsListUnfiltered()
                val custPayKeys = localCustPayments.map { "${it.customerId}_${it.amount}_${it.date}_${it.firmId}" }.toSet()
                for (i in 0 until custPayArray.length()) {
                    val obj = custPayArray.getJSONObject(i)
                    val rCustId = obj.getLong("customerId")
                    val lCustId = remoteToLocalCustIdMap[rCustId] ?: continue
                    val amount = obj.getDouble("amount")
                    val date = obj.optLong("date", System.currentTimeMillis())
                    val payMode = obj.optString("paymentMode", "Cash")
                    val refNo = obj.optString("referenceNo", "")
                    val notes = obj.optString("notes", "")
                    val firmId = obj.optLong("firmId", 1L)

                    val key = "${lCustId}_${amount}_${date}_$firmId"
                    if (!custPayKeys.contains(key)) {
                        repository.insertCustomerPayment(com.example.data.CustomerPaymentEntity(
                            customerId = lCustId,
                            amount = amount,
                            date = date,
                            paymentMode = payMode,
                            referenceNo = refNo,
                            notes = notes,
                            firmId = firmId
                        ))
                    }
                }

                val suppPayArray = root.optJSONArray("supplierPayments") ?: org.json.JSONArray()
                val localSuppPayments = repository.getAllSupplierPaymentsListUnfiltered()
                val suppPayKeys = localSuppPayments.map { "${it.supplierId}_${it.amount}_${it.date}_${it.firmId}" }.toSet()
                for (i in 0 until suppPayArray.length()) {
                    val obj = suppPayArray.getJSONObject(i)
                    val rSuppId = obj.getLong("supplierId")
                    val lSuppId = remoteToLocalSuppIdMap[rSuppId] ?: continue
                    val amount = obj.getDouble("amount")
                    val date = obj.optLong("date", System.currentTimeMillis())
                    val payMode = obj.optString("paymentMode", "Cash")
                    val refNo = obj.optString("referenceNo", "")
                    val notes = obj.optString("notes", "")
                    val firmId = obj.optLong("firmId", 1L)

                    val key = "${lSuppId}_${amount}_${date}_$firmId"
                    if (!suppPayKeys.contains(key)) {
                        repository.insertSupplierPayment(com.example.data.SupplierPaymentEntity(
                            supplierId = lSuppId,
                            amount = amount,
                            date = date,
                            paymentMode = payMode,
                            referenceNo = refNo,
                            notes = notes,
                            firmId = firmId
                        ))
                    }
                }

                // 4. Restore Invoices & Items
                val invArray = root.optJSONArray("invoices") ?: org.json.JSONArray()
                val localInvoices = repository.getAllInvoicesListUnfiltered()
                val localInvNos = localInvoices.associateBy { "${it.invoiceNumber}_${it.firmId}" }
                val remoteToLocalInvIdMap = mutableMapOf<Long, Long>()

                for (i in 0 until invArray.length()) {
                    val obj = invArray.getJSONObject(i)
                    val rId = obj.getLong("id")
                    val number = obj.getString("invoiceNumber")
                    val party = obj.getString("partyName")
                    val type = obj.getString("type")
                    val date = obj.optLong("date", System.currentTimeMillis())
                    val discount = obj.optDouble("discount", 0.0)
                    val tax = obj.optDouble("tax", 0.0)
                    val total = obj.optDouble("totalAmount", 0.0)
                    val notes = obj.optString("notes", "")
                    val credit = obj.optBoolean("isCreditSale", false)
                    val outstanding = obj.optDouble("outstandingAmount", 0.0)
                    val dueDate = obj.optLong("dueDate", 0L)
                    val firmId = obj.optLong("firmId", 1L)

                    val key = "${number}_$firmId"
                    var existing = localInvNos[key]
                    if (existing == null) {
                        // Create invoice directly to get custom invoiceNumber
                        val newId = com.example.data.AppDatabase.getDatabase(application).invoiceDao().insertInvoice(
                            com.example.data.InvoiceEntity(
                                invoiceNumber = number,
                                partyName = party,
                                type = type,
                                date = date,
                                discount = discount,
                                tax = tax,
                                totalAmount = total,
                                notes = notes,
                                isCreditSale = credit,
                                outstandingAmount = outstanding,
                                dueDate = dueDate,
                                firmId = firmId
                            )
                        )
                        remoteToLocalInvIdMap[rId] = newId
                    } else {
                        remoteToLocalInvIdMap[rId] = existing.id
                    }
                }

                val invItemsArray = root.optJSONArray("invoiceItems") ?: org.json.JSONArray()
                val localInvItems = repository.getAllInvoiceItemsList()
                val localInvItemKeys = localInvItems.map { "${it.invoiceId}_${it.name}_${it.quantity}_${it.totalPrice}" }.toSet()
                val itemsToInsert = mutableListOf<com.example.data.InvoiceItemEntity>()

                for (i in 0 until invItemsArray.length()) {
                    val obj = invItemsArray.getJSONObject(i)
                    val rInvId = obj.getLong("invoiceId")
                    val lInvId = remoteToLocalInvIdMap[rInvId] ?: continue
                    val name = obj.getString("name")
                    val price = obj.getDouble("price")
                    val qty = obj.getDouble("quantity")
                    val total = obj.getDouble("totalPrice")
                    val hsn = obj.optString("hsnCode", "")

                    val key = "${lInvId}_${name}_${qty}_$total"
                    if (!localInvItemKeys.contains(key)) {
                        itemsToInsert.add(com.example.data.InvoiceItemEntity(
                            invoiceId = lInvId,
                            name = name,
                            price = price,
                            quantity = qty,
                            totalPrice = total,
                            hsnCode = if (hsn.isEmpty()) null else hsn
                        ))
                    }
                }
                if (itemsToInsert.isNotEmpty()) {
                    repository.insertInvoiceItemsDirect(itemsToInsert)
                }

                // 5. Restore Sequences
                val seqArray = root.optJSONArray("sequences") ?: org.json.JSONArray()
                for (i in 0 until seqArray.length()) {
                    val obj = seqArray.getJSONObject(i)
                    val type = obj.getString("type")
                    val value = obj.getInt("lastVal")
                    val firmId = obj.optLong("firmId", 1L)
                    val currentStored = repository.getLastSequenceValue(firmId, type) ?: 0
                    if (value > currentStored) {
                        repository.insertSequenceValue(com.example.data.InvoiceSequenceEntity(firmId = firmId, type = type, lastVal = value))
                    }
                }

                // 6. Restore Settings Map
                val settingsObj = root.optJSONObject("settings") ?: org.json.JSONObject()
                val editor = sharedPrefs.edit()
                val iter = settingsObj.keys()
                while (iter.hasNext()) {
                    val key = iter.next()
                    val value = settingsObj.get(key)
                    if (value is Boolean) {
                        editor.putBoolean(key, value)
                    } else if (value is Float || value is Double) {
                        editor.putFloat(key, value.toString().toFloat())
                    } else if (value is Int) {
                        editor.putInt(key, value)
                    } else if (value is Long) {
                        editor.putLong(key, value)
                    } else {
                        editor.putString(key, value.toString())
                    }
                }
                editor.apply()

                // Reload profile details into viewModel streams
                loadBusinessProfile()

                val time = System.currentTimeMillis()
                sharedPrefs.edit().putLong("last_sync_time", time).apply()
                lastSyncTime.value = time
                syncStatusMessage.value = "Cloud restore and merge completed successfully"
            } catch (e: Exception) {
                e.printStackTrace()
                syncStatusMessage.value = "Restore failed: ${e.localizedMessage}"
            } finally {
                isSyncing.value = false
            }
        }
    }

    fun dismissSyncStatus() {
        syncStatusMessage.value = null
    }

    // A helper method for instant multi-device review
    fun simulateSecondDevice() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                com.example.data.AppDatabase.getDatabase(application).clearAllTables()
                loadBusinessProfile()
                syncStatusMessage.value = "Device data successfully wiped. You are now simulated as a second device. Simply click Restore to sync all cloud data!"
            } catch (e: Exception) {
                syncStatusMessage.value = "Simulation failed: ${e.localizedMessage}"
            }
        }
    }
}

sealed class UpdateStatus {
    object Idle : UpdateStatus()
    object Checking : UpdateStatus()
    data class NewUpdateAvailable(
        val currentVersion: String,
        val latestVersion: String,
        val whatsNew: String,
        val apkUrl: String,
        val forceUpdate: Boolean
    ) : UpdateStatus()
    data class Downloading(val progress: Float) : UpdateStatus()
    data class Error(val message: String) : UpdateStatus()
    data class DownloadCompleted(val apkFile: java.io.File) : UpdateStatus()
}

data class DashboardStats(
    val totalSales: Double = 0.0,
    val totalPurchases: Double = 0.0,
    val netBalance: Double = 0.0,
    val totalProfit: Double = 0.0,
    val salesCount: Int = 0,
    val purchasesCount: Int = 0,
    val remainingStockValue: Double = 0.0,
    val totalOutstandingCredit: Double = 0.0,
    val overdueCreditInvoicesCount: Int = 0,
    val todaySales: Double = 0.0,
    val todayProfit: Double = 0.0
)

class BillingViewModelFactory(
    private val repository: InvoiceRepository,
    private val application: android.app.Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BillingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BillingViewModel(repository, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
