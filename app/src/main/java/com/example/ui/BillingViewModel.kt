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
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class BillingScreen {
    DASHBOARD,
    PROFIT_LOSS,
    HISTORY,
    ADD_INVOICE,
    INVOICE_DETAIL
}

data class ProfitLossLedgerEntry(
    val invoiceWithItems: InvoiceWithItems,
    val saleAmount: Double?,
    val purchaseAmount: Double?,
    val runningBalance: Double
)

data class InvoiceItemDraft(
    val name: String = "",
    val price: String = "",
    val quantity: String = "1"
)

class BillingViewModel(private val repository: InvoiceRepository) : ViewModel() {

    // Screen State
    val currentScreen = MutableStateFlow(BillingScreen.DASHBOARD)

    // Filter and Search States
    val searchQuery = MutableStateFlow("")
    val typeFilter = MutableStateFlow("ALL") // "ALL", "SALE", "PURCHASE"

    // Raw invoices flow from repository
    private val rawInvoices = repository.allInvoices

    // Suggestions flows
    val distinctItemNames: StateFlow<List<String>> = repository.distinctItemNames
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val distinctPartyNames: StateFlow<List<String>> = repository.distinctPartyNames
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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

        list.forEach { item ->
            if (item.invoice.type == "SALE") {
                totalSales += item.invoice.totalAmount
                salesCount++
            } else {
                totalPurchases += item.invoice.totalAmount
                purchasesCount++
            }
        }

        DashboardStats(
            totalSales = totalSales,
            totalPurchases = totalPurchases,
            netBalance = totalSales - totalPurchases,
            salesCount = salesCount,
            purchasesCount = purchasesCount
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardStats())

    // Profit and Loss Ledger Flow
    val profitLossLedger: StateFlow<List<ProfitLossLedgerEntry>> = rawInvoices.combine(MutableStateFlow(0)) { list, _ ->
        val chronologicalList = list.sortedBy { it.invoice.date }
        var currentBalance = 0.0
        val entries = chronologicalList.map { item ->
            val amount = item.invoice.totalAmount
            val isSale = item.invoice.type == "SALE"
            if (isSale) {
                currentBalance += amount
            } else {
                currentBalance -= amount
            }
            ProfitLossLedgerEntry(
                invoiceWithItems = item,
                saleAmount = if (isSale) amount else null,
                purchaseAmount = if (!isSale) amount else null,
                runningBalance = currentBalance
            )
        }
        entries.reversed()
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

    // Form validation message
    val formErrorMessage = mutableStateOf<String?>(null)

    fun setScreen(screen: BillingScreen) {
        currentScreen.value = screen
    }

    fun viewInvoiceDetail(invoice: InvoiceWithItems) {
        selectedInvoice.value = invoice
        setScreen(BillingScreen.INVOICE_DETAIL)
    }

    fun prepareNewInvoiceForm() {
        formPartyName.value = ""
        // Auto-generate a simple invoice receipt number based on timestamp
        val sdf = SimpleDateFormat("yyMMdd-HHmmss", Locale.getDefault())
        formInvoiceNumber.value = "INV-${sdf.format(Date())}"
        formDate.value = System.currentTimeMillis()
        formDiscount.value = ""
        formTax.value = ""
        formNotes.value = ""
        formItems.clear()
        formItems.add(InvoiceItemDraft())
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

        val invoiceEntity = InvoiceEntity(
            invoiceNumber = invNo,
            partyName = party,
            type = type,
            date = formDate.value,
            discount = discountVal,
            tax = taxPercentage,
            totalAmount = grandTotal,
            notes = notes
        )

        viewModelScope.launch {
            repository.insertInvoiceWithItems(invoiceEntity, finalItems)
        }

        formErrorMessage.value = null
        setScreen(BillingScreen.HISTORY)
        return true
    }

    fun deleteInvoice(id: Long) {
        viewModelScope.launch {
            repository.deleteInvoice(id)
            if (selectedInvoice.value?.invoice?.id == id) {
                selectedInvoice.value = null
                setScreen(BillingScreen.HISTORY)
            }
        }
    }

    fun getShareableBillText(item: InvoiceWithItems): String {
        val inv = item.invoice
        val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        val typeLabel = if (inv.type == "SALE") "SALE INVOICE" else "PURCHASE BILL"
        
        val sb = StringBuilder()
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
}

data class DashboardStats(
    val totalSales: Double = 0.0,
    val totalPurchases: Double = 0.0,
    val netBalance: Double = 0.0,
    val salesCount: Int = 0,
    val purchasesCount: Int = 0
)

class BillingViewModelFactory(private val repository: InvoiceRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BillingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BillingViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
