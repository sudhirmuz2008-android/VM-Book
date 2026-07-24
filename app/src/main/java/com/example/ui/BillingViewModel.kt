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
    val name: String = "",
    val price: String = "",
    val quantity: String = "1"
)

class BillingViewModel(
    private val repository: InvoiceRepository,
    private val application: android.app.Application
) : ViewModel() {

    private val sharedPrefs = application.getSharedPreferences("business_profile_prefs", android.content.Context.MODE_PRIVATE)

    val businessProfile = MutableStateFlow<BusinessProfile?>(null)

    init {
        loadBusinessProfile()
    }

    fun loadBusinessProfile() {
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

    fun saveBusinessProfile(profile: BusinessProfile) {
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
    val rawInvoices = repository.allInvoices
    val allInvoices: StateFlow<List<InvoiceWithItems>> = repository.allInvoices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Product calculations and summaries
    val productSummaries: StateFlow<List<ProductSummary>> = allInvoices.map { list ->
        val summaries = mutableMapOf<String, Triple<Double, Double, Double>>() // name -> (qtyPurchased, qtySold, totalPurchaseVal)
        val salesVal = mutableMapOf<String, Double>() // name -> totalSalesVal
        
        list.forEach { invWithItems ->
            val type = invWithItems.invoice.type
            invWithItems.items.forEach { item ->
                val name = item.name.trim()
                if (name.isNotEmpty()) {
                    val current = summaries[name] ?: Triple(0.0, 0.0, 0.0)
                    if (type == "PURCHASE") {
                        summaries[name] = Triple(
                            current.first + item.quantity,
                            current.second,
                            current.third + item.totalPrice
                        )
                    } else {
                        summaries[name] = Triple(
                            current.first,
                            current.second + item.quantity,
                            current.third
                        )
                        salesVal[name] = (salesVal[name] ?: 0.0) + item.totalPrice
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
    val distinctItemNames: StateFlow<List<String>> = repository.distinctItemNames
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val distinctPartyNames: StateFlow<List<String>> = repository.distinctPartyNames
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Separate Suggestions Flows
    val customerSuggestions: StateFlow<List<String>> = repository.allCustomers
        .map { list -> list.map { it.name } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val supplierSuggestions: StateFlow<List<String>> = repository.allSuppliers
        .map { list -> list.map { it.name } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Search and Selection States for Customers/Suppliers
    val customerSearchQuery = MutableStateFlow("")
    val supplierSearchQuery = MutableStateFlow("")
    val selectedCustomerId = MutableStateFlow<Long?>(null)
    val selectedSupplierId = MutableStateFlow<Long?>(null)

    // Customer / Supplier with balances
    val customersWithBalance: StateFlow<List<CustomerWithBalance>> = combine(
        repository.allCustomers,
        rawInvoices,
        repository.allCustomerPayments,
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
        repository.allSuppliers,
        rawInvoices,
        repository.allSupplierPayments,
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
        repository.allCustomers
    ) { id, customers ->
        customers.find { it.id == id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val selectedCustomerLedger: StateFlow<List<CustomerLedgerEntry>> = combine(
        selectedCustomer,
        rawInvoices,
        repository.allCustomerPayments
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
        repository.allSuppliers
    ) { id, suppliers ->
        suppliers.find { it.id == id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val selectedSupplierLedger: StateFlow<List<SupplierLedgerEntry>> = combine(
        selectedSupplier,
        rawInvoices,
        repository.allSupplierPayments
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
            repository.insertCustomer(customer)
        }
    }

    fun deleteCustomer(id: Long) {
        viewModelScope.launch {
            repository.deleteCustomerById(id)
        }
    }

    fun addCustomerPayment(payment: com.example.data.CustomerPaymentEntity) {
        viewModelScope.launch {
            repository.insertCustomerPayment(payment)
        }
    }

    fun addCustomerSale(customerId: Long, partyName: String, amount: Double, notes: String, date: Long) {
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
            dueDate = 0L
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
        viewModelScope.launch {
            repository.insertInvoiceWithItems(invoice, items)
        }
    }

    fun addSupplierPurchase(supplierId: Long, partyName: String, amount: Double, notes: String, date: Long) {
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
            dueDate = 0L
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
        viewModelScope.launch {
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
            repository.insertSupplier(supplier)
        }
    }

    fun deleteSupplier(id: Long) {
        viewModelScope.launch {
            repository.deleteSupplierById(id)
        }
    }

    fun addSupplierPayment(payment: com.example.data.SupplierPaymentEntity) {
        viewModelScope.launch {
            repository.insertSupplierPayment(payment)
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
                invoiceWithItems.items.forEach { item ->
                    val name = item.name.trim()
                    if (name.isNotEmpty()) {
                        val qty = (productPurchaseQty[name] ?: 0.0) + item.quantity
                        val totalVal = (productPurchaseVal[name] ?: 0.0) + item.totalPrice
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
        val now = System.currentTimeMillis()
        val todayCal = java.util.Calendar.getInstance()
        val itemCal = java.util.Calendar.getInstance()

        list.forEach { item ->
            if (item.invoice.type == "SALE") {
                totalSales += item.invoice.totalAmount
                salesCount++

                // Calculate today's sales
                itemCal.timeInMillis = item.invoice.date
                if (itemCal.get(java.util.Calendar.YEAR) == todayCal.get(java.util.Calendar.YEAR) &&
                    itemCal.get(java.util.Calendar.DAY_OF_YEAR) == todayCal.get(java.util.Calendar.DAY_OF_YEAR)) {
                    todaySales += item.invoice.totalAmount
                }

                // Calculate profit for this sale
                val subTotal = item.items.sumOf { it.totalPrice }
                val cogs = item.items.sumOf { it.quantity * (productAvgPurchasePrice[it.name.trim()] ?: 0.0) }
                val profit = (subTotal - item.invoice.discount) - cogs
                totalRealizedProfit += profit

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
        val productGroups = mutableMapOf<String, MutableList<Pair<String, InvoiceItemEntity>>>()
        list.forEach { invoiceWithItems ->
            val type = invoiceWithItems.invoice.type
            invoiceWithItems.items.forEach { item ->
                val normalizedName = item.name.trim()
                if (normalizedName.isNotEmpty()) {
                    productGroups.getOrPut(normalizedName) { mutableListOf() }.add(type to item)
                }
            }
        }

        var remainingStockValue = 0.0
        productGroups.forEach { (name, history) ->
            val purchases = history.filter { it.first == "PURCHASE" }.map { it.second }
            val sales = history.filter { it.first == "SALE" }.map { it.second }

            val qtyPurchased = purchases.sumOf { it.quantity }
            val totalPurchaseValue = purchases.sumOf { it.totalPrice }
            val avgPurchasePrice = if (qtyPurchased > 0) totalPurchaseValue / qtyPurchased else {
                val qtySold = sales.sumOf { it.quantity }
                if (qtySold > 0) sales.sumOf { it.totalPrice } / qtySold else 0.0
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
            todaySales = todaySales
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
                invoiceWithItems.items.forEach { item ->
                    val name = item.name.trim()
                    if (name.isNotEmpty()) {
                        val qty = (productPurchaseQty[name] ?: 0.0) + item.quantity
                        val totalVal = (productPurchaseVal[name] ?: 0.0) + item.totalPrice
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
                val subTotal = item.items.sumOf { it.totalPrice }
                val cogs = item.items.sumOf { it.quantity * (productAvgPurchasePrice[it.name.trim()] ?: 0.0) }
                (subTotal - item.invoice.discount) - cogs
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
        val productGroups = mutableMapOf<String, MutableList<Pair<String, InvoiceItemEntity>>>() // name -> list of (Type, Item)
        list.forEach { invoiceWithItems ->
            val type = invoiceWithItems.invoice.type
            invoiceWithItems.items.forEach { item ->
                val normalizedName = item.name.trim()
                if (normalizedName.isNotEmpty()) {
                    productGroups.getOrPut(normalizedName) { mutableListOf() }.add(type to item)
                }
            }
        }

        productGroups.map { (name, history) ->
            val purchases = history.filter { it.first == "PURCHASE" }.map { it.second }
            val sales = history.filter { it.first == "SALE" }.map { it.second }

            val qtyPurchased = purchases.sumOf { it.quantity }
            val totalPurchaseValue = purchases.sumOf { it.totalPrice }
            val avgPurchasePrice = if (qtyPurchased > 0) totalPurchaseValue / qtyPurchased else {
                val qtySold = sales.sumOf { it.quantity }
                if (qtySold > 0) sales.sumOf { it.totalPrice } / qtySold else 0.0
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

    fun getNextInvoiceNumber(type: String): String {
        val prefix = if (type == "SALE") "INV-" else "PUR-"
        val existingInvoices = allInvoices.value
        
        var maxNum = 0
        existingInvoices.forEach { item ->
            val numStr = item.invoice.invoiceNumber
            if (numStr.startsWith(prefix, ignoreCase = true)) {
                val suffix = numStr.substring(prefix.length)
                val parsed = suffix.toIntOrNull()
                if (parsed != null && parsed > maxNum) {
                    maxNum = parsed
                }
            }
        }
        val nextNum = maxNum + 1
        return "$prefix${String.format(Locale.US, "%04d", nextNum)}"
    }

    fun updateFormType(type: String) {
        formType.value = type
        formInvoiceNumber.value = if (type == "SALE") getNextInvoiceNumber(type) else ""
    }

    fun prepareNewInvoiceForm() {
        formPartyName.value = ""
        formInvoiceNumber.value = if (formType.value == "SALE") getNextInvoiceNumber(formType.value) else ""
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
        var subTotal = 0.0

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

            val lineTotal = priceVal * qtyVal
            subTotal += lineTotal

            finalItems.add(
                InvoiceItemEntity(
                    invoiceId = 0, // set by repo during save
                    name = itemName,
                    price = priceVal,
                    quantity = qtyVal,
                    totalPrice = lineTotal
                )
            )
        }

        if (finalItems.isEmpty()) {
            formErrorMessage.value = "Please add at least one item line"
            return false
        }

        // Calculations
        val taxAmount = subTotal * (taxPercentage / 100.0)
        val grandTotal = subTotal + taxAmount - discountVal

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
            discount = discountVal,
            tax = taxPercentage,
            totalAmount = grandTotal,
            notes = notes,
            isCreditSale = isCredit,
            outstandingAmount = outstandingAmount,
            dueDate = dueDateVal
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
                val list = repository.allInvoices.first()
                
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
                            val oneDayMs = 24 * 60 * 60 * 1000L
                            if (dueDate - now <= oneDayMs) {
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
                dueDate = 0L
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
    val todaySales: Double = 0.0
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
