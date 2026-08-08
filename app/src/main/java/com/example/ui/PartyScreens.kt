package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.CustomerEntity
import com.example.data.CustomerPaymentEntity
import com.example.data.SupplierEntity
import com.example.data.SupplierPaymentEntity
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.launch

// ==========================================
// ==========================================
// CUSTOMER MASTER MODULE
// ==========================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartiesScreen(viewModel: BillingViewModel) {
    var activeTab by remember { mutableStateOf(0) } // 0 = Customers, 1 = Suppliers

    val customers by viewModel.customersWithBalance.collectAsStateWithLifecycle()
    val customerQuery by viewModel.customerSearchQuery.collectAsStateWithLifecycle()

    val suppliers by viewModel.suppliersWithBalance.collectAsStateWithLifecycle()
    val supplierQuery by viewModel.supplierSearchQuery.collectAsStateWithLifecycle()

    var showAddCustomerDialog by remember { mutableStateOf(false) }
    var editingCustomer by remember { mutableStateOf<CustomerEntity?>(null) }
    var customerToDelete by remember { mutableStateOf<CustomerEntity?>(null) }

    var showAddSupplierDialog by remember { mutableStateOf(false) }
    var editingSupplier by remember { mutableStateOf<SupplierEntity?>(null) }
    var supplierToDelete by remember { mutableStateOf<SupplierEntity?>(null) }
    var showDeleteLockVerify by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Parties", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(
                        onClick = {
                            if (activeTab == 0) {
                                showAddCustomerDialog = true
                            } else {
                                showAddSupplierDialog = true
                            }
                        },
                        modifier = Modifier.testTag(if (activeTab == 0) "add_customer_button" else "add_supplier_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Party")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Material 3 Primary TabRow for Customers / Suppliers
            TabRow(
                selectedTabIndex = activeTab,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    text = { 
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.People, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Customers", fontWeight = FontWeight.SemiBold)
                        }
                    },
                    modifier = Modifier.testTag("parties_tab_customers")
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = { 
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Business, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Suppliers", fontWeight = FontWeight.SemiBold)
                        }
                    },
                    modifier = Modifier.testTag("parties_tab_suppliers")
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                if (activeTab == 0) {
                    // Search Input
                    OutlinedTextField(
                        value = customerQuery,
                        onValueChange = { viewModel.customerSearchQuery.value = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                            .testTag("customer_search"),
                        placeholder = { Text("Search by name or phone...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    if (customers.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.People,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    "No Customers Found",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    "Tap the '+' icon at the top right to add a customer.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(customers, key = { it.customer.id }) { item ->
                                CustomerCard(
                                    item = item,
                                    onCardClick = {
                                        viewModel.selectedCustomerId.value = item.customer.id
                                        viewModel.setScreen(BillingScreen.CUSTOMER_LEDGER)
                                    },
                                    onEditClick = { editingCustomer = item.customer },
                                    onDeleteClick = { customerToDelete = item.customer }
                                )
                            }
                        }
                    }
                } else {
                    // Search Input
                    OutlinedTextField(
                        value = supplierQuery,
                        onValueChange = { viewModel.supplierSearchQuery.value = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                            .testTag("supplier_search"),
                        placeholder = { Text("Search by name or phone...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    if (suppliers.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.Business,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    "No Suppliers Found",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    "Tap the '+' icon at the top right to add a supplier.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(suppliers, key = { it.supplier.id }) { item ->
                                SupplierCard(
                                    item = item,
                                    onCardClick = {
                                        viewModel.selectedSupplierId.value = item.supplier.id
                                        viewModel.setScreen(BillingScreen.SUPPLIER_LEDGER)
                                    },
                                    onEditClick = { editingSupplier = item.supplier },
                                    onDeleteClick = { supplierToDelete = item.supplier }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Customer Add/Edit Dialog
    if (showAddCustomerDialog || editingCustomer != null) {
        CustomerFormDialog(
            customer = editingCustomer,
            onDismiss = {
                showAddCustomerDialog = false
                editingCustomer = null
            },
            onSave = { name, phone, email, address, notes ->
                val newCustomer = CustomerEntity(
                    id = editingCustomer?.id ?: 0,
                    name = name,
                    phone = phone,
                    email = email,
                    address = address,
                    notes = notes
                )
                viewModel.addCustomer(newCustomer)
                showAddCustomerDialog = false
                editingCustomer = null
            }
        )
    }

    // Customer Delete Confirmation Dialog
    if (customerToDelete != null) {
        AlertDialog(
            onDismissRequest = { customerToDelete = null },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (viewModel.isTransactionSecurityEnabled()) {
                            showDeleteLockVerify = true
                        } else {
                            customerToDelete?.let { viewModel.deleteCustomer(it.id) }
                            customerToDelete = null
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { customerToDelete = null }) {
                    Text("Cancel")
                }
            },
            title = { Text("Delete Customer?") },
            text = { Text("Are you sure you want to delete '${customerToDelete?.name}'? This will remove their record from the master list.") }
        )
    }

    // Supplier Add/Edit Dialog
    if (showAddSupplierDialog || editingSupplier != null) {
        SupplierFormDialog(
            supplier = editingSupplier,
            onDismiss = {
                showAddSupplierDialog = false
                editingSupplier = null
            },
            onSave = { name, phone, email, address, notes ->
                val newSupplier = SupplierEntity(
                    id = editingSupplier?.id ?: 0,
                    name = name,
                    phone = phone,
                    email = email,
                    address = address,
                    notes = notes
                )
                viewModel.addSupplier(newSupplier)
                showAddSupplierDialog = false
                editingSupplier = null
            }
        )
    }

    // Supplier Delete Confirmation Dialog
    if (supplierToDelete != null) {
        AlertDialog(
            onDismissRequest = { supplierToDelete = null },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (viewModel.isTransactionSecurityEnabled()) {
                            showDeleteLockVerify = true
                        } else {
                            supplierToDelete?.let { viewModel.deleteSupplier(it.id) }
                            supplierToDelete = null
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { supplierToDelete = null }) {
                    Text("Cancel")
                }
            },
            title = { Text("Delete Supplier?") },
            text = { Text("Are you sure you want to delete '${supplierToDelete?.name}'? This will remove their record from the master list.") }
        )
    }

    if (showDeleteLockVerify) {
        PasswordVerificationDialog(
            viewModel = viewModel,
            onVerified = {
                showDeleteLockVerify = false
                customerToDelete?.let { viewModel.deleteCustomer(it.id) }
                customerToDelete = null
                supplierToDelete?.let { viewModel.deleteSupplier(it.id) }
                supplierToDelete = null
            },
            onDismiss = {
                showDeleteLockVerify = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomersScreen(viewModel: BillingViewModel) {
    val customers by viewModel.customersWithBalance.collectAsStateWithLifecycle()
    val searchQuery by viewModel.customerSearchQuery.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var editingCustomer by remember { mutableStateOf<CustomerEntity?>(null) }
    var customerToDelete by remember { mutableStateOf<CustomerEntity?>(null) }
    var showDeleteLockVerify by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Customers", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(
                        onClick = { showAddDialog = true },
                        modifier = Modifier.testTag("add_customer_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Customer")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.customerSearchQuery.value = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .testTag("customer_search"),
                placeholder = { Text("Search by name or phone...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            if (customers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.People,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "No Customers Found",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "Tap the '+' icon at the top right to add a customer.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(customers, key = { it.customer.id }) { item ->
                        CustomerCard(
                            item = item,
                            onCardClick = {
                                viewModel.selectedCustomerId.value = item.customer.id
                                viewModel.setScreen(BillingScreen.CUSTOMER_LEDGER)
                            },
                            onEditClick = { editingCustomer = item.customer },
                            onDeleteClick = { customerToDelete = item.customer }
                        )
                    }
                }
            }
        }
    }

    // Add/Edit Dialog
    if (showAddDialog || editingCustomer != null) {
        CustomerFormDialog(
            customer = editingCustomer,
            onDismiss = {
                showAddDialog = false
                editingCustomer = null
            },
            onSave = { name, phone, email, address, notes ->
                val newCustomer = CustomerEntity(
                    id = editingCustomer?.id ?: 0,
                    name = name,
                    phone = phone,
                    email = email,
                    address = address,
                    notes = notes
                )
                viewModel.addCustomer(newCustomer)
                showAddDialog = false
                editingCustomer = null
            }
        )
    }

    // Delete Confirmation Dialog
    if (customerToDelete != null) {
        AlertDialog(
            onDismissRequest = { customerToDelete = null },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (viewModel.isTransactionSecurityEnabled()) {
                            showDeleteLockVerify = true
                        } else {
                            customerToDelete?.let { viewModel.deleteCustomer(it.id) }
                            customerToDelete = null
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { customerToDelete = null }) {
                    Text("Cancel")
                }
            },
            title = { Text("Delete Customer?") },
            text = { Text("Are you sure you want to delete '${customerToDelete?.name}'? This will remove their record from the master list.") }
        )
    }

    if (showDeleteLockVerify) {
        PasswordVerificationDialog(
            viewModel = viewModel,
            onVerified = {
                showDeleteLockVerify = false
                customerToDelete?.let { viewModel.deleteCustomer(it.id) }
                customerToDelete = null
            },
            onDismiss = {
                showDeleteLockVerify = false
            }
        )
    }
}

@Composable
fun CustomerCard(
    item: CustomerWithBalance,
    onCardClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() }
            .testTag("customer_card_${item.customer.id}"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.customer.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (item.customer.phone.isNotEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Icon(
                                Icons.Default.Phone,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = item.customer.phone,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Outstanding Balance Display
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "Outstanding",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "₹${String.format(Locale.US, "%.2f", item.outstandingBalance)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (item.outstandingBalance > 0) Color(0xFFD32F2F) else Color(0xFF388E3C)
                    )
                }
            }

            if (item.customer.address.isNotEmpty()) {
                Text(
                    text = "Addr: ${item.customer.address}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // View Ledger link
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onCardClick() }
                ) {
                    Text(
                        "View Ledger & Payments",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Icon(
                        Icons.Default.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                // Action Buttons
                Row {
                    IconButton(onClick = onEditClick, modifier = Modifier.size(36.dp)) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onDeleteClick, modifier = Modifier.size(36.dp)) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CustomerFormDialog(
    customer: CustomerEntity?,
    onDismiss: () -> Unit,
    onSave: (name: String, phone: String, email: String, address: String, notes: String) -> Unit
) {
    var name by remember { mutableStateOf(customer?.name ?: "") }
    var phone by remember { mutableStateOf(customer?.phone ?: "") }
    var email by remember { mutableStateOf(customer?.email ?: "") }
    var address by remember { mutableStateOf(customer?.address ?: "") }
    var notes by remember { mutableStateOf(customer?.notes ?: "") }

    var showError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        showError = true
                    } else {
                        onSave(name, phone, email, address, notes)
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        title = { Text(if (customer == null) "Add Customer" else "Edit Customer") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (showError) {
                    Text(
                        "Customer Name is required!",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        showError = false
                    },
                    label = { Text("Customer Name *") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    singleLine = true
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Address") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Internal Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )
            }
        }
    )
}

// ==========================================
// CUSTOMER LEDGER, STATEMENT, & PAYMENTS
// ==========================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerLedgerScreen(viewModel: BillingViewModel) {
    val customer by viewModel.selectedCustomer.collectAsStateWithLifecycle()
    val ledgerEntries by viewModel.selectedCustomerLedger.collectAsStateWithLifecycle()

    var activeTab by remember { mutableStateOf(0) } // 0: Statement/Ledger, 1: Payment History
    var showReceivedDialog by remember { mutableStateOf(false) }
    var showGivenDialog by remember { mutableStateOf(false) }
    var paymentToDelete by remember { mutableStateOf<CustomerLedgerEntry?>(null) }
    var showDeleteLockVerify by remember { mutableStateOf(false) }

    val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())

    if (customer == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val totalSales = ledgerEntries.filter { it.type == "SALE" }.sumOf { it.debit }
    val totalPayments = ledgerEntries.filter { it.type == "PAYMENT" }.sumOf { it.credit }
    val outstandingBalance = totalSales - totalPayments

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(customer?.name ?: "", fontWeight = FontWeight.Bold)
                        if (customer?.phone?.isNotEmpty() == true) {
                            Text(customer?.phone ?: "", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.goBack() },
                        modifier = Modifier.testTag("btn_back_customer_ledger")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            // Fixed Bottom Action Bar for Khata Book style
            Surface(
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 8.dp,
                shadowElevation = 16.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Given (Red ↑)
                    Button(
                        onClick = { showGivenDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("customer_given_btn"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFD32F2F),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(16.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowUpward,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Given ₹", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    // Received (Green ↓)
                    Button(
                        onClick = { showReceivedDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("customer_received_btn"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0F9D58),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(16.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowDownward,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Received ₹", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // Redesigned Running Balance top card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                border = BorderStroke(
                    width = 1.5.dp,
                    color = when {
                        outstandingBalance > 0 -> Color(0xFF0F9D58).copy(alpha = 0.4f)
                        outstandingBalance < 0 -> Color(0xFFD32F2F).copy(alpha = 0.4f)
                        else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
                    }
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = when {
                            outstandingBalance > 0 -> "YOU'LL RECEIVE"
                            outstandingBalance < 0 -> "YOU OWE"
                            else -> "SETTLED"
                        },
                        style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.5.sp),
                        fontWeight = FontWeight.Bold,
                        color = when {
                            outstandingBalance > 0 -> Color(0xFF0F9D58)
                            outstandingBalance < 0 -> Color(0xFFD32F2F)
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "₹${String.format(Locale.US, "%.2f", Math.abs(outstandingBalance))}",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = when {
                            outstandingBalance > 0 -> Color(0xFF0F9D58)
                            outstandingBalance < 0 -> Color(0xFFD32F2F)
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f))
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Total Sales", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                "₹${String.format(Locale.US, "%.2f", totalSales)}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Box(
                            modifier = Modifier
                                .height(28.dp)
                                .width(1.dp)
                                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Total Received", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                "₹${String.format(Locale.US, "%.2f", totalPayments)}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F9D58)
                            )
                        }
                    }
                }
            }

            if (outstandingBalance > 0) {
                val context = androidx.compose.ui.platform.LocalContext.current
                val businessProfileState by viewModel.businessProfile.collectAsStateWithLifecycle()
                val firmName = businessProfileState?.firmName?.takeIf { it.isNotEmpty() } ?: "VM Book"
                
                Button(
                    onClick = {
                        val message = """
                            Dear ${customer?.name ?: ""},
                            
                            This is a friendly payment reminder from *${firmName}*.
                            
                            An outstanding balance of *₹${String.format(Locale.US, "%.2f", outstandingBalance)}* is pending against your customer ledger.
                            
                            Kindly arrange the payment at your earliest convenience.
                            
                            Thank you!
                            
                            Regards,
                            *${firmName}*
                        """.trimIndent()
                        
                        try {
                            val uri = android.net.Uri.parse("https://api.whatsapp.com/send?phone=${customer?.phone ?: ""}&text=${android.net.Uri.encode(message)}")
                            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, uri)
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            android.widget.Toast.makeText(context, "WhatsApp is not installed", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp, bottom = 4.dp)
                        .testTag("action_whatsapp_reminder_ledger"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Send WhatsApp Reminder", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            // Tab Rows
            TabRow(selectedTabIndex = activeTab) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    text = { Text("Statement") }
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = { Text("Payments") }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (activeTab == 0) {
                // Statement List
                if (ledgerEntries.isEmpty()) {
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Text("No Transactions Found", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(ledgerEntries) { entry ->
                            StatementRow(entry, sdf)
                        }
                    }
                }
            } else {
                // Payment History List
                val paymentEntries = ledgerEntries.filter { it.type == "PAYMENT" }
                if (paymentEntries.isEmpty()) {
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Text("No Payments Recorded", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(paymentEntries) { entry ->
                            PaymentHistoryRow(
                                entry = entry,
                                sdf = sdf,
                                onDeleteClick = { paymentToDelete = entry }
                            )
                        }
                    }
                }
            }
        }
    }

    // Dialog: Received (Customer payment)
    if (showReceivedDialog) {
        RecordKhataEntryDialog(
            title = "Record Payment Received",
            isIncomeType = true, // Green
            onDismiss = { showReceivedDialog = false },
            onSave = { amount, date, refNo, notes ->
                viewModel.addCustomerPayment(
                    CustomerPaymentEntity(
                        customerId = customer?.id ?: 0,
                        amount = amount,
                        date = date,
                        paymentMode = if (refNo.isNotEmpty()) "Bank" else "Cash",
                        referenceNo = refNo,
                        notes = notes
                    )
                )
                showReceivedDialog = false
            }
        )
    }

    // Dialog: Given (Credit Sale)
    if (showGivenDialog) {
        CreateSaleInvoiceDialog(
            partyName = customer?.name ?: "",
            viewModel = viewModel,
            onDismiss = { showGivenDialog = false },
            onSave = { showGivenDialog = false }
        )
    }

    // Delete Payment Confirmation
    if (paymentToDelete != null) {
        AlertDialog(
            onDismissRequest = { paymentToDelete = null },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (viewModel.isTransactionSecurityEnabled()) {
                            showDeleteLockVerify = true
                        } else {
                            paymentToDelete?.let { viewModel.deleteCustomerPayment(it.id) }
                            paymentToDelete = null
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete Payment", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { paymentToDelete = null }) {
                    Text("Cancel")
                }
            },
            title = { Text("Delete Payment Entry?") },
            text = { Text("Are you sure you want to delete this payment record? This will adjust the customer's outstanding balance back up.") }
        )
    }

    if (showDeleteLockVerify) {
        PasswordVerificationDialog(
            viewModel = viewModel,
            onVerified = {
                showDeleteLockVerify = false
                paymentToDelete?.let { viewModel.deleteCustomerPayment(it.id) }
                paymentToDelete = null
            },
            onDismiss = {
                showDeleteLockVerify = false
            }
        )
    }
}

@Composable
fun StatementRow(entry: CustomerLedgerEntry, sdf: SimpleDateFormat) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (entry.type == "SALE") MaterialTheme.colorScheme.surface else Color(0xFFE8F5E9)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = if (entry.type == "SALE") "Sale Invoice" else "Payment Received",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (entry.type == "SALE") MaterialTheme.colorScheme.onSurface else Color(0xFF2E7D32)
                    )
                    Text(
                        text = "Ref: ${entry.reference}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = sdf.format(Date(entry.date)),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    if (entry.type == "SALE") {
                        Text(
                            text = "+₹${String.format(Locale.US, "%.2f", entry.debit)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD32F2F)
                        )
                    } else {
                        Text(
                            text = "-₹${String.format(Locale.US, "%.2f", entry.credit)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Bal: ₹${String.format(Locale.US, "%.2f", entry.runningBalance)}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (entry.notes.isNotEmpty()) {
                Text(
                    text = "Notes: ${entry.notes}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun PaymentHistoryRow(
    entry: CustomerLedgerEntry,
    sdf: SimpleDateFormat,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
        border = BorderStroke(1.dp, Color(0xFFA5D6A7))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.reference,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32)
                )
                Text(
                    text = sdf.format(Date(entry.date)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (entry.notes.isNotEmpty()) {
                    Text(
                        text = entry.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "₹${String.format(Locale.US, "%.2f", entry.credit)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32)
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete Payment",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}


// ==========================================
// SUPPLIER MASTER MODULE
// ==========================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuppliersScreen(viewModel: BillingViewModel) {
    val suppliers by viewModel.suppliersWithBalance.collectAsStateWithLifecycle()
    val searchQuery by viewModel.supplierSearchQuery.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var editingSupplier by remember { mutableStateOf<SupplierEntity?>(null) }
    var supplierToDelete by remember { mutableStateOf<SupplierEntity?>(null) }
    var showDeleteLockVerify by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Suppliers", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(
                        onClick = { showAddDialog = true },
                        modifier = Modifier.testTag("add_supplier_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Supplier")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.supplierSearchQuery.value = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .testTag("supplier_search"),
                placeholder = { Text("Search by name or phone...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            if (suppliers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Business,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "No Suppliers Found",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "Tap the '+' icon at the top right to add a supplier.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(suppliers, key = { it.supplier.id }) { item ->
                        SupplierCard(
                            item = item,
                            onCardClick = {
                                viewModel.selectedSupplierId.value = item.supplier.id
                                viewModel.setScreen(BillingScreen.SUPPLIER_LEDGER)
                            },
                            onEditClick = { editingSupplier = item.supplier },
                            onDeleteClick = { supplierToDelete = item.supplier }
                        )
                    }
                }
            }
        }
    }

    // Add/Edit Dialog
    if (showAddDialog || editingSupplier != null) {
        SupplierFormDialog(
            supplier = editingSupplier,
            onDismiss = {
                showAddDialog = false
                editingSupplier = null
            },
            onSave = { name, phone, email, address, notes ->
                val newSupplier = SupplierEntity(
                    id = editingSupplier?.id ?: 0,
                    name = name,
                    phone = phone,
                    email = email,
                    address = address,
                    notes = notes
                )
                viewModel.addSupplier(newSupplier)
                showAddDialog = false
                editingSupplier = null
            }
        )
    }

    // Delete Confirmation Dialog
    if (supplierToDelete != null) {
        AlertDialog(
            onDismissRequest = { supplierToDelete = null },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (viewModel.isTransactionSecurityEnabled()) {
                            showDeleteLockVerify = true
                        } else {
                            supplierToDelete?.let { viewModel.deleteSupplier(it.id) }
                            supplierToDelete = null
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { supplierToDelete = null }) {
                    Text("Cancel")
                }
            },
            title = { Text("Delete Supplier?") },
            text = { Text("Are you sure you want to delete '${supplierToDelete?.name}'? This will remove their record from the master list.") }
        )
    }

    if (showDeleteLockVerify) {
        PasswordVerificationDialog(
            viewModel = viewModel,
            onVerified = {
                showDeleteLockVerify = false
                supplierToDelete?.let { viewModel.deleteSupplier(it.id) }
                supplierToDelete = null
            },
            onDismiss = {
                showDeleteLockVerify = false
            }
        )
    }
}

@Composable
fun SupplierCard(
    item: SupplierWithBalance,
    onCardClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() }
            .testTag("supplier_card_${item.supplier.id}"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.supplier.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (item.supplier.phone.isNotEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Icon(
                                Icons.Default.Phone,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = item.supplier.phone,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Outstanding Balance Display
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "Outstanding",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "₹${String.format(Locale.US, "%.2f", item.outstandingBalance)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (item.outstandingBalance > 0) Color(0xFFD32F2F) else Color(0xFF388E3C)
                    )
                }
            }

            if (item.supplier.address.isNotEmpty()) {
                Text(
                    text = "Addr: ${item.supplier.address}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onCardClick() }
                ) {
                    Text(
                        "View Ledger & Payments",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Icon(
                        Icons.Default.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Row {
                    IconButton(onClick = onEditClick, modifier = Modifier.size(36.dp)) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onDeleteClick, modifier = Modifier.size(36.dp)) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SupplierFormDialog(
    supplier: SupplierEntity?,
    onDismiss: () -> Unit,
    onSave: (name: String, phone: String, email: String, address: String, notes: String) -> Unit
) {
    var name by remember { mutableStateOf(supplier?.name ?: "") }
    var phone by remember { mutableStateOf(supplier?.phone ?: "") }
    var email by remember { mutableStateOf(supplier?.email ?: "") }
    var address by remember { mutableStateOf(supplier?.address ?: "") }
    var notes by remember { mutableStateOf(supplier?.notes ?: "") }

    var showError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        showError = true
                    } else {
                        onSave(name, phone, email, address, notes)
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        title = { Text(if (supplier == null) "Add Supplier" else "Edit Supplier") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (showError) {
                    Text(
                        "Supplier Name is required!",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        showError = false
                    },
                    label = { Text("Supplier Name *") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    singleLine = true
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Address") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Internal Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )
            }
        }
    )
}

// ==========================================
// SUPPLIER LEDGER, STATEMENT, & PAYMENTS
// ==========================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupplierLedgerScreen(viewModel: BillingViewModel) {
    val supplier by viewModel.selectedSupplier.collectAsStateWithLifecycle()
    val ledgerEntries by viewModel.selectedSupplierLedger.collectAsStateWithLifecycle()

    var activeTab by remember { mutableStateOf(0) } // 0: Statement/Ledger, 1: Payment History
    var showPaidDialog by remember { mutableStateOf(false) }
    var showReceivedDialog by remember { mutableStateOf(false) }
    var paymentToDelete by remember { mutableStateOf<SupplierLedgerEntry?>(null) }
    var showDeleteLockVerify by remember { mutableStateOf(false) }

    val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())

    if (supplier == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val totalPurchases = ledgerEntries.filter { it.type == "PURCHASE" }.sumOf { it.credit }
    val totalPayments = ledgerEntries.filter { it.type == "PAYMENT" }.sumOf { it.debit }
    val outstandingBalance = totalPurchases - totalPayments

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(supplier?.name ?: "", fontWeight = FontWeight.Bold)
                        if (supplier?.phone?.isNotEmpty() == true) {
                            Text(supplier?.phone ?: "", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.goBack() },
                        modifier = Modifier.testTag("btn_back_supplier_ledger")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            // Fixed Bottom Action Bar for Supplier
            Surface(
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 8.dp,
                shadowElevation = 16.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Paid (Red ↑)
                    Button(
                        onClick = { showPaidDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("supplier_paid_btn"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFD32F2F),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(16.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowUpward,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Paid ₹", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    // Received (Green ↓)
                    Button(
                        onClick = { showReceivedDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("supplier_received_btn"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0F9D58),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(16.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowDownward,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Received ₹", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // Redesigned running balance card for Supplier
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                border = BorderStroke(
                    width = 1.5.dp,
                    color = when {
                        outstandingBalance > 0 -> Color(0xFFD32F2F).copy(alpha = 0.4f) // We owe (Red)
                        outstandingBalance < 0 -> Color(0xFF0F9D58).copy(alpha = 0.4f) // We will receive / advance paid (Green)
                        else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
                    }
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = when {
                            outstandingBalance > 0 -> "YOU OWE"
                            outstandingBalance < 0 -> "YOU'LL RECEIVE"
                            else -> "SETTLED"
                        },
                        style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.5.sp),
                        fontWeight = FontWeight.Bold,
                        color = when {
                            outstandingBalance > 0 -> Color(0xFFD32F2F)
                            outstandingBalance < 0 -> Color(0xFF0F9D58)
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "₹${String.format(Locale.US, "%.2f", Math.abs(outstandingBalance))}",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = when {
                            outstandingBalance > 0 -> Color(0xFFD32F2F)
                            outstandingBalance < 0 -> Color(0xFF0F9D58)
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f))
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Total Purchases", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                "₹${String.format(Locale.US, "%.2f", totalPurchases)}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Box(
                            modifier = Modifier
                                .height(28.dp)
                                .width(1.dp)
                                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Total Paid", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                "₹${String.format(Locale.US, "%.2f", totalPayments)}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD32F2F)
                            )
                        }
                    }
                }
            }

            // Tab Rows
            TabRow(selectedTabIndex = activeTab) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    text = { Text("Statement") }
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = { Text("Payments") }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (activeTab == 0) {
                // Statement List
                if (ledgerEntries.isEmpty()) {
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Text("No Transactions Found", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(ledgerEntries) { entry ->
                            SupplierStatementRow(entry, sdf)
                        }
                    }
                }
            } else {
                // Payment History List
                val paymentEntries = ledgerEntries.filter { it.type == "PAYMENT" }
                if (paymentEntries.isEmpty()) {
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Text("No Payments Recorded", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(paymentEntries) { entry ->
                            SupplierPaymentHistoryRow(
                                entry = entry,
                                sdf = sdf,
                                onDeleteClick = { paymentToDelete = entry }
                            )
                        }
                    }
                }
            }
        }
    }

    // Dialog: Paid (Supplier Payment)
    if (showPaidDialog) {
        RecordKhataEntryDialog(
            title = "Record Payment Made",
            isIncomeType = false, // Red (paying out)
            onDismiss = { showPaidDialog = false },
            onSave = { amount, date, refNo, notes ->
                viewModel.addSupplierPayment(
                    SupplierPaymentEntity(
                        supplierId = supplier?.id ?: 0,
                        amount = amount,
                        date = date,
                        paymentMode = if (refNo.isNotEmpty()) "Bank" else "Cash",
                        referenceNo = refNo,
                        notes = notes
                    )
                )
                showPaidDialog = false
            }
        )
    }

    // Dialog: Received (Supplier Purchase Bill)
    if (showReceivedDialog) {
        CreatePurchaseDialog(
            partyName = supplier?.name ?: "",
            viewModel = viewModel,
            onDismiss = { showReceivedDialog = false },
            onSave = { showReceivedDialog = false }
        )
    }

    // Delete Payment Confirmation
    if (paymentToDelete != null) {
        AlertDialog(
            onDismissRequest = { paymentToDelete = null },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (viewModel.isTransactionSecurityEnabled()) {
                            showDeleteLockVerify = true
                        } else {
                            paymentToDelete?.let { viewModel.deleteSupplierPayment(it.id) }
                            paymentToDelete = null
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete Payment", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { paymentToDelete = null }) {
                    Text("Cancel")
                }
            },
            title = { Text("Delete Payment Entry?") },
            text = { Text("Are you sure you want to delete this payment record? This will adjust the supplier's outstanding balance back up.") }
        )
    }

    if (showDeleteLockVerify) {
        PasswordVerificationDialog(
            viewModel = viewModel,
            onVerified = {
                showDeleteLockVerify = false
                paymentToDelete?.let { viewModel.deleteSupplierPayment(it.id) }
                paymentToDelete = null
            },
            onDismiss = {
                showDeleteLockVerify = false
            }
        )
    }
}

@Composable
fun SupplierStatementRow(entry: SupplierLedgerEntry, sdf: SimpleDateFormat) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (entry.type == "PURCHASE") MaterialTheme.colorScheme.surface else Color(0xFFE8F5E9)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = if (entry.type == "PURCHASE") "Purchase Entry" else "Payment Made",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (entry.type == "PURCHASE") MaterialTheme.colorScheme.onSurface else Color(0xFF2E7D32)
                    )
                    Text(
                        text = "Ref: ${entry.reference}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = sdf.format(Date(entry.date)),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    if (entry.type == "PURCHASE") {
                        Text(
                            text = "+₹${String.format(Locale.US, "%.2f", entry.credit)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD32F2F)
                        )
                    } else {
                        Text(
                            text = "-₹${String.format(Locale.US, "%.2f", entry.debit)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Bal: ₹${String.format(Locale.US, "%.2f", entry.runningBalance)}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (entry.notes.isNotEmpty()) {
                Text(
                    text = "Notes: ${entry.notes}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun SupplierPaymentHistoryRow(
    entry: SupplierLedgerEntry,
    sdf: SimpleDateFormat,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
        border = BorderStroke(1.dp, Color(0xFFA5D6A7))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.reference,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32)
                )
                Text(
                    text = sdf.format(Date(entry.date)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (entry.notes.isNotEmpty()) {
                    Text(
                        text = entry.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "₹${String.format(Locale.US, "%.2f", entry.debit)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32)
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete Payment",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

// ==========================================
// SHARED RECORD PAYMENT DIALOG (WITH DATE PICKER)
// ==========================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordPaymentDialog(
    title: String,
    onDismiss: () -> Unit,
    onSave: (amount: Double, date: Long, paymentMode: String, refNo: String, notes: String) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var paymentMode by remember { mutableStateOf("Cash") }
    var refNo by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    var formDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }

    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    var showError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    val parsedAmount = amount.toDoubleOrNull()
                    if (parsedAmount == null || parsedAmount <= 0) {
                        showError = true
                    } else {
                        onSave(parsedAmount, formDate, paymentMode, refNo, notes)
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        title = { Text(title) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (showError) {
                    Text(
                        "Please enter a valid payment amount (> 0)",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                OutlinedTextField(
                    value = amount,
                    onValueChange = {
                        amount = it
                        showError = false
                    },
                    label = { Text("Payment Amount (₹) *") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )

                // Date Picker field
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = sdf.format(Date(formDate)),
                        onValueChange = {},
                        label = { Text("Payment Date") },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) }
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { showDatePicker = true }
                    )
                }

                // Payment Mode Dropdown Row
                Text("Payment Mode", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val modes = listOf("Cash", "Bank", "UPI", "Card")
                    modes.forEach { mode ->
                        FilterChip(
                            selected = paymentMode == mode,
                            onClick = { paymentMode = mode },
                            label = { Text(mode) }
                        )
                    }
                }

                OutlinedTextField(
                    value = refNo,
                    onValueChange = { refNo = it },
                    label = { Text("Reference / Trans No (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )
            }
        }
    )

    if (showDatePicker) {
        val today = Calendar.getInstance()
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = formDate,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    val cellLocal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                        timeInMillis = utcTimeMillis
                    }
                    val localToday = Calendar.getInstance()
                    return !cellLocal.after(localToday)
                }

                override fun isSelectableYear(year: Int): Boolean {
                    val currentYear = today.get(Calendar.YEAR)
                    return year <= currentYear
                }
            }
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { selected ->
                            val localCal = Calendar.getInstance().apply {
                                timeInMillis = selected
                                // Keep current time portion
                                val now = Calendar.getInstance()
                                set(Calendar.HOUR_OF_DAY, now.get(Calendar.HOUR_OF_DAY))
                                set(Calendar.MINUTE, now.get(Calendar.MINUTE))
                                set(Calendar.SECOND, now.get(Calendar.SECOND))
                            }
                            formDate = localCal.timeInMillis
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

// ==========================================
// REDESIGNED DYNAMIC KHATA ENTRY DIALOG (GIVEN/RECEIVED)
// ==========================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordKhataEntryDialog(
    title: String,
    isIncomeType: Boolean, // true: Received (Green), false: Given/Paid (Red)
    onDismiss: () -> Unit,
    onSave: (amount: Double, date: Long, refNo: String, notes: String) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var refNo by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    var formDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }

    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    var showError by remember { mutableStateOf(false) }

    val themeColor = if (isIncomeType) Color(0xFF0F9D58) else Color(0xFFD32F2F)

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    val parsedAmount = amount.toDoubleOrNull()
                    if (parsedAmount == null || parsedAmount <= 0) {
                        showError = true
                    } else {
                        onSave(parsedAmount, formDate, refNo, notes)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = themeColor,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Save Entry", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
            ) {
                Text("Cancel")
            }
        },
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = if (isIncomeType) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                    contentDescription = null,
                    tint = themeColor,
                    modifier = Modifier.size(24.dp)
                )
                Text(title, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (showError) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                            Text(
                                "Please enter a valid amount greater than 0",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = amount,
                    onValueChange = {
                        amount = it
                        showError = false
                    },
                    label = { Text("Amount (₹) *") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = themeColor,
                        focusedLabelColor = themeColor
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                // Date Picker field
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = sdf.format(Date(formDate)),
                        onValueChange = {},
                        label = { Text("Entry Date") },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null, tint = themeColor) },
                        shape = RoundedCornerShape(12.dp)
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { showDatePicker = true }
                    )
                }

                OutlinedTextField(
                    value = refNo,
                    onValueChange = { refNo = it },
                    label = { Text("Reference / Trans No (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }
    )

    if (showDatePicker) {
        val today = Calendar.getInstance()
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = formDate,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    val cellLocal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                        timeInMillis = utcTimeMillis
                    }
                    val localToday = Calendar.getInstance()
                    return !cellLocal.after(localToday)
                }

                override fun isSelectableYear(year: Int): Boolean {
                    val currentYear = today.get(Calendar.YEAR)
                    return year <= currentYear
                }
            }
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { selected ->
                            val localCal = Calendar.getInstance().apply {
                                timeInMillis = selected
                                // Keep current time portion
                                val now = Calendar.getInstance()
                                set(Calendar.HOUR_OF_DAY, now.get(Calendar.HOUR_OF_DAY))
                                set(Calendar.MINUTE, now.get(Calendar.MINUTE))
                                set(Calendar.SECOND, now.get(Calendar.SECOND))
                            }
                            formDate = localCal.timeInMillis
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK", color = themeColor, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateSaleInvoiceDialog(
    partyName: String,
    viewModel: BillingViewModel,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    var productName by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("1") }
    var rate by remember { mutableStateOf("") }
    var hsnCode by remember { mutableStateOf("") }
    var gstPercentageStr by remember { mutableStateOf("18.0") } // Default to 18%
    
    val products by viewModel.productSummaries.collectAsStateWithLifecycle()
    val allProductItems by viewModel.productItems.collectAsStateWithLifecycle()
    val distinctItemNames by viewModel.distinctItemNames.collectAsStateWithLifecycle()
    
    // Expanded state for product search suggestions dropdown
    var expandedDropdown by remember { mutableStateOf(false) }
    
    // Auto calculate values
    val parsedQty = quantity.toDoubleOrNull() ?: 0.0
    val parsedRate = rate.toDoubleOrNull() ?: 0.0
    val parsedGst = gstPercentageStr.toDoubleOrNull() ?: 0.0
    val netPrice = parsedQty * parsedRate
    val gstAmount = netPrice * (parsedGst / 100.0)
    val grandTotal = netPrice + gstAmount
    
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    
    val filteredSuggestions = remember(productName, distinctItemNames) {
        if (productName.isEmpty()) emptyList()
        else distinctItemNames.filter { it.contains(productName, ignoreCase = true) }
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    if (productName.trim().isEmpty()) {
                        errorMessage = "Product Name cannot be empty"
                        showError = true
                        return@Button
                    }
                    if (parsedQty <= 0) {
                        errorMessage = "Quantity must be greater than 0"
                        showError = true
                        return@Button
                    }
                    if (parsedRate < 0) {
                        errorMessage = "Rate cannot be negative"
                        showError = true
                        return@Button
                    }
                    
                    scope.launch {
                        // Create Sale Invoice and Invoice Items
                        val nextInvNo = viewModel.getNextInvoiceNumber("SALE")
                        // Append HSN if provided, so we can extract it deterministically
                        val finalItemName = if (hsnCode.trim().isNotEmpty()) {
                            "${productName.trim()} [HSN: ${hsnCode.trim()}]"
                        } else {
                            productName.trim()
                        }
                        
                        val invoice = com.example.data.InvoiceEntity(
                            invoiceNumber = nextInvNo,
                            partyName = partyName,
                            type = "SALE",
                            date = System.currentTimeMillis(),
                            discount = 0.0,
                            tax = parsedGst,
                            totalAmount = grandTotal,
                            notes = "Generated from Customer Ledger",
                            isCreditSale = true,
                            outstandingAmount = grandTotal,
                            dueDate = System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000L
                        )
                        
                        val item = com.example.data.InvoiceItemEntity(
                            invoiceId = 0,
                            name = finalItemName,
                            price = parsedRate,
                            quantity = parsedQty,
                            totalPrice = netPrice
                        )
                        
                        viewModel.saveDirectInvoice(invoice, listOf(item))
                        onSave()
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF0F9D58),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Save & Generate GST Invoice", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Receipt,
                    contentDescription = null,
                    tint = Color(0xFF0F9D58),
                    modifier = Modifier.size(24.dp)
                )
                Text("TAX INVOICE (GST)", fontWeight = FontWeight.Bold, color = Color(0xFF0F9D58))
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (showError) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                            Text(errorMessage, color = MaterialTheme.colorScheme.onErrorContainer, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                
                // Select/Add Product field with custom suggestions drop-down
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = productName,
                        onValueChange = {
                            productName = it
                            showError = false
                            expandedDropdown = it.isNotEmpty()
                            // Try to prefill HSN code if product exists in summaries
                            val matchedProd = products.find { p -> p.name.trim().equals(it.trim(), ignoreCase = true) }
                            if (matchedProd != null) {
                                hsnCode = getDeterministicHsn(matchedProd.name)
                                if (matchedProd.avgSalePrice > 0) {
                                    rate = String.format(Locale.US, "%.2f", matchedProd.avgSalePrice)
                                } else if (matchedProd.avgPurchasePrice > 0) {
                                    rate = String.format(Locale.US, "%.2f", matchedProd.avgPurchasePrice)
                                }
                            }
                        },
                        label = { Text("Product Name *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        trailingIcon = {
                            IconButton(onClick = { expandedDropdown = !expandedDropdown }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Suggestions")
                            }
                        }
                    )
                    
                    if (expandedDropdown && filteredSuggestions.isNotEmpty()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 64.dp)
                                .align(Alignment.TopStart),
                            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(modifier = Modifier.heightIn(max = 200.dp).verticalScroll(rememberScrollState())) {
                                filteredSuggestions.forEach { suggestion ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                productName = suggestion
                                                expandedDropdown = false
                                                // Pre-fill fields
                                                hsnCode = getDeterministicHsn(suggestion)
                                                val matchedProd = products.find { p -> p.name.trim().equals(suggestion.trim(), ignoreCase = true) }
                                                if (matchedProd != null) {
                                                    if (matchedProd.avgSalePrice > 0) {
                                                        rate = String.format(Locale.US, "%.2f", matchedProd.avgSalePrice)
                                                    } else if (matchedProd.avgPurchasePrice > 0) {
                                                        rate = String.format(Locale.US, "%.2f", matchedProd.avgPurchasePrice)
                                                    }
                                                }
                                            }
                                            .padding(12.dp)
                                    ) {
                                        Text(suggestion)
                                    }
                                    HorizontalDivider()
                                }
                            }
                        }
                    }
                }
                
                // HSN Code
                OutlinedTextField(
                    value = hsnCode,
                    onValueChange = { hsnCode = it },
                    label = { Text("HSN Code") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                
                // Quantity and Rate row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = {
                            quantity = it
                            showError = false
                        },
                        label = { Text("Quantity *") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = rate,
                        onValueChange = {
                            rate = it
                            showError = false
                        },
                        label = { Text("Rate (₹) *") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )
                }
                
                // GST % Input
                OutlinedTextField(
                    value = gstPercentageStr,
                    onValueChange = { gstPercentageStr = it },
                    label = { Text("GST %") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
                
                // Quick selection Chips for GST %
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("0", "5", "12", "18", "28").forEach { pct ->
                        val selected = gstPercentageStr == pct || gstPercentageStr == "$pct.0"
                        SuggestionChip(
                            onClick = { gstPercentageStr = "$pct.0" },
                            label = { Text("$pct%") },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = if (selected) Color(0xFF0F9D58).copy(alpha = 0.15f) else Color.Transparent,
                                labelColor = if (selected) Color(0xFF0F9D58) else MaterialTheme.colorScheme.onSurface
                            ),
                            border = BorderStroke(1.dp, if (selected) Color(0xFF0F9D58) else MaterialTheme.colorScheme.outlineVariant)
                        )
                    }
                }
                
                // Auto Calculated Summary Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Net Price:", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("₹${String.format(Locale.US, "%.2f", netPrice)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        }
                        
                        if (parsedGst > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("CGST (${parsedGst / 2}%):", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("₹${String.format(Locale.US, "%.2f", gstAmount / 2)}", style = MaterialTheme.typography.bodySmall)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("SGST (${parsedGst / 2}%):", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("₹${String.format(Locale.US, "%.2f", gstAmount / 2)}", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        
                        HorizontalDivider()
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Grand Total:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("₹${String.format(Locale.US, "%.2f", grandTotal)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF0F9D58))
                        }
                    }
                }
            }
        }
    )
}

data class PurchaseItemDraft(
    val productName: String = "",
    val hsnCode: String = "",
    val quantity: String = "1",
    val rate: String = "",
    val gstPercentageStr: String = "18.0"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePurchaseDialog(
    partyName: String,
    viewModel: BillingViewModel,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    var supplierInvoiceNo by remember { mutableStateOf("") }
    var supplierInvoiceDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var supplierBillRef by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }

    val purchaseItems = remember { mutableStateListOf(PurchaseItemDraft()) }
    
    val products by viewModel.productSummaries.collectAsStateWithLifecycle()
    val distinctItemNames by viewModel.distinctItemNames.collectAsStateWithLifecycle()
    
    // Track which item's product suggestions dropdown is expanded
    var expandedDropdownIndex by remember { mutableStateOf<Int?>(null) }
    
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    
    val dateSdf = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    // Calculate totals dynamically for the summary card and saving
    var totalNetSum = 0.0
    var totalGstSum = 0.0
    purchaseItems.forEach { itemDraft ->
        val qty = itemDraft.quantity.toDoubleOrNull() ?: 0.0
        val price = itemDraft.rate.toDoubleOrNull() ?: 0.0
        val gstPct = itemDraft.gstPercentageStr.toDoubleOrNull() ?: 0.0
        val net = qty * price
        val gst = net * (gstPct / 100.0)
        totalNetSum += net
        totalGstSum += gst
    }
    val overallGrandTotal = totalNetSum + totalGstSum

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    if (supplierInvoiceNo.trim().isEmpty()) {
                        errorMessage = "Supplier Invoice Number is mandatory!"
                        showError = true
                        return@Button
                    }
                    if (purchaseItems.isEmpty()) {
                        errorMessage = "Please add at least one item line"
                        showError = true
                        return@Button
                    }
                    if (purchaseItems.any { it.productName.trim().isEmpty() }) {
                        errorMessage = "Product Name cannot be empty for any item"
                        showError = true
                        return@Button
                    }
                    if (purchaseItems.any { (it.quantity.toDoubleOrNull() ?: 0.0) <= 0 }) {
                        errorMessage = "Quantity must be greater than 0 for all items"
                        showError = true
                        return@Button
                    }
                    if (purchaseItems.any { (it.rate.toDoubleOrNull() ?: 0.0) < 0 }) {
                        errorMessage = "Rate cannot be negative for any item"
                        showError = true
                        return@Button
                    }
                    
                    scope.launch {
                        var totalNet = 0.0
                        var totalGst = 0.0
                        
                        val invoiceItems = purchaseItems.map { itemDraft ->
                            val productNameTrimmed = itemDraft.productName.trim()
                            val hsnTrimmed = itemDraft.hsnCode.trim()
                            val finalItemName = if (hsnTrimmed.isNotEmpty()) {
                                "$productNameTrimmed [HSN: $hsnTrimmed]"
                            } else {
                                productNameTrimmed
                            }
                            val itemQty = itemDraft.quantity.toDoubleOrNull() ?: 0.0
                            val itemRate = itemDraft.rate.toDoubleOrNull() ?: 0.0
                            val itemGst = itemDraft.gstPercentageStr.toDoubleOrNull() ?: 0.0
                            
                            val itemNet = itemQty * itemRate
                            val itemGstAmt = itemNet * (itemGst / 100.0)
                            
                            totalNet += itemNet
                            totalGst += itemGstAmt
                            
                            com.example.data.InvoiceItemEntity(
                                invoiceId = 0,
                                name = finalItemName,
                                price = itemRate,
                                quantity = itemQty,
                                totalPrice = itemNet
                            )
                        }
                        
                        val effectiveGstRate = if (totalNet > 0) (totalGst / totalNet) * 100.0 else 0.0
                        val billRefText = if (supplierBillRef.trim().isNotEmpty()) {
                            "Bill Ref: ${supplierBillRef.trim()}"
                        } else {
                            "Purchase Entry"
                        }

                        val invoice = com.example.data.InvoiceEntity(
                            invoiceNumber = supplierInvoiceNo.trim(),
                            partyName = partyName,
                            type = "PURCHASE",
                            date = supplierInvoiceDate,
                            discount = 0.0,
                            tax = effectiveGstRate,
                            totalAmount = totalNet + totalGst,
                            notes = billRefText,
                            isCreditSale = false,
                            outstandingAmount = 0.0,
                            dueDate = 0L
                        )
                        
                        viewModel.saveDirectInvoice(invoice, invoiceItems)
                        onSave()
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1E88E5), // Blue for Purchase
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Save Purchase", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = null,
                    tint = Color(0xFF1E88E5),
                    modifier = Modifier.size(24.dp)
                )
                Text("Record Purchase", fontWeight = FontWeight.Bold, color = Color(0xFF1E88E5))
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (showError) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                            Text(errorMessage, color = MaterialTheme.colorScheme.onErrorContainer, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                // 1. Supplier Invoice Number (Mandatory)
                OutlinedTextField(
                    value = supplierInvoiceNo,
                    onValueChange = {
                        supplierInvoiceNo = it
                        showError = false
                    },
                    label = { Text("Supplier Invoice Number *") },
                    modifier = Modifier.fillMaxWidth().testTag("supplier_invoice_number"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // 2. Supplier Invoice Date Picker
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = dateSdf.format(Date(supplierInvoiceDate)),
                        onValueChange = {},
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Supplier Invoice Date *") },
                        shape = RoundedCornerShape(12.dp),
                        readOnly = true,
                        leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Color(0xFF1E88E5)) }
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { showDatePicker = true }
                    )
                }

                // 3. Supplier Bill/Attachment Reference (with simulated attachment button)
                OutlinedTextField(
                    value = supplierBillRef,
                    onValueChange = { supplierBillRef = it },
                    label = { Text("Supplier Bill Reference (optional)") },
                    placeholder = { Text("File path or scan details") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    trailingIcon = {
                        IconButton(onClick = {
                            supplierBillRef = "scanned_bill_inv_${if (supplierInvoiceNo.trim().isNotEmpty()) supplierInvoiceNo.trim() else "file"}.pdf (Attached)"
                        }) {
                            Icon(
                                imageVector = Icons.Default.Attachment,
                                contentDescription = "Attach Bill Scan",
                                tint = Color(0xFF1E88E5)
                            )
                        }
                    }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                
                // Items List Title and Add Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "PURCHASED PRODUCTS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(
                        onClick = { purchaseItems.add(PurchaseItemDraft()) },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF1E88E5)),
                        contentPadding = PaddingValues(horizontal = 8.dp),
                        modifier = Modifier.testTag("purchase_add_item")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Item", fontWeight = FontWeight.Bold)
                    }
                }

                // Render dynamic item list
                purchaseItems.forEachIndexed { index, itemDraft ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Header: Item index and Delete Button
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Item #${index + 1}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E88E5)
                                )
                                if (purchaseItems.size > 1) {
                                    IconButton(
                                        onClick = { purchaseItems.removeAt(index) },
                                        modifier = Modifier.size(24.dp).testTag("purchase_delete_item_$index")
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

                            // Product Name Selection with suggestions
                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = itemDraft.productName,
                                    onValueChange = { newName ->
                                        val matchedProd = products.find { p -> p.name.trim().equals(newName.trim(), ignoreCase = true) }
                                        val prefilledRate = if (matchedProd != null && matchedProd.avgPurchasePrice > 0) {
                                            String.format(Locale.US, "%.2f", matchedProd.avgPurchasePrice)
                                        } else {
                                            itemDraft.rate
                                        }
                                        purchaseItems[index] = itemDraft.copy(
                                            productName = newName,
                                            hsnCode = matchedProd?.let { getDeterministicHsn(it.name) } ?: itemDraft.hsnCode,
                                            rate = prefilledRate
                                        )
                                        expandedDropdownIndex = index
                                        showError = false
                                    },
                                    label = { Text("Product Name *") },
                                    modifier = Modifier.fillMaxWidth().testTag("purchase_product_name_$index"),
                                    singleLine = true,
                                    trailingIcon = {
                                        IconButton(onClick = {
                                            expandedDropdownIndex = if (expandedDropdownIndex == index) null else index
                                        }) {
                                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Suggestions")
                                        }
                                    }
                                )

                                val filteredSuggestions = distinctItemNames.filter {
                                    it.contains(itemDraft.productName, ignoreCase = true) && it != itemDraft.productName
                                }

                                if (expandedDropdownIndex == index && filteredSuggestions.isNotEmpty() && itemDraft.productName.isNotEmpty()) {
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 64.dp),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                    ) {
                                        Column(modifier = Modifier.heightIn(max = 150.dp).verticalScroll(rememberScrollState())) {
                                            filteredSuggestions.forEach { suggestion ->
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clickable {
                                                            val matchedProd = products.find { p -> p.name.trim().equals(suggestion.trim(), ignoreCase = true) }
                                                            val prefilledRate = if (matchedProd != null && matchedProd.avgPurchasePrice > 0) {
                                                                String.format(Locale.US, "%.2f", matchedProd.avgPurchasePrice)
                                                            } else {
                                                                itemDraft.rate
                                                            }
                                                            purchaseItems[index] = itemDraft.copy(
                                                                productName = suggestion,
                                                                hsnCode = getDeterministicHsn(suggestion),
                                                                rate = prefilledRate
                                                            )
                                                            expandedDropdownIndex = null
                                                        }
                                                        .padding(12.dp)
                                                ) {
                                                    Text(suggestion)
                                                }
                                                HorizontalDivider()
                                            }
                                        }
                                    }
                                }
                            }

                            // HSN Code
                            OutlinedTextField(
                                value = itemDraft.hsnCode,
                                onValueChange = {
                                    purchaseItems[index] = itemDraft.copy(hsnCode = it)
                                },
                                label = { Text("HSN Code") },
                                modifier = Modifier.fillMaxWidth().testTag("purchase_hsn_code_$index"),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true
                            )

                            // Quantity and Purchase Rate row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedTextField(
                                    value = itemDraft.quantity,
                                    onValueChange = {
                                        purchaseItems[index] = itemDraft.copy(quantity = it)
                                        showError = false
                                    },
                                    label = { Text("Quantity *") },
                                    modifier = Modifier.weight(1f).testTag("purchase_qty_$index"),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = itemDraft.rate,
                                    onValueChange = {
                                        purchaseItems[index] = itemDraft.copy(rate = it)
                                        showError = false
                                    },
                                    label = { Text("Purchase Rate (₹) *") },
                                    modifier = Modifier.weight(1f).testTag("purchase_rate_$index"),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true
                                )
                            }

                            // GST % Input & Chips Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedTextField(
                                    value = itemDraft.gstPercentageStr,
                                    onValueChange = {
                                        purchaseItems[index] = itemDraft.copy(gstPercentageStr = it)
                                    },
                                    label = { Text("GST %") },
                                    modifier = Modifier.weight(1f).testTag("purchase_gst_$index"),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true
                                )

                                Row(
                                    modifier = Modifier.weight(1.5f),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    listOf("0", "5", "12", "18", "28").forEach { pct ->
                                        val selected = itemDraft.gstPercentageStr == pct || itemDraft.gstPercentageStr == "$pct.0"
                                        SuggestionChip(
                                            onClick = {
                                                purchaseItems[index] = itemDraft.copy(gstPercentageStr = "$pct.0")
                                            },
                                            label = { Text("$pct%", fontSize = 11.sp) },
                                            colors = SuggestionChipDefaults.suggestionChipColors(
                                                containerColor = if (selected) Color(0xFF1E88E5).copy(alpha = 0.15f) else Color.Transparent,
                                                labelColor = if (selected) Color(0xFF1E88E5) else MaterialTheme.colorScheme.onSurface
                                            ),
                                            border = BorderStroke(1.dp, if (selected) Color(0xFF1E88E5) else MaterialTheme.colorScheme.outlineVariant)
                                        )
                                    }
                                }
                            }

                            // Row Calculations Display
                            val itemQty = itemDraft.quantity.toDoubleOrNull() ?: 0.0
                            val itemRate = itemDraft.rate.toDoubleOrNull() ?: 0.0
                            val itemGstPct = itemDraft.gstPercentageStr.toDoubleOrNull() ?: 0.0
                            val itemNet = itemQty * itemRate
                            val itemGstAmt = itemNet * (itemGstPct / 100.0)
                            val itemTotal = itemNet + itemGstAmt

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Net Amt: ₹${String.format(Locale.US, "%.2f", itemNet)}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                                    Text("GST: ₹${String.format(Locale.US, "%.2f", itemGstAmt)} ($itemGstPct%)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text(
                                    "Total: ₹${String.format(Locale.US, "%.2f", itemTotal)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E88E5)
                                )
                            }
                        }
                    }
                }

                // Add Item Text Button below the cards
                OutlinedButton(
                    onClick = { purchaseItems.add(PurchaseItemDraft()) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Item Line", fontWeight = FontWeight.Bold)
                }

                // 4. Auto Calculated Bill Summary Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "BILL SUMMARY",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Subtotal Items Value:", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("₹${String.format(Locale.US, "%.2f", totalNetSum)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        }
                        
                        if (totalGstSum > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("CGST (Half of Total GST):", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("₹${String.format(Locale.US, "%.2f", totalGstSum / 2)}", style = MaterialTheme.typography.bodySmall)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("SGST (Half of Total GST):", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("₹${String.format(Locale.US, "%.2f", totalGstSum / 2)}", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        
                        HorizontalDivider()
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("ESTIMATED GRAND TOTAL:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("₹${String.format(Locale.US, "%.2f", overallGrandTotal)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF1E88E5))
                        }
                    }
                }
            }
        }
    )

    if (showDatePicker) {
        val today = Calendar.getInstance()
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = supplierInvoiceDate,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    val cellLocal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                        timeInMillis = utcTimeMillis
                    }
                    val localToday = Calendar.getInstance()
                    return !cellLocal.after(localToday)
                }

                override fun isSelectableYear(year: Int): Boolean {
                    val currentYear = today.get(Calendar.YEAR)
                    return year <= currentYear
                }
            }
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { selected ->
                            val localCal = Calendar.getInstance().apply {
                                timeInMillis = selected
                                val now = Calendar.getInstance()
                                set(Calendar.HOUR_OF_DAY, now.get(Calendar.HOUR_OF_DAY))
                                set(Calendar.MINUTE, now.get(Calendar.MINUTE))
                                set(Calendar.SECOND, now.get(Calendar.SECOND))
                            }
                            supplierInvoiceDate = localCal.timeInMillis
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
