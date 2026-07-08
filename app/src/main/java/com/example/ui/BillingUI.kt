package com.example.ui

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.InvoiceWithItems
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillingAppContent(viewModel: BillingViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val selectedInvoice by viewModel.selectedInvoice.collectAsStateWithLifecycle()

    // Handle back button elegantly based on current screen
    BackHandler(enabled = currentScreen != BillingScreen.DASHBOARD) {
        when (currentScreen) {
            BillingScreen.HISTORY -> viewModel.setScreen(BillingScreen.DASHBOARD)
            BillingScreen.PROFIT_LOSS -> viewModel.setScreen(BillingScreen.DASHBOARD)
            BillingScreen.ADD_INVOICE -> viewModel.setScreen(BillingScreen.DASHBOARD)
            BillingScreen.INVOICE_DETAIL -> viewModel.setScreen(BillingScreen.HISTORY)
            else -> viewModel.setScreen(BillingScreen.DASHBOARD)
        }
    }

    Scaffold(
        bottomBar = {
            if (currentScreen == BillingScreen.DASHBOARD || currentScreen == BillingScreen.HISTORY || currentScreen == BillingScreen.PROFIT_LOSS) {
                NavigationBar(
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    NavigationBarItem(
                        selected = currentScreen == BillingScreen.DASHBOARD,
                        onClick = { viewModel.setScreen(BillingScreen.DASHBOARD) },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Dashboard") },
                        label = { Text("Dashboard") },
                        modifier = Modifier.testTag("nav_dashboard")
                    )
                    NavigationBarItem(
                        selected = currentScreen == BillingScreen.PROFIT_LOSS,
                        onClick = { viewModel.setScreen(BillingScreen.PROFIT_LOSS) },
                        icon = { Icon(Icons.Default.ShowChart, contentDescription = "Profit/Loss") },
                        label = { Text("Profit/Loss") },
                        modifier = Modifier.testTag("nav_profit_loss")
                    )
                    NavigationBarItem(
                        selected = currentScreen == BillingScreen.HISTORY,
                        onClick = { viewModel.setScreen(BillingScreen.HISTORY) },
                        icon = { Icon(Icons.Default.List, contentDescription = "Transactions") },
                        label = { Text("History") },
                        modifier = Modifier.testTag("nav_history")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                BillingScreen.DASHBOARD -> DashboardScreen(viewModel)
                BillingScreen.PROFIT_LOSS -> ProfitLossScreen(viewModel)
                BillingScreen.HISTORY -> HistoryScreen(viewModel)
                BillingScreen.ADD_INVOICE -> AddInvoiceScreen(viewModel)
                BillingScreen.INVOICE_DETAIL -> selectedInvoice?.let {
                    InvoiceDetailScreen(viewModel, it)
                } ?: viewModel.setScreen(BillingScreen.DASHBOARD)
            }
        }
    }
}

@Composable
fun DashboardScreen(viewModel: BillingViewModel) {
    val stats by viewModel.dashboardStats.collectAsStateWithLifecycle()
    val recentInvoices by viewModel.invoices.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // App Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp, top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0D0E10)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.ic_vm_book_logo),
                    contentDescription = "VM BOOK App Icon",
                    modifier = Modifier.size(38.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "VM BOOK",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Manage your sales & purchases offline",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Net Flow Banner
        val balanceColor = if (stats.netBalance >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (stats.netBalance >= 0) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "NET CASH BALANCE",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (stats.netBalance >= 0) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "₹${String.format(Locale.US, "%.2f", stats.netBalance)}",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Black,
                    color = balanceColor
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Total Sales minus Total Purchases",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (stats.netBalance >= 0) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
                )
            }
        }

        // Summary Cards Grid
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Sales Summary
            Card(
                modifier = Modifier
                    .weight(1.0f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE2F3EB)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.TrendingUp,
                                contentDescription = "Sales",
                                tint = Color(0xFF0F9D58),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Text(
                            text = "${stats.salesCount} Bills",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Total Sales",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "₹${String.format(Locale.US, "%.2f", stats.totalSales)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F9D58)
                    )
                }
            }

            // Purchases Summary
            Card(
                modifier = Modifier
                    .weight(1.0f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE3F2FD)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.TrendingDown,
                                contentDescription = "Purchases",
                                tint = Color(0xFF1E88E5),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Text(
                            text = "${stats.purchasesCount} Bills",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Total Purchases",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "₹${String.format(Locale.US, "%.2f", stats.totalPurchases)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E88E5)
                    )
                }
            }
        }

        // Quick Actions
        Text(
            text = "QUICK TRANSACTIONS",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = {
                    viewModel.formType.value = "SALE"
                    viewModel.prepareNewInvoiceForm()
                },
                modifier = Modifier
                    .weight(1f)
                    .height(54.dp)
                    .testTag("action_new_sale"),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F9D58)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("New Sale", fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = {
                    viewModel.formType.value = "PURCHASE"
                    viewModel.prepareNewInvoiceForm()
                },
                modifier = Modifier
                    .weight(1f)
                    .height(54.dp)
                    .testTag("action_new_purchase"),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("New Purchase", fontWeight = FontWeight.Bold)
            }
        }

        // Recent Invoices Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "RECENT BILLS",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TextButton(onClick = { viewModel.setScreen(BillingScreen.HISTORY) }) {
                Text("See All", fontWeight = FontWeight.Bold)
            }
        }

        // Recent Invoices List
        if (recentInvoices.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant,
                        RoundedCornerShape(16.dp)
                    )
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.ReceiptLong,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No bills saved yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                recentInvoices.take(4).forEach { item ->
                    InvoiceCompactRow(item = item, onClick = { viewModel.viewInvoiceDetail(item) })
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun HistoryScreen(viewModel: BillingViewModel) {
    val invoices by viewModel.invoices.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val typeFilter by viewModel.typeFilter.collectAsStateWithLifecycle()

    var showDeleteConfirmation by remember { mutableStateOf<Long?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Page Title
        Text(
            text = "Transaction History",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.searchQuery.value = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .testTag("history_search"),
            placeholder = { Text("Search by customer, supplier, bill #...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear search")
                    }
                }
            },
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        // Filter Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val filters = listOf("ALL" to "All", "SALE" to "Sales Only", "PURCHASE" to "Purchases Only")
            filters.forEach { (key, label) ->
                val selected = typeFilter == key
                FilterChip(
                    selected = selected,
                    onClick = { viewModel.typeFilter.value = key },
                    label = { Text(label, fontWeight = FontWeight.SemiBold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = if (key == "SALE") Color(0xFFE2F3EB) else if (key == "PURCHASE") Color(0xFFE3F2FD) else MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = if (key == "SALE") Color(0xFF0F9D58) else if (key == "PURCHASE") Color(0xFF1E88E5) else MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
            }
        }

        // List of Transactions
        if (invoices.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.0f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.SearchOff,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (searchQuery.isEmpty()) "No invoices found" else "No matching invoices found",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Try modifying your filters or search text",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.0f)
            ) {
                items(invoices, key = { it.invoice.id }) { item ->
                    InvoiceCompactRow(
                        item = item,
                        onClick = { viewModel.viewInvoiceDetail(item) },
                        onDeleteClick = { showDeleteConfirmation = item.invoice.id }
                    )
                }
            }
        }
    }

    // Delete confirmation dialog
    if (showDeleteConfirmation != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = null },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmation?.let { viewModel.deleteInvoice(it) }
                        showDeleteConfirmation = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = null }) {
                    Text("Cancel")
                }
            },
            title = { Text("Delete Bill?") },
            text = { Text("Are you sure you want to permanently delete this billing record? This action cannot be undone.") }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddInvoiceScreen(viewModel: BillingViewModel) {
    val partyNameSuggestions by viewModel.distinctPartyNames.collectAsStateWithLifecycle()
    val itemNameSuggestions by viewModel.distinctItemNames.collectAsStateWithLifecycle()

    val currentType = viewModel.formType.value
    val invoiceError by viewModel.formErrorMessage

    val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.setScreen(BillingScreen.DASHBOARD) }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go back")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (currentType == "SALE") "New Sale Invoice" else "New Purchase Bill",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        // Scrollable Form
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            // Type Toggle Selector
            Text(
                text = "BILL TYPE",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(if (currentType == "SALE") Color(0xFF0F9D58) else Color.Transparent)
                        .clickable { viewModel.formType.value = "SALE" }
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "SALE / CASH RECEIPT",
                        fontWeight = FontWeight.Bold,
                        color = if (currentType == "SALE") Color.White else MaterialTheme.colorScheme.onSurface
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(if (currentType == "PURCHASE") Color(0xFF1E88E5) else Color.Transparent)
                        .clickable { viewModel.formType.value = "PURCHASE" }
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "PURCHASE / EXPENSE",
                        fontWeight = FontWeight.Bold,
                        color = if (currentType == "PURCHASE") Color.White else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Error Bar
            if (invoiceError != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = "Error",
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = invoiceError ?: "",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // Party Name Input
            Text(
                text = if (currentType == "SALE") "CUSTOMER NAME" else "VENDOR / SUPPLIER NAME",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            // Auto-suggestion horizontal chips for Party Name
            val filteredPartySuggestions = partyNameSuggestions.filter {
                it.contains(viewModel.formPartyName.value, ignoreCase = true) && it != viewModel.formPartyName.value
            }
            if (filteredPartySuggestions.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    filteredPartySuggestions.forEach { name ->
                        SuggestionChip(
                            onClick = { viewModel.formPartyName.value = name },
                            label = { Text(name) }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = viewModel.formPartyName.value,
                onValueChange = { viewModel.formPartyName.value = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .testTag("form_party_name"),
                placeholder = { Text(if (currentType == "SALE") "Enter Customer Name" else "Enter Supplier Name") },
                leadingIcon = { Icon(Icons.Default.AccountCircle, contentDescription = null) },
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                singleLine = true
            )

            // Invoice No & Date Grid
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = viewModel.formInvoiceNumber.value,
                    onValueChange = { viewModel.formInvoiceNumber.value = it },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("form_invoice_number"),
                    label = { Text("Bill / Invoice No") },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = sdf.format(Date(viewModel.formDate.value)),
                    onValueChange = {},
                    modifier = Modifier.weight(1.2f),
                    label = { Text("Date & Time") },
                    shape = RoundedCornerShape(12.dp),
                    readOnly = true,
                    leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) }
                )
            }

            // Items List Title
            Divider(modifier = Modifier.padding(vertical = 12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ITEM LINES",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(
                    onClick = { viewModel.addDraftItem() },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    modifier = Modifier.testTag("form_add_item")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Line", fontWeight = FontWeight.Bold)
                }
            }

            // Interactive Dynamic Item Lines
            viewModel.formItems.forEachIndexed { index, draftItem ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        // Header line with item number & delete button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Item #${index + 1}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (viewModel.formItems.size > 1) {
                                IconButton(
                                    onClick = { viewModel.removeDraftItem(index) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Delete item line",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        // Item name input
                        // Show horizontal past item suggestions if matching
                        val filteredItemSuggestions = itemNameSuggestions.filter {
                            it.contains(draftItem.name, ignoreCase = true) && it != draftItem.name
                        }
                        if (filteredItemSuggestions.isNotEmpty()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                filteredItemSuggestions.forEach { name ->
                                    SuggestionChip(
                                        onClick = {
                                            viewModel.updateDraftItem(index, draftItem.copy(name = name))
                                        },
                                        label = { Text(name, fontSize = 11.sp) }
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = draftItem.name,
                            onValueChange = {
                                viewModel.updateDraftItem(index, draftItem.copy(name = it))
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .testTag("item_name_$index"),
                            placeholder = { Text("Item / Service Name") },
                            shape = RoundedCornerShape(8.dp),
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                            singleLine = true
                        )

                        // Quantity & Price Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = draftItem.price,
                                onValueChange = {
                                    viewModel.updateDraftItem(index, draftItem.copy(price = it))
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("item_price_$index"),
                                label = { Text("Price (₹)") },
                                placeholder = { Text("0.00") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = draftItem.quantity,
                                onValueChange = {
                                    viewModel.updateDraftItem(index, draftItem.copy(quantity = it))
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("item_qty_$index"),
                                label = { Text("Qty") },
                                placeholder = { Text("1") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true
                            )

                            // Quick sum display
                            val priceVal = draftItem.price.toDoubleOrNull() ?: 0.0
                            val qtyVal = draftItem.quantity.toDoubleOrNull() ?: 0.0
                            val lineTotal = priceVal * qtyVal
                            Column(
                                modifier = Modifier
                                    .weight(0.8f)
                                    .align(Alignment.CenterVertically),
                                horizontalAlignment = Alignment.End
                            ) {
                                Text(
                                    "Total",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    "₹${String.format(Locale.US, "%.2f", lineTotal)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            // Calculations panel
            Divider(modifier = Modifier.padding(vertical = 12.dp))
            Text(
                text = "BILL SUMMARY",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Subtotal Calculation in real time
            val subTotal = viewModel.formItems.sumOf { draft ->
                val p = draft.price.toDoubleOrNull() ?: 0.0
                val q = draft.quantity.toDoubleOrNull() ?: 0.0
                p * q
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Subtotal Items Value", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("₹${String.format(Locale.US, "%.2f", subTotal)}", fontWeight = FontWeight.Medium)
            }

            // Tax and Discount Rows
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = viewModel.formTax.value,
                    onValueChange = { viewModel.formTax.value = it },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("form_tax"),
                    label = { Text("Tax %") },
                    placeholder = { Text("0") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = viewModel.formDiscount.value,
                    onValueChange = { viewModel.formDiscount.value = it },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("form_discount"),
                    label = { Text("Flat Discount (₹)") },
                    placeholder = { Text("0.00") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            // Note Pad
            OutlinedTextField(
                value = viewModel.formNotes.value,
                onValueChange = { viewModel.formNotes.value = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .testTag("form_notes"),
                label = { Text("Additional Notes / Remarks (Optional)") },
                shape = RoundedCornerShape(12.dp),
                maxLines = 3
            )

            // Grand Total preview
            val taxPercentage = viewModel.formTax.value.toDoubleOrNull() ?: 0.0
            val discountVal = viewModel.formDiscount.value.toDoubleOrNull() ?: 0.0
            val taxAmount = subTotal * (taxPercentage / 100.0)
            val grandTotal = (subTotal + taxAmount - discountVal).coerceAtLeast(0.0)

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "ESTIMATED GRAND TOTAL",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        "₹${String.format(Locale.US, "%.2f", grandTotal)}",
                        fontWeight = FontWeight.Black,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }

        // Action Save / Cancel
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = { viewModel.setScreen(BillingScreen.DASHBOARD) },
                modifier = Modifier
                    .weight(0.8f)
                    .height(54.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Cancel", fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = { viewModel.saveInvoice() },
                modifier = Modifier
                    .weight(1.2f)
                    .height(54.dp)
                    .testTag("form_save_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (currentType == "SALE") Color(0xFF0F9D58) else Color(0xFF1E88E5)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Bill", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun InvoiceDetailScreen(viewModel: BillingViewModel, item: InvoiceWithItems) {
    val context = LocalContext.current
    val inv = item.invoice
    val sdf = SimpleDateFormat("dd MMMM yyyy, hh:mm a", Locale.getDefault())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { viewModel.setScreen(BillingScreen.HISTORY) }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to history")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (inv.type == "SALE") "Sale Invoice" else "Purchase Bill",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            // Quick Delete Button
            var showDeleteCheck by remember { mutableStateOf(false) }
            IconButton(onClick = { showDeleteCheck = true }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete bill", tint = MaterialTheme.colorScheme.error)
            }

            if (showDeleteCheck) {
                AlertDialog(
                    onDismissRequest = { showDeleteCheck = false },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                viewModel.deleteInvoice(inv.id)
                                showDeleteCheck = false
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Delete Record", fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteCheck = false }) {
                            Text("Cancel")
                        }
                    },
                    title = { Text("Delete Billing Record?") },
                    text = { Text("Are you sure you want to permanently delete this billing record? This is irreversible.") }
                )
            }
        }

        // Receipt Card Visual Display
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.0f)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Retail Slip Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(
                            text = if (inv.type == "SALE") "CASH INVOICE" else "PURCHASE RECORD",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Black,
                            color = if (inv.type == "SALE") Color(0xFF0F9D58) else Color(0xFF1E88E5)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "No. ${inv.invoiceNumber}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Simple stamp badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (inv.type == "SALE") Color(0xFFE2F3EB) else Color(0xFFE3F2FD))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = inv.type,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (inv.type == "SALE") Color(0xFF0F9D58) else Color(0xFF1E88E5)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Date & Party Box
                Text(
                    text = "DATE & TIME",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = sdf.format(Date(inv.date)),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Text(
                    text = if (inv.type == "SALE") "CUSTOMER DETAILS" else "VENDOR DETAILS",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                ) {
                    Icon(
                        Icons.Default.AccountBox,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = inv.partyName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Dotted Separator
                DashedLine(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp))

                // Itemized Items Table Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "ITEM LINES",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1.8f)
                    )
                    Text(
                        "QTY",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(0.6f),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        "RATE",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1.0f),
                        textAlign = TextAlign.End
                    )
                    Text(
                        "TOTAL",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1.1f),
                        textAlign = TextAlign.End
                    )
                }

                Divider(modifier = Modifier.padding(bottom = 8.dp))

                // Items list
                var subtotalVal = 0.0
                item.items.forEach { line ->
                    subtotalVal += line.totalPrice
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = line.name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1.8f)
                        )
                        val formattedQty = if (line.quantity % 1 == 0.0) line.quantity.toInt().toString() else line.quantity.toString()
                        Text(
                            text = formattedQty,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(0.6f),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "₹${String.format(Locale.US, "%.2f", line.price)}",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1.0f),
                            textAlign = TextAlign.End
                        )
                        Text(
                            text = "₹${String.format(Locale.US, "%.2f", line.totalPrice)}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1.1f),
                            textAlign = TextAlign.End
                        )
                    }
                }

                // Financial Calculations
                DashedLine(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Subtotal", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("₹${String.format(Locale.US, "%.2f", subtotalVal)}")
                }

                if (inv.tax > 0) {
                    val taxAmount = subtotalVal * (inv.tax / 100.0)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Tax (${inv.tax}%)", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("₹${String.format(Locale.US, "%.2f", taxAmount)}")
                    }
                }

                if (inv.discount > 0) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Discount", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("-₹${String.format(Locale.US, "%.2f", inv.discount)}")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "GRAND TOTAL",
                        fontWeight = FontWeight.Black,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        "₹${String.format(Locale.US, "%.2f", inv.totalAmount)}",
                        fontWeight = FontWeight.Black,
                        style = MaterialTheme.typography.headlineSmall,
                        color = if (inv.type == "SALE") Color(0xFF0F9D58) else Color(0xFF1E88E5)
                    )
                }

                // Dotted Separator
                DashedLine(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp))

                // Notes Box
                if (inv.notes.isNotEmpty()) {
                    Text(
                        text = "REMARKS / MEMO",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = inv.notes,
                            style = MaterialTheme.typography.bodyMedium,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
                }
            }
        }

        // Quick Sharing Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = {
                    val shareText = viewModel.getShareableBillText(item)
                    val intent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, shareText)
                        type = "text/plain"
                    }
                    val shareIntent = Intent.createChooser(intent, "Share Invoice Bill Text")
                    context.startActivity(shareIntent)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("action_share_invoice"),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Share, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Share Bill Receipt", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun InvoiceCompactRow(
    item: InvoiceWithItems,
    onClick: () -> Unit,
    onDeleteClick: (() -> Unit)? = null
) {
    val inv = item.invoice
    val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())

    val accentColor = if (inv.type == "SALE") Color(0xFF0F9D58) else Color(0xFF1E88E5)
    val backgroundContainerColor = if (inv.type == "SALE") Color(0xFFE2F3EB) else Color(0xFFE3F2FD)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("invoice_row_${inv.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Type Circle Badge
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(backgroundContainerColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (inv.type == "SALE") Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                        contentDescription = inv.type,
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = inv.partyName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = inv.invoiceNumber,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = " • ",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = sdf.format(Date(inv.date)),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "₹${String.format(Locale.US, "%.2f", inv.totalAmount)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = accentColor,
                    textAlign = TextAlign.End
                )

                if (onDeleteClick != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete record",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DashedLine(modifier: Modifier = Modifier, color: Color = Color.Gray.copy(alpha = 0.5f)) {
    Canvas(modifier = modifier.height(1.dp)) {
        val pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
        drawLine(
            color = color,
            start = Offset(0f, 0f),
            end = Offset(size.width, 0f),
            pathEffect = pathEffect,
            strokeWidth = 2f
        )
    }
}

@Composable
fun ProfitLossScreen(viewModel: BillingViewModel) {
    val stats by viewModel.dashboardStats.collectAsStateWithLifecycle()
    val ledgerEntries by viewModel.profitLossLedger.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Page Title
        Text(
            text = "Profit & Loss Statement",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = "Real-time ledger tracking margins and balances",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        // Summary Card
        val isProfit = stats.netBalance >= 0
        val profitMargin = if (stats.totalSales > 0) {
            (stats.netBalance / stats.totalSales) * 100.0
        } else {
            0.0
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isProfit) Color(0xFFE2F3EB) else Color(0xFFFCE8E6)
            ),
            border = BorderStroke(
                1.dp,
                if (isProfit) Color(0xFF0F9D58).copy(alpha = 0.3f) else Color(0xFFD93025).copy(alpha = 0.3f)
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isProfit) "NET PROFIT" else "NET LOSS",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isProfit) Color(0xFF0F9D58) else Color(0xFFD93025)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "₹${String.format(Locale.US, "%.2f", stats.netBalance)}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            color = if (isProfit) Color(0xFF0F9D58) else Color(0xFFD93025)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isProfit) Color(0xFF0F9D58) else Color(0xFFD93025))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Margin: ${String.format(Locale.US, "%.1f", profitMargin)}%",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = (if (isProfit) Color(0xFF0F9D58) else Color(0xFFD93025)).copy(alpha = 0.15f))
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Total Sales (+)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "₹${String.format(Locale.US, "%.2f", stats.totalSales)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F9D58)
                        )
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "Total Purchases (-)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "₹${String.format(Locale.US, "%.2f", stats.totalPurchases)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E88E5)
                        )
                    }
                }
            }
        }

        // Ledger Columns Header
        Text(
            text = "PROFIT & LOSS LEDGER COLUMN",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Ledger Table/Columns Header Row
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Party / Particular",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1.8f)
                )
                Text(
                    text = "Sale (+)",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1.1f),
                    color = Color(0xFF0F9D58)
                )
                Text(
                    text = "Purchase (-)",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1.1f),
                    color = Color(0xFF1E88E5)
                )
                Text(
                    text = "Net P&L",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1.1f)
                )
            }
        }

        // Ledger List
        if (ledgerEntries.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant,
                        RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp)
                    )
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.ShowChart,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No sales or purchases recorded yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant,
                        RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp)
                    )
            ) {
                items(ledgerEntries) { entry ->
                    val inv = entry.invoiceWithItems.invoice
                    val sdf = SimpleDateFormat("dd MMM yy", Locale.getDefault())

                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.viewInvoiceDetail(entry.invoiceWithItems) }
                                .padding(horizontal = 12.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Column 1: Party & Date
                            Column(modifier = Modifier.weight(1.8f)) {
                                Text(
                                    text = inv.partyName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = sdf.format(Date(inv.date)),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = " • ",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = inv.invoiceNumber,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            // Column 2: Sale (+)
                            val isSale = entry.saleAmount != null
                            Text(
                                text = if (isSale) "₹${String.format(Locale.US, "%.0f", entry.saleAmount)}" else "—",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSale) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSale) Color(0xFF0F9D58) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                textAlign = TextAlign.End,
                                modifier = Modifier.weight(1.1f)
                            )

                            // Column 3: Purchase (-)
                            val isPurchase = entry.purchaseAmount != null
                            Text(
                                text = if (isPurchase) "₹${String.format(Locale.US, "%.0f", entry.purchaseAmount)}" else "—",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isPurchase) FontWeight.Bold else FontWeight.Normal,
                                color = if (isPurchase) Color(0xFF1E88E5) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                textAlign = TextAlign.End,
                                modifier = Modifier.weight(1.1f)
                            )

                            // Column 4: Running Net Balance (Net P&L up to this date)
                            val entryProfit = entry.runningBalance >= 0
                            Text(
                                text = "₹${String.format(Locale.US, "%.0f", entry.runningBalance)}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (entryProfit) Color(0xFF0F9D58) else Color(0xFFD93025),
                                textAlign = TextAlign.End,
                                modifier = Modifier.weight(1.1f)
                            )
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

