package com.example.ui

import android.content.Intent
import android.net.Uri
import kotlinx.coroutines.launch
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import coil.compose.rememberAsyncImagePainter
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.delay
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.InvoiceWithItems
import java.text.SimpleDateFormat
import java.util.*
import android.webkit.WebView
import android.webkit.WebViewClient
import android.print.PrintManager
import android.print.PrintAttributes
import android.content.Context
import android.content.ContextWrapper
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricManager
import androidx.core.content.ContextCompat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillingAppContent(viewModel: BillingViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val selectedInvoice by viewModel.selectedInvoice.collectAsStateWithLifecycle()

    AppUpdateHandler(viewModel = viewModel)

    // Handle back button elegantly based on current screen
    BackHandler(enabled = currentScreen != BillingScreen.DASHBOARD && currentScreen != BillingScreen.SPLASH && currentScreen != BillingScreen.LOCK_SCREEN) {
        viewModel.goBack()
    }

    Scaffold(
        bottomBar = {
            if (currentScreen == BillingScreen.DASHBOARD || 
                currentScreen == BillingScreen.REPORTS || 
                currentScreen == BillingScreen.MORE ||
                currentScreen == BillingScreen.PARTIES ||
                currentScreen == BillingScreen.CUSTOMERS ||
                currentScreen == BillingScreen.SUPPLIERS
            ) {
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
                        selected = currentScreen == BillingScreen.PARTIES,
                        onClick = { viewModel.setScreen(BillingScreen.PARTIES) },
                        icon = { Icon(Icons.Default.People, contentDescription = "Parties") },
                        label = { Text("Parties") },
                        modifier = Modifier.testTag("nav_parties")
                    )
                    NavigationBarItem(
                        selected = currentScreen == BillingScreen.REPORTS,
                        onClick = { viewModel.setScreen(BillingScreen.REPORTS) },
                        icon = { Icon(Icons.Default.Assessment, contentDescription = "Reports") },
                        label = { Text("Reports") },
                        modifier = Modifier.testTag("nav_reports")
                    )
                    NavigationBarItem(
                        selected = currentScreen == BillingScreen.MORE,
                        onClick = { viewModel.setScreen(BillingScreen.MORE) },
                        icon = { Icon(Icons.Default.MoreHoriz, contentDescription = "More") },
                        label = { Text("More") },
                        modifier = Modifier.testTag("nav_more")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (currentScreen == BillingScreen.SPLASH || currentScreen == BillingScreen.LOCK_SCREEN) {
                        Modifier
                    } else {
                        Modifier.padding(innerPadding)
                    }
                )
        ) {
            when (currentScreen) {
                BillingScreen.SPLASH -> SplashScreen(viewModel)
                BillingScreen.LOCK_SCREEN -> LockScreen(viewModel)
                BillingScreen.DASHBOARD -> DashboardScreen(viewModel)
                BillingScreen.PROFIT_LOSS -> ProfitLossScreen(viewModel)
                BillingScreen.REPORTS -> ReportsScreen(viewModel)
                BillingScreen.ADD_INVOICE -> AddInvoiceScreen(viewModel)
                BillingScreen.INVOICE_DETAIL -> selectedInvoice?.let {
                    InvoiceDetailScreen(viewModel, it)
                } ?: viewModel.setScreen(BillingScreen.DASHBOARD)
                BillingScreen.CUSTOMERS -> CustomersScreen(viewModel)
                BillingScreen.SUPPLIERS -> SuppliersScreen(viewModel)
                BillingScreen.PARTIES -> PartiesScreen(viewModel)
                BillingScreen.CUSTOMER_LEDGER -> CustomerLedgerScreen(viewModel)
                BillingScreen.SUPPLIER_LEDGER -> SupplierLedgerScreen(viewModel)
                BillingScreen.BUSINESS_PROFILE -> BusinessProfileScreen(viewModel)
                BillingScreen.CREDIT_REMINDERS -> CreditRemindersScreen(viewModel)
                BillingScreen.MORE -> MoreScreen(viewModel)
                BillingScreen.PRODUCTS -> ProductsScreen(viewModel)
                BillingScreen.SETTINGS -> SettingsScreen(viewModel)
                BillingScreen.HELP_SUPPORT -> HelpSupportScreen(viewModel)
                BillingScreen.ABOUT_VM_BOOK -> AboutVmBookScreen(viewModel)
                BillingScreen.BACKUP_RESTORE -> BackupRestoreScreen(viewModel)
                BillingScreen.EXPORT_DATA -> ExportDataScreen(viewModel)
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun DashboardScreen(viewModel: BillingViewModel) {
    val stats by viewModel.dashboardStats.collectAsStateWithLifecycle()
    val recentInvoices by viewModel.invoices.collectAsStateWithLifecycle()
    val businessProfileState by viewModel.businessProfile.collectAsStateWithLifecycle()
    val isPrivacyHidden by viewModel.isPrivacyHidden.collectAsStateWithLifecycle()

    var showFirmBottomSheet by remember { mutableStateOf(false) }
    val firms by viewModel.allFirms.collectAsStateWithLifecycle()
    val currentFirmId by viewModel.currentFirmId.collectAsStateWithLifecycle()

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
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { showFirmBottomSheet = true }
                    .testTag("firm_dropdown_trigger")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = businessProfileState?.firmName?.takeIf { it.isNotEmpty() } ?: "VM BOOK",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.testTag("current_firm_name_text")
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Switch Firm",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(
                    text = "Simple, Smart & Complete Billing",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(
                onClick = { viewModel.togglePrivacyHidden() },
                modifier = Modifier.testTag("action_toggle_privacy")
            ) {
                Icon(
                    imageVector = if (isPrivacyHidden) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = "Toggle Privacy Mode",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            IconButton(
                onClick = { viewModel.setScreen(BillingScreen.BUSINESS_PROFILE) },
                modifier = Modifier.testTag("action_business_profile")
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Business Profile",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        if (showFirmBottomSheet) {
            var showCreateFirmDialog by remember { mutableStateOf(false) }

            ModalBottomSheet(
                onDismissRequest = { showFirmBottomSheet = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                modifier = Modifier.testTag("firm_select_bottom_sheet")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    Text(
                        text = "SELECT FIRM",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(firms) { firm ->
                            val isSelected = firm.id == currentFirmId
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                        else Color.Transparent
                                    )
                                    .clickable {
                                        viewModel.selectFirm(firm.id)
                                        showFirmBottomSheet = false
                                    }
                                    .padding(16.dp)
                                    .testTag("firm_item_${firm.id}"),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF2196F3)),
                                    contentAlignment = Alignment.Center
                                ) {}
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(
                                    text = firm.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier.weight(1f)
                                )
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            showCreateFirmDialog = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("create_new_firm_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Create New Firm"
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "CREATE NEW FIRM",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            if (showCreateFirmDialog) {
                var newFirmName by remember { mutableStateOf("") }
                var newFirmPhone by remember { mutableStateOf("") }
                var newFirmGstin by remember { mutableStateOf("") }
                var newFirmAddress by remember { mutableStateOf("") }
                var newFirmEmail by remember { mutableStateOf("") }
                
                AlertDialog(
                    onDismissRequest = { showCreateFirmDialog = false },
                    title = { Text("Create New Firm") },
                    text = {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = newFirmName,
                                onValueChange = { newFirmName = it },
                                label = { Text("Firm/Business Name *") },
                                modifier = Modifier.fillMaxWidth().testTag("new_firm_name_input"),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = newFirmPhone,
                                onValueChange = { newFirmPhone = it },
                                label = { Text("Phone Number") },
                                modifier = Modifier.fillMaxWidth().testTag("new_firm_phone_input"),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = newFirmGstin,
                                onValueChange = { newFirmGstin = it },
                                label = { Text("GSTIN") },
                                modifier = Modifier.fillMaxWidth().testTag("new_firm_gstin_input"),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = newFirmAddress,
                                onValueChange = { newFirmAddress = it },
                                label = { Text("Address") },
                                modifier = Modifier.fillMaxWidth().testTag("new_firm_address_input"),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = newFirmEmail,
                                onValueChange = { newFirmEmail = it },
                                label = { Text("Email") },
                                modifier = Modifier.fillMaxWidth().testTag("new_firm_email_input"),
                                singleLine = true
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (newFirmName.trim().isNotEmpty()) {
                                    viewModel.createNewFirm(
                                        name = newFirmName.trim(),
                                        phone = newFirmPhone.trim(),
                                        email = newFirmEmail.trim(),
                                        address = newFirmAddress.trim(),
                                        gstin = newFirmGstin.trim()
                                    )
                                    showCreateFirmDialog = false
                                    showFirmBottomSheet = false
                                }
                            },
                            enabled = newFirmName.trim().isNotEmpty(),
                            modifier = Modifier.testTag("confirm_create_firm_button")
                        ) {
                            Text("Create")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showCreateFirmDialog = false },
                            modifier = Modifier.testTag("dismiss_create_firm_button")
                        ) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }

        // Today's Sales & Today's Profit Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Today's Sales Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .testTag("dashboard_today_sales_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "TODAY'S SALES",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isPrivacyHidden) "₹••••" else "₹${String.format(Locale.US, "%.2f", stats.todaySales)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Sales generated today",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Today's Profit Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .testTag("dashboard_today_profit_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "TODAY'S PROFIT",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isPrivacyHidden) "₹••••" else "₹${String.format(Locale.US, "%.2f", stats.todayProfit)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Net profit made today",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
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
                        text = if (isPrivacyHidden) "₹••••" else "₹${String.format(Locale.US, "%.2f", stats.totalSales)}",
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
                                Icons.Default.Inventory,
                                contentDescription = "Remaining Stock Value",
                                tint = Color(0xFF1E88E5),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Text(
                            text = "In Stock",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Remaining Stock",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (isPrivacyHidden) "₹••••" else "₹${String.format(Locale.US, "%.2f", stats.remainingStockValue)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E88E5)
                    )
                }
            }
        }

        // Credit Sales & Reminders Banner
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "CREDIT SALES & REMINDERS",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Pending Dues",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    if (stats.overdueCreditInvoicesCount > 0) {
                        Badge(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError,
                            modifier = Modifier.padding(end = 4.dp)
                        ) {
                            Text(
                                text = "${stats.overdueCreditInvoicesCount} OVERDUE",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Outstanding Amount",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (isPrivacyHidden) "₹••••" else "₹${String.format(Locale.US, "%.2f", stats.totalOutstandingCredit)}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Button(
                        onClick = { viewModel.setScreen(BillingScreen.CREDIT_REMINDERS) },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.testTag("action_manage_reminders")
                    ) {
                        Text("Manage Reminders", fontWeight = FontWeight.Bold)
                    }
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
                    viewModel.updateFormType("SALE")
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
                    viewModel.updateFormType("PURCHASE")
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
            TextButton(onClick = { viewModel.setScreen(BillingScreen.REPORTS) }) {
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

fun isTimestampInFilter(timestamp: Long, filter: String, customStart: Long, customEnd: Long): Boolean {
    val itemCal = java.util.Calendar.getInstance().apply { timeInMillis = timestamp }
    return when (filter) {
        "TODAY" -> {
            val todayCal = java.util.Calendar.getInstance()
            itemCal.get(java.util.Calendar.YEAR) == todayCal.get(java.util.Calendar.YEAR) &&
            itemCal.get(java.util.Calendar.DAY_OF_YEAR) == todayCal.get(java.util.Calendar.DAY_OF_YEAR)
        }
        "WEEK" -> {
            val weekCal = java.util.Calendar.getInstance()
            weekCal.set(java.util.Calendar.HOUR_OF_DAY, 0)
            weekCal.set(java.util.Calendar.MINUTE, 0)
            weekCal.set(java.util.Calendar.SECOND, 0)
            weekCal.set(java.util.Calendar.MILLISECOND, 0)
            weekCal.set(java.util.Calendar.DAY_OF_WEEK, weekCal.firstDayOfWeek)
            val weekStart = weekCal.timeInMillis
            
            weekCal.add(java.util.Calendar.DAY_OF_WEEK, 7)
            val weekEnd = weekCal.timeInMillis
            
            timestamp in weekStart until weekEnd
        }
        "MONTH" -> {
            val monthCal = java.util.Calendar.getInstance()
            itemCal.get(java.util.Calendar.YEAR) == monthCal.get(java.util.Calendar.YEAR) &&
            itemCal.get(java.util.Calendar.MONTH) == monthCal.get(java.util.Calendar.MONTH)
        }
        "YEAR" -> {
            val yearCal = java.util.Calendar.getInstance()
            itemCal.get(java.util.Calendar.YEAR) == yearCal.get(java.util.Calendar.YEAR)
        }
        "CUSTOM" -> {
            val startCal = java.util.Calendar.getInstance().apply {
                timeInMillis = customStart
                set(java.util.Calendar.HOUR_OF_DAY, 0)
                set(java.util.Calendar.MINUTE, 0)
                set(java.util.Calendar.SECOND, 0)
                set(java.util.Calendar.MILLISECOND, 0)
            }
            val endCal = java.util.Calendar.getInstance().apply {
                timeInMillis = customEnd
                set(java.util.Calendar.HOUR_OF_DAY, 23)
                set(java.util.Calendar.MINUTE, 59)
                set(java.util.Calendar.SECOND, 59)
                set(java.util.Calendar.MILLISECOND, 999)
            }
            timestamp in startCal.timeInMillis..endCal.timeInMillis
        }
        else -> true
    }
}

fun shareFile(context: android.content.Context, file: java.io.File, mimeType: String) {
    try {
        val authority = "${context.packageName}.fileprovider"
        val uri = androidx.core.content.FileProvider.getUriForFile(context, authority, file)
        
        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(android.content.Intent.EXTRA_STREAM, uri)
            putExtra(android.content.Intent.EXTRA_SUBJECT, file.name)
            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        context.startActivity(android.content.Intent.createChooser(intent, "Share Report via"))
    } catch (e: Exception) {
        e.printStackTrace()
        android.widget.Toast.makeText(context, "Failed to share report: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
    }
}

fun exportReportToPdf(
    context: android.content.Context,
    reportTitle: String,
    filterName: String,
    totalBills: Int,
    totalAmount: Double,
    transactions: List<InvoiceWithItems>
) {
    try {
        val pdfDocument = android.graphics.pdf.PdfDocument()
        val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 842, 1).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas
        
        val titlePaint = android.graphics.Paint().apply {
            color = android.graphics.Color.BLACK
            textSize = 20f
            isFakeBoldText = true
        }
        val subTitlePaint = android.graphics.Paint().apply {
            color = android.graphics.Color.DKGRAY
            textSize = 12f
        }
        val headerPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.BLACK
            textSize = 12f
            isFakeBoldText = true
        }
        val bodyPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.BLACK
            textSize = 10f
        }
        val boldBodyPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.BLACK
            textSize = 10f
            isFakeBoldText = true
        }
        
        var y = 50f
        
        canvas.drawText(reportTitle, 50f, y, titlePaint)
        y += 25f
        canvas.drawText("Filter Period: $filterName", 50f, y, subTitlePaint)
        y += 20f
        canvas.drawText("Generated on: ${SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())}", 50f, y, subTitlePaint)
        y += 30f
        
        canvas.drawRect(50f, y, 545f, y + 50f, android.graphics.Paint().apply {
            color = android.graphics.Color.LTGRAY
            style = android.graphics.Paint.Style.STROKE
            strokeWidth = 1f
        })
        canvas.drawText("Summary", 60f, y + 15f, boldBodyPaint)
        canvas.drawText("Total Bills: $totalBills", 60f, y + 35f, bodyPaint)
        canvas.drawText("Total Amount: INR ${String.format(Locale.US, "%.2f", totalAmount)}", 300f, y + 35f, boldBodyPaint)
        
        y += 80f
        
        canvas.drawText("Date", 50f, y, headerPaint)
        canvas.drawText("Bill / Invoice #", 140f, y, headerPaint)
        canvas.drawText("Party Name", 260f, y, headerPaint)
        canvas.drawText("Amount", 480f, y, headerPaint)
        
        canvas.drawLine(50f, y + 5f, 545f, y + 5f, android.graphics.Paint().apply {
            color = android.graphics.Color.BLACK
            strokeWidth = 1f
        })
        
        y += 20f
        
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        
        for (item in transactions) {
            if (y > 800f) {
                pdfDocument.finishPage(page)
                val newPageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 842, pdfDocument.pages.size + 1).create()
                page = pdfDocument.startPage(newPageInfo)
                canvas = page.canvas
                y = 50f
                
                canvas.drawText("Date", 50f, y, headerPaint)
                canvas.drawText("Bill / Invoice #", 140f, y, headerPaint)
                canvas.drawText("Party Name", 260f, y, headerPaint)
                canvas.drawText("Amount", 480f, y, headerPaint)
                
                canvas.drawLine(50f, y + 5f, 545f, y + 5f, android.graphics.Paint().apply {
                    color = android.graphics.Color.BLACK
                    strokeWidth = 1f
                })
                y += 20f
            }
            
            val inv = item.invoice
            val dateStr = sdf.format(Date(inv.date))
            val billNo = inv.invoiceNumber
            val party = if (inv.partyName.length > 25) inv.partyName.substring(0, 22) + "..." else inv.partyName
            val amountStr = String.format(Locale.US, "%.2f", inv.totalAmount)
            
            canvas.drawText(dateStr, 50f, y, bodyPaint)
            canvas.drawText(billNo, 140f, y, bodyPaint)
            canvas.drawText(party, 260f, y, bodyPaint)
            canvas.drawText("Rs. $amountStr", 480f, y, boldBodyPaint)
            
            y += 20f
        }
        
        pdfDocument.finishPage(page)
        
        val fileName = "${reportTitle.replace(" ", "_")}_Report.pdf"
        val file = java.io.File(context.cacheDir, fileName)
        val outputStream = java.io.FileOutputStream(file)
        pdfDocument.writeTo(outputStream)
        pdfDocument.close()
        outputStream.close()
        
        shareFile(context, file, "application/pdf")
    } catch (e: Exception) {
        e.printStackTrace()
        android.widget.Toast.makeText(context, "Error generating PDF: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
    }
}

fun exportReportToExcel(
    context: android.content.Context,
    reportTitle: String,
    filterName: String,
    totalBills: Int,
    totalAmount: Double,
    transactions: List<InvoiceWithItems>
) {
    try {
        val fileName = "${reportTitle.replace(" ", "_")}_Report.csv"
        val file = java.io.File(context.cacheDir, fileName)
        val fileWriter = java.io.FileWriter(file)
        
        val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        val dateSdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        
        fileWriter.append("\"$reportTitle\"\n")
        fileWriter.append("\"Filter Period:\",\"$filterName\"\n")
        fileWriter.append("\"Generated on:\",\"${sdf.format(Date())}\"\n")
        fileWriter.append("\n")
        
        fileWriter.append("\"SUMMARY\"\n")
        fileWriter.append("\"Total Bills:\",\"$totalBills\"\n")
        fileWriter.append("\"Total Amount:\",\"INR ${String.format(Locale.US, "%.2f", totalAmount)}\"\n")
        fileWriter.append("\n")
        
        fileWriter.append("\"Date\",\"Bill / Invoice #\",\"Party Name\",\"Subtotal (Items Total)\",\"Discount\",\"Tax (%)\",\"Total Amount\",\"Outstanding Amount\"\n")
        
        for (item in transactions) {
            val inv = item.invoice
            val escapedParty = inv.partyName.replace("\"", "\"\"")
            val escapedBill = inv.invoiceNumber.replace("\"", "\"\"")
            val subtotal = item.items.sumOf { it.totalPrice }
            
            fileWriter.append("\"${dateSdf.format(Date(inv.date))}\",")
            fileWriter.append("\"$escapedBill\",")
            fileWriter.append("\"$escapedParty\",")
            fileWriter.append("\"${String.format(Locale.US, "%.2f", subtotal)}\",")
            fileWriter.append("\"${String.format(Locale.US, "%.2f", inv.discount)}\",")
            fileWriter.append("\"${String.format(Locale.US, "%.1f", inv.tax)}\",")
            fileWriter.append("\"${String.format(Locale.US, "%.2f", inv.totalAmount)}\",")
            fileWriter.append("\"${String.format(Locale.US, "%.2f", inv.outstandingAmount)}\"\n")
        }
        
        fileWriter.flush()
        fileWriter.close()
        
        shareFile(context, file, "text/csv")
    } catch (e: Exception) {
        e.printStackTrace()
        android.widget.Toast.makeText(context, "Error generating Excel/CSV: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(viewModel: BillingViewModel) {
    val context = LocalContext.current
    val invoices by viewModel.allInvoices.collectAsStateWithLifecycle()
    
    var selectedTab by remember { mutableStateOf(0) } // 0 for Sales, 1 for Purchases
    var selectedFilter by remember { mutableStateOf("MONTH") } // "TODAY", "WEEK", "MONTH", "CUSTOM"
    
    var startDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var endDate by remember { mutableStateOf(System.currentTimeMillis()) }
    
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    
    var showDeleteConfirmation by remember { mutableStateOf<Long?>(null) }
    var showDeleteVerificationDialog by remember { mutableStateOf(false) }
    
    val displayDateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    
    val activeType = if (selectedTab == 0) "SALE" else "PURCHASE"
    val filteredInvoices = remember(invoices, selectedTab, selectedFilter, startDate, endDate) {
        invoices.filter { item ->
            val matchesType = item.invoice.type == activeType
            val matchesDate = isTimestampInFilter(item.invoice.date, selectedFilter, startDate, endDate)
            matchesType && matchesDate
        }
    }
    
    val totalBills = filteredInvoices.size
    val totalAmount = filteredInvoices.sumOf { it.invoice.totalAmount }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Reports",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        TabRow(
            selectedTabIndex = selectedTab,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            containerColor = Color.Transparent
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Sales", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Purchases", fontWeight = FontWeight.Bold) }
            )
        }
        
        Text(
            text = "FILTER PERIOD",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val filters = listOf(
                "TODAY" to "Daily",
                "WEEK" to "Weekly",
                "MONTH" to "Monthly",
                "YEAR" to "Yearly",
                "CUSTOM" to "Custom Date"
            )
            filters.forEach { (key, label) ->
                val selected = selectedFilter == key
                FilterChip(
                    selected = selected,
                    onClick = { selectedFilter = key },
                    label = { Text(label, fontWeight = FontWeight.SemiBold) },
                    shape = RoundedCornerShape(20.dp)
                )
            }
        }
        
        if (selectedFilter == "CUSTOM") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { showStartDatePicker = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "From: " + displayDateFormat.format(Date(startDate)),
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                OutlinedButton(
                    onClick = { showEndDatePicker = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "To: " + displayDateFormat.format(Date(endDate)),
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "TOTAL BILLS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$totalBills",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                Column(modifier = Modifier.weight(1.2f), horizontalAlignment = Alignment.End) {
                    Text(
                        text = "TOTAL AMOUNT",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "₹${String.format(Locale.US, "%.2f", totalAmount)}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color = if (selectedTab == 0) Color(0xFF0F9D58) else Color(0xFF1E88E5)
                    )
                }
            }
        }
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = {
                    val reportTitle = if (selectedTab == 0) "Sales Report" else "Purchases Report"
                    val filterName = when (selectedFilter) {
                        "TODAY" -> "Today"
                        "WEEK" -> "This Week"
                        "MONTH" -> "This Month"
                        "YEAR" -> "This Year"
                        "CUSTOM" -> "${displayDateFormat.format(Date(startDate))} to ${displayDateFormat.format(Date(endDate))}"
                        else -> "All Time"
                    }
                    exportReportToPdf(context, reportTitle, filterName, totalBills, totalAmount, filteredInvoices)
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("btn_export_pdf"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Export to PDF", fontWeight = FontWeight.Bold)
            }
            
            OutlinedButton(
                onClick = {
                    val reportTitle = if (selectedTab == 0) "Sales Report" else "Purchases Report"
                    val filterName = when (selectedFilter) {
                        "TODAY" -> "Today"
                        "WEEK" -> "This Week"
                        "MONTH" -> "This Month"
                        "YEAR" -> "This Year"
                        "CUSTOM" -> "${displayDateFormat.format(Date(startDate))} to ${displayDateFormat.format(Date(endDate))}"
                        else -> "All Time"
                    }
                    exportReportToExcel(context, reportTitle, filterName, totalBills, totalAmount, filteredInvoices)
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("btn_export_excel"),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Icon(Icons.Default.List, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Export to Excel", fontWeight = FontWeight.Bold)
            }
        }
        
        Text(
            text = "DETAILED TRANSACTIONS",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        if (filteredInvoices.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
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
                        text = "No transactions found",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Try modifying your period filter",
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
                    .weight(1f)
            ) {
                items(filteredInvoices, key = { it.invoice.id }) { item ->
                    InvoiceCompactRow(
                        item = item,
                        onClick = { viewModel.viewInvoiceDetail(item) },
                        onDeleteClick = { showDeleteConfirmation = item.invoice.id }
                    )
                }
            }
        }
    }
    
    if (showStartDatePicker) {
        val calendar = Calendar.getInstance().apply { timeInMillis = startDate }
        android.app.DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val newCal = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                startDate = newCal.timeInMillis
                showStartDatePicker = false
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            setOnDismissListener { showStartDatePicker = false }
            show()
        }
    }
    
    if (showEndDatePicker) {
        val calendar = Calendar.getInstance().apply { timeInMillis = endDate }
        android.app.DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val newCal = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }
                endDate = newCal.timeInMillis
                showEndDatePicker = false
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            setOnDismissListener { showEndDatePicker = false }
            show()
        }
    }
    
    if (showDeleteConfirmation != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = null },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (viewModel.isTransactionSecurityEnabled()) {
                            showDeleteVerificationDialog = true
                        } else {
                            showDeleteConfirmation?.let { viewModel.deleteInvoice(it) }
                            showDeleteConfirmation = null
                        }
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

    if (showDeleteVerificationDialog) {
        PasswordVerificationDialog(
            viewModel = viewModel,
            onVerified = {
                showDeleteVerificationDialog = false
                showDeleteConfirmation?.let { viewModel.deleteInvoice(it) }
                showDeleteConfirmation = null
            },
            onDismiss = {
                showDeleteVerificationDialog = false
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AddInvoiceScreen(viewModel: BillingViewModel) {
    val currentType = viewModel.formType.value
    val partyNameSuggestions = if (currentType == "SALE") {
        viewModel.customerSuggestions.collectAsStateWithLifecycle().value
    } else {
        viewModel.supplierSuggestions.collectAsStateWithLifecycle().value
    }
    val itemNameSuggestions by viewModel.distinctItemNames.collectAsStateWithLifecycle()
    val categories by viewModel.productCategories.collectAsStateWithLifecycle()
    val allProductItems by viewModel.productItems.collectAsStateWithLifecycle()
    val stockBalances by viewModel.stockBalances.collectAsStateWithLifecycle()
    val invoiceError by viewModel.formErrorMessage
    var showDatePicker by remember { mutableStateOf(false) }

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
            IconButton(
                onClick = { viewModel.goBack() },
                modifier = Modifier.testTag("btn_back_add_invoice")
            ) {
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
                        .clickable { viewModel.updateFormType("SALE") }
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
                        .clickable { viewModel.updateFormType("PURCHASE") }
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

                Box(
                    modifier = Modifier.weight(1.2f)
                ) {
                    OutlinedTextField(
                        value = sdf.format(Date(viewModel.formDate.value)),
                        onValueChange = {},
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Date & Time") },
                        shape = RoundedCornerShape(12.dp),
                        readOnly = true,
                        leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) }
                    )
                    // Transparent Overlay to capture click without interfering with TextField layout
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { showDatePicker = true }
                    )
                }
            }

            if (showDatePicker) {
                val datePickerState = rememberDatePickerState(
                    initialSelectedDateMillis = viewModel.formDate.value,
                    selectableDates = object : SelectableDates {
                        override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                            val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                                timeInMillis = utcTimeMillis
                            }
                            val year = calendar.get(Calendar.YEAR)
                            val month = calendar.get(Calendar.MONTH)
                            val day = calendar.get(Calendar.DAY_OF_MONTH)

                            val localToday = Calendar.getInstance().apply {
                                set(Calendar.HOUR_OF_DAY, 0)
                                set(Calendar.MINUTE, 0)
                                set(Calendar.SECOND, 0)
                                set(Calendar.MILLISECOND, 0)
                            }

                            val cellLocal = Calendar.getInstance().apply {
                                clear()
                                set(year, month, day)
                            }

                            return !cellLocal.after(localToday)
                        }

                        override fun isSelectableYear(year: Int): Boolean {
                            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                            return year <= currentYear
                        }
                    }
                )

                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                datePickerState.selectedDateMillis?.let { selectedMillis ->
                                    val selectedCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                                        timeInMillis = selectedMillis
                                    }
                                    val localCal = Calendar.getInstance().apply {
                                        set(Calendar.YEAR, selectedCal.get(Calendar.YEAR))
                                        set(Calendar.MONTH, selectedCal.get(Calendar.MONTH))
                                        set(Calendar.DAY_OF_MONTH, selectedCal.get(Calendar.DAY_OF_MONTH))
                                    }
                                    viewModel.formDate.value = localCal.timeInMillis
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

                        // Redesigned Category -> Item dropdown selection
                        var showCategoryDialog by remember { mutableStateOf(false) }
                        var showItemDialog by remember { mutableStateOf(false) }

                        // Category Selector
                        OutlinedTextField(
                            value = draftItem.categoryName,
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { showCategoryDialog = true },
                            placeholder = { Text("Select Category (Required)") },
                            label = { Text("Category") },
                            trailingIcon = {
                                IconButton(onClick = { showCategoryDialog = true }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
                                }
                            },
                            shape = RoundedCornerShape(8.dp)
                        )

                        if (showCategoryDialog) {
                            AlertDialog(
                                onDismissRequest = { showCategoryDialog = false },
                                title = { Text("Select Product Category", fontWeight = FontWeight.Bold) },
                                text = {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(max = 300.dp)
                                            .verticalScroll(rememberScrollState()),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        if (categories.isEmpty()) {
                                            Text("No categories available. Please add categories in Product Master first.", style = MaterialTheme.typography.bodyMedium)
                                        } else {
                                            categories.forEach { cat ->
                                                Card(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clickable {
                                                            viewModel.updateDraftItem(
                                                                index,
                                                                draftItem.copy(
                                                                    categoryName = cat.name,
                                                                    name = "",
                                                                    hsnCode = ""
                                                                )
                                                            )
                                                            showCategoryDialog = false
                                                        },
                                                    colors = CardDefaults.cardColors(
                                                        containerColor = if (draftItem.categoryName == cat.name) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                                    )
                                                ) {
                                                    Text(
                                                        text = cat.name,
                                                        modifier = Modifier.padding(16.dp),
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (draftItem.categoryName == cat.name) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                                    )
                                                }
                                            }
                                        }
                                    }
                                },
                                confirmButton = {
                                    TextButton(onClick = { showCategoryDialog = false }) {
                                        Text("Close")
                                    }
                                }
                            )
                        }

                        // Item Selector
                        val itemsForSelectedCategory = remember(draftItem.categoryName, allProductItems) {
                            allProductItems.filter { it.categoryName.equals(draftItem.categoryName, ignoreCase = true) }
                        }

                        OutlinedTextField(
                            value = draftItem.name,
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { if (draftItem.categoryName.isNotEmpty()) showItemDialog = true },
                            enabled = draftItem.categoryName.isNotEmpty(),
                            placeholder = { 
                                Text(
                                    if (draftItem.categoryName.isEmpty()) "Select category first..." 
                                    else "Select Item (Required)"
                                ) 
                            },
                            label = { Text("Product Item") },
                            trailingIcon = {
                                IconButton(
                                    onClick = { if (draftItem.categoryName.isNotEmpty()) showItemDialog = true },
                                    enabled = draftItem.categoryName.isNotEmpty()
                                ) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
                                }
                            },
                            shape = RoundedCornerShape(8.dp)
                        )

                        if (showItemDialog) {
                            AlertDialog(
                                onDismissRequest = { showItemDialog = false },
                                title = { Text("Select Item under ${draftItem.categoryName}", fontWeight = FontWeight.Bold) },
                                text = {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(max = 300.dp)
                                            .verticalScroll(rememberScrollState()),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        if (itemsForSelectedCategory.isEmpty()) {
                                            Text("No items registered under this category. Please create item master under '${draftItem.categoryName}' first.", style = MaterialTheme.typography.bodyMedium)
                                        } else {
                                            itemsForSelectedCategory.forEach { prodItem ->
                                                Card(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clickable {
                                                            val initialPrice = if (currentType == "SALE" && prodItem.defaultSellingRate != null) {
                                                                String.format(Locale.US, "%.2f", prodItem.defaultSellingRate)
                                                            } else {
                                                                draftItem.price
                                                            }
                                                            val initialDiscount = if (currentType == "SALE" && prodItem.defaultDiscountValue != null) {
                                                                val pVal = prodItem.defaultSellingRate ?: 0.0
                                                                val qVal = draftItem.quantity.toDoubleOrNull() ?: 1.0
                                                                if (prodItem.defaultDiscountType == "%") {
                                                                    String.format(Locale.US, "%.2f", pVal * qVal * prodItem.defaultDiscountValue / 100.0)
                                                                } else {
                                                                    String.format(Locale.US, "%.2f", prodItem.defaultDiscountValue)
                                                                }
                                                            } else {
                                                                draftItem.discount
                                                            }
                                                            viewModel.updateDraftItem(
                                                                index,
                                                                draftItem.copy(
                                                                    name = prodItem.name,
                                                                    hsnCode = prodItem.hsnCode ?: "",
                                                                    price = initialPrice,
                                                                    discount = initialDiscount
                                                                )
                                                            )
                                                            showItemDialog = false
                                                        },
                                                    colors = CardDefaults.cardColors(
                                                        containerColor = if (draftItem.name == prodItem.name) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                                    )
                                                ) {
                                                    Column(modifier = Modifier.padding(16.dp)) {
                                                        Text(
                                                            text = prodItem.name,
                                                            fontWeight = FontWeight.Bold,
                                                            color = if (draftItem.name == prodItem.name) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                                        )
                                                        if (!prodItem.hsnCode.isNullOrBlank()) {
                                                            Spacer(modifier = Modifier.height(2.dp))
                                                            Text(
                                                                text = "HSN Code: ${prodItem.hsnCode}",
                                                                style = MaterialTheme.typography.labelSmall,
                                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                },
                                confirmButton = {
                                    TextButton(onClick = { showItemDialog = false }) {
                                        Text("Close")
                                    }
                                }
                            )
                        }

                        // Dynamic Stock Display
                        if (draftItem.name.isNotEmpty()) {
                            val stockItem = stockBalances.find { it.name.trim().equals(draftItem.name.trim(), ignoreCase = true) }
                            val currentStockVal = stockItem?.currentStock ?: 0.0
                            val itemUnit = getDeterministicUnit(draftItem.name)
                            val currentStockStr = if (currentStockVal % 1 == 0.0) currentStockVal.toInt().toString() else String.format(Locale.US, "%.2f", currentStockVal)
                            
                            val qtyEnteredVal = draftItem.quantity.toDoubleOrNull() ?: 0.0
                            val isSale = currentType == "SALE"
                            val stockAfterVal = if (isSale) currentStockVal - qtyEnteredVal else currentStockVal + qtyEnteredVal
                            val stockAfterStr = if (stockAfterVal % 1 == 0.0) stockAfterVal.toInt().toString() else String.format(Locale.US, "%.2f", stockAfterVal)
                            val isOverStock = isSale && qtyEnteredVal > currentStockVal
                            
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isOverStock) {
                                    MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.9f)
                                } else {
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
                                },
                                modifier = Modifier
                                    .padding(vertical = 4.dp)
                                    .fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Info,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp),
                                                tint = if (isOverStock) {
                                                    MaterialTheme.colorScheme.error
                                                } else {
                                                    MaterialTheme.colorScheme.primary
                                                }
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Available Stock: $currentStockStr $itemUnit",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isOverStock) {
                                                    MaterialTheme.colorScheme.onErrorContainer
                                                } else {
                                                    MaterialTheme.colorScheme.onSurface
                                                }
                                            )
                                        }
                                        Text(
                                            text = if (isSale) "Stock After Sale: $stockAfterStr $itemUnit" else "Stock After Purchase: $stockAfterStr $itemUnit",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (isSale && stockAfterVal < 0.0) {
                                                MaterialTheme.colorScheme.error
                                            } else {
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                            }
                                        )
                                    }
                                    if (isOverStock) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "⚠️ Warning: Requested quantity exceeds available stock!",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }

                        // Auto Filled HSN Display
                        if (draftItem.hsnCode.isNotEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f),
                                modifier = Modifier
                                    .padding(vertical = 4.dp)
                                    .fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Info, 
                                        contentDescription = null, 
                                        modifier = Modifier.size(16.dp), 
                                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "HSN Code: ${draftItem.hsnCode}",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                        }

                        // Quantity & Price Row
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
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
                        }

                        // Discount & Taxable Value Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = draftItem.discount,
                                onValueChange = {
                                    viewModel.updateDraftItem(index, draftItem.copy(discount = it))
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("item_discount_$index"),
                                label = { Text("Discount (₹)") },
                                placeholder = { Text("0.00") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true
                            )

                            val priceVal = draftItem.price.toDoubleOrNull() ?: 0.0
                            val qtyVal = draftItem.quantity.toDoubleOrNull() ?: 0.0
                            val discountVal = draftItem.discount.toDoubleOrNull() ?: 0.0
                            val itemAmount = priceVal * qtyVal
                            val taxableAmount = (itemAmount - discountVal).coerceAtLeast(0.0)

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 4.dp),
                                horizontalAlignment = Alignment.End
                            ) {
                                Text(
                                    "Item Amount: ₹${String.format(Locale.US, "%.2f", itemAmount)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    "Taxable: ₹${String.format(Locale.US, "%.2f", taxableAmount)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
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

            // Tax Rate % Input Row
            OutlinedTextField(
                value = viewModel.formTax.value,
                onValueChange = { viewModel.formTax.value = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .testTag("form_tax"),
                label = { Text("Tax % (GST Rate)") },
                placeholder = { Text("0") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Calculations in real time
            val totalAmountBeforeDiscount = viewModel.formItems.sumOf { draft ->
                val p = draft.price.toDoubleOrNull() ?: 0.0
                val q = draft.quantity.toDoubleOrNull() ?: 0.0
                p * q
            }

            val totalDiscount = viewModel.formItems.sumOf { draft ->
                draft.discount.toDoubleOrNull() ?: 0.0
            }

            val taxableAmount = (totalAmountBeforeDiscount - totalDiscount).coerceAtLeast(0.0)
            val taxPercentage = viewModel.formTax.value.toDoubleOrNull() ?: 0.0
            val cgstVal = taxableAmount * (taxPercentage / 200.0)
            val sgstVal = taxableAmount * (taxPercentage / 200.0)
            val grandTotal = taxableAmount + cgstVal + sgstVal

            // Ordered invoice summary rows
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total Amount (Before Discount)", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("₹${String.format(Locale.US, "%.2f", totalAmountBeforeDiscount)}", fontWeight = FontWeight.Medium)
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total Discount", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("-₹${String.format(Locale.US, "%.2f", totalDiscount)}", fontWeight = FontWeight.Medium, color = Color(0xFFD32F2F))
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Taxable Amount", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("₹${String.format(Locale.US, "%.2f", taxableAmount)}", fontWeight = FontWeight.Bold)
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("CGST (${taxPercentage / 2}%)", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("₹${String.format(Locale.US, "%.2f", cgstVal)}", fontWeight = FontWeight.Medium)
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("SGST (${taxPercentage / 2}%)", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("₹${String.format(Locale.US, "%.2f", sgstVal)}", fontWeight = FontWeight.Medium)
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Grand Total", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text("₹${String.format(Locale.US, "%.2f", grandTotal)}", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
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

            if (currentType == "SALE") {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Payment,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        "Mark as Credit Sale",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        "Store outstanding amount and due date",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Switch(
                                checked = viewModel.formIsCreditSale.value,
                                onCheckedChange = { viewModel.formIsCreditSale.value = it }
                            )
                        }

                        if (viewModel.formIsCreditSale.value) {
                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                OutlinedTextField(
                                    value = viewModel.formOutstandingAmount.value,
                                    onValueChange = { viewModel.formOutstandingAmount.value = it },
                                    modifier = Modifier.weight(1f),
                                    label = { Text("Outstanding (₹)") },
                                    placeholder = { Text(String.format(Locale.US, "%.2f", grandTotal)) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    shape = RoundedCornerShape(12.dp),
                                    singleLine = true
                                )

                                var showDueDatePicker by remember { mutableStateOf(false) }
                                Box(modifier = Modifier.weight(1.2f)) {
                                    OutlinedTextField(
                                        value = sdf.format(Date(viewModel.formDueDate.value)),
                                        onValueChange = {},
                                        modifier = Modifier.fillMaxWidth(),
                                        label = { Text("Due Date") },
                                        shape = RoundedCornerShape(12.dp),
                                        readOnly = true,
                                        leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) }
                                    )
                                    Box(
                                        modifier = Modifier
                                            .matchParentSize()
                                            .clickable { showDueDatePicker = true }
                                    )
                                }

                                if (showDueDatePicker) {
                                    val dueDatePickerState = rememberDatePickerState(
                                        initialSelectedDateMillis = viewModel.formDueDate.value
                                    )
                                    DatePickerDialog(
                                        onDismissRequest = { showDueDatePicker = false },
                                        confirmButton = {
                                            TextButton(
                                                onClick = {
                                                    dueDatePickerState.selectedDateMillis?.let { selectedMillis ->
                                                        viewModel.formDueDate.value = selectedMillis
                                                    }
                                                    showDueDatePicker = false
                                                }
                                            ) {
                                                Text("OK")
                                            }
                                        },
                                        dismissButton = {
                                            TextButton(onClick = { showDueDatePicker = false }) {
                                                Text("Cancel")
                                            }
                                        }
                                    ) {
                                        DatePicker(state = dueDatePickerState)
                                    }
                                }
                            }
                        }
                    }
                }
            }

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
                onClick = { viewModel.goBack() },
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
    val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())

    val allProductItems by viewModel.productItems.collectAsStateWithLifecycle()

    var selectedFormat by remember { mutableStateOf(0) } // 0: A4 Tax Invoice, 1: 80mm Thermal Receipt

    val customersList by viewModel.customersWithBalance.collectAsStateWithLifecycle()
    val suppliersList by viewModel.suppliersWithBalance.collectAsStateWithLifecycle()

    val customerMatch = remember(inv.partyName, customersList) {
        customersList.firstOrNull { it.customer.name.equals(inv.partyName, ignoreCase = true) }?.customer
    }
    val supplierMatch = remember(inv.partyName, suppliersList) {
        suppliersList.firstOrNull { it.supplier.name.equals(inv.partyName, ignoreCase = true) }?.supplier
    }

    if (inv.type == "PURCHASE") {
        PurchaseDetailView(viewModel, item, supplierMatch)
        return
    }

    val profileState by viewModel.businessProfile.collectAsStateWithLifecycle()
    val profile = profileState

    val leftName = profile?.firmName ?: "Our Business"
    val leftAddress = profile?.address ?: "Address Not Saved"
    val leftMobile = profile?.mobileNumber ?: "Mobile Not Saved"
    val leftGstin = profile?.gstin ?: ""
    val leftEmail = profile?.email ?: ""

    val rightName = inv.partyName
    val rightAddress = if (inv.type == "SALE") (customerMatch?.address ?: "Address: Not Provided") else (supplierMatch?.address ?: "Address: Not Provided")
    val rightMobile = if (inv.type == "SALE") (customerMatch?.phone ?: "Mobile: Not Provided") else (supplierMatch?.phone ?: "Mobile: Not Provided")
    val rightEmail = if (inv.type == "SALE") (customerMatch?.email ?: "") else (supplierMatch?.email ?: "")
    val rightGstin = if (inv.type == "SALE") (customerMatch?.let { parseGstinFromNotes(it.notes) } ?: "") else (supplierMatch?.let { parseGstinFromNotes(it.notes) } ?: "")

    val totalAmountBeforeDiscountVal = item.items.sumOf { it.price * it.quantity }
    val totalDiscountVal = item.items.sumOf { it.discount }
    val taxableAmountVal = totalAmountBeforeDiscountVal - totalDiscountVal
    val taxRate = inv.tax
    val cgstVal = taxableAmountVal * (taxRate / 200.0)
    val sgstVal = taxableAmountVal * (taxRate / 200.0)
    val rawTotal = taxableAmountVal + cgstVal + sgstVal
    val roundedTotal = Math.round(rawTotal).toDouble()
    val roundOff = roundedTotal - rawTotal

    val paymentMode = if (inv.isCreditSale) "Credit" else "Cash"
    val amountInWords = convertNumberToWords(roundedTotal)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { viewModel.goBack() },
                    modifier = Modifier.testTag("btn_back_invoice_detail")
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (inv.type == "SALE") "Tax Invoice (GST)" else "Purchase Invoice",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            // Quick Delete Button
            var showDeleteCheck by remember { mutableStateOf(false) }
            var showDeleteLockVerify by remember { mutableStateOf(false) }
            IconButton(onClick = { showDeleteCheck = true }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete bill", tint = MaterialTheme.colorScheme.error)
            }

            if (showDeleteCheck) {
                AlertDialog(
                    onDismissRequest = { showDeleteCheck = false },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                if (viewModel.isTransactionSecurityEnabled()) {
                                    showDeleteLockVerify = true
                                } else {
                                    viewModel.deleteInvoice(inv.id)
                                    showDeleteCheck = false
                                }
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

            if (showDeleteLockVerify) {
                PasswordVerificationDialog(
                    viewModel = viewModel,
                    onVerified = {
                        showDeleteLockVerify = false
                        showDeleteCheck = false
                        viewModel.deleteInvoice(inv.id)
                    },
                    onDismiss = {
                        showDeleteLockVerify = false
                    }
                )
            }
        }

        // REDESIGNED TAB SELECTOR FOR CHOSING A4 vs 80mm
        TabRow(
            selectedTabIndex = selectedFormat,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .clip(RoundedCornerShape(12.dp)),
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            indicator = @Composable {}
        ) {
            Tab(
                selected = selectedFormat == 0,
                onClick = { selectedFormat = 0 },
                modifier = Modifier
                    .padding(4.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (selectedFormat == 0) MaterialTheme.colorScheme.primary else Color.Transparent),
                text = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = if (selectedFormat == 0) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "A4 GST Format",
                            color = if (selectedFormat == 0) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            )
            Tab(
                selected = selectedFormat == 1,
                onClick = { selectedFormat = 1 },
                modifier = Modifier
                    .padding(4.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (selectedFormat == 1) MaterialTheme.colorScheme.primary else Color.Transparent),
                text = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Print,
                            contentDescription = null,
                            tint = if (selectedFormat == 1) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "80mm Thermal POS",
                            color = if (selectedFormat == 1) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            )
        }

        // Receipt Card Visual Display
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.0f)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = if (selectedFormat == 0) Color.White else Color(0xFFF9F9F9))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                if (selectedFormat == 0) {
                    // ==========================================
                    // A4 TAX INVOICE FORMAT (COMPOSABLE VIEW)
                    // ==========================================
                    Text(
                        text = "TAX INVOICE",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF0F9D58),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    )

                    // Two Column Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Left Column: Firm Details (No header)
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            Text(
                                text = leftName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = Color.Black
                            )
                            Text(leftAddress, style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
                            Text("Mobile: $leftMobile", style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
                            if (leftGstin.isNotEmpty()) {
                                Text("GSTIN: $leftGstin", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color.Black)
                            }
                        }

                        // Vertical separator
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(120.dp)
                                .background(Color.LightGray.copy(alpha = 0.5f))
                        )

                        // Right Column: Party Details (No header)
                        Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                            Text(
                                text = rightName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = Color.Black
                            )
                            Text(rightAddress, style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
                            Text("Mobile: $rightMobile", style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
                            if (rightGstin.isNotEmpty()) {
                                Text("GSTIN: $rightGstin", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color.Black)
                            } else {
                                Text("GSTIN: Not Available", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Meta Row: Invoice No, Invoice Date
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF5F5F5))
                            .border(1.dp, Color.LightGray.copy(alpha = 0.5f))
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("INVOICE NO.", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
                            Text(inv.invoiceNumber, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("INVOICE DATE", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
                            Text(sdf.format(Date(inv.date)), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = Color.Black)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Product Table Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF222222))
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Product Name", style = MaterialTheme.typography.labelSmall, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.8f))
                        Text("HSN Code", style = MaterialTheme.typography.labelSmall, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.8f))
                        Text("Qty", style = MaterialTheme.typography.labelSmall, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.5f), textAlign = TextAlign.Center)
                        Text("Unit", style = MaterialTheme.typography.labelSmall, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.5f), textAlign = TextAlign.Center)
                        Text("Rate", style = MaterialTheme.typography.labelSmall, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.8f), textAlign = TextAlign.End)
                        Text("Discount", style = MaterialTheme.typography.labelSmall, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.8f), textAlign = TextAlign.End)
                        Text("Amount", style = MaterialTheme.typography.labelSmall, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.0f), textAlign = TextAlign.End)
                    }

                    // Product Table Rows
                    item.items.forEach { line ->
                        val qtyStr = if (line.quantity % 1 == 0.0) line.quantity.toInt().toString() else line.quantity.toString()
                        val hsn = getProductHsn(line.name, line.hsnCode, allProductItems)
                        val unit = getDeterministicUnit(line.name)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(0.5.dp, Color.LightGray.copy(alpha = 0.5f))
                                .padding(horizontal = 8.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(getCleanProductName(line.name), style = MaterialTheme.typography.bodySmall, color = Color.Black, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.8f))
                            Text(hsn, style = MaterialTheme.typography.bodySmall, color = Color.DarkGray, modifier = Modifier.weight(0.8f))
                            Text(qtyStr, style = MaterialTheme.typography.bodySmall, color = Color.Black, modifier = Modifier.weight(0.5f), textAlign = TextAlign.Center)
                            Text(unit, style = MaterialTheme.typography.bodySmall, color = Color.DarkGray, modifier = Modifier.weight(0.5f), textAlign = TextAlign.Center)
                            Text("₹${String.format(Locale.US, "%.2f", line.price)}", style = MaterialTheme.typography.bodySmall, color = Color.Black, modifier = Modifier.weight(0.8f), textAlign = TextAlign.End)
                            Text("₹${String.format(Locale.US, "%.2f", line.discount)}", style = MaterialTheme.typography.bodySmall, color = Color(0xFFD32F2F), modifier = Modifier.weight(0.8f), textAlign = TextAlign.End)
                            Text("₹${String.format(Locale.US, "%.2f", line.totalPrice)}", style = MaterialTheme.typography.bodySmall, color = Color.Black, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.0f), textAlign = TextAlign.End)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Amount in Words
                    Text(
                        text = "Amount in Words: $amountInWords",
                        style = MaterialTheme.typography.bodySmall,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        color = Color.DarkGray
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Bottom Columns: T&C and Financial Summary
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Left: Terms & Conditions
                        Column(modifier = Modifier.weight(1.2f).padding(end = 16.dp)) {
                            Text("TERMS & CONDITIONS", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("1. Goods once sold will not be returned or exchanged.", style = MaterialTheme.typography.bodySmall, color = Color.DarkGray, fontSize = 10.sp, lineHeight = 12.sp)
                            Text("2. Interest @ 18% p.a. will be charged after due date.", style = MaterialTheme.typography.bodySmall, color = Color.DarkGray, fontSize = 10.sp, lineHeight = 12.sp)
                            Text("3. All disputes subject to local jurisdiction only.", style = MaterialTheme.typography.bodySmall, color = Color.DarkGray, fontSize = 10.sp, lineHeight = 12.sp)
                        }

                        // Right: Calculations Summary
                        Column(modifier = Modifier.weight(1f)) {
                            // Total Amount (Before Discount)
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Total Amount (Before Discount):", style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
                                Text("₹${String.format(Locale.US, "%.2f", totalAmountBeforeDiscountVal)}", style = MaterialTheme.typography.bodySmall, color = Color.Black, fontWeight = FontWeight.Medium)
                            }
                            // Total Discount
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Total Discount:", style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
                                Text("-₹${String.format(Locale.US, "%.2f", totalDiscountVal)}", style = MaterialTheme.typography.bodySmall, color = Color(0xFFD32F2F), fontWeight = FontWeight.Medium)
                            }
                            // Taxable Amount
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Taxable Amount:", style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
                                Text("₹${String.format(Locale.US, "%.2f", taxableAmountVal)}", style = MaterialTheme.typography.bodySmall, color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                            // CGST & SGST
                            if (inv.tax > 0) {
                                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("CGST (${inv.tax / 2}%):", style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
                                    Text("₹${String.format(Locale.US, "%.2f", cgstVal)}", style = MaterialTheme.typography.bodySmall, color = Color.Black, fontWeight = FontWeight.Medium)
                                }
                                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("SGST (${inv.tax / 2}%):", style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
                                    Text("₹${String.format(Locale.US, "%.2f", sgstVal)}", style = MaterialTheme.typography.bodySmall, color = Color.Black, fontWeight = FontWeight.Medium)
                                }
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = Color.Gray.copy(alpha = 0.5f), thickness = 1.dp)

                            // Round Off
                            if (Math.abs(roundOff) > 0.001) {
                                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Round Off:", style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
                                    val sign = if (roundOff > 0.001) "+" else ""
                                    Text("₹$sign${String.format(Locale.US, "%.2f", roundOff)}", style = MaterialTheme.typography.bodySmall, color = Color.Black, fontWeight = FontWeight.Medium)
                                }
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = Color.Black, thickness = 1.5.dp)

                            // Grand Total
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("GRAND TOTAL:", style = MaterialTheme.typography.bodyMedium, color = Color.Black, fontWeight = FontWeight.Bold)
                                Text("₹${String.format(Locale.US, "%.2f", roundedTotal)}", style = MaterialTheme.typography.bodyLarge, color = Color.Black, fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            val amountRecd = if (inv.type == "SALE" && inv.isCreditSale) roundedTotal - inv.outstandingAmount else roundedTotal
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Amount Received:", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                Text("₹${String.format(Locale.US, "%.2f", amountRecd)}", style = MaterialTheme.typography.bodySmall, color = Color.Black)
                            }

                            val balDue = if (inv.type == "SALE" && inv.isCreditSale) inv.outstandingAmount else 0.0
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Balance Due:", style = MaterialTheme.typography.bodySmall, color = if (balDue > 0) Color(0xFFD32F2F) else Color.Gray, fontWeight = if (balDue > 0) FontWeight.Bold else FontWeight.Normal)
                                Text("₹${String.format(Locale.US, "%.2f", balDue)}", style = MaterialTheme.typography.bodySmall, color = if (balDue > 0) Color(0xFFD32F2F) else Color.Black, fontWeight = if (balDue > 0) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Signature block
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(end = 8.dp)) {
                            Text("For ${if (leftName == "Our Business" || leftName.isEmpty() || leftName.contains("Business")) "VM Tech Services" else leftName}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color.Black)
                            Spacer(modifier = Modifier.height(36.dp))
                            Box(modifier = Modifier.width(160.dp).height(1.dp).background(Color.Black))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Authorized Signature", style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
                        }
                    }

                } else {
                    // ==========================================
                    // 80mm THERMAL RECEIPT FORMAT (COMPOSABLE VIEW)
                    // ==========================================
                    Column(
                        modifier = Modifier
                            .widthIn(max = 300.dp)
                            .align(Alignment.CenterHorizontally),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Center Heading
                        Text(
                            text = leftName.uppercase(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = Color.Black,
                            textAlign = TextAlign.Center
                        )
                        Text(leftAddress, style = MaterialTheme.typography.bodySmall, color = Color.DarkGray, textAlign = TextAlign.Center)
                        Text("Ph: $leftMobile", style = MaterialTheme.typography.bodySmall, color = Color.DarkGray, textAlign = TextAlign.Center)
                        if (leftGstin.isNotEmpty()) {
                            Text("GSTIN: $leftGstin", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color.Black, textAlign = TextAlign.Center)
                        }

                        DashedLine(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp))

                        Text(
                            text = if (inv.type == "SALE") "CASH INVOICE (80mm)" else "PURCHASE RECORD",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Compact meta
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Bill No: ${inv.invoiceNumber}", style = MaterialTheme.typography.bodySmall, color = Color.Black, fontWeight = FontWeight.Bold)
                            Text("Date: ${sdf.format(Date(inv.date))}", style = MaterialTheme.typography.bodySmall, color = Color.Black)
                        }

                        DashedLine(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp))

                        // Bill to
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text("BILL TO:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.Gray)
                            Text(rightName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color.Black)
                            if (rightAddress.isNotEmpty() && rightAddress != "Address: Not Provided") {
                                Text(rightAddress, style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
                            }
                            Text("Ph: $rightMobile", style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
                            if (rightGstin.isNotEmpty()) {
                                Text("GSTIN: $rightGstin", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color.Black)
                            }
                        }

                        DashedLine(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp))

                        // Items Header
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text("Item / HSN", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.Black, modifier = Modifier.weight(1.8f))
                            Text("Qty/Unit", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.Black, modifier = Modifier.weight(0.8f), textAlign = TextAlign.Center)
                            Text("Rate", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.Black, modifier = Modifier.weight(0.9f), textAlign = TextAlign.End)
                            Text("Amount", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.Black, modifier = Modifier.weight(1.1f), textAlign = TextAlign.End)
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Items
                        item.items.forEach { line ->
                            val qtyStr = if (line.quantity % 1 == 0.0) line.quantity.toInt().toString() else line.quantity.toString()
                            val hsn = getProductHsn(line.name, line.hsnCode, allProductItems)
                            val unit = getDeterministicUnit(line.name)
                            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(getCleanProductName(line.name), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color.Black, modifier = Modifier.weight(1.8f))
                                    Text("₹${String.format(Locale.US, "%.2f", line.totalPrice)}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color.Black, modifier = Modifier.weight(1.1f), textAlign = TextAlign.End)
                                }
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    Text("HSN: $hsn | $qtyStr $unit @ ₹${String.format(Locale.US, "%.2f", line.price)}", style = MaterialTheme.typography.bodySmall, color = Color.DarkGray, modifier = Modifier.weight(2.5f))
                                    Text("[G:${inv.tax}%]", style = MaterialTheme.typography.bodySmall, color = Color.DarkGray, modifier = Modifier.weight(0.9f), textAlign = TextAlign.End)
                                }
                            }
                        }

                        DashedLine(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp))

                        // Calculations block
                        Column(modifier = Modifier.fillMaxWidth()) {
                            // Total Amount (Before Discount)
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Total Amount (Before Discount):", style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
                                Text("₹${String.format(Locale.US, "%.2f", totalAmountBeforeDiscountVal)}", style = MaterialTheme.typography.bodySmall, color = Color.Black)
                            }
                            // Total Discount
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Total Discount:", style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
                                Text("-₹${String.format(Locale.US, "%.2f", totalDiscountVal)}", style = MaterialTheme.typography.bodySmall, color = Color(0xFFD32F2F))
                            }
                            // Taxable Amount
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Taxable Amount:", style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
                                Text("₹${String.format(Locale.US, "%.2f", taxableAmountVal)}", style = MaterialTheme.typography.bodySmall, color = Color.Black, fontWeight = FontWeight.Bold)
                            }

                            // CGST & SGST
                            if (inv.tax > 0) {
                                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("CGST (${inv.tax / 2}%):", style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
                                    Text("₹${String.format(Locale.US, "%.2f", cgstVal)}", style = MaterialTheme.typography.bodySmall, color = Color.Black)
                                }
                                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("SGST (${inv.tax / 2}%):", style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
                                    Text("₹${String.format(Locale.US, "%.2f", sgstVal)}", style = MaterialTheme.typography.bodySmall, color = Color.Black)
                                }
                            }

                            DashedLine(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp))

                            // Round Off
                            if (Math.abs(roundOff) > 0.001) {
                                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Round Off:", style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
                                    val sign = if (roundOff > 0.001) "+" else ""
                                    Text("₹$sign${String.format(Locale.US, "%.2f", roundOff)}", style = MaterialTheme.typography.bodySmall, color = Color.Black)
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.Black))
                            Spacer(modifier = Modifier.height(2.dp))
                            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.Black))
                            Spacer(modifier = Modifier.height(4.dp))

                            // Grand Total
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("GRAND TOTAL:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color.Black)
                                Text("₹${String.format(Locale.US, "%.2f", roundedTotal)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color.Black)
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.Black))
                            Spacer(modifier = Modifier.height(4.dp))

                            val amountRecd = if (inv.type == "SALE" && inv.isCreditSale) roundedTotal - inv.outstandingAmount else roundedTotal
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Amt Received:", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                Text("₹${String.format(Locale.US, "%.2f", amountRecd)}", style = MaterialTheme.typography.bodySmall, color = Color.Black)
                            }

                            val balDue = if (inv.type == "SALE" && inv.isCreditSale) inv.outstandingAmount else 0.0
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Balance Due:", style = MaterialTheme.typography.bodySmall, color = if (balDue > 0) Color(0xFFD32F2F) else Color.Gray, fontWeight = if (balDue > 0) FontWeight.Bold else FontWeight.Normal)
                                Text("₹${String.format(Locale.US, "%.2f", balDue)}", style = MaterialTheme.typography.bodySmall, color = if (balDue > 0) Color(0xFFD32F2F) else Color.Black, fontWeight = if (balDue > 0) FontWeight.Bold else FontWeight.Normal)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Words: $amountInWords",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.DarkGray,
                            fontSize = 9.sp,
                            textAlign = TextAlign.Center
                        )

                        DashedLine(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp))

                        // Receipt Footer
                        Text(
                            text = "TERMS & CONDITIONS:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Text(
                            text = "Goods once sold are not returnable.\nInterest @ 18% charged if unpaid.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.DarkGray,
                            fontSize = 8.sp,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Thank you! Visit again.",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // Action Buttons Row: Print PDF & Share Receipt
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Print PDF Button
            Button(
                onClick = {
                    printInvoice(
                        context = context,
                        item = item,
                        profile = profile,
                        isA4 = (selectedFormat == 0),
                        customerMatch = customerMatch,
                        supplierMatch = supplierMatch,
                        allProductItems = allProductItems
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Print, contentDescription = "Print Invoice")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Print / PDF", fontWeight = FontWeight.Bold)
            }

            // Share text button
            OutlinedButton(
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
                    .weight(1f)
                    .height(52.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Share, contentDescription = "Share")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Share Text", fontWeight = FontWeight.Bold)
            }
        }
    }
}


fun parseGstinFromNotes(notes: String): String {
    val cleanNotes = notes.trim()
    if (cleanNotes.isEmpty()) return ""
    val lines = cleanNotes.split("\n")
    for (line in lines) {
        if (line.contains("GSTIN", ignoreCase = true)) {
            val parts = line.split(":")
            if (parts.size > 1) {
                return parts[1].trim()
            }
        }
    }
    return ""
}

fun getDeterministicHsn(name: String): String {
    // If the name contains a custom HSN code like "[HSN: 123456]", extract and return it
    val hsnPattern = "\\[HSN:\\s*(.*?)\\]".toRegex()
    val match = hsnPattern.find(name)
    if (match != null) {
        return match.groupValues[1].trim()
    }
    return "N/A"
}

fun getCleanProductName(name: String): String {
    val hsnPattern = "\\s*\\[HSN:\\s*(.*?)\\]".toRegex()
    return name.replace(hsnPattern, "").trim()
}

fun getProductHsn(lineName: String, lineHsn: String?, allProductItems: List<com.example.data.ProductItemEntity>): String {
    val cleanName = getCleanProductName(lineName)
    val matchedProd = allProductItems.find { it.name.trim().equals(cleanName, ignoreCase = true) }
    if (matchedProd != null) {
        val hsn = matchedProd.hsnCode?.trim().orEmpty()
        if (hsn.isNotEmpty()) return hsn
    }
    val nameHsnPattern = "\\[HSN:\\s*(.*?)\\]".toRegex()
    val match = nameHsnPattern.find(lineName)
    if (match != null) {
        val extracted = match.groupValues[1].trim()
        if (extracted.isNotEmpty()) return extracted
    }
    val fieldHsn = lineHsn?.trim().orEmpty()
    if (fieldHsn.isNotEmpty()) return fieldHsn
    return "N/A"
}

fun getDeterministicUnit(name: String): String {
    val lower = name.lowercase()
    return when {
        lower.contains("service") || lower.contains("labor") || lower.contains("fee") || lower.contains("charge") -> "SERV"
        lower.contains("kg") || lower.contains("gram") || lower.contains("weight") || lower.contains("cement") || lower.contains("sugar") -> "KG"
        lower.contains("liter") || lower.contains("liquid") || lower.contains("oil") || lower.contains("milk") -> "LTR"
        lower.contains("box") || lower.contains("carton") || lower.contains("pack") -> "BOX"
        lower.contains("meter") || lower.contains("wire") || lower.contains("pipe") || lower.contains("cable") -> "MTR"
        lower.contains("hour") || lower.contains("day") -> "HRS"
        else -> "PCS"
    }
}

fun convertNumberToWords(amount: Double): String {
    val num = amount.toLong()
    if (num == 0L) return "Zero Rupees Only"
    
    val units = arrayOf(
        "", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten",
        "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen", "Seventeen", "Eighteen", "Nineteen"
    )
    val tens = arrayOf(
        "", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety"
    )
    
    fun helper(n: Long): String {
        return when {
            n < 20 -> units[n.toInt()]
            n < 100 -> tens[(n / 10).toInt()] + if (n % 10 != 0L) " " + units[(n % 10).toInt()] else ""
            n < 1000 -> units[(n / 100).toInt()] + " Hundred" + if (n % 100 != 0L) " and " + helper(n % 100) else ""
            n < 100000 -> helper(n / 1000) + " Thousand" + if (n % 1000 != 0L) " " + helper(n % 1000) else ""
            n < 10000000 -> helper(n / 100000) + " Lakh" + if (n % 100000 != 0L) " " + helper(n % 100000) else ""
            else -> helper(n / 10000000) + " Crore" + if (n % 10000000 != 0L) " " + helper(n % 10000000) else ""
        }
    }
    
    val paisa = Math.round((amount - num) * 100).toInt()
    val rupeesStr = helper(num) + " Rupees"
    val paisaStr = if (paisa > 0) " and " + helper(paisa.toLong()) + " Paisa" else ""
    return "$rupeesStr$paisaStr Only"
}

fun printInvoice(
    context: Context,
    item: InvoiceWithItems,
    profile: com.example.ui.BusinessProfile?,
    isA4: Boolean,
    customerMatch: com.example.data.CustomerEntity?,
    supplierMatch: com.example.data.SupplierEntity?,
    allProductItems: List<com.example.data.ProductItemEntity>
) {
    val html = generateInvoiceHtml(item, profile, isA4, customerMatch, supplierMatch, allProductItems)
    val webView = WebView(context)
    webView.webViewClient = object : WebViewClient() {
        override fun onPageFinished(view: WebView?, url: String?) {
            val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
            val jobName = "Invoice_${item.invoice.invoiceNumber}"
            val printAdapter = webView.createPrintDocumentAdapter(jobName)
            val printAttributes = PrintAttributes.Builder()
                .setMediaSize(if (isA4) PrintAttributes.MediaSize.ISO_A4 else PrintAttributes.MediaSize.JPN_YOU4)
                .build()
            printManager.print(jobName, printAdapter, printAttributes)
        }
    }
    webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
}

fun generateInvoiceHtml(
    item: InvoiceWithItems,
    profile: com.example.ui.BusinessProfile?,
    isA4: Boolean,
    customerMatch: com.example.data.CustomerEntity?,
    supplierMatch: com.example.data.SupplierEntity?,
    allProductItems: List<com.example.data.ProductItemEntity>
): String {
    val inv = item.invoice
    val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    val totalAmountBeforeDiscountVal = item.items.sumOf { it.price * it.quantity }
    val totalDiscountVal = item.items.sumOf { it.discount }
    val taxableAmountVal = totalAmountBeforeDiscountVal - totalDiscountVal
    val taxRate = inv.tax
    val cgstVal = taxableAmountVal * (taxRate / 200.0)
    val sgstVal = taxableAmountVal * (taxRate / 200.0)
    val rawTotal = taxableAmountVal + cgstVal + sgstVal
    val roundedTotal = Math.round(rawTotal).toDouble()
    val roundOff = roundedTotal - rawTotal
    
    val paymentMode = if (inv.isCreditSale) "Credit" else "Cash"
    
    val leftName = profile?.firmName ?: "Our Business"
    val leftAddress = profile?.address ?: "Address Not Saved"
    val leftMobile = profile?.mobileNumber ?: "Mobile Not Saved"
    val leftGstin = profile?.gstin ?: ""
    val leftEmail = profile?.email ?: ""
    
    val rightName = inv.partyName
    val rightAddress = if (inv.type == "SALE") (customerMatch?.address ?: "Not Provided") else (supplierMatch?.address ?: "Not Provided")
    val rightMobile = if (inv.type == "SALE") (customerMatch?.phone ?: "Not Provided") else (supplierMatch?.phone ?: "Not Provided")
    val rightEmail = if (inv.type == "SALE") (customerMatch?.email ?: "") else (supplierMatch?.email ?: "")
    val rightGstin = if (inv.type == "SALE") (customerMatch?.let { parseGstinFromNotes(it.notes) } ?: "") else (supplierMatch?.let { parseGstinFromNotes(it.notes) } ?: "")
    
    val amountInWords = convertNumberToWords(roundedTotal)
    
    if (isA4) {
        return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8">
            <style>
                body { font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; color: #333; margin: 0; padding: 20px; font-size: 13px; line-height: 1.4; }
                .invoice-container { border: 1px solid #ccc; padding: 30px; max-width: 800px; margin: 0 auto; background: #fff; box-shadow: 0 0 10px rgba(0,0,0,0.05); }
                .invoice-title { text-align: center; font-size: 20px; font-weight: bold; letter-spacing: 2px; margin-bottom: 25px; border-bottom: 2px solid #333; padding-bottom: 10px; color: #1a1a1a; }
                .header-table { width: 100%; border-collapse: collapse; margin-bottom: 25px; }
                .header-cell { width: 50%; vertical-align: top; padding: 0 10px; box-sizing: border-box; }
                .header-cell:first-child { padding-left: 0; border-right: 1px solid #eee; }
                .header-cell:last-child { padding-right: 0; }
                .section-title { font-size: 11px; text-transform: uppercase; color: #888; font-weight: bold; margin-bottom: 8px; letter-spacing: 1px; }
                .firm-name { font-size: 16px; font-weight: bold; color: #000; margin-bottom: 5px; }
                .meta-table { width: 100%; border-collapse: collapse; margin-bottom: 25px; background: #f9f9f9; border: 1px solid #eee; }
                .meta-table td { padding: 8px 12px; border: 1px solid #eee; }
                .meta-label { font-weight: bold; color: #555; font-size: 11px; text-transform: uppercase; }
                .product-table { width: 100%; border-collapse: collapse; margin-bottom: 25px; }
                .product-table th { background: #333; color: #fff; text-align: left; padding: 8px 10px; font-size: 11px; text-transform: uppercase; font-weight: bold; }
                .product-table td { padding: 10px; border-bottom: 1px solid #eee; vertical-align: middle; }
                .text-right { text-align: right; }
                .text-center { text-align: center; }
                .summary-table { width: 100%; border-collapse: collapse; margin-top: 15px; }
                .summary-table td { padding: 6px 12px; }
                .summary-label { font-weight: bold; color: #555; text-align: right; width: 75%; }
                .summary-value { text-align: right; font-weight: bold; width: 25%; }
                .grand-total-row td { border-top: 1px double #333; border-bottom: 1px double #333; background: #f5f5f5; padding: 10px 12px; }
                .invoice-title { text-align: center; font-size: 20px; font-weight: bold; letter-spacing: 2px; margin-bottom: 25px; border-bottom: 2px solid #0F9D58; padding-bottom: 10px; color: #0F9D58; }
                .header-table { width: 100%; border-collapse: collapse; margin-bottom: 25px; }
                .header-cell { width: 50%; vertical-align: top; padding: 0 10px; box-sizing: border-box; }
                .header-cell:first-child { padding-left: 0; border-right: 1px solid #eee; }
                .header-cell:last-child { padding-right: 0; }
                .firm-name { font-size: 16px; font-weight: bold; color: #000; margin-bottom: 5px; }
                .meta-table { width: 100%; border-collapse: collapse; margin-bottom: 25px; background: #f9f9f9; border: 1px solid #eee; }
                .meta-table td { padding: 8px 12px; border: 1px solid #eee; }
                .meta-label { font-weight: bold; color: #555; font-size: 11px; text-transform: uppercase; }
                .product-table { width: 100%; border-collapse: collapse; margin-bottom: 25px; }
                .product-table th { background: #333; color: #fff; text-align: left; padding: 8px 10px; font-size: 11px; text-transform: uppercase; font-weight: bold; }
                .product-table td { padding: 10px; border-bottom: 1px solid #eee; vertical-align: middle; }
                .text-right { text-align: right; }
                .text-center { text-align: center; }
                .summary-table { width: 100%; border-collapse: collapse; margin-top: 15px; }
                .summary-table td { padding: 6px 12px; }
                .summary-label { font-weight: bold; color: #555; text-align: right; width: 75%; }
                .summary-value { text-align: right; font-weight: bold; width: 25%; }
                .grand-total-row td { border-top: 1px double #333; border-bottom: 1px double #333; background: #f5f5f5; padding: 10px 12px; }
                .grand-total-label { font-size: 14px; font-weight: bold; color: #000; text-align: right; }
                .grand-total-value { font-size: 16px; font-weight: bold; color: #000; text-align: right; }
                .bottom-section { display: table; width: 100%; margin-top: 30px; border-top: 1px solid #eee; padding-top: 20px; }
                .bottom-left { display: table-cell; width: 60%; vertical-align: top; padding-right: 20px; }
                .bottom-right { display: table-cell; width: 40%; vertical-align: bottom; text-align: right; }
                .terms-title { font-weight: bold; font-size: 11px; text-transform: uppercase; color: #555; margin-bottom: 6px; }
                .terms-text { font-size: 11px; color: #777; margin: 0; padding-left: 15px; }
                .sig-box { border-top: 1px solid #333; display: inline-block; width: 180px; text-align: center; padding-top: 5px; margin-top: 50px; font-weight: bold; font-size: 11px; }
                .amount-words { font-style: italic; color: #555; margin-bottom: 15px; font-size: 12px; }
            </style>
        </head>
        <body>
            <div class="invoice-container">
                <div class="invoice-title">TAX INVOICE</div>
                
                <table class="header-table">
                    <tr>
                        <td class="header-cell">
                            <div class="firm-name">$leftName</div>
                            <div>$leftAddress</div>
                            <div>Mobile: $leftMobile</div>
                            ${if (leftGstin.isNotEmpty()) "<div><strong>GSTIN: $leftGstin</strong></div>" else ""}
                        </td>
                        <td class="header-cell" style="padding-left: 20px;">
                            <div class="firm-name">$rightName</div>
                            <div>$rightAddress</div>
                            <div>Mobile: $rightMobile</div>
                            ${if (rightGstin.isNotEmpty()) "<div><strong>GSTIN: $rightGstin</strong></div>" else ""}
                        </td>
                    </tr>
                </table>
                
                <table class="meta-table">
                    <tr>
                        <td><span class="meta-label">Invoice No:</span><br><strong>${inv.invoiceNumber}</strong></td>
                        <td><span class="meta-label">Invoice Date:</span><br><strong>${sdf.format(Date(inv.date))}</strong></td>
                    </tr>
                </table>
                
                <table class="product-table">
                    <thead>
                        <tr>
                            <th>Product Name</th>
                            <th>HSN Code</th>
                            <th class="text-center">Qty</th>
                            <th class="text-center">Unit</th>
                            <th class="text-right">Rate</th>
                            <th class="text-right">Discount</th>
                            <th class="text-right">Amount</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${item.items.joinToString("") { line ->
                            val qtyStr = if (line.quantity % 1 == 0.0) line.quantity.toInt().toString() else line.quantity.toString()
                            val hsn = getProductHsn(line.name, line.hsnCode, allProductItems)
                            val unit = getDeterministicUnit(line.name)
                            """
                            <tr>
                                <td><strong>${getCleanProductName(line.name)}</strong></td>
                                <td>$hsn</td>
                                <td class="text-center">$qtyStr</td>
                                <td class="text-center">$unit</td>
                                <td class="text-right">₹${String.format(Locale.US, "%.2f", line.price)}</td>
                                <td class="text-right">₹${String.format(Locale.US, "%.2f", line.discount)}</td>
                                <td class="text-right">₹${String.format(Locale.US, "%.2f", line.totalPrice)}</td>
                            </tr>
                            """.trimIndent()
                        }}
                    </tbody>
                </table>
                
                <div class="amount-words"><strong>Amount in Words:</strong> $amountInWords</div>
                
                <table class="summary-table">
                    <tr>
                        <td class="summary-label">Total Amount (Before Discount):</td>
                        <td class="summary-value">₹${String.format(Locale.US, "%.2f", totalAmountBeforeDiscountVal)}</td>
                    </tr>
                    <tr>
                        <td class="summary-label">Total Discount:</td>
                        <td class="summary-value" style="color: #D32F2F;">-₹${String.format(Locale.US, "%.2f", totalDiscountVal)}</td>
                    </tr>
                    <tr>
                        <td class="summary-label">Taxable Amount:</td>
                        <td class="summary-value">₹${String.format(Locale.US, "%.2f", taxableAmountVal)}</td>
                    </tr>
                    ${if (inv.tax > 0) """
                    <tr>
                        <td class="summary-label" style="padding-top: 12px;">CGST (${inv.tax / 2}%):</td>
                        <td class="summary-value" style="padding-top: 12px;">₹${String.format(Locale.US, "%.2f", cgstVal)}</td>
                    </tr>
                    <tr>
                        <td class="summary-label">SGST (${inv.tax / 2}%):</td>
                        <td class="summary-value">₹${String.format(Locale.US, "%.2f", sgstVal)}</td>
                    </tr>
                    """ else ""}
                    ${if (Math.abs(roundOff) > 0.001) """
                    <tr>
                        <td class="summary-label" style="padding-top: 12px;">Round Off:</td>
                        <td class="summary-value" style="padding-top: 12px;">₹${if (roundOff > 0.001) "+" else ""}${String.format(Locale.US, "%.2f", roundOff)}</td>
                    </tr>
                    """ else ""}
                    <tr class="grand-total-row">
                        <td class="grand-total-label">GRAND TOTAL:</td>
                        <td class="grand-total-value">₹${String.format(Locale.US, "%.2f", roundedTotal)}</td>
                    </tr>
                    ${if (inv.type == "SALE" && inv.isCreditSale) """
                    <tr>
                        <td class="summary-label">Amount Received:</td>
                        <td class="summary-value">₹${String.format(Locale.US, "%.2f", roundedTotal - inv.outstandingAmount)}</td>
                    </tr>
                    <tr>
                        <td class="summary-label" style="color:#D32F2F;">Balance Due:</td>
                        <td class="summary-value" style="color:#D32F2F;">₹${String.format(Locale.US, "%.2f", inv.outstandingAmount)}</td>
                    </tr>
                    """ else """
                    <tr>
                        <td class="summary-label">Amount Received:</td>
                        <td class="summary-value">₹${String.format(Locale.US, "%.2f", roundedTotal)}</td>
                    </tr>
                    <tr>
                        <td class="summary-label">Balance Due:</td>
                        <td class="summary-value">₹0.00</td>
                    </tr>
                    """}
                </table>
                
                <div class="bottom-section">
                    <div class="bottom-left">
                        <div class="terms-title">Terms & Conditions:</div>
                        <ol class="terms-text">
                            <li>Goods once sold will not be returned or exchanged.</li>
                            <li>Interest at 18% per annum will be charged if unpaid.</li>
                            <li>All disputes are subject to local jurisdiction only.</li>
                        </ol>
                    </div>
                    <div class="bottom-right">
                        <div>For <strong>${if (leftName == "Our Business" || leftName.isEmpty() || leftName.contains("Business")) "VM Tech Services" else leftName}</strong></div>
                        <div class="sig-box">Authorized Signature</div>
                    </div>
                </div>
            </div>
        </body>
        </html>
        """.trimIndent()
    } else {
        return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8">
            <style>
                body { font-family: 'Courier New', Courier, monospace; color: #000; margin: 0; padding: 10px; font-size: 11px; line-height: 1.3; width: 280px; }
                .receipt-container { width: 100%; margin: 0 auto; background: #fff; }
                .center { text-align: center; }
                .bold { font-weight: bold; }
                .firm-name { font-size: 14px; font-weight: bold; text-transform: uppercase; margin-bottom: 4px; }
                .divider { border-top: 1px dashed #000; margin: 8px 0; }
                .meta-row { display: flex; justify-content: space-between; margin-bottom: 3px; }
                .meta-label { font-weight: bold; }
                .product-list { width: 100%; margin-top: 8px; border-collapse: collapse; }
                .product-list th { text-align: left; border-bottom: 1px dashed #000; padding: 4px 0; font-size: 10px; }
                .product-list td { padding: 4px 0; vertical-align: top; }
                .text-right { text-align: right; }
                .summary-box { margin-top: 8px; font-size: 11px; }
                .summary-row { display: flex; justify-content: space-between; padding: 3px 0; }
                .grand-total { font-size: 13px; font-weight: bold; border-top: 1px dashed #000; border-bottom: 1px dashed #000; padding: 5px 0; margin-top: 5px; }
                .amount-words { font-style: italic; font-size: 9px; margin-top: 5px; text-transform: capitalize; }
                .terms { font-size: 9px; text-align: center; margin-top: 15px; }
                .footer { text-align: center; margin-top: 15px; font-size: 10px; }
            </style>
        </head>
        <body>
            <div class="receipt-container">
                <div class="center">
                    <div class="firm-name">$leftName</div>
                    <div>$leftAddress</div>
                    <div>Ph: $leftMobile</div>
                    ${if (leftGstin.isNotEmpty()) "<div>GSTIN: $leftGstin</div>" else ""}
                </div>
                
                <div class="divider"></div>
                <div class="center bold" style="font-size: 12px; margin-bottom: 5px;">TAX INVOICE (80mm)</div>
                
                <div class="meta-row"><span class="meta-label">Bill No:</span><span>${inv.invoiceNumber}</span></div>
                <div class="meta-row"><span class="meta-label">Date:</span><span>${sdf.format(Date(inv.date))}</span></div>
                
                <div class="divider"></div>
                
                <div>
                    <span class="bold">BILL TO:</span><br>
                    $rightName<br>
                    ${if (rightAddress.isNotEmpty() && rightAddress != "Not Provided") "$rightAddress<br>" else ""}
                    Ph: $rightMobile<br>
                    ${if (rightGstin.isNotEmpty()) "GSTIN: $rightGstin<br>" else ""}
                </div>
                
                <div class="divider"></div>
                
                <table class="product-list">
                    <thead>
                        <tr>
                            <th style="width: 50%;">Item/HSN</th>
                            <th class="text-right" style="width: 15%;">Qty</th>
                            <th class="text-right" style="width: 15%;">Rate</th>
                            <th class="text-right" style="width: 20%;">Total</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${item.items.joinToString("") { line ->
                            val qtyStr = if (line.quantity % 1 == 0.0) line.quantity.toInt().toString() else line.quantity.toString()
                            val hsn = getProductHsn(line.name, line.hsnCode, allProductItems)
                            val unit = getDeterministicUnit(line.name)
                            """
                            <tr>
                                <td>
                                    <strong>${getCleanProductName(line.name)}</strong><br>
                                    <span style="font-size: 8px; color: #555;">HSN: $hsn | Disc: ₹${String.format(Locale.US, "%.1f", line.discount)}</span>
                                </td>
                                <td class="text-right">$qtyStr $unit</td>
                                <td class="text-right">${String.format(Locale.US, "%.1f", line.price)}</td>
                                <td class="text-right">${String.format(Locale.US, "%.1f", line.totalPrice)}</td>
                            </tr>
                            """.trimIndent()
                        }}
                    </tbody>
                </table>
                
                <div class="divider"></div>
                
                <div class="summary-box">
                    <div class="summary-row"><span>Total Before Disc:</span><span>₹${String.format(Locale.US, "%.2f", totalAmountBeforeDiscountVal)}</span></div>
                    <div class="summary-row"><span>Total Discount:</span><span>-₹${String.format(Locale.US, "%.2f", totalDiscountVal)}</span></div>
                    <div class="summary-row"><span>Taxable Amount:</span><span>₹${String.format(Locale.US, "%.2f", taxableAmountVal)}</span></div>
                    ${if (inv.tax > 0) """
                    <div class="summary-row" style="margin-top: 4px;"><span>CGST (${inv.tax / 2}%):</span><span>₹${String.format(Locale.US, "%.2f", cgstVal)}</span></div>
                    <div class="summary-row"><span>SGST (${inv.tax / 2}%):</span><span>₹${String.format(Locale.US, "%.2f", sgstVal)}</span></div>
                    """ else ""}
                    
                    <div class="divider"></div>
                    
                    ${if (Math.abs(roundOff) > 0.001) """
                    <div class="summary-row" style="margin-top: 4px;"><span>Round Off:</span><span>₹${if (roundOff > 0.001) "+" else ""}${String.format(Locale.US, "%.2f", roundOff)}</span></div>
                    """ else ""}
                    
                    <div class="summary-row grand-total">
                        <span>GRAND TOTAL:</span>
                        <span>₹${String.format(Locale.US, "%.2f", roundedTotal)}</span>
                    </div>
                    ${if (inv.type == "SALE" && inv.isCreditSale) """
                    <div class="summary-row"><span>Amount Recd:</span><span>₹${String.format(Locale.US, "%.2f", roundedTotal - inv.outstandingAmount)}</span></div>
                    <div class="summary-row bold" style="color: #D32F2F;"><span>Balance Due:</span><span>₹${String.format(Locale.US, "%.2f", inv.outstandingAmount)}</span></div>
                    """ else """
                    <div class="summary-row"><span>Amount Recd:</span><span>₹${String.format(Locale.US, "%.2f", roundedTotal)}</span></div>
                    <div class="summary-row"><span>Balance Due:</span><span>₹0.00</span></div>
                    """}
                </div>
                
                <div class="amount-words bold">Words: $amountInWords</div>
                
                <div class="divider"></div>
                
                <div class="terms">
                    <span class="bold">TERMS & CONDITIONS:</span><br>
                    Goods once sold will not be exchanged.<br>
                    Interest @ 18% p.a. charged after due date.
                </div>
                
                <div class="footer">
                    <strong>Thank you! Visit again.</strong><br>
                    Generated by VM BOOK App
                </div>
            </div>
        </body>
        </html>
        """.trimIndent()
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
    val stockEntries by viewModel.stockBalances.collectAsStateWithLifecycle()

    var activeTab by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header with Back Arrow
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.goBack() },
                modifier = Modifier.testTag("btn_back_profit_loss")
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "Profit & Loss Statement",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Real-time ledger tracking margins and balances",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Business Profile Header if saved
        val businessProfileState by viewModel.businessProfile.collectAsStateWithLifecycle()
        businessProfileState?.let { profile ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (profile.businessLogoUri.isNotEmpty()) {
                    Image(
                        painter = rememberAsyncImagePainter(model = profile.businessLogoUri),
                        contentDescription = "Business Logo",
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp)),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                }
                Column {
                    Text(
                        text = profile.firmName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Ph: ${profile.mobileNumber} | ${profile.address}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            HorizontalDivider(modifier = Modifier.padding(bottom = 16.dp))
        }

        // Summary Card
        val isProfit = stats.totalProfit >= 0
        val profitMargin = if (stats.totalSales > 0) {
            (stats.totalProfit / stats.totalSales) * 100.0
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
                            text = "₹${String.format(Locale.US, "%.2f", stats.totalProfit)}",
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
                            text = "Remaining Stock (+)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "₹${String.format(Locale.US, "%.2f", stats.remainingStockValue)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E88E5)
                        )
                    }
                }
            }
        }

        // Tab Row for selecting Ledger vs. Stock Balance
        TabRow(
            selectedTabIndex = activeTab,
            modifier = Modifier.padding(bottom = 16.dp),
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary,
            divider = {}
        ) {
            Tab(
                selected = activeTab == 0,
                onClick = { activeTab = 0 },
                text = { Text("P&L Ledger", fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.ShowChart, contentDescription = null) }
            )
            Tab(
                selected = activeTab == 1,
                onClick = { activeTab = 1 },
                text = { Text("Stock Balance", fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.Inventory, contentDescription = null) }
            )
        }

        if (activeTab == 0) {
            // LEDGER TAB
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
                        text = "Net Profit",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(1.2f)
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

                                // Column 4: Net P&L of this transaction and running balance
                                val isSaleRow = entry.saleAmount != null
                                val rowProfit = entry.rowProfitLoss
                                val isRowProfit = rowProfit >= 0
                                Column(
                                    modifier = Modifier.weight(1.2f),
                                    horizontalAlignment = Alignment.End
                                ) {
                                    Text(
                                        text = if (isSaleRow) "₹${String.format(Locale.US, "%.0f", rowProfit)}" else "—",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSaleRow) {
                                            if (isRowProfit) Color(0xFF0F9D58) else Color(0xFFD93025)
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                        },
                                        textAlign = TextAlign.End
                                    )
                                    Text(
                                        text = "Bal: ₹${String.format(Locale.US, "%.0f", entry.runningBalance)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                        textAlign = TextAlign.End
                                    )
                                }
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        }
                    }
                }
            }
        } else {
            // STOCK BALANCE TAB
            Text(
                text = "PRODUCT STOCK BALANCE COLUMN",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Stock Table Header
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
                        text = "Item Name / Valuation",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1.8f)
                    )
                    Text(
                        text = "Qty In",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(0.9f),
                        color = Color(0xFF1E88E5)
                    )
                    Text(
                        text = "Qty Out",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(0.9f),
                        color = Color(0xFFE65100)
                    )
                    Text(
                        text = "Stock Bal",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(1.2f)
                    )
                }
            }

            // Stock List
            if (stockEntries.isEmpty()) {
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
                            Icons.Default.Inventory,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No stock items registered. Add some sales or purchases first.",
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
                    items(stockEntries) { stockItem ->
                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Column 1: Name and Valuation details
                                Column(modifier = Modifier.weight(1.8f)) {
                                    Text(
                                        text = stockItem.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Cost: ₹${String.format(Locale.US, "%.1f", stockItem.averagePurchasePrice)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = " • ",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "Val: ₹${String.format(Locale.US, "%.0f", stockItem.currentStockValue)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }

                                // Column 2: Qty Purchased (In)
                                Text(
                                    text = String.format(Locale.US, "%.1f", stockItem.quantityPurchased),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF1E88E5),
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.weight(0.9f)
                                )

                                // Column 3: Qty Sold (Out)
                                Text(
                                    text = String.format(Locale.US, "%.1f", stockItem.quantitySold),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFFE65100),
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.weight(0.9f)
                                )

                                // Column 4: Remaining Stock
                                val isLowStock = stockItem.currentStock <= 0
                                Box(
                                    modifier = Modifier
                                        .weight(1.2f)
                                        .padding(start = 4.dp),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(
                                                if (isLowStock) Color(0xFFFCE8E6) else Color(0xFFE2F3EB)
                                            )
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = String.format(Locale.US, "%.1f", stockItem.currentStock),
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isLowStock) Color(0xFFD93025) else Color(0xFF0F9D58),
                                            textAlign = TextAlign.End
                                        )
                                    }
                                }
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessProfileScreen(viewModel: BillingViewModel) {
    val context = LocalContext.current
    val currentProfile by viewModel.businessProfile.collectAsStateWithLifecycle()

    var firmName by remember { mutableStateOf("") }
    var mobileNumber by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var gstin by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var logoUri by remember { mutableStateOf("") }

    var showErrorMsg by remember { mutableStateOf<String?>(null) }

    // Initialize fields from saved profile
    LaunchedEffect(currentProfile) {
        currentProfile?.let {
            firmName = it.firmName
            mobileNumber = it.mobileNumber
            address = it.address
            gstin = it.gstin
            email = it.email
            logoUri = it.businessLogoUri
        }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                val file = java.io.File(context.filesDir, "business_logo.png")
                val outputStream = java.io.FileOutputStream(file)
                inputStream?.use { input ->
                    outputStream.use { output ->
                        input.copyTo(output)
                    }
                }
                logoUri = file.absolutePath
            } catch (e: Exception) {
                e.printStackTrace()
                logoUri = it.toString()
            }
        }
    }

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
            IconButton(
                onClick = { viewModel.goBack() },
                modifier = Modifier.testTag("btn_back_profile")
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go back")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = currentProfile?.firmName?.takeIf { it.isNotEmpty() } ?: "VM Book",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Business Profile Settings",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Scrollable Form
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            // Logo Picker Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (logoUri.isNotEmpty()) {
                            Image(
                                painter = rememberAsyncImagePainter(model = logoUri),
                                contentDescription = "Business Logo",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Business,
                                contentDescription = "No Logo",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Button(
                            onClick = { launcher.launch("image/*") },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Select Logo", style = MaterialTheme.typography.labelLarge)
                        }
                        if (logoUri.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            TextButton(
                                onClick = { logoUri = "" },
                                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                            ) {
                                Text("Remove Logo")
                            }
                        }
                    }
                }
            }

            if (showErrorMsg != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Text(
                        text = showErrorMsg ?: "",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Firm Name (Required)
            OutlinedTextField(
                value = firmName,
                onValueChange = { firmName = it },
                label = { Text("Firm Name (Required) *") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .testTag("input_firm_name"),
                singleLine = true
            )

            // Mobile Number (Required)
            OutlinedTextField(
                value = mobileNumber,
                onValueChange = { mobileNumber = it },
                label = { Text("Mobile Number (Required) *") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .testTag("input_mobile_number"),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )

            // Address (Required)
            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Address (Required) *") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .testTag("input_address"),
                minLines = 2,
                maxLines = 4
            )

            // GSTIN (Optional)
            OutlinedTextField(
                value = gstin,
                onValueChange = { gstin = it },
                label = { Text("GSTIN (Optional)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .testTag("input_gstin"),
                singleLine = true
            )

            // Email (Optional)
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email (Optional)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
                    .testTag("input_email"),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            // Save Button
            Button(
                onClick = {
                    if (firmName.trim().isEmpty()) {
                        showErrorMsg = "Firm Name is required"
                    } else if (mobileNumber.trim().isEmpty()) {
                        showErrorMsg = "Mobile Number is required"
                    } else if (address.trim().isEmpty()) {
                        showErrorMsg = "Address is required"
                    } else {
                        showErrorMsg = null
                        val profile = BusinessProfile(
                            firmName = firmName.trim(),
                            mobileNumber = mobileNumber.trim(),
                            address = address.trim(),
                            gstin = gstin.trim(),
                            email = email.trim(),
                            businessLogoUri = logoUri
                        )
                        viewModel.saveBusinessProfile(profile)
                        android.widget.Toast.makeText(context, "Business Profile Saved Successfully", android.widget.Toast.LENGTH_SHORT).show()
                        viewModel.goBack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .padding(bottom = 16.dp)
                    .testTag("action_save_profile"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Save Profile", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            }
        }

        // Fixed Footer at the bottom
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.ic_vm_book_logo),
                    contentDescription = "VM Book Logo",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "VM Book",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "v1.0.0",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
            Text(
                text = "Developed with ❤️ in India",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "© 2026 VM Book. All Rights Reserved.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreditRemindersScreen(viewModel: BillingViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val invoices by viewModel.invoices.collectAsStateWithLifecycle()
    val businessProfileState by viewModel.businessProfile.collectAsStateWithLifecycle()
    
    val firmName = businessProfileState?.firmName?.takeIf { it.isNotEmpty() } ?: "VM Book"
    
    // States
    var searchQuery by remember { mutableStateOf("") }
    
    // Filtered credit sales invoices with outstanding amount > 0
    val creditSales = invoices.filter {
        it.invoice.type == "SALE" && 
        it.invoice.isCreditSale && 
        it.invoice.outstandingAmount > 0 &&
        (searchQuery.isEmpty() || it.invoice.partyName.contains(searchQuery, ignoreCase = true) || it.invoice.invoiceNumber.contains(searchQuery, ignoreCase = true))
    }.sortedBy { it.invoice.dueDate }

    val totalOutstanding = creditSales.sumOf { it.invoice.outstandingAmount }
    val overdueCount = creditSales.count { it.invoice.dueDate > 0L && it.invoice.dueDate < System.currentTimeMillis() }
    
    // State for payment update dialog
    var showPaymentDialogForInvoice by remember { mutableStateOf<InvoiceWithItems?>(null) }
    var paymentAmountInput by remember { mutableStateOf("") }
    var paymentMode by remember { mutableStateOf("Cash") }
    var refNo by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var formDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(showPaymentDialogForInvoice) {
        if (showPaymentDialogForInvoice != null) {
            val invoice = showPaymentDialogForInvoice!!.invoice
            paymentAmountInput = String.format(Locale.US, "%.2f", invoice.outstandingAmount)
            paymentMode = "Cash"
            refNo = ""
            notes = "Received towards Invoice #${invoice.invoiceNumber}"
            formDate = System.currentTimeMillis()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Top App Bar/Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.goBack() },
                modifier = Modifier.testTag("action_back_to_dashboard")
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "Credit Reminders",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Track outstanding dues and send alerts",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Summary Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "TOTAL CREDIT OUTSTANDING",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "₹${String.format(Locale.US, "%.2f", totalOutstanding)}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                
                if (overdueCount > 0) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "$overdueCount OVERDUE",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search by customer name or invoice #...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = null)
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .testTag("search_credit_reminders")
        )

        // Title List
        Text(
            text = "OUTSTANDING BILLS (${creditSales.size})",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (creditSales.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (searchQuery.isEmpty()) "No outstanding credit sales! 🎉" else "No matching bills found",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (searchQuery.isEmpty()) "All credit payments are cleared." else "Try adjusting your search query",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(creditSales) { item ->
                    val invoice = item.invoice
                    val now = System.currentTimeMillis()
                    val isOverdue = invoice.dueDate > 0L && invoice.dueDate < now
                    
                    val dateSdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                    val formattedDueDate = if (invoice.dueDate > 0L) dateSdf.format(Date(invoice.dueDate)) else "Not Set"
                    
                    val statusText = if (isOverdue) "OVERDUE" else "Pending"
                    val statusColor = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("credit_invoice_card_${invoice.id}"),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(
                            1.dp, 
                            if (isOverdue) MaterialTheme.colorScheme.error.copy(alpha = 0.3f) else MaterialTheme.colorScheme.outlineVariant
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Top Header: Customer Name and Due/Overdue status
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = invoice.partyName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Inv #${invoice.invoiceNumber} • ${dateSdf.format(Date(invoice.date))}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(statusColor.copy(alpha = 0.1f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = statusText,
                                        color = statusColor,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(12.dp))

                            // Details block: Outstanding vs Total & Due Date
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "Due Date",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = formattedDueDate,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "Outstanding / Total",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Row(verticalAlignment = Alignment.Bottom) {
                                        Text(
                                            text = "₹${String.format(Locale.US, "%.2f", invoice.outstandingAmount)}",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = " / ₹${String.format(Locale.US, "%.2f", invoice.totalAmount)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Action buttons: Share, WhatsApp Reminder, Record Payment
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // WhatsApp button
                                Button(
                                    onClick = {
                                        // WhatsApp reminder message construction
                                        val message = """
                                            Dear ${invoice.partyName},
                                            
                                            This is a friendly payment reminder from *${firmName}*.
                                            
                                            An outstanding balance of *₹${String.format(Locale.US, "%.2f", invoice.outstandingAmount)}* is pending against Invoice Number *#${invoice.invoiceNumber}* which is due on *${formattedDueDate}*.
                                            
                                            Kindly arrange the payment at your earliest convenience.
                                            
                                            Thank you!
                                            
                                            Regards,
                                            *${firmName}*
                                        """.trimIndent()
                                        
                                        try {
                                            val uri = Uri.parse("https://api.whatsapp.com/send?text=${Uri.encode(message)}")
                                            val intent = Intent(Intent.ACTION_VIEW, uri)
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            android.widget.Toast.makeText(context, "WhatsApp is not installed", android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Share, 
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("WhatsApp", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                                }

                                // General Share button (for sharing Reminders, and can serve as option to share text or documents)
                                OutlinedButton(
                                    onClick = {
                                        val message = """
                                            Dear ${invoice.partyName},
                                            
                                            Please find the outstanding payment detail for Invoice #${invoice.invoiceNumber}.
                                            Pending Amount: ₹${String.format(Locale.US, "%.2f", invoice.outstandingAmount)}
                                            Due Date: ${formattedDueDate}
                                            
                                            Regards,
                                            ${firmName}
                                        """.trimIndent()
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_SUBJECT, "Payment Reminder - $firmName")
                                            putExtra(Intent.EXTRA_TEXT, message)
                                        }
                                        context.startActivity(Intent.createChooser(shareIntent, "Share Payment Reminder"))
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Send, 
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Share Text", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                                }

                                // Receive Payment / Collect dues button
                                Button(
                                    onClick = {
                                        showPaymentDialogForInvoice = item
                                        paymentAmountInput = String.format(Locale.US, "%.2f", invoice.outstandingAmount)
                                    },
                                    modifier = Modifier.weight(1.3f).testTag("customer_receive_payment_btn"),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF0F9D58), // Green for collecting payment
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowDownward, 
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Receive ₹", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Payment Dialog for recording payment received on the credit sale invoice!
    if (showPaymentDialogForInvoice != null) {
        val invoiceItem = showPaymentDialogForInvoice!!
        val invoice = invoiceItem.invoice
        AlertDialog(
            onDismissRequest = { showPaymentDialogForInvoice = null },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowDownward,
                        contentDescription = null,
                        tint = Color(0xFF0F9D58),
                        modifier = Modifier.size(24.dp)
                    )
                    Text("Receive Payment", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Info Card with Customer Name and Invoice Details
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Customer: ${invoice.partyName}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Invoice #${invoice.invoiceNumber} • Total: ₹${String.format(Locale.US, "%.2f", invoice.totalAmount)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Outstanding Balance: ₹${String.format(Locale.US, "%.2f", invoice.outstandingAmount)}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Payment Amount text field
                    OutlinedTextField(
                        value = paymentAmountInput,
                        onValueChange = { paymentAmountInput = it },
                        label = { Text("Amount to Receive (₹) *") },
                        placeholder = { Text("0.00") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF0F9D58),
                            focusedLabelColor = Color(0xFF0F9D58)
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("payment_receive_amount_input")
                    )

                    // Live calculation of remaining balance
                    val enteredAmount = paymentAmountInput.toDoubleOrNull() ?: 0.0
                    val remainingBalance = (invoice.outstandingAmount - enteredAmount).coerceAtLeast(0.0)
                    val isFullPayment = enteredAmount >= invoice.outstandingAmount

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Remaining Outstanding:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "₹${String.format(Locale.US, "%.2f", remainingBalance)}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (remainingBalance > 0.0) MaterialTheme.colorScheme.error else Color(0xFF0F9D58)
                        )
                    }

                    if (isFullPayment) {
                        Text(
                            text = "🎉 Full Payment (Outstanding balance will be fully cleared)",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF0F9D58),
                            fontWeight = FontWeight.SemiBold
                        )
                    } else if (enteredAmount > 0.0) {
                        Text(
                            text = "ℹ️ Partial Payment (Remaining balance will remain outstanding)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // Payment Mode Selector (Cash/Bank)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Cash", "Bank").forEach { mode ->
                            val isSelected = paymentMode == mode
                            OutlinedCard(
                                onClick = { paymentMode = mode },
                                modifier = Modifier.weight(1f).height(40.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.outlinedCardColors(
                                    containerColor = if (isSelected) Color(0xFF0F9D58).copy(alpha = 0.1f) else Color.Transparent
                                ),
                                border = BorderStroke(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) Color(0xFF0F9D58) else MaterialTheme.colorScheme.outline
                                )
                            ) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text(
                                        text = mode,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (isSelected) Color(0xFF0F9D58) else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    // Date Picker field
                    Box(modifier = Modifier.fillMaxWidth()) {
                        val dateSdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                        OutlinedTextField(
                            value = dateSdf.format(Date(formDate)),
                            onValueChange = {},
                            label = { Text("Payment Date") },
                            modifier = Modifier.fillMaxWidth(),
                            readOnly = true,
                            leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Color(0xFF0F9D58)) },
                            shape = RoundedCornerShape(12.dp)
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { showDatePicker = true }
                        )
                    }

                    if (paymentMode == "Bank") {
                        OutlinedTextField(
                            value = refNo,
                            onValueChange = { refNo = it },
                            label = { Text("Reference / Trans No (Optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes (Optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val enteredAmount = paymentAmountInput.toDoubleOrNull() ?: 0.0
                        if (enteredAmount <= 0.0) {
                            android.widget.Toast.makeText(context, "Please enter a valid payment amount", android.widget.Toast.LENGTH_SHORT).show()
                        } else {
                            val remainingBalance = (invoice.outstandingAmount - enteredAmount).coerceAtLeast(0.0)
                            
                            viewModel.addCustomerPaymentWithInvoiceUpdate(
                                partyName = invoice.partyName,
                                paymentAmount = enteredAmount,
                                invoiceId = invoice.id,
                                newOutstandingAmount = remainingBalance,
                                date = formDate,
                                paymentMode = paymentMode,
                                referenceNo = refNo,
                                notes = notes
                            )
                            
                            showPaymentDialogForInvoice = null
                            android.widget.Toast.makeText(context, "Payment received successfully!", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0F9D58),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Receive Payment", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPaymentDialogForInvoice = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showDatePicker) {
        val today = java.util.Calendar.getInstance()
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = formDate,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    val cellLocal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")).apply {
                        timeInMillis = utcTimeMillis
                    }
                    val localToday = java.util.Calendar.getInstance()
                    return !cellLocal.after(localToday)
                }

                override fun isSelectableYear(year: Int): Boolean {
                    val currentYear = today.get(java.util.Calendar.YEAR)
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
                            val localCal = java.util.Calendar.getInstance().apply {
                                timeInMillis = selected
                                val now = java.util.Calendar.getInstance()
                                set(java.util.Calendar.HOUR_OF_DAY, now.get(java.util.Calendar.HOUR_OF_DAY))
                                set(java.util.Calendar.MINUTE, now.get(java.util.Calendar.MINUTE))
                                set(java.util.Calendar.SECOND, now.get(java.util.Calendar.SECOND))
                            }
                            formDate = localCal.timeInMillis
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK", color = Color(0xFF0F9D58), fontWeight = FontWeight.Bold)
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
// NEW "MORE" MENU SUB-SCREENS (IMPLEMENTATION)
// ==========================================

@Composable
fun MoreScreen(viewModel: BillingViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "More Options",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = "Configure, backup & manage business metrics",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        @Composable
        fun CategoryHeader(title: String) {
            Text(
                text = title.uppercase(Locale.getDefault()),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        @Composable
        fun MoreOptionItem(
            title: String,
            description: String,
            icon: androidx.compose.ui.graphics.vector.ImageVector,
            iconTint: Color = MaterialTheme.colorScheme.primary,
            testTag: String,
            onClick: () -> Unit
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .testTag(testTag)
                    .clickable { onClick() },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = iconTint.copy(alpha = 0.12f),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(icon, contentDescription = title, tint = iconTint, modifier = Modifier.size(22.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        CategoryHeader("Financial Analysis")
        MoreOptionItem(
            title = "Profit & Loss",
            description = "Track overall sales, purchase, cost of goods & net profit margins",
            icon = Icons.Default.TrendingUp,
            iconTint = Color(0xFF0F9D58),
            testTag = "more_profit_loss",
            onClick = { viewModel.setScreen(BillingScreen.PROFIT_LOSS) }
        )

        CategoryHeader("Business Management")
        MoreOptionItem(
            title = "Products",
            description = "Check unique products list, total purchase/sale qty & stock balance",
            icon = Icons.Default.Inventory,
            iconTint = Color(0xFF1E88E5),
            testTag = "more_products",
            onClick = { viewModel.setScreen(BillingScreen.PRODUCTS) }
        )
        MoreOptionItem(
            title = "Business Profile",
            description = "Update firm name, mobile number, address, email & GSTIN info",
            icon = Icons.Default.Store,
            iconTint = Color(0xFFFFB300),
            testTag = "more_profile",
            onClick = { viewModel.setScreen(BillingScreen.BUSINESS_PROFILE) }
        )

        CategoryHeader("Data Utilities")
        MoreOptionItem(
            title = "Backup & Restore",
            description = "Securely backup your complete database and restore when needed",
            icon = Icons.Default.CloudUpload,
            iconTint = Color(0xFF8E24AA),
            testTag = "more_backup",
            onClick = { viewModel.setScreen(BillingScreen.BACKUP_RESTORE) }
        )
        MoreOptionItem(
            title = "Export Data",
            description = "Download company logs, billing transaction ledgers as PDF or Excel",
            icon = Icons.Default.FileDownload,
            iconTint = Color(0xFF00ACC1),
            testTag = "more_export",
            onClick = { viewModel.setScreen(BillingScreen.EXPORT_DATA) }
        )

        CategoryHeader("App Information")
        MoreOptionItem(
            title = "Settings",
            description = "Configure custom invoice prefix, billing terms and formats",
            icon = Icons.Default.Settings,
            iconTint = Color(0xFF5D4037),
            testTag = "more_settings",
            onClick = { viewModel.setScreen(BillingScreen.SETTINGS) }
        )
        MoreOptionItem(
            title = "Help & Support",
            description = "Explore help documentations, FAQs and contact technical support desk",
            icon = Icons.Default.Help,
            iconTint = Color(0xFF0F9D58),
            testTag = "more_help",
            onClick = { viewModel.setScreen(BillingScreen.HELP_SUPPORT) }
        )
        MoreOptionItem(
            title = "About VM BOOK",
            description = "System hardware diagnostics, application version & software info",
            icon = Icons.Default.Info,
            iconTint = Color(0xFF37474F),
            testTag = "more_about",
            onClick = { viewModel.setScreen(BillingScreen.ABOUT_VM_BOOK) }
        )
    }
}

@Composable
fun ProductsScreen(viewModel: BillingViewModel) {
    val categories by viewModel.productCategories.collectAsStateWithLifecycle()
    val productItems by viewModel.productItems.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var selectedTab by remember { mutableStateOf(0) } // 0: Product Items, 1: Product Categories

    // Search and Filter states
    var itemSearchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf<String?>(null) }
    var categorySearchQuery by remember { mutableStateOf("") }

    // Dialog states for Categories
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var categoryToEdit by remember { mutableStateOf<com.example.data.ProductCategoryEntity?>(null) }
    var categoryToDelete by remember { mutableStateOf<com.example.data.ProductCategoryEntity?>(null) }

    // Dialog states for Items
    var showAddItemDialog by remember { mutableStateOf(false) }
    var itemToEdit by remember { mutableStateOf<com.example.data.ProductItemEntity?>(null) }
    var itemToDelete by remember { mutableStateOf<com.example.data.ProductItemEntity?>(null) }

    val filteredItems = remember(productItems, itemSearchQuery, selectedCategoryFilter) {
        productItems.filter { item ->
            val matchesSearch = item.name.contains(itemSearchQuery, ignoreCase = true) ||
                    (item.hsnCode?.contains(itemSearchQuery, ignoreCase = true) ?: false)
            val matchesCategory = selectedCategoryFilter == null || item.categoryName.equals(selectedCategoryFilter, ignoreCase = true)
            matchesSearch && matchesCategory
        }
    }

    val filteredCategories = remember(categories, categorySearchQuery) {
        categories.filter { cat ->
            cat.name.contains(categorySearchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (selectedTab == 0) {
                        showAddItemDialog = true
                    } else {
                        showAddCategoryDialog = true
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("fab_add_master")
            ) {
                Icon(
                    Icons.Default.Add, 
                    contentDescription = if (selectedTab == 0) "Add Product Item" else "Add Category"
                )
            }
        },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.goBack() },
                    modifier = Modifier.testTag("btn_back_products")
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Inventory Masters",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Material 3 Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                containerColor = Color.Transparent,
                divider = {}
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { 
                        Text(
                            text = "Stock Items (${productItems.size})",
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                        ) 
                    },
                    modifier = Modifier.testTag("tab_stock_items")
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { 
                        Text(
                            text = "Stock Groups (${categories.size})",
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                        ) 
                    },
                    modifier = Modifier.testTag("tab_stock_groups")
                )
            }

            if (selectedTab == 0) {
                // STOCK ITEMS TAB
                // Search Bar
                OutlinedTextField(
                    value = itemSearchQuery,
                    onValueChange = { itemSearchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .testTag("search_items_input"),
                    placeholder = { Text("Search item name or HSN...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                // Category Filter Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Filter Group:",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(end = 8.dp)
                    )

                    var showFilterDropdown by remember { mutableStateOf(false) }

                    Surface(
                        onClick = { showFilterDropdown = true },
                        shape = RoundedCornerShape(8.dp),
                        color = if (selectedCategoryFilter != null) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = selectedCategoryFilter ?: "All Stock Groups",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedCategoryFilter != null) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                Icons.Default.ArrowDropDown, 
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (selectedCategoryFilter != null) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (showFilterDropdown) {
                        AlertDialog(
                            onDismissRequest = { showFilterDropdown = false },
                            title = { Text("Filter by Stock Group", fontWeight = FontWeight.Bold) },
                            text = {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 250.dp)
                                        .verticalScroll(rememberScrollState()),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                selectedCategoryFilter = null
                                                showFilterDropdown = false
                                            },
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (selectedCategoryFilter == null) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                        )
                                    ) {
                                        Text(
                                            "All Stock Groups (No Filter)",
                                            modifier = Modifier.padding(12.dp),
                                            fontWeight = FontWeight.Bold,
                                            color = if (selectedCategoryFilter == null) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    categories.forEach { cat ->
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    selectedCategoryFilter = cat.name
                                                    showFilterDropdown = false
                                                },
                                            colors = CardDefaults.cardColors(
                                                containerColor = if (selectedCategoryFilter == cat.name) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                            )
                                        ) {
                                            Text(
                                                cat.name,
                                                modifier = Modifier.padding(12.dp),
                                                fontWeight = FontWeight.Bold,
                                                color = if (selectedCategoryFilter == cat.name) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            },
                            confirmButton = {
                                TextButton(onClick = { showFilterDropdown = false }) {
                                    Text("Close")
                                }
                            }
                        )
                    }
                }

                // Items list
                if (filteredItems.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Inventory,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No stock items found",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredItems) { item ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            // Category badge
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                                            ) {
                                                Text(
                                                    text = item.categoryName,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }

                                            // HSN badge
                                            if (!item.hsnCode.isNullOrBlank()) {
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f)
                                                ) {
                                                    Text(
                                                        text = "HSN: ${item.hsnCode}",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    // Action buttons
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        IconButton(onClick = { itemToEdit = item }) {
                                            Icon(
                                                Icons.Default.Edit, 
                                                contentDescription = "Edit Item",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        IconButton(onClick = { itemToDelete = item }) {
                                            Icon(
                                                Icons.Default.Delete, 
                                                contentDescription = "Delete Item",
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // STOCK GROUPS (CATEGORIES) TAB
                // Search Bar
                OutlinedTextField(
                    value = categorySearchQuery,
                    onValueChange = { categorySearchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .testTag("search_groups_input"),
                    placeholder = { Text("Search category name...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                // Categories list
                if (filteredCategories.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Category,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No product categories found",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredCategories) { cat ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = cat.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        val itemCount = remember(cat.name, productItems) {
                                            productItems.count { it.categoryName.equals(cat.name, ignoreCase = true) }
                                        }
                                        Text(
                                            text = "$itemCount Stock Items registered",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        IconButton(onClick = { categoryToEdit = cat }) {
                                            Icon(
                                                Icons.Default.Edit, 
                                                contentDescription = "Edit Category",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        IconButton(onClick = { categoryToDelete = cat }) {
                                            Icon(
                                                Icons.Default.Delete, 
                                                contentDescription = "Delete Category",
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // DIALOGS SECTION

    // 1. Add Category Dialog
    if (showAddCategoryDialog) {
        var catName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddCategoryDialog = false },
            title = { Text("Add Stock Group (Category)", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Enter a unique group name (e.g. Battery, Tyre, Oil)", style = MaterialTheme.typography.bodyMedium)
                    OutlinedTextField(
                        value = catName,
                        onValueChange = { catName = it },
                        placeholder = { Text("Category Name") },
                        modifier = Modifier.fillMaxWidth().testTag("input_category_name"),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (catName.trim().isBlank()) {
                            android.widget.Toast.makeText(context, "Group name cannot be blank!", android.widget.Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.addCategory(catName.trim())
                            android.widget.Toast.makeText(context, "Group added successfully!", android.widget.Toast.LENGTH_SHORT).show()
                            showAddCategoryDialog = false
                        }
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Add Group")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCategoryDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // 2. Edit Category Dialog
    categoryToEdit?.let { cat ->
        var catName by remember { mutableStateOf(cat.name) }
        AlertDialog(
            onDismissRequest = { categoryToEdit = null },
            title = { Text("Edit Stock Group (Category)", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Modify Group Name:", style = MaterialTheme.typography.bodyMedium)
                    OutlinedTextField(
                        value = catName,
                        onValueChange = { catName = it },
                        modifier = Modifier.fillMaxWidth().testTag("edit_category_name_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (catName.trim().isBlank()) {
                            android.widget.Toast.makeText(context, "Group name cannot be blank!", android.widget.Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.editCategory(cat, catName.trim())
                            android.widget.Toast.makeText(context, "Group modified!", android.widget.Toast.LENGTH_SHORT).show()
                            categoryToEdit = null
                        }
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                TextButton(onClick = { categoryToEdit = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // 3. Delete Category Dialog
    categoryToDelete?.let { cat ->
        AlertDialog(
            onDismissRequest = { categoryToDelete = null },
            title = { Text("Delete Stock Group?", fontWeight = FontWeight.Bold) },
            text = {
                Text("Are you sure you want to delete the group '${cat.name}'? Existing stock items in this group will remain, but the group itself will be removed from masters.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteCategory(cat)
                        android.widget.Toast.makeText(context, "Group deleted!", android.widget.Toast.LENGTH_SHORT).show()
                        categoryToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { categoryToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // 4. Add Item Dialog
    if (showAddItemDialog) {
        var selectedCatName by remember { mutableStateOf("") }
        var itemName by remember { mutableStateOf("") }
        var hsnCode by remember { mutableStateOf("") }
        var defaultSellingRateStr by remember { mutableStateOf("") }
        var defaultDiscountValueStr by remember { mutableStateOf("") }
        var defaultDiscountType by remember { mutableStateOf("Rs") }
        var showCatSelector by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showAddItemDialog = false },
            title = { Text("Add Stock Item", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 1. Select Group (Required)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Select Group *", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        OutlinedTextField(
                            value = selectedCatName,
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth().clickable { showCatSelector = true },
                            trailingIcon = {
                                IconButton(onClick = { showCatSelector = true }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                            },
                            placeholder = { Text("Choose a group...") },
                            shape = RoundedCornerShape(8.dp)
                        )
                    }

                    if (showCatSelector) {
                        AlertDialog(
                            onDismissRequest = { showCatSelector = false },
                            title = { Text("Select Stock Group", fontWeight = FontWeight.Bold) },
                            text = {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 200.dp)
                                        .verticalScroll(rememberScrollState()),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    if (categories.isEmpty()) {
                                        Text("No groups available. Please create a group first.")
                                    } else {
                                        categories.forEach { cat ->
                                            Card(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        selectedCatName = cat.name
                                                        showCatSelector = false
                                                    },
                                                colors = CardDefaults.cardColors(
                                                    containerColor = if (selectedCatName == cat.name) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                                )
                                            ) {
                                                Text(cat.name, modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            },
                            confirmButton = {
                                TextButton(onClick = { showCatSelector = false }) {
                                    Text("Close")
                                }
                            }
                        )
                    }

                    // 2. Item Name (Required)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Item Name *", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        OutlinedTextField(
                            value = itemName,
                            onValueChange = { itemName = it },
                            placeholder = { Text("e.g. GF Z4") },
                            modifier = Modifier.fillMaxWidth().testTag("input_item_name"),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )
                    }

                    // 3. Default Selling Rate (Optional)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Default Selling Rate (Optional)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        OutlinedTextField(
                            value = defaultSellingRateStr,
                            onValueChange = { defaultSellingRateStr = it },
                            leadingIcon = { Text("₹", style = MaterialTheme.typography.bodyMedium) },
                            placeholder = { Text("0.00") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth().testTag("input_item_selling_rate"),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )
                    }

                    // 4. Default Discount (Optional)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Default Discount (Optional)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            var showTypeDropdown by remember { mutableStateOf(false) }
                            Box(modifier = Modifier.width(100.dp)) {
                                OutlinedTextField(
                                    value = defaultDiscountType,
                                    onValueChange = {},
                                    readOnly = true,
                                    modifier = Modifier.fillMaxWidth().clickable { showTypeDropdown = true },
                                    trailingIcon = {
                                        Icon(
                                            Icons.Default.ArrowDropDown,
                                            contentDescription = null,
                                            modifier = Modifier.clickable { showTypeDropdown = true }
                                        )
                                    },
                                    shape = RoundedCornerShape(8.dp)
                                )
                                DropdownMenu(
                                    expanded = showTypeDropdown,
                                    onDismissRequest = { showTypeDropdown = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Rs") },
                                        onClick = {
                                            defaultDiscountType = "Rs"
                                            showTypeDropdown = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("%") },
                                        onClick = {
                                            defaultDiscountType = "%"
                                            showTypeDropdown = false
                                        }
                                    )
                                }
                            }

                            OutlinedTextField(
                                value = defaultDiscountValueStr,
                                onValueChange = { defaultDiscountValueStr = it },
                                placeholder = { Text("__________") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f).testTag("input_item_discount_val"),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }

                    // 5. HSN Code (Optional)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("HSN Code (Optional)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        OutlinedTextField(
                            value = hsnCode,
                            onValueChange = { hsnCode = it },
                            placeholder = { Text("e.g. 85071000") },
                            modifier = Modifier.fillMaxWidth().testTag("input_item_hsn"),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (selectedCatName.isBlank()) {
                            android.widget.Toast.makeText(context, "Stock Group is required!", android.widget.Toast.LENGTH_SHORT).show()
                        } else if (itemName.trim().isBlank()) {
                            android.widget.Toast.makeText(context, "Item name is required!", android.widget.Toast.LENGTH_SHORT).show()
                        } else {
                            val rateVal = defaultSellingRateStr.toDoubleOrNull()
                            val discVal = defaultDiscountValueStr.toDoubleOrNull()
                            viewModel.addProductItem(
                                selectedCatName,
                                itemName.trim(),
                                hsnCode.trim().ifBlank { null },
                                rateVal,
                                discVal,
                                if (discVal != null) defaultDiscountType else null
                            )
                            android.widget.Toast.makeText(context, "Stock Item added!", android.widget.Toast.LENGTH_SHORT).show()
                            showAddItemDialog = false
                        }
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Add Item")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddItemDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // 5. Edit Item Dialog
    itemToEdit?.let { item ->
        var selectedCatName by remember { mutableStateOf(item.categoryName) }
        var itemName by remember { mutableStateOf(item.name) }
        var hsnCode by remember { mutableStateOf(item.hsnCode ?: "") }
        var defaultSellingRateStr by remember { mutableStateOf(item.defaultSellingRate?.let { String.format(Locale.US, "%.2f", it) } ?: "") }
        var defaultDiscountValueStr by remember { mutableStateOf(item.defaultDiscountValue?.let { String.format(Locale.US, "%.2f", it) } ?: "") }
        var defaultDiscountType by remember { mutableStateOf(item.defaultDiscountType ?: "Rs") }
        var showCatSelector by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { itemToEdit = null },
            title = { Text("Edit Stock Item", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 1. Select Group (Required)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Select Group *", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        OutlinedTextField(
                            value = selectedCatName,
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth().clickable { showCatSelector = true },
                            trailingIcon = {
                                IconButton(onClick = { showCatSelector = true }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                            },
                            placeholder = { Text("Choose a group...") },
                            shape = RoundedCornerShape(8.dp)
                        )
                    }

                    if (showCatSelector) {
                        AlertDialog(
                            onDismissRequest = { showCatSelector = false },
                            title = { Text("Select Stock Group", fontWeight = FontWeight.Bold) },
                            text = {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 200.dp)
                                        .verticalScroll(rememberScrollState()),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    categories.forEach { cat ->
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    selectedCatName = cat.name
                                                    showCatSelector = false
                                                },
                                            colors = CardDefaults.cardColors(
                                                containerColor = if (selectedCatName == cat.name) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                            )
                                        ) {
                                            Text(cat.name, modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            },
                            confirmButton = {
                                TextButton(onClick = { showCatSelector = false }) {
                                    Text("Close")
                                }
                            }
                        )
                    }

                    // 2. Item Name (Required)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Item Name *", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        OutlinedTextField(
                            value = itemName,
                            onValueChange = { itemName = it },
                            placeholder = { Text("e.g. GF Z4") },
                            modifier = Modifier.fillMaxWidth().testTag("edit_item_name_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )
                    }

                    // 3. Default Selling Rate (Optional)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Default Selling Rate (Optional)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        OutlinedTextField(
                            value = defaultSellingRateStr,
                            onValueChange = { defaultSellingRateStr = it },
                            leadingIcon = { Text("₹", style = MaterialTheme.typography.bodyMedium) },
                            placeholder = { Text("0.00") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth().testTag("edit_item_selling_rate"),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )
                    }

                    // 4. Default Discount (Optional)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Default Discount (Optional)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            var showTypeDropdown by remember { mutableStateOf(false) }
                            Box(modifier = Modifier.width(100.dp)) {
                                OutlinedTextField(
                                    value = defaultDiscountType,
                                    onValueChange = {},
                                    readOnly = true,
                                    modifier = Modifier.fillMaxWidth().clickable { showTypeDropdown = true },
                                    trailingIcon = {
                                        Icon(
                                            Icons.Default.ArrowDropDown,
                                            contentDescription = null,
                                            modifier = Modifier.clickable { showTypeDropdown = true }
                                        )
                                    },
                                    shape = RoundedCornerShape(8.dp)
                                )
                                DropdownMenu(
                                    expanded = showTypeDropdown,
                                    onDismissRequest = { showTypeDropdown = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Rs") },
                                        onClick = {
                                            defaultDiscountType = "Rs"
                                            showTypeDropdown = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("%") },
                                        onClick = {
                                            defaultDiscountType = "%"
                                            showTypeDropdown = false
                                        }
                                    )
                                }
                            }

                            OutlinedTextField(
                                value = defaultDiscountValueStr,
                                onValueChange = { defaultDiscountValueStr = it },
                                placeholder = { Text("__________") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f).testTag("edit_item_discount_val"),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }

                    // 5. HSN Code (Optional)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("HSN Code (Optional)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        OutlinedTextField(
                            value = hsnCode,
                            onValueChange = { hsnCode = it },
                            placeholder = { Text("e.g. 85071000") },
                            modifier = Modifier.fillMaxWidth().testTag("edit_item_hsn_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (selectedCatName.isBlank()) {
                            android.widget.Toast.makeText(context, "Stock Group is required!", android.widget.Toast.LENGTH_SHORT).show()
                        } else if (itemName.trim().isBlank()) {
                            android.widget.Toast.makeText(context, "Item name is required!", android.widget.Toast.LENGTH_SHORT).show()
                        } else {
                            val rateVal = defaultSellingRateStr.toDoubleOrNull()
                            val discVal = defaultDiscountValueStr.toDoubleOrNull()
                            viewModel.editProductItem(
                                item,
                                selectedCatName,
                                itemName.trim(),
                                hsnCode.trim().ifBlank { null },
                                rateVal,
                                discVal,
                                if (discVal != null) defaultDiscountType else null
                            )
                            android.widget.Toast.makeText(context, "Stock Item updated!", android.widget.Toast.LENGTH_SHORT).show()
                            itemToEdit = null
                        }
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToEdit = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // 6. Delete Item Dialog
    itemToDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = { Text("Delete Stock Item?", fontWeight = FontWeight.Bold) },
            text = {
                Text("Are you sure you want to delete the item '${item.name}' from the item master?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteProductItem(item)
                        android.widget.Toast.makeText(context, "Stock Item deleted!", android.widget.Toast.LENGTH_SHORT).show()
                        itemToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SettingsCategoryCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                modifier = Modifier.padding(bottom = 12.dp)
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                content = content
            )
        }
    }
}

@Composable
fun SettingsRowItem(
    title: String,
    subtitle: String? = null,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    trailing: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (trailing != null) {
            trailing()
        }
    }
}

@Composable
fun SettingsScreen(viewModel: BillingViewModel) {
    var prefix by remember { mutableStateOf(viewModel.getDefaultInvoicePrefix()) }
    var terms by remember { mutableStateOf(viewModel.getDefaultInvoiceTerms()) }
    val context = LocalContext.current

    var appLockEnabled by remember { mutableStateOf(viewModel.isAppLockEnabled()) }
    var appLockPin by remember { mutableStateOf(viewModel.getAppLockPin()) }
    var fingerprintEnabled by remember { mutableStateOf(viewModel.isFingerprintEnabled()) }
    var autoLockDuration by remember { mutableStateOf(viewModel.getAutoLockDuration()) }
    var privacyModeEnabled by remember { mutableStateOf(viewModel.isPrivacyModeEnabled()) }
    var autoBackupReminder by remember { mutableStateOf(viewModel.isAutoBackupReminderEnabled()) }
    var backupBeforeReset by remember { mutableStateOf(viewModel.isBackupBeforeResetEnabled()) }

    var transactionSecurityEnabled by remember { mutableStateOf(viewModel.isTransactionSecurityEnabled()) }
    var transactionPassword by remember { mutableStateOf(viewModel.getTransactionPassword()) }
    var transactionFingerprintEnabled by remember { mutableStateOf(viewModel.isTransactionFingerprintEnabled()) }

    // Additional Settings states
    var appThemeState by remember { mutableStateOf(viewModel.getThemeMode()) }
    var appDateFormatState by remember { mutableStateOf(viewModel.getDateFormatPref()) }
    var appCurrencyState by remember { mutableStateOf(viewModel.getCurrencyPref()) }
    var appLanguageState by remember { mutableStateOf(viewModel.getLanguagePref()) }

    var paymentReminder by remember { mutableStateOf(viewModel.isPaymentReminderEnabled()) }
    var creditReminder by remember { mutableStateOf(viewModel.isCreditReminderEnabled()) }
    var dueDateReminder by remember { mutableStateOf(viewModel.isDueDateReminderEnabled()) }

    var defaultGst by remember { mutableStateOf(viewModel.getDefaultGstRate()) }
    var gstBilling by remember { mutableStateOf(viewModel.isGstBillingEnabled()) }

    // Dialog flags
    var showPinDialog by remember { mutableStateOf(false) }
    var tempPin by remember { mutableStateOf("") }
    var showSetPasswordDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showGstDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val success = restoreDatabaseFile(context, uri)
            if (success) {
                android.widget.Toast.makeText(context, "Database restored successfully! Restarting app...", android.widget.Toast.LENGTH_LONG).show()
                restartApp(context)
            } else {
                android.widget.Toast.makeText(context, "Failed to restore database from backup!", android.widget.Toast.LENGTH_LONG).show()
            }
        }
    }

    // PIN Dialog
    if (showPinDialog) {
        AlertDialog(
            onDismissRequest = { showPinDialog = false },
            title = { Text("Set 4-Digit PIN", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Enter a 4-digit security PIN:", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = tempPin,
                        onValueChange = { input ->
                            if (input.length <= 4 && input.all { it.isDigit() }) {
                                tempPin = input
                            }
                        },
                        label = { Text("PIN") },
                        placeholder = { Text("1234") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (tempPin.length == 4) {
                            appLockPin = tempPin
                            showPinDialog = false
                        } else {
                            android.widget.Toast.makeText(context, "PIN must be exactly 4 digits!", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Save PIN")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPinDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Set Password Dialog
    if (showSetPasswordDialog) {
        var pwdInput by remember { mutableStateOf("") }
        var confirmPwdInput by remember { mutableStateOf("") }
        var isPwdVisible by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showSetPasswordDialog = false },
            title = { Text("Set Transaction Password", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("This password protects sensitive operations like edits, deletes, and stock adjustments.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    
                    OutlinedTextField(
                        value = pwdInput,
                        onValueChange = { input -> if (input.all { it.isDigit() }) pwdInput = input },
                        label = { Text("Enter Password (4+ digits)") },
                        visualTransformation = if (isPwdVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("input_tx_pwd_set")
                    )

                    OutlinedTextField(
                        value = confirmPwdInput,
                        onValueChange = { input -> if (input.all { it.isDigit() }) confirmPwdInput = input },
                        label = { Text("Confirm Password") },
                        visualTransformation = if (isPwdVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("input_tx_pwd_confirm")
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { isPwdVisible = !isPwdVisible }
                    ) {
                        Checkbox(checked = isPwdVisible, onCheckedChange = { isPwdVisible = it })
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Show Password", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (pwdInput.length < 4) {
                            android.widget.Toast.makeText(context, "Password must be at least 4 digits!", android.widget.Toast.LENGTH_SHORT).show()
                        } else if (pwdInput != confirmPwdInput) {
                            android.widget.Toast.makeText(context, "Passwords do not match!", android.widget.Toast.LENGTH_SHORT).show()
                        } else {
                            transactionPassword = pwdInput
                            showSetPasswordDialog = false
                            android.widget.Toast.makeText(context, "Transaction password set locally!", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Save Password")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSetPasswordDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Change Password Dialog
    if (showChangePasswordDialog) {
        var currentInput by remember { mutableStateOf("") }
        var newInput by remember { mutableStateOf("") }
        var confirmNewInput by remember { mutableStateOf("") }
        var isPwdVisible by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showChangePasswordDialog = false },
            title = { Text("Change Transaction Password", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = currentInput,
                        onValueChange = { input -> if (input.all { it.isDigit() }) currentInput = input },
                        label = { Text("Current Password") },
                        visualTransformation = if (isPwdVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("input_tx_pwd_current")
                    )

                    OutlinedTextField(
                        value = newInput,
                        onValueChange = { input -> if (input.all { it.isDigit() }) newInput = input },
                        label = { Text("New Password (4+ digits)") },
                        visualTransformation = if (isPwdVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("input_tx_pwd_new")
                    )

                    OutlinedTextField(
                        value = confirmNewInput,
                        onValueChange = { input -> if (input.all { it.isDigit() }) confirmNewInput = input },
                        label = { Text("Confirm New Password") },
                        visualTransformation = if (isPwdVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("input_tx_pwd_confirm_new")
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { isPwdVisible = !isPwdVisible }
                    ) {
                        Checkbox(checked = isPwdVisible, onCheckedChange = { isPwdVisible = it })
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Show Passwords", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (currentInput != transactionPassword) {
                            android.widget.Toast.makeText(context, "Current password is incorrect!", android.widget.Toast.LENGTH_SHORT).show()
                        } else if (newInput.length < 4) {
                            android.widget.Toast.makeText(context, "New password must be at least 4 digits!", android.widget.Toast.LENGTH_SHORT).show()
                        } else if (newInput != confirmNewInput) {
                            android.widget.Toast.makeText(context, "Passwords do not match!", android.widget.Toast.LENGTH_SHORT).show()
                        } else {
                            transactionPassword = newInput
                            showChangePasswordDialog = false
                            android.widget.Toast.makeText(context, "Transaction password changed!", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Change Password")
                }
            },
            dismissButton = {
                TextButton(onClick = { showChangePasswordDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // GST Settings Dialog
    if (showGstDialog) {
        AlertDialog(
            onDismissRequest = { showGstDialog = false },
            title = { Text("GST Settings Configuration", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Configure GST defaults for invoices:", style = MaterialTheme.typography.bodyMedium)
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Enable GST Invoicing", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                        Switch(
                            checked = gstBilling,
                            onCheckedChange = { gstBilling = it }
                        )
                    }

                    if (gstBilling) {
                        Text("Default GST rate (%)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val rates = listOf(0f, 5f, 12f, 18f, 28f)
                            rates.forEach { rate ->
                                val selected = defaultGst == rate
                                FilterChip(
                                    selected = selected,
                                    onClick = { defaultGst = rate },
                                    label = { Text("${rate.toInt()}%") },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showGstDialog = false },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Apply Defaults")
                }
            },
            dismissButton = {
                TextButton(onClick = { showGstDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Privacy Dialog
    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = { Text("Privacy Policy", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = "VM BOOK Ledger is committed to protecting your privacy.\n\n" +
                               "1. Data Collection: All business data, invoice entries, and customer contacts are saved locally on your device in a secure SQLite Room database.\n\n" +
                               "2. Data Transmission: We do not upload or store any of your business transactions on any remote server. Your data stays strictly on your device.\n\n" +
                               "3. Security: Your database backups are completely managed by your own choosing. We use industry-standard security models to safeguard local data access.\n\n" +
                               "4. Third-party APIs: The application only uses platform APIs for local functions and PDF generation. No third-party analytical trackers are present.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showPrivacyDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Terms Dialog
    if (showTermsDialog) {
        AlertDialog(
            onDismissRequest = { showTermsDialog = false },
            title = { Text("Terms & Conditions", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = "Please read these Terms & Conditions carefully before using the app.\n\n" +
                               "1. Local Storage: VM BOOK Ledger operates locally. If you clear app storage or uninstall the app without a backup, your ledger and transaction history will be permanently deleted.\n\n" +
                               "2. Limitation of Liability: VM Tech Services is not liable for any data loss, financial discrepancy, or accounting errors that may occur. Please verify invoices with original copies.\n\n" +
                               "3. Intended Use: This application is built as a ledger, billing utility, and invoicing aid. Users are solely responsible for local tax regulations and bookkeeping laws.\n\n" +
                               "4. Modification of Services: We reserve the right to modify, update, or deprecate functional modules to comply with Android standards or security policies.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showTermsDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.goBack() },
                modifier = Modifier.testTag("btn_back_settings")
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Application Settings",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // --- BUSINESS CONFIGURATION CARD ---
        SettingsCategoryCard(
            title = "BUSINESS & BILLING",
            icon = Icons.Default.Business
        ) {
            SettingsRowItem(
                title = "Business Profile",
                subtitle = "Manage brand name, contact details & GSTIN",
                icon = Icons.Default.Store,
                trailing = { Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                onClick = { viewModel.setScreen(BillingScreen.BUSINESS_PROFILE) }
            )

            SettingsRowItem(
                title = "GST Settings",
                subtitle = "Configure default tax rates & GST billing option",
                icon = Icons.Default.ReceiptLong,
                trailing = {
                    Text(
                        text = if (gstBilling) "Active (${defaultGst.toInt()}%)" else "Disabled",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (gstBilling) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                onClick = { showGstDialog = true }
            )

            // Invoice prefix text field inside Business card
            OutlinedTextField(
                value = prefix,
                onValueChange = { prefix = it },
                label = { Text("Invoice Number Prefix") },
                placeholder = { Text("e.g. INV") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_settings_prefix"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Invoice terms text field inside Business card
            OutlinedTextField(
                value = terms,
                onValueChange = { terms = it },
                label = { Text("Invoice Footer Notes") },
                placeholder = { Text("Thank you for your business!") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .testTag("input_settings_terms"),
                maxLines = 3,
                shape = RoundedCornerShape(12.dp)
            )
        }

        // --- SECURITY CARD ---
        SettingsCategoryCard(
            title = "SECURITY & ACCESS",
            icon = Icons.Default.Security
        ) {
            // Enable App Lock row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("App Lock (PIN)", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                        Text("Require 4-digit PIN on app startup", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Switch(
                    checked = appLockEnabled,
                    onCheckedChange = { appLockEnabled = it },
                    modifier = Modifier.testTag("switch_app_lock")
                )
            }

            if (appLockEnabled) {
                // PIN Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Change Security PIN", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                        Text("Current PIN: $appLockPin", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Button(
                        onClick = {
                            tempPin = ""
                            showPinDialog = true
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("btn_change_pin")
                    ) {
                        Text("Set PIN")
                    }
                }

                // Fingerprint row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Fingerprint, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Fingerprint Lock", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                            Text("Enable biometrics if supported by device", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Switch(
                        checked = fingerprintEnabled,
                        onCheckedChange = { fingerprintEnabled = it },
                        modifier = Modifier.testTag("switch_fingerprint")
                    )
                }

                // Auto Lock Delays row
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Auto Lock Delay", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val options = listOf(
                            "immediate" to "Immediately",
                            "1" to "1 Min",
                            "5" to "5 Min"
                        )
                        options.forEach { (value, label) ->
                            val selected = autoLockDuration == value
                            FilterChip(
                                selected = selected,
                                onClick = { autoLockDuration = value },
                                label = { Text(label) },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

            // Transaction Security ON/OFF Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Transaction Security", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                        Text("Require PIN to delete or edit ledger logs", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Switch(
                    checked = transactionSecurityEnabled,
                    onCheckedChange = { 
                        if (it && transactionPassword.isEmpty()) {
                            showSetPasswordDialog = true
                        }
                        transactionSecurityEnabled = it 
                    },
                    modifier = Modifier.testTag("switch_transaction_security")
                )
            }

            if (transactionSecurityEnabled) {
                // Password Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Transaction Password", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = if (transactionPassword.isEmpty()) "Not set" else "Current: ••••••",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Button(
                        onClick = {
                            if (transactionPassword.isEmpty()) {
                                showSetPasswordDialog = true
                            } else {
                                showChangePasswordDialog = true
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("btn_tx_pwd_action")
                    ) {
                        Text(if (transactionPassword.isEmpty()) "Set Password" else "Change Password")
                    }
                }

                // Transaction Fingerprint row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.Fingerprint, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Biometric Approval", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                            Text("Confirm ledger operations via fingerprint", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Switch(
                        checked = transactionFingerprintEnabled,
                        onCheckedChange = { transactionFingerprintEnabled = it },
                        modifier = Modifier.testTag("switch_tx_fingerprint")
                    )
                }
            }
        }

        // --- DATA MANAGEMENT CARD ---
        SettingsCategoryCard(
            title = "DATA UTILITIES",
            icon = Icons.Default.Storage
        ) {
            SettingsRowItem(
                title = "Backup & Restore (Local / Cloud)",
                subtitle = "Manage secure offline database copies and VM Cloud Sync",
                icon = Icons.Default.CloudUpload,
                onClick = { viewModel.setScreen(BillingScreen.BACKUP_RESTORE) }
            )

            SettingsRowItem(
                title = "Export Reports",
                subtitle = "Export transaction tables as PDF/Excel",
                icon = Icons.Default.FileDownload,
                onClick = { viewModel.setScreen(BillingScreen.EXPORT_DATA) }
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

            // Auto Backup Reminder Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Backup, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Auto Backup Reminder", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                        Text("Prompt regularly to take database backup", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Switch(
                    checked = autoBackupReminder,
                    onCheckedChange = { autoBackupReminder = it },
                    modifier = Modifier.testTag("switch_auto_backup")
                )
            }

            // Backup Before Reset Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Restore, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Backup Before Reset", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                        Text("Ensure backup is taken before app database reset", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Switch(
                    checked = backupBeforeReset,
                    onCheckedChange = { backupBeforeReset = it },
                    modifier = Modifier.testTag("switch_backup_before_reset")
                )
            }
        }

        // --- APPEARANCE CARD ---
        SettingsCategoryCard(
            title = "APPEARANCE & FORMATS",
            icon = Icons.Default.Palette
        ) {
            // Theme selector row
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Theme", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val themes = listOf(
                        "system" to "System",
                        "light" to "Light",
                        "dark" to "Dark"
                    )
                    themes.forEach { (value, label) ->
                        val selected = appThemeState == value
                        FilterChip(
                            selected = selected,
                            onClick = { appThemeState = value },
                            label = { Text(label) },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Date format selector row
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Date Format", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val formats = listOf("dd MMM yyyy", "dd-MM-yyyy", "yyyy-MM-dd")
                    formats.forEach { format ->
                        val selected = appDateFormatState == format
                        FilterChip(
                            selected = selected,
                            onClick = { appDateFormatState = format },
                            label = { Text(format) },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Currency selector row
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Currency Symbol", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val currencies = listOf("₹", "$", "€")
                    currencies.forEach { currency ->
                        val selected = appCurrencyState == currency
                        FilterChip(
                            selected = selected,
                            onClick = { appCurrencyState = currency },
                            label = { Text(currency) },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1.0f)
                        )
                    }
                }
            }

            // Language Selector Row
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Language (Future Ready)", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val langs = listOf(
                        "en" to "English",
                        "hi" to "Hindi",
                        "ta" to "Tamil"
                    )
                    langs.forEach { (code, name) ->
                        val selected = appLanguageState == code
                        FilterChip(
                            selected = selected,
                            onClick = { appLanguageState = code },
                            label = { Text(name) },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // --- NOTIFICATIONS CARD ---
        SettingsCategoryCard(
            title = "REMINDERS & NOTIFICATIONS",
            icon = Icons.Default.Notifications
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Payment Reminder", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                    Text("Daily check for payments due", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = paymentReminder, onCheckedChange = { paymentReminder = it })
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Credit Reminder", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                    Text("Alerts on large outstanding credit", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = creditReminder, onCheckedChange = { creditReminder = it })
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Due Date Reminder", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                    Text("Alerts when invoice due dates arrive", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = dueDateReminder, onCheckedChange = { dueDateReminder = it })
            }
        }

        // --- UPDATE CARD ---
        SettingsCategoryCard(
            title = "IN-APP SYSTEM UPDATES",
            icon = Icons.Default.SystemUpdate
        ) {
            var updateUrlInput by remember { mutableStateOf(viewModel.getUpdateJsonUrl()) }

            Text(
                text = "VM BOOK Update Channel",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Configure and trigger check for the latest versions without Google Play Store.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = updateUrlInput,
                onValueChange = { 
                    updateUrlInput = it
                    viewModel.setUpdateJsonUrl(it.trim())
                },
                label = { Text("Remote Update JSON URL") },
                placeholder = { Text("https://example.com/update.json") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_update_json_url"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    viewModel.checkForUpdates(manual = true)
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("btn_check_for_updates")
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Check for Updates Now")
            }
        }

        // --- SUPPORT CARD ---
        SettingsCategoryCard(
            title = "HELP & CUSTOMER SUPPORT",
            icon = Icons.Default.Help
        ) {
            SettingsRowItem(
                title = "Contact Support Desk",
                subtitle = "Ask queries to our support staff",
                icon = Icons.Default.Phone,
                onClick = {
                    try {
                        val intent = android.content.Intent(android.content.Intent.ACTION_DIAL, Uri.parse("tel:+910000000000"))
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        android.widget.Toast.makeText(context, "Call dialer not found", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            )

            SettingsRowItem(
                title = "WhatsApp Support",
                subtitle = "Live chat with customer support representatives",
                icon = Icons.Default.Phone,
                onClick = {
                    try {
                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, Uri.parse("https://wa.me/910000000000"))
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        android.widget.Toast.makeText(context, "Cannot open WhatsApp client", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            )

            SettingsRowItem(
                title = "Email Support Team",
                subtitle = "support@vmtechservices.in",
                icon = Icons.Default.Email,
                onClick = {
                    try {
                        val intent = android.content.Intent(android.content.Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:support@vmtechservices.in")
                            putExtra(android.content.Intent.EXTRA_SUBJECT, "VM BOOK Support Request")
                        }
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        android.widget.Toast.makeText(context, "Cannot open email service client", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            )

            SettingsRowItem(
                title = "Visit Corporate Website",
                subtitle = "www.vmtechservices.in",
                icon = Icons.Default.Public,
                onClick = {
                    try {
                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, Uri.parse("https://www.vmtechservices.in"))
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        android.widget.Toast.makeText(context, "Web browser not found", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            )

            SettingsRowItem(
                title = "Privacy Policy",
                subtitle = "How your local data is protected",
                icon = Icons.Default.Lock,
                onClick = { showPrivacyDialog = true }
            )

            SettingsRowItem(
                title = "Terms & Conditions",
                subtitle = "Application usage terms & disclaimers",
                icon = Icons.Default.Description,
                onClick = { showTermsDialog = true }
            )
        }

        // --- Privacy Mode switch inside Settings ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.VisibilityOff, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Privacy Mode", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    Text("Mask sensitive value tags by default", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Switch(
                checked = privacyModeEnabled,
                onCheckedChange = { privacyModeEnabled = it },
                modifier = Modifier.testTag("switch_privacy_mode")
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- COMPREHENSIVE SAVE SETTINGS ACTION BUTTON ---
        Button(
            onClick = {
                viewModel.setDefaultInvoicePrefix(prefix.trim())
                viewModel.setDefaultInvoiceTerms(terms.trim())
                viewModel.setAppLockEnabled(appLockEnabled)
                viewModel.setAppLockPin(appLockPin)
                viewModel.setFingerprintEnabled(fingerprintEnabled)
                viewModel.setAutoLockDuration(autoLockDuration)
                viewModel.setPrivacyModeEnabled(privacyModeEnabled)
                viewModel.setAutoBackupReminderEnabled(autoBackupReminder)
                viewModel.setBackupBeforeResetEnabled(backupBeforeReset)
                viewModel.setTransactionSecurityEnabled(transactionSecurityEnabled)
                viewModel.setTransactionPassword(transactionPassword)
                viewModel.setTransactionFingerprintEnabled(transactionFingerprintEnabled)

                // Additional Save
                viewModel.setThemeMode(appThemeState)
                viewModel.setDateFormatPref(appDateFormatState)
                viewModel.setCurrencyPref(appCurrencyState)
                viewModel.setLanguagePref(appLanguageState)

                viewModel.setPaymentReminderEnabled(paymentReminder)
                viewModel.setCreditReminderEnabled(creditReminder)
                viewModel.setDueDateReminderEnabled(dueDateReminder)

                viewModel.setDefaultGstRate(defaultGst)
                viewModel.setGstBillingEnabled(gstBilling)

                android.widget.Toast.makeText(context, "Premium configurations saved successfully!", android.widget.Toast.LENGTH_SHORT).show()
                viewModel.setScreen(BillingScreen.MORE)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("btn_save_settings"),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.Check, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Save Settings", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "SYSTEM INFORMATION",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("App Version", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("v2.1.0-Premium", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Database System", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Room SQLite", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Storage State", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Offline-First local", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
fun HelpSupportScreen(viewModel: BillingViewModel) {
    var expandedFaqIndex by remember { mutableStateOf(-1) }

    val faqs = listOf(
        "How do I add a new sales invoice?" to "Tap 'Dashboard' -> '+' Floating Action Button at the bottom-right of the recent transaction list. Fill in firm details, add itemized goods with rates/quantities, set credit/paid status, and save. It'll show up in Dashboard and Reports instantly.",
        "What are reports in VM Book?" to "Reports show summaries of total sales and purchases for any period. You can toggle between Daily, Weekly, Monthly, Yearly, or Custom Date filters, view detailed lists of invoices, and tap 'Export to PDF' or 'Export to Excel' to share them.",
        "How does Profit & Loss work?" to "Profit & Loss calculates your metrics automatically! Net sales minus total cost of goods sold (COGS) calculates gross and net realized profit. Product cost is calculated using weighted chronologically recorded purchases.",
        "Can I export all database logs?" to "Yes! Go to More -> Export Data to save comprehensive Sales Ledger, Purchases Ledger, and ledger balances, or use Backup & Restore to take an offline DB copy."
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.goBack() },
                modifier = Modifier.testTag("btn_back_help")
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Help & Support Desk",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Text(
            text = "FREQUENTLY ASKED QUESTIONS",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        faqs.forEachIndexed { index, (question, answer) ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
                    .clickable { expandedFaqIndex = if (expandedFaqIndex == index) -1 else index },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = question,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = if (expandedFaqIndex == index) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (expandedFaqIndex == index) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = answer,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "STILL HAVE QUESTIONS?",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Contact VM Book Support Desk",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "We are here 24/7 to help resolve your billing or accounting queries.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val context = LocalContext.current
                    OutlinedButton(
                        onClick = {
                            try {
                                val emailIntent = android.content.Intent(android.content.Intent.ACTION_SENDTO).apply {
                                    data = android.net.Uri.parse("mailto:support@vmbook.com")
                                    putExtra(android.content.Intent.EXTRA_SUBJECT, "VM Book Support Inquiry")
                                }
                                context.startActivity(emailIntent)
                            } catch (e: Exception) {
                                android.widget.Toast.makeText(context, "Mail client not found!", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Email Support", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            try {
                                val dialIntent = android.content.Intent(android.content.Intent.ACTION_DIAL).apply {
                                    data = android.net.Uri.parse("tel:+18005550199")
                                }
                                context.startActivity(dialIntent)
                            } catch (e: Exception) {
                                android.widget.Toast.makeText(context, "Dialer app not found!", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Call Helpline", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun AboutVmBookScreen(viewModel: BillingViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var showUpdateDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }
    var checkingUpdates by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.goBack() },
                modifier = Modifier.testTag("btn_back_about")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "About Application",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // App Branding Logo Card
        Card(
            modifier = Modifier
                .size(120.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.ic_vm_book_logo),
                    contentDescription = "VM BOOK Logo",
                    modifier = Modifier.size(80.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // App Title & Tagline
        Text(
            text = "VM BOOK Ledger",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Simple, Smart & Complete Billing",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Section 1: Application Information Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 600.dp)
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Application Information",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Version", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("v2.1.0-Premium", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Developed by", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("VM Tech Services", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Platform", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Android", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Database", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("SQLite Room Database", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Status", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Offline-First Verified", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Section 2: Contact Support Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 600.dp)
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Customer Support",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                // Email Support
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            try {
                                val intent = android.content.Intent(android.content.Intent.ACTION_SENDTO).apply {
                                    data = android.net.Uri.parse("mailto:support@vmtechservices.in")
                                    putExtra(android.content.Intent.EXTRA_SUBJECT, "VM BOOK Ledger Support Request")
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                android.widget.Toast.makeText(context, "Cannot open email client", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }
                        .padding(vertical = 8.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = "Email Support",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Support Email", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("support@vmtechservices.in", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Contact",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Website Support
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            try {
                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://www.vmtechservices.in"))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                android.widget.Toast.makeText(context, "Cannot open web browser", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }
                        .padding(vertical = 8.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Public,
                        contentDescription = "Website Support",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Website", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("www.vmtechservices.in", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Go",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(16.dp)
                    )
                }

                // WhatsApp Support
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            try {
                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://wa.me/910000000000"))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                android.widget.Toast.makeText(context, "Cannot open WhatsApp", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }
                        .padding(vertical = 8.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = "WhatsApp Support",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("WhatsApp Support", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("+91-0000000000", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Chat",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // Section 3: Quick Actions Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 600.dp)
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Quick Actions",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), modifier = Modifier.padding(bottom = 8.dp))

                // Rate App Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            try {
                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("market://details?id=${context.packageName}"))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                try {
                                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://play.google.com/store/apps/details?id=${context.packageName}"))
                                    context.startActivity(intent)
                                } catch (ex: Exception) {
                                    android.widget.Toast.makeText(context, "Play Store not found", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                        .padding(vertical = 12.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Star, contentDescription = "Rate", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Rate This App", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                    Icon(Icons.Default.ChevronRight, contentDescription = "Go", tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f), modifier = Modifier.size(16.dp))
                }

                // Share App Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            try {
                                val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(android.content.Intent.EXTRA_SUBJECT, "VM BOOK Ledger")
                                    putExtra(android.content.Intent.EXTRA_TEXT, "Manage your business transactions, invoices and customer ledger with VM BOOK Ledger app. Simple, Smart & Complete Billing. Download now!")
                                }
                                context.startActivity(android.content.Intent.createChooser(intent, "Share via"))
                            } catch (e: Exception) {
                                android.widget.Toast.makeText(context, "Error sharing app", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }
                        .padding(vertical = 12.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Share, contentDescription = "Share", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Share App", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                    Icon(Icons.Default.ChevronRight, contentDescription = "Go", tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f), modifier = Modifier.size(16.dp))
                }

                // Check Updates Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            checkingUpdates = true
                            android.widget.Toast.makeText(context, "Checking for updates...", android.widget.Toast.LENGTH_SHORT).show()
                            coroutineScope.launch {
                                kotlinx.coroutines.delay(1200)
                                checkingUpdates = false
                                showUpdateDialog = true
                            }
                        }
                        .padding(vertical = 12.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Update", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Check for Updates", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                    Icon(Icons.Default.ChevronRight, contentDescription = "Go", tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f), modifier = Modifier.size(16.dp))
                }

                // Privacy Policy Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { showPrivacyDialog = true }
                        .padding(vertical = 12.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Lock, contentDescription = "Privacy", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Privacy Policy", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                    Icon(Icons.Default.ChevronRight, contentDescription = "Go", tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f), modifier = Modifier.size(16.dp))
                }

                // Terms & Conditions Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { showTermsDialog = true }
                        .padding(vertical = 12.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Description, contentDescription = "Terms", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Terms & Conditions", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                    Icon(Icons.Default.ChevronRight, contentDescription = "Go", tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f), modifier = Modifier.size(16.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Footer
        Text(
            text = "Developed with ❤️ in India",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "© 2026 VM Tech Services.\nAll Rights Reserved.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            lineHeight = 16.sp
        )

        Spacer(modifier = Modifier.height(24.dp))
    }

    // Dialogs
    if (showUpdateDialog) {
        AlertDialog(
            onDismissRequest = { showUpdateDialog = false },
            title = { Text("App is up to date", color = MaterialTheme.colorScheme.onSurface) },
            text = { Text("You are currently running the latest stable release (v2.1.0-Premium) of VM BOOK Ledger.", color = MaterialTheme.colorScheme.onSurfaceVariant) },
            confirmButton = {
                TextButton(onClick = { showUpdateDialog = false }) {
                    Text("OK", color = MaterialTheme.colorScheme.primary)
                }
            }
        )
    }

    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = { Text("Privacy Policy", color = MaterialTheme.colorScheme.onSurface) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = "VM BOOK Ledger is committed to protecting your privacy.\n\n" +
                               "1. Data Collection: All business data, invoice entries, and customer contacts are saved locally on your device in a secure SQLite Room database.\n\n" +
                               "2. Data Transmission: We do not upload or store any of your business transactions on any remote server. Your data stays strictly on your device.\n\n" +
                               "3. Security: Your database backups are completely managed by your own choosing. We use industry-standard security models to safeguard local data access.\n\n" +
                               "4. Third-party APIs: The application only uses platform APIs for local functions and PDF generation. No third-party analytical trackers are present.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showPrivacyDialog = false }) {
                    Text("Close", color = MaterialTheme.colorScheme.primary)
                }
            }
        )
    }

    if (showTermsDialog) {
        AlertDialog(
            onDismissRequest = { showTermsDialog = false },
            title = { Text("Terms & Conditions", color = MaterialTheme.colorScheme.onSurface) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = "Please read these Terms & Conditions carefully before using the app.\n\n" +
                               "1. Local Storage: VM BOOK Ledger operates locally. If you clear app storage or uninstall the app without a backup, your ledger and transaction history will be permanently deleted.\n\n" +
                               "2. Limitation of Liability: VM Tech Services is not liable for any data loss, financial discrepancy, or accounting errors that may occur. Please verify invoices with original copies.\n\n" +
                               "3. Intended Use: This application is built as a ledger, billing utility, and invoicing aid. Users are solely responsible for local tax regulations and bookkeeping laws.\n\n" +
                               "4. Modification of Services: We reserve the right to modify, update, or deprecate functional modules to comply with Android standards or security policies.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showTermsDialog = false }) {
                    Text("Close", color = MaterialTheme.colorScheme.primary)
                }
            }
        )
    }
}

fun backupDatabaseFile(context: android.content.Context) {
    try {
        val dbFile = context.getDatabasePath("billing_database")
        if (dbFile.exists()) {
            val backupFile = java.io.File(context.cacheDir, "vm_book_backup_${System.currentTimeMillis()}.db")
            dbFile.copyTo(backupFile, overwrite = true)
            shareFile(context, backupFile, "application/octet-stream")
        } else {
            android.widget.Toast.makeText(context, "Database file not found!", android.widget.Toast.LENGTH_SHORT).show()
        }
    } catch (e: Exception) {
        android.widget.Toast.makeText(context, "Backup failed: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
    }
}

fun restoreDatabaseFile(context: android.content.Context, fileUri: Uri): Boolean {
    return try {
        com.example.data.AppDatabase.closeDatabase()
        val dbFile = context.getDatabasePath("billing_database")
        val dbWalFile = context.getDatabasePath("billing_database-wal")
        val dbShmFile = context.getDatabasePath("billing_database-shm")
        if (dbFile.exists()) dbFile.delete()
        if (dbWalFile.exists()) dbWalFile.delete()
        if (dbShmFile.exists()) dbShmFile.delete()
        dbFile.parentFile?.mkdirs()
        context.contentResolver.openInputStream(fileUri).use { inputStream ->
            if (inputStream != null) {
                dbFile.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            } else {
                throw Exception("Failed to open backup file input stream")
            }
        }
        com.example.data.AppDatabase.getDatabase(context)
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

fun restartApp(context: android.content.Context) {
    val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
    if (intent != null) {
        intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK)
        context.startActivity(intent)
    }
    android.os.Process.killProcess(android.os.Process.myPid())
    java.lang.System.exit(0)
}

fun getFileName(context: android.content.Context, uri: Uri): String? {
    var result: String? = null
    if (uri.scheme == "content") {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val index = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (index != -1) {
                    result = it.getString(index)
                }
            }
        }
    }
    if (result == null) {
        result = uri.path
        val cut = result?.lastIndexOf('/') ?: -1
        if (cut != -1) {
            result = result?.substring(cut + 1)
        }
    }
    return result
}

@Composable
fun BackupRestoreScreen(viewModel: BillingViewModel) {
    val context = LocalContext.current
    var showFactoryResetDialog by remember { mutableStateOf(false) }
    var showResetSuccessDialog by remember { mutableStateOf(false) }
    var showRestoreConfirmDialog by remember { mutableStateOf(false) }
    var showRestoreSuccessDialog by remember { mutableStateOf(false) }
    var showRestoreErrorDialog by remember { mutableStateOf(false) }
    var showBackupSuccessDialog by remember { mutableStateOf(false) }
    var showBackupErrorDialog by remember { mutableStateOf(false) }
    var selectedRestoreUri by remember { mutableStateOf<Uri?>(null) }
    var errorMessage by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    val cloudUserMobile by viewModel.cloudUserMobile.collectAsStateWithLifecycle()
    val cloudServerUrl by viewModel.cloudServerUrl.collectAsStateWithLifecycle()
    val isCloudSandboxEnabled by viewModel.isCloudSandboxEnabled.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()
    val lastSyncTime by viewModel.lastSyncTime.collectAsStateWithLifecycle()
    val syncStatusMessage by viewModel.syncStatusMessage.collectAsStateWithLifecycle()

    val loginStep by viewModel.loginStep.collectAsStateWithLifecycle()
    val loginMobileInput by viewModel.loginMobileInput.collectAsStateWithLifecycle()
    val loginOtpInput by viewModel.loginOtpInput.collectAsStateWithLifecycle()
    val loginError by viewModel.loginError.collectAsStateWithLifecycle()

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val name = getFileName(context, uri) ?: ""
            if (!name.endsWith(".vmbook", ignoreCase = true)) {
                errorMessage = "Invalid file format. Please select only a valid '.vmbook' backup file."
                showRestoreErrorDialog = true
            } else {
                val isValid = com.example.data.LocalBackupService.validateBackupFile(context, uri)
                if (isValid) {
                    selectedRestoreUri = uri
                    showRestoreConfirmDialog = true
                } else {
                    errorMessage = "The backup file is invalid, corrupted, or damaged. Restoration aborted."
                    showRestoreErrorDialog = true
                }
            }
        }
    }

    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                val success = com.example.data.LocalBackupService.performBackupToUri(context, uri)
                if (success) {
                    showBackupSuccessDialog = true
                } else {
                    errorMessage = "Failed to create local backup."
                    showBackupErrorDialog = true
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.goBack() },
                modifier = Modifier.testTag("btn_back_backup")
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Backup & Restore",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // --- CLOUD SYNC & MULTI-DEVICE SUPPORT ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CloudUpload,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "VM Secure Cloud Sync",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))

                if (cloudUserMobile.isEmpty()) {
                    // Not Logged In - Show Login Flow
                    Text(
                        text = "Enable automated backup, cloud restore, and real-time multi-device synchronization across all your tablets and smartphones securely.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    if (loginStep == 0) {
                        OutlinedTextField(
                            value = loginMobileInput,
                            onValueChange = { viewModel.loginMobileInput.value = it },
                            label = { Text("Mobile Number") },
                            placeholder = { Text("Enter 10-digit mobile") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth().testTag("input_cloud_mobile"),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                        )
                        if (loginError != null) {
                            Text(
                                text = loginError!!,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { viewModel.sendCloudOtp(loginMobileInput) },
                            modifier = Modifier.fillMaxWidth().testTag("btn_send_otp")
                        ) {
                            Text("Send Secure OTP", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        OutlinedTextField(
                            value = loginOtpInput,
                            onValueChange = { viewModel.loginOtpInput.value = it },
                            label = { Text("Enter 6-digit OTP") },
                            placeholder = { Text("Enter OTP code") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth().testTag("input_cloud_otp"),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        if (loginError != null) {
                            Text(
                                text = loginError!!,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = { viewModel.verifyCloudOtp(loginOtpInput) },
                                modifier = Modifier.weight(1.5f).testTag("btn_verify_otp")
                            ) {
                                Text("Verify & Login", fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedButton(
                                onClick = { viewModel.loginStep.value = 0 },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Back")
                            }
                        }
                    }
                } else {
                    // Logged In - Show Sync Control Center
                    Text(
                        text = "Account: +91 $cloudUserMobile",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    val syncDateText = if (lastSyncTime > 0L) {
                        java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", java.util.Locale.getDefault()).format(java.util.Date(lastSyncTime))
                    } else {
                        "Never"
                    }
                    Text(
                        text = "Last synced: $syncDateText",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    if (isSyncing) {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Row(modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = { viewModel.performCloudBackup() },
                            modifier = Modifier.weight(1f).testTag("btn_cloud_backup"),
                            enabled = !isSyncing
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Backup Sync", fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        ElevatedButton(
                            onClick = { viewModel.performCloudRestore() },
                            modifier = Modifier.weight(1f).testTag("btn_cloud_restore"),
                            enabled = !isSyncing
                        ) {
                            Text("Cloud Restore", fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.logoutCloud() },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Disconnect")
                        }

                        Button(
                            onClick = { viewModel.simulateSecondDevice() },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Text("Simulate New Device")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(12.dp))

                // Settings & Emulator controls inside card
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Cloud Sandbox Mode (Demo)",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Simulates OTP delivery and real-time backup/restore instantly in preview.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isCloudSandboxEnabled,
                        onCheckedChange = { viewModel.setCloudSandboxEnabled(it) }
                    )
                }

                if (!isCloudSandboxEnabled) {
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = cloudServerUrl,
                        onValueChange = { viewModel.setCloudServerUrl(it) },
                        label = { Text("Production Sync Server URL") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Icon(
                    Icons.Default.Storage,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Local Backup & Restore",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Generate a complete backup of your ledger containing Customers, Suppliers, Products, Sales, Purchases, Payments, Settings, and your Security PIN. The backup file is saved as a secure '.vmbook' archive.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd_HH-mm", java.util.Locale.US)
                        val defaultFileName = "VMBOOK_Backup_${dateFormat.format(java.util.Date())}.vmbook"
                        createDocumentLauncher.launch(defaultFileName)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("btn_backup_now"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Backup Now (.vmbook)", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(12.dp))
                ElevatedButton(
                    onClick = { filePickerLauncher.launch("*/*") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("btn_restore_database"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Restore Database (.vmbook)", fontWeight = FontWeight.Bold)
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Factory Reset System Data",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Completely clears and purges all databases, delete customers, suppliers, payment books, and invoices. This action cannot be undone.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f),
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { showFactoryResetDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("btn_factory_reset"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Erase All Data", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showFactoryResetDialog) {
        AlertDialog(
            onDismissRequest = { showFactoryResetDialog = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Are you absolutely sure?") },
            text = { Text("This will permanently wipe out all invoice listings, product catalogs, customer balances and payments records. You will lose everything.") },
            confirmButton = {
                Button(
                    onClick = {
                        showFactoryResetDialog = false
                        coroutineScope.launch {
                            try {
                                viewModel.clearAllData()
                                showResetSuccessDialog = true
                            } catch (e: Exception) {
                                android.widget.Toast.makeText(context, "Clear failed: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Yes, Erase Everything")
                }
            },
            dismissButton = {
                TextButton(onClick = { showFactoryResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showResetSuccessDialog) {
        AlertDialog(
            onDismissRequest = { 
                showResetSuccessDialog = false
                viewModel.setScreen(BillingScreen.DASHBOARD)
            },
            icon = { Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF0F9D58)) },
            title = { Text("Database Purged") },
            text = { Text("Your application state has been reset to brand new factory settings. All listings have been deleted.") },
            confirmButton = {
                Button(
                    onClick = {
                        showResetSuccessDialog = false
                        viewModel.setScreen(BillingScreen.DASHBOARD)
                    }
                ) {
                    Text("OK")
                }
            }
        )
    }

    if (showRestoreConfirmDialog && selectedRestoreUri != null) {
        AlertDialog(
            onDismissRequest = { showRestoreConfirmDialog = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text("Restore Backup") },
            text = { Text("Current data will be replaced. Continue?") },
            confirmButton = {
                Button(
                    onClick = {
                        showRestoreConfirmDialog = false
                        coroutineScope.launch {
                            val success = com.example.data.LocalBackupService.performRestoreFromUri(context, selectedRestoreUri!!)
                            if (success) {
                                showRestoreSuccessDialog = true
                            } else {
                                errorMessage = "Failed to restore database backup."
                                showRestoreErrorDialog = true
                            }
                        }
                    }
                ) {
                    Text("Yes, Restore")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showRestoreSuccessDialog) {
        AlertDialog(
            onDismissRequest = { /* force click OK */ },
            icon = { Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF0F9D58)) },
            title = { Text("Restore Successful") },
            text = { Text("Your database and settings have been restored successfully. The application needs to restart to apply all changes.") },
            confirmButton = {
                Button(
                    onClick = {
                        showRestoreSuccessDialog = false
                        restartApp(context)
                    }
                ) {
                    Text("Restart App")
                }
            }
        )
    }

    if (showRestoreErrorDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreErrorDialog = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Restore Failed") },
            text = { Text(errorMessage.ifEmpty { "An unexpected error occurred during database restoration." }) },
            confirmButton = {
                Button(
                    onClick = { showRestoreErrorDialog = false }
                ) {
                    Text("OK")
                }
            }
        )
    }

    if (showBackupSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showBackupSuccessDialog = false },
            icon = { Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF0F9D58)) },
            title = { Text("Backup Success") },
            text = { Text("The complete VM BOOK ledger backup has been successfully created and saved.") },
            confirmButton = {
                Button(
                    onClick = { showBackupSuccessDialog = false }
                ) {
                    Text("OK")
                }
            }
        )
    }

    if (showBackupErrorDialog) {
        AlertDialog(
            onDismissRequest = { showBackupErrorDialog = false },
            icon = { Icon(Icons.Default.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Backup Failed") },
            text = { Text(errorMessage.ifEmpty { "An unexpected error occurred during backup creation." }) },
            confirmButton = {
                Button(
                    onClick = { showBackupErrorDialog = false }
                ) {
                    Text("OK")
                }
            }
        )
    }

    if (syncStatusMessage != null) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissSyncStatus() },
            title = { Text("VM BOOK Secure Cloud") },
            text = { Text(syncStatusMessage!!) },
            confirmButton = {
                Button(
                    onClick = { viewModel.dismissSyncStatus() }
                ) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
fun ExportDataScreen(viewModel: BillingViewModel) {
    val invoices by viewModel.allInvoices.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val salesInvoices = remember(invoices) { invoices.filter { it.invoice.type == "SALE" } }
    val purchaseInvoices = remember(invoices) { invoices.filter { it.invoice.type == "PURCHASE" } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.goBack() },
                modifier = Modifier.testTag("btn_back_export")
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Export Ledger Sheets",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        @Composable
        fun ExportCard(
            title: String,
            subtitle: String,
            itemCount: Int,
            totalAmt: Double,
            onExportPdf: () -> Unit,
            onExportExcel: () -> Unit
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Record Count: $itemCount bills", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Total Amount: ₹${String.format(Locale.US, "%.2f", totalAmt)}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onExportPdf,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Export PDF", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = onExportExcel,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.List, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Export Excel", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Text(
            text = "SALES TRANSACTION LEDGERS",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        ExportCard(
            title = "All Sales Ledger",
            subtitle = "Download all recorded sales bills and client transaction sheets.",
            itemCount = salesInvoices.size,
            totalAmt = salesInvoices.sumOf { it.invoice.totalAmount },
            onExportPdf = {
                exportReportToPdf(context, "Full Sales Ledger", "All Time", salesInvoices.size, salesInvoices.sumOf { it.invoice.totalAmount }, salesInvoices)
            },
            onExportExcel = {
                exportReportToExcel(context, "Full Sales Ledger", "All Time", salesInvoices.size, salesInvoices.sumOf { it.invoice.totalAmount }, salesInvoices)
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "PURCHASES TRANSACTION LEDGERS",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        ExportCard(
            title = "All Purchases Ledger",
            subtitle = "Download all recorded suppliers procurement books and invoice journals.",
            itemCount = purchaseInvoices.size,
            totalAmt = purchaseInvoices.sumOf { it.invoice.totalAmount },
            onExportPdf = {
                exportReportToPdf(context, "Full Purchase Ledger", "All Time", purchaseInvoices.size, purchaseInvoices.sumOf { it.invoice.totalAmount }, purchaseInvoices)
            },
            onExportExcel = {
                exportReportToExcel(context, "Full Purchase Ledger", "All Time", purchaseInvoices.size, purchaseInvoices.sumOf { it.invoice.totalAmount }, purchaseInvoices)
            }
        )
    }
}

@Composable
fun SplashScreen(viewModel: BillingViewModel) {
    var startAnimation by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (startAnimation) 1.2f else 0.5f,
        animationSpec = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
        label = "logoScale"
    )
    val alpha by animateFloatAsState(
        targetValue = if (startAnimation) 1.0f else 0.0f,
        animationSpec = tween(durationMillis = 1500, easing = LinearEasing),
        label = "logoAlpha"
    )

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(2500)
        if (viewModel.isAppLockEnabled()) {
            viewModel.setScreen(BillingScreen.LOCK_SCREEN)
        } else {
            viewModel.setScreen(BillingScreen.DASHBOARD)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(Color(0xFF0D1117), Color(0xFF04060A))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        alpha = alpha
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.ic_vm_book_logo),
                    contentDescription = "VM BOOK Logo",
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "VM BOOK",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                fontSize = 32.sp,
                letterSpacing = 1.5.sp,
                modifier = Modifier.graphicsLayer(alpha = alpha)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Simple, Smart & Complete Billing",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.7f),
                letterSpacing = 0.5.sp,
                modifier = Modifier.graphicsLayer(alpha = alpha)
            )
        }

        Text(
            text = "Developed by VM Tech Services",
            style = MaterialTheme.typography.labelMedium,
            color = Color.White.copy(alpha = 0.4f),
            fontWeight = FontWeight.Normal,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 36.dp)
        )
    }
}

@Composable
fun LockScreen(viewModel: BillingViewModel) {
    val context = LocalContext.current
    var enteredPin by remember { mutableStateOf("") }
    val correctPin = viewModel.getAppLockPin()
    val isFingerprintEnabled = viewModel.isFingerprintEnabled()

    var wrongAttempts by remember { mutableStateOf(0) }
    var lockoutSecondsLeft by remember { mutableStateOf(0) }

    fun triggerBiometric() {
        showBiometricAuthentication(
            context = context,
            title = "VM BOOK Lock",
            subtitle = "Scan your fingerprint to unlock VM BOOK",
            negativeButtonText = "Use PIN",
            onSuccess = { result ->
                if (result != null) {
                    viewModel.setScreen(BillingScreen.DASHBOARD)
                }
            },
            onError = { errorCode, errString ->
                if (errorCode != BiometricPrompt.ERROR_USER_CANCELED && errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                    Toast.makeText(context, "Authentication failed: $errString", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    // Auto-trigger biometric on load if enabled
    if (isFingerprintEnabled && lockoutSecondsLeft <= 0) {
        LaunchedEffect(Unit) {
            triggerBiometric()
        }
    }

    // Lockout countdown timer
    if (lockoutSecondsLeft > 0) {
        LaunchedEffect(lockoutSecondsLeft) {
            delay(1000)
            lockoutSecondsLeft -= 1
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(Color(0xFF0F172A), Color(0xFF020617))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header: Logo & Title
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 48.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.ic_vm_book_logo),
                        contentDescription = "VM BOOK Logo",
                        modifier = Modifier.size(70.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "VM BOOK",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                if (lockoutSecondsLeft > 0) {
                    Text(
                        text = "Too many wrong attempts.\nLocked for $lockoutSecondsLeft seconds.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                } else {
                    Text(
                        text = "Enter PIN to Continue",
                        color = Color.White.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // PIN Dots Indicator
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(4) { index ->
                        val isFilled = index < enteredPin.length
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .border(1.5.dp, Color.White.copy(alpha = 0.6f), CircleShape)
                                .background(if (isFilled) Color.White else Color.Transparent)
                        )
                    }
                }
            }

            // Keypad Row & Column Layout
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 32.dp)
            ) {
                val keys = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("F", "0", "B") // F = Fingerprint, B = Backspace
                )
                
                keys.forEach { rowKeys ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(28.dp),
                        modifier = Modifier.padding(vertical = 10.dp)
                    ) {
                        rowKeys.forEach { key ->
                            val isDigit = key != "F" && key != "B"
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isDigit) Color.White.copy(alpha = 0.08f)
                                        else Color.Transparent
                                    )
                                    .clickable(enabled = lockoutSecondsLeft <= 0) {
                                        if (key == "B") {
                                            if (enteredPin.isNotEmpty()) {
                                                enteredPin = enteredPin.dropLast(1)
                                            }
                                        } else if (key == "F") {
                                            if (isFingerprintEnabled) {
                                                triggerBiometric()
                                            } else {
                                                android.widget.Toast.makeText(context, "Biometric lock is disabled. Please set it up in Settings.", android.widget.Toast.LENGTH_SHORT).show()
                                            }
                                        } else {
                                            if (enteredPin.length < 4) {
                                                enteredPin += key
                                                if (enteredPin.length == 4) {
                                                    if (enteredPin == correctPin) {
                                                        enteredPin = ""
                                                        wrongAttempts = 0
                                                        viewModel.setScreen(BillingScreen.DASHBOARD)
                                                    } else {
                                                        wrongAttempts += 1
                                                        enteredPin = ""
                                                        if (wrongAttempts >= 5) {
                                                            lockoutSecondsLeft = 30
                                                            android.widget.Toast.makeText(context, "Too many wrong attempts! Locked for 30s.", android.widget.Toast.LENGTH_LONG).show()
                                                        } else {
                                                            val remaining = 5 - wrongAttempts
                                                            android.widget.Toast.makeText(context, "Incorrect PIN! $remaining attempts remaining.", android.widget.Toast.LENGTH_SHORT).show()
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                when (key) {
                                    "B" -> Icon(
                                        Icons.Default.Backspace,
                                        contentDescription = "Backspace",
                                        tint = Color.White
                                    )
                                    "F" -> {
                                        if (isFingerprintEnabled) {
                                            Icon(
                                                Icons.Default.Fingerprint,
                                                contentDescription = "Biometric Lock",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(32.dp)
                                            )
                                        }
                                    }
                                    else -> Text(
                                        text = key,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PurchaseDetailView(
    viewModel: BillingViewModel,
    item: InvoiceWithItems,
    supplierMatch: com.example.data.SupplierEntity?
) {
    val inv = item.invoice
    val sdf = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }
    var showDeleteCheck by remember { mutableStateOf(false) }

    val allProductItems by viewModel.productItems.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { viewModel.goBack() },
                    modifier = Modifier.testTag("btn_back_purchase_detail")
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Purchase Entry Record",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            // Quick Delete Button
            var showDeleteLockVerify by remember { mutableStateOf(false) }
            IconButton(onClick = { showDeleteCheck = true }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete record", tint = MaterialTheme.colorScheme.error)
            }

            if (showDeleteCheck) {
                AlertDialog(
                    onDismissRequest = { showDeleteCheck = false },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                if (viewModel.isTransactionSecurityEnabled()) {
                                    showDeleteLockVerify = true
                                } else {
                                    viewModel.deleteInvoice(inv.id)
                                    showDeleteCheck = false
                                }
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
                    title = { Text("Delete Purchase Record?") },
                    text = { Text("Are you sure you want to permanently delete this purchase entry record? This will adjust your stock and supplier balance back.") }
                )
            }

            if (showDeleteLockVerify) {
                PasswordVerificationDialog(
                    viewModel = viewModel,
                    onVerified = {
                        showDeleteLockVerify = false
                        showDeleteCheck = false
                        viewModel.deleteInvoice(inv.id)
                    },
                    onDismiss = {
                        showDeleteLockVerify = false
                    }
                )
            }
        }

        // Details Container
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.0f)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header: Title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Receipt,
                        contentDescription = null,
                        tint = Color(0xFF1E88E5),
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "SUPPLIER INVOICE RECORD",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF1E88E5)
                    )
                }

                HorizontalDivider()

                // Row: Supplier and Bill info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Supplier Info
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "SUPPLIER DETAILS",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = inv.partyName,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                        if (supplierMatch != null) {
                            Text(
                                text = "Mobile: ${supplierMatch.phone}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (supplierMatch.address.isNotEmpty()) {
                                Text(
                                    text = supplierMatch.address,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Invoice Info
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "RECORD DETAILS",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Inv No: ${inv.invoiceNumber}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Date: ${sdf.format(Date(inv.date))}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (inv.notes.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = inv.notes,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1565C0),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                HorizontalDivider()

                // Items list
                Text(
                    text = "PURCHASED PRODUCTS",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item.items.forEach { line ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1.5f)) {
                                Text(
                                    text = getCleanProductName(line.name),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                val hsn = getProductHsn(line.name, line.hsnCode, allProductItems)
                                if (hsn.isNotEmpty()) {
                                    Text(
                                        text = "HSN: $hsn",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Text(
                                text = "${line.quantity} Unit x ₹${String.format(Locale.US, "%.2f", line.price)}",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(1.2f),
                                textAlign = TextAlign.End,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "₹${String.format(Locale.US, "%.2f", line.totalPrice)}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.End
                            )
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Calculations Block
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val subtotal = item.items.sumOf { it.totalPrice }
                        val gstRate = inv.tax
                        val gstAmt = subtotal * (gstRate / 100.0)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Net Price:", style = MaterialTheme.typography.bodyMedium)
                            Text("₹${String.format(Locale.US, "%.2f", subtotal)}", style = MaterialTheme.typography.bodyMedium)
                        }

                        if (gstRate > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("CGST (${gstRate / 2}%):", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("₹${String.format(Locale.US, "%.2f", gstAmt / 2)}", style = MaterialTheme.typography.bodySmall)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("SGST (${gstRate / 2}%):", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("₹${String.format(Locale.US, "%.2f", gstAmt / 2)}", style = MaterialTheme.typography.bodySmall)
                            }
                        }

                        HorizontalDivider()

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("GRAND TOTAL:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("₹${String.format(Locale.US, "%.2f", subtotal + gstAmt)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF1E88E5))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PasswordVerificationDialog(
    viewModel: BillingViewModel,
    onVerified: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var inputPassword by remember { mutableStateOf("") }
    var isPwdVisible by remember { mutableStateOf(false) }
    val isBiometricEnabled = viewModel.isTransactionFingerprintEnabled()

    val correctPassword = viewModel.getTransactionPassword()

    fun triggerBiometric() {
        showBiometricAuthentication(
            context = context,
            title = "Transaction Authorization",
            subtitle = "Scan fingerprint to authorize transaction",
            onSuccess = { result ->
                if (result != null) {
                    onVerified()
                }
            },
            onError = { errorCode, errString ->
                if (errorCode != BiometricPrompt.ERROR_USER_CANCELED && errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                    Toast.makeText(context, "Verification failed: $errString", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    if (isBiometricEnabled) {
        LaunchedEffect(Unit) {
            triggerBiometric()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Security,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Enter Transaction Password", fontWeight = FontWeight.Bold)
                }
                if (isBiometricEnabled) {
                    IconButton(onClick = { triggerBiometric() }) {
                        Icon(
                            Icons.Default.Fingerprint,
                            contentDescription = "Scan Fingerprint",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "This action is protected. Please enter your transaction password to proceed.",
                    style = MaterialTheme.typography.bodyMedium
                )
                OutlinedTextField(
                    value = inputPassword,
                    onValueChange = { input -> if (input.all { it.isDigit() }) inputPassword = input },
                    label = { Text("Password") },
                    visualTransformation = if (isPwdVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("input_tx_verification_pwd")
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { isPwdVisible = !isPwdVisible }
                ) {
                    Checkbox(checked = isPwdVisible, onCheckedChange = { isPwdVisible = it })
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Show Password", style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (inputPassword == correctPassword) {
                        onVerified()
                    } else {
                        android.widget.Toast.makeText(context, "Incorrect Password!", android.widget.Toast.LENGTH_SHORT).show()
                    }
                },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Verify")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AppUpdateHandler(viewModel: BillingViewModel) {
    val context = LocalContext.current
    val updateState by viewModel.updateStatus.collectAsStateWithLifecycle()
    var showPermissionDialog by remember { mutableStateOf(false) }
    var pendingApkFile by remember { mutableStateOf<java.io.File?>(null) }

    val launchInstaller = { apkFile: java.io.File ->
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O && !context.packageManager.canRequestPackageInstalls()) {
            pendingApkFile = apkFile
            showPermissionDialog = true
        } else {
            try {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    val uri = androidx.core.content.FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        apkFile
                    )
                    setDataAndType(uri, "application/vnd.android.package-archive")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                android.widget.Toast.makeText(context, "Error launching installer: ${e.localizedMessage}", android.widget.Toast.LENGTH_LONG).show()
            }
        }
    }

    if (updateState is UpdateStatus.DownloadCompleted) {
        val apkFile = (updateState as UpdateStatus.DownloadCompleted).apkFile
        LaunchedEffect(apkFile) {
            launchInstaller(apkFile)
        }
    }

    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text("Permission Required", fontWeight = FontWeight.Bold) },
            text = { Text("To install the update, please allow VM BOOK to install apps from this source.") },
            confirmButton = {
                Button(
                    onClick = {
                        showPermissionDialog = false
                        try {
                            val intent = Intent(android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                                data = Uri.parse("package:${context.packageName}")
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            android.widget.Toast.makeText(context, "Failed to open settings: ${e.localizedMessage}", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Settings")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    when (val state = updateState) {
        is UpdateStatus.NewUpdateAvailable -> {
            AlertDialog(
                onDismissRequest = {
                    if (!state.forceUpdate) {
                        viewModel.dismissUpdateDialog()
                    }
                },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CloudDownload,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("New Update Available", fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        Text(
                            text = "A new version of VM BOOK is available. Please update to get the latest features and improvements.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Current Version", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(state.currentVersion, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Latest Version", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(state.latestVersion, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                        
                        if (state.whatsNew.isNotBlank()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("What's New:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = state.whatsNew,
                                    modifier = Modifier.padding(12.dp),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.downloadAndInstallApk(state.apkUrl)
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("btn_update_now")
                    ) {
                        Text("Update Now")
                    }
                },
                dismissButton = {
                    if (!state.forceUpdate) {
                        TextButton(
                            onClick = { viewModel.dismissUpdateDialog() },
                            modifier = Modifier.testTag("btn_update_later")
                        ) {
                            Text("Later")
                        }
                    }
                }
            )
        }
        is UpdateStatus.Downloading -> {
            AlertDialog(
                onDismissRequest = {},
                title = { Text("Downloading Update", fontWeight = FontWeight.Bold) },
                text = {
                    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Please wait while the update is downloading...",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.align(Alignment.Start)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        LinearProgressIndicator(
                            progress = { state.progress },
                            modifier = Modifier.fillMaxWidth(),
                            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "${(state.progress * 100).toInt()}%",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                confirmButton = {},
                dismissButton = {}
            )
        }
        is UpdateStatus.DownloadCompleted -> {
            AlertDialog(
                onDismissRequest = {},
                title = { Text("Download Completed", fontWeight = FontWeight.Bold) },
                text = { Text("The update file has been successfully downloaded. If the installer didn't launch automatically, please click 'Install' to start.") },
                confirmButton = {
                    Button(
                        onClick = { launchInstaller(state.apkFile) },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Install")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.dismissUpdateDialog() }) {
                        Text("Cancel")
                    }
                }
            )
        }
        is UpdateStatus.Error -> {
            AlertDialog(
                onDismissRequest = { viewModel.dismissUpdateDialog() },
                title = { Text("Update Status", fontWeight = FontWeight.Bold) },
                text = { Text(state.message) },
                confirmButton = {
                    Button(
                        onClick = { viewModel.dismissUpdateDialog() },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("OK")
                    }
                }
            )
        }
        else -> {}
    }
}

private fun Context.findActivity(): FragmentActivity? {
    var currentContext = this
    while (currentContext is ContextWrapper) {
        if (currentContext is FragmentActivity) {
            return currentContext
        }
        currentContext = currentContext.baseContext
    }
    return null
}

private fun showBiometricAuthentication(
    context: Context,
    title: String,
    subtitle: String,
    negativeButtonText: String = "Cancel",
    onSuccess: (BiometricPrompt.AuthenticationResult) -> Unit,
    onError: (Int, String) -> Unit = { _, _ -> }
) {
    val activity = context.findActivity()
    if (activity == null) {
        Toast.makeText(context, "FragmentActivity not found", Toast.LENGTH_SHORT).show()
        return
    }

    val executor = ContextCompat.getMainExecutor(activity)
    val biometricPrompt = BiometricPrompt(
        activity,
        executor,
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                onError(errorCode, errString.toString())
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onSuccess(result)
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
            }
        }
    )

    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle(title)
        .setSubtitle(subtitle)
        .setNegativeButtonText(negativeButtonText)
        .build()

    try {
        biometricPrompt.authenticate(promptInfo)
    } catch (e: Exception) {
        Toast.makeText(context, "Biometric error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
    }
}


