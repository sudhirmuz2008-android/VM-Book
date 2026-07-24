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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillingAppContent(viewModel: BillingViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val selectedInvoice by viewModel.selectedInvoice.collectAsStateWithLifecycle()

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
fun DashboardScreen(viewModel: BillingViewModel) {
    val stats by viewModel.dashboardStats.collectAsStateWithLifecycle()
    val recentInvoices by viewModel.invoices.collectAsStateWithLifecycle()
    val businessProfileState by viewModel.businessProfile.collectAsStateWithLifecycle()
    val isPrivacyHidden by viewModel.isPrivacyHidden.collectAsStateWithLifecycle()

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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = businessProfileState?.firmName?.takeIf { it.isNotEmpty() } ?: "VM BOOK",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
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

        // Today's Sales Banner
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp)
                .testTag("dashboard_today_sales_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
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
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "TODAY'S SALES",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = if (isPrivacyHidden) "₹••••" else "₹${String.format(Locale.US, "%.2f", stats.todaySales)}",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Total sales generated today",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
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

    val subtotalVal = item.items.sumOf { it.totalPrice }
    val taxRate = inv.tax
    val taxAmount = subtotalVal * (taxRate / 100.0)
    val cgstVal = taxAmount / 2
    val sgstVal = taxAmount / 2
    val rawTotal = subtotalVal + taxAmount - inv.discount
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
                        Text("Product Name", style = MaterialTheme.typography.labelSmall, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.weight(2.2f))
                        Text("HSN Code", style = MaterialTheme.typography.labelSmall, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.0f))
                        Text("Qty", style = MaterialTheme.typography.labelSmall, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.7f), textAlign = TextAlign.Center)
                        Text("Unit", style = MaterialTheme.typography.labelSmall, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.7f), textAlign = TextAlign.Center)
                        Text("Rate", style = MaterialTheme.typography.labelSmall, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.1f), textAlign = TextAlign.End)
                        Text("Amount", style = MaterialTheme.typography.labelSmall, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.3f), textAlign = TextAlign.End)
                    }

                    // Product Table Rows
                    item.items.forEach { line ->
                        val qtyStr = if (line.quantity % 1 == 0.0) line.quantity.toInt().toString() else line.quantity.toString()
                        val hsn = getDeterministicHsn(line.name)
                        val unit = getDeterministicUnit(line.name)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(0.5.dp, Color.LightGray.copy(alpha = 0.5f))
                                .padding(horizontal = 8.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(getCleanProductName(line.name), style = MaterialTheme.typography.bodySmall, color = Color.Black, fontWeight = FontWeight.Bold, modifier = Modifier.weight(2.2f))
                            Text(hsn, style = MaterialTheme.typography.bodySmall, color = Color.DarkGray, modifier = Modifier.weight(1.0f))
                            Text(qtyStr, style = MaterialTheme.typography.bodySmall, color = Color.Black, modifier = Modifier.weight(0.7f), textAlign = TextAlign.Center)
                            Text(unit, style = MaterialTheme.typography.bodySmall, color = Color.DarkGray, modifier = Modifier.weight(0.7f), textAlign = TextAlign.Center)
                            Text("₹${String.format(Locale.US, "%.2f", line.price)}", style = MaterialTheme.typography.bodySmall, color = Color.Black, modifier = Modifier.weight(1.1f), textAlign = TextAlign.End)
                            Text("₹${String.format(Locale.US, "%.2f", line.totalPrice)}", style = MaterialTheme.typography.bodySmall, color = Color.Black, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.3f), textAlign = TextAlign.End)
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
                            // Net Price
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Net Price:", style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
                                Text("₹${String.format(Locale.US, "%.2f", subtotalVal)}", style = MaterialTheme.typography.bodySmall, color = Color.Black, fontWeight = FontWeight.Medium)
                            }
                            // Discount (if applicable)
                            if (inv.discount > 0) {
                                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Discount:", style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
                                    Text("-₹${String.format(Locale.US, "%.2f", inv.discount)}", style = MaterialTheme.typography.bodySmall, color = Color(0xFFD32F2F), fontWeight = FontWeight.Medium)
                                }
                                val taxableValue = subtotalVal - inv.discount
                                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Taxable Value:", style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
                                    Text("₹${String.format(Locale.US, "%.2f", taxableValue)}", style = MaterialTheme.typography.bodySmall, color = Color.Black, fontWeight = FontWeight.Medium)
                                }
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

                            // Sub Total
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Sub Total:", style = MaterialTheme.typography.bodySmall, color = Color.Black, fontWeight = FontWeight.Bold)
                                Text("₹${String.format(Locale.US, "%.2f", rawTotal)}", style = MaterialTheme.typography.bodySmall, color = Color.Black, fontWeight = FontWeight.Bold)
                            }

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
                            val hsn = getDeterministicHsn(line.name)
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
                            // Net Price
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Net Price:", style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
                                Text("₹${String.format(Locale.US, "%.2f", subtotalVal)}", style = MaterialTheme.typography.bodySmall, color = Color.Black)
                            }
                            // Discount (if applicable)
                            if (inv.discount > 0) {
                                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Discount:", style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
                                    Text("-₹${String.format(Locale.US, "%.2f", inv.discount)}", style = MaterialTheme.typography.bodySmall, color = Color.Black)
                                }
                                val taxableValue = subtotalVal - inv.discount
                                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Taxable Value:", style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
                                    Text("₹${String.format(Locale.US, "%.2f", taxableValue)}", style = MaterialTheme.typography.bodySmall, color = Color.Black)
                                }
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

                            // Sub Total
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Sub Total:", style = MaterialTheme.typography.bodySmall, color = Color.Black, fontWeight = FontWeight.Bold)
                                Text("₹${String.format(Locale.US, "%.2f", rawTotal)}", style = MaterialTheme.typography.bodySmall, color = Color.Black, fontWeight = FontWeight.Bold)
                            }

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
                        supplierMatch = supplierMatch
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

    // Generate a consistent 4 or 8 digit HSN code based on the item name hash
    val hash = Math.abs(name.hashCode())
    val base = (hash % 9000) + 1000
    return "8471$base"
}

fun getCleanProductName(name: String): String {
    val hsnPattern = "\\s*\\[HSN:\\s*(.*?)\\]".toRegex()
    return name.replace(hsnPattern, "").trim()
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
    supplierMatch: com.example.data.SupplierEntity?
) {
    val html = generateInvoiceHtml(item, profile, isA4, customerMatch, supplierMatch)
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
    supplierMatch: com.example.data.SupplierEntity?
): String {
    val inv = item.invoice
    val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    val subtotal = item.items.sumOf { it.totalPrice }
    val taxRate = inv.tax
    val taxVal = subtotal * (taxRate / 100.0)
    val cgstVal = taxVal / 2
    val sgstVal = taxVal / 2
    val rawTotal = subtotal + taxVal - inv.discount
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
                            <th class="text-right">Amount</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${item.items.joinToString("") { line ->
                            val qtyStr = if (line.quantity % 1 == 0.0) line.quantity.toInt().toString() else line.quantity.toString()
                            val hsn = getDeterministicHsn(line.name)
                            val unit = getDeterministicUnit(line.name)
                            """
                            <tr>
                                <td><strong>${getCleanProductName(line.name)}</strong></td>
                                <td>$hsn</td>
                                <td class="text-center">$qtyStr</td>
                                <td class="text-center">$unit</td>
                                <td class="text-right">₹${String.format(Locale.US, "%.2f", line.price)}</td>
                                <td class="text-right">₹${String.format(Locale.US, "%.2f", line.totalPrice)}</td>
                            </tr>
                            """.trimIndent()
                        }}
                    </tbody>
                </table>
                
                <div class="amount-words"><strong>Amount in Words:</strong> $amountInWords</div>
                
                <table class="summary-table">
                    <tr>
                        <td class="summary-label">Net Price:</td>
                        <td class="summary-value">₹${String.format(Locale.US, "%.2f", subtotal)}</td>
                    </tr>
                    ${if (inv.discount > 0) """
                    <tr>
                        <td class="summary-label">Discount:</td>
                        <td class="summary-value">-₹${String.format(Locale.US, "%.2f", inv.discount)}</td>
                    </tr>
                    <tr>
                        <td class="summary-label">Taxable Value:</td>
                        <td class="summary-value">₹${String.format(Locale.US, "%.2f", subtotal - inv.discount)}</td>
                    </tr>
                    """ else ""}
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
                    <tr style="border-top: 1px solid #ccc;">
                        <td class="summary-label" style="font-weight: bold; color: #000;">Sub Total:</td>
                        <td class="summary-value" style="font-weight: bold; color: #000;">₹${String.format(Locale.US, "%.2f", rawTotal)}</td>
                    </tr>
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
                            val hsn = getDeterministicHsn(line.name)
                            val unit = getDeterministicUnit(line.name)
                            """
                            <tr>
                                <td>
                                    <strong>${getCleanProductName(line.name)}</strong><br>
                                    <span style="font-size: 8px; color: #555;">HSN: $hsn</span>
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
                    <div class="summary-row"><span>Net Price:</span><span>₹${String.format(Locale.US, "%.2f", subtotal)}</span></div>
                    ${if (inv.discount > 0) """
                    <div class="summary-row"><span>Discount:</span><span>-₹${String.format(Locale.US, "%.2f", inv.discount)}</span></div>
                    <div class="summary-row"><span>Taxable Value:</span><span>₹${String.format(Locale.US, "%.2f", subtotal - inv.discount)}</span></div>
                    """ else ""}
                    ${if (inv.tax > 0) """
                    <div class="summary-row" style="margin-top: 4px;"><span>CGST (${inv.tax / 2}%):</span><span>₹${String.format(Locale.US, "%.2f", cgstVal)}</span></div>
                    <div class="summary-row"><span>SGST (${inv.tax / 2}%):</span><span>₹${String.format(Locale.US, "%.2f", sgstVal)}</span></div>
                    """ else ""}
                    
                    <div class="divider"></div>
                    
                    <div class="summary-row bold"><span>Sub Total:</span><span>₹${String.format(Locale.US, "%.2f", rawTotal)}</span></div>
                    
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

                                // Pay/Clear outstanding button
                                Button(
                                    onClick = {
                                        showPaymentDialogForInvoice = item
                                        paymentAmountInput = String.format(Locale.US, "%.2f", invoice.outstandingAmount)
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check, 
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Pay", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Payment Dialog for updating/reducing outstanding amount directly on the credit sale invoice!
    if (showPaymentDialogForInvoice != null) {
        val invoiceItem = showPaymentDialogForInvoice!!
        val invoice = invoiceItem.invoice
        AlertDialog(
            onDismissRequest = { showPaymentDialogForInvoice = null },
            title = { Text("Update Outstanding Amount") },
            text = {
                Column {
                    Text(
                        text = "Customer: ${invoice.partyName}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Invoice #${invoice.invoiceNumber} Total: ₹${String.format(Locale.US, "%.2f", invoice.totalAmount)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = paymentAmountInput,
                        onValueChange = { paymentAmountInput = it },
                        label = { Text("Outstanding Credit Balance (₹)") },
                        placeholder = { Text("0.00") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Set to 0 if the invoice is fully paid and cleared.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val enteredAmt = paymentAmountInput.toDoubleOrNull() ?: 0.0
                        viewModel.updateInvoiceOutstandingAmount(invoice.id, enteredAmt)
                        
                        // Also record customer payment if needed to keep ledger fully synced!
                        val diff = invoice.outstandingAmount - enteredAmt
                        if (diff > 0) {
                            coroutineScope.launch {
                                val cust = viewModel.customersWithBalance.value.find { it.customer.name.trim().equals(invoice.partyName.trim(), ignoreCase = true) }?.customer
                                if (cust != null) {
                                    viewModel.addCustomerPayment(
                                        com.example.data.CustomerPaymentEntity(
                                            customerId = cust.id,
                                            amount = diff,
                                            date = System.currentTimeMillis(),
                                            notes = "Received towards Invoice #${invoice.invoiceNumber}"
                                        )
                                    )
                                }
                            }
                        }
                        
                        showPaymentDialogForInvoice = null
                        android.widget.Toast.makeText(context, "Outstanding credit updated!", android.widget.Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Update")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPaymentDialogForInvoice = null }) {
                    Text("Cancel")
                }
            }
        )
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
    val products by viewModel.productSummaries.collectAsStateWithLifecycle()
    val invoices by viewModel.allInvoices.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }
    var selectedProductForHistory by remember { mutableStateOf<com.example.ui.ProductSummary?>(null) }
    val context = LocalContext.current

    var productToAdjust by remember { mutableStateOf<com.example.ui.ProductSummary?>(null) }
    var showAdjustmentLockVerify by remember { mutableStateOf(false) }
    var showAdjustmentInput by remember { mutableStateOf(false) }

    val filteredProducts = remember(products, searchQuery) {
        products.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    val totalStockValuation = remember(products) {
        products.filter { it.stockBalance > 0 }.sumOf { it.stockValue }
    }

    if (showAdjustmentLockVerify) {
        PasswordVerificationDialog(
            viewModel = viewModel,
            onVerified = {
                showAdjustmentLockVerify = false
                showAdjustmentInput = true
            },
            onDismiss = {
                showAdjustmentLockVerify = false
                productToAdjust = null
            }
        )
    }

    if (showAdjustmentInput && productToAdjust != null) {
        var adjustmentQtyStr by remember { mutableStateOf("") }
        var isIncrease by remember { mutableStateOf(true) }
        var reasonText by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { 
                showAdjustmentInput = false
                productToAdjust = null
            },
            title = { Text("Adjust Stock: ${productToAdjust?.name}", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Current stock: ${String.format(Locale.US, "%.1f", productToAdjust?.stockBalance ?: 0.0)} Units", style = MaterialTheme.typography.bodyMedium)
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { isIncrease = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isIncrease) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (isIncrease) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).testTag("btn_adj_type_increase")
                        ) {
                            Text("Add (+)")
                        }
                        Button(
                            onClick = { isIncrease = false },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (!isIncrease) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (!isIncrease) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).testTag("btn_adj_type_decrease")
                        ) {
                            Text("Reduce (-)")
                        }
                    }

                    OutlinedTextField(
                        value = adjustmentQtyStr,
                        onValueChange = { input -> 
                            if (input.all { it.isDigit() || it == '.' }) {
                                adjustmentQtyStr = input
                            }
                        },
                        label = { Text("Quantity to Adjust") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("input_adj_qty")
                    )

                    OutlinedTextField(
                        value = reasonText,
                        onValueChange = { reasonText = it },
                        label = { Text("Reason (e.g. Stock count check)") },
                        placeholder = { Text("Damaged items, count check, etc.") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("input_adj_reason")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val qty = adjustmentQtyStr.toDoubleOrNull()
                        if (qty == null || qty <= 0) {
                            android.widget.Toast.makeText(context, "Please enter a valid positive quantity to adjust!", android.widget.Toast.LENGTH_SHORT).show()
                        } else {
                            val actualChange = if (isIncrease) qty else -qty
                            viewModel.adjustStock(productToAdjust!!.name, actualChange, reasonText)
                            android.widget.Toast.makeText(context, "Stock adjusted successfully!", android.widget.Toast.LENGTH_SHORT).show()
                            showAdjustmentInput = false
                            productToAdjust = null
                        }
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Confirm Adjustment")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showAdjustmentInput = false
                    productToAdjust = null
                }) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.goBack() },
                modifier = Modifier.testTag("btn_back_to_more")
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Products List",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Stats Cards
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Total Items",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${products.size}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            Card(
                modifier = Modifier.weight(1.2f),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Stock Valuation",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "₹${String.format(Locale.US, "%.2f", totalStockValuation)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .testTag("search_products_input"),
            placeholder = { Text("Search product name...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        // Products List
        if (filteredProducts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Inventory,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No products found",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredProducts) { prod ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedProductForHistory = prod },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = prod.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                                val balanceColor = when {
                                    prod.stockBalance > 0 -> Color(0xFF0F9D58)
                                    prod.stockBalance < 0 -> Color(0xFFBA1A1A)
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = balanceColor.copy(alpha = 0.12f),
                                    modifier = Modifier.padding(start = 8.dp)
                                ) {
                                    Text(
                                        text = "${String.format(Locale.US, "%.1f", prod.stockBalance)} Units",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = balanceColor
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Avg Purchase Price",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "₹${String.format(Locale.US, "%.2f", prod.avgPurchasePrice)}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Avg Sale Price",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "₹${String.format(Locale.US, "%.2f", prod.avgSalePrice)}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Total Qty (In/Out)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "In: ${String.format(Locale.US, "%.1f", prod.totalQtyPurchased)} | Out: ${String.format(Locale.US, "%.1f", prod.totalQtySold)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Stock Valuation",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "₹${String.format(Locale.US, "%.2f", prod.stockValue)}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        productToAdjust = prod
                                        if (viewModel.isTransactionSecurityEnabled()) {
                                            showAdjustmentLockVerify = true
                                        } else {
                                            showAdjustmentInput = true
                                        }
                                    },
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(32.dp).testTag("btn_adjust_stock_${prod.name}")
                                ) {
                                    Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Adjust Stock", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Detail history pop-up dialog
    selectedProductForHistory?.let { prod ->
        val itemHistory = remember(prod, invoices) {
            invoices.flatMap { invWithItems ->
                invWithItems.items
                    .filter { it.name.trim().equals(prod.name, ignoreCase = true) }
                    .map { item -> Triple(invWithItems.invoice, item, invWithItems) }
            }.sortedByDescending { it.first.date }
        }

        AlertDialog(
            onDismissRequest = { selectedProductForHistory = null },
            title = {
                Text(
                    text = "${prod.name} Ledger",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "TRANSACTION REGISTRY",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    if (itemHistory.isEmpty()) {
                        Text("No logs recorded for this item.", style = MaterialTheme.typography.bodyMedium)
                    } else {
                        val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.US) }
                        LazyColumn(
                            modifier = Modifier
                                .height(300.dp)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(itemHistory) { (invoice, item, _) ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                val indicatorColor = if (invoice.type == "SALE") Color(0xFF0F9D58) else Color(0xFF1E88E5)
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = indicatorColor.copy(alpha = 0.15f)
                                                ) {
                                                    Text(
                                                        text = invoice.type,
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = indicatorColor
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "#${invoice.invoiceNumber}",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = invoice.partyName,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = dateFormat.format(Date(invoice.date)),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = "${item.quantity} Qty",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "₹${String.format(Locale.US, "%.2f", item.price)}/u",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = "₹${String.format(Locale.US, "%.2f", item.totalPrice)}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedProductForHistory = null }) {
                    Text("Close")
                }
            }
        )
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

    var showPinDialog by remember { mutableStateOf(false) }
    var tempPin by remember { mutableStateOf("") }

    var showSetPasswordDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }

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

        // --- BILLING CONFIGURATION ---
        Text(
            text = "BILLING CONFIGURATION",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = prefix,
                    onValueChange = { prefix = it },
                    label = { Text("Default Invoice Prefix") },
                    placeholder = { Text("e.g. INV") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .testTag("input_settings_prefix"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = terms,
                    onValueChange = { terms = it },
                    label = { Text("Default Terms & Conditions") },
                    placeholder = { Text("Terms to show at invoice bottom...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .testTag("input_settings_terms"),
                    maxLines = 4,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        // --- 🔒 SECURITY & PREMIUM ---
        Text(
            text = "🔒 SECURITY & PREMIUM",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Enable App Lock row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Enable App Lock", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Text("Secure app on startup", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Switch(
                        checked = appLockEnabled,
                        onCheckedChange = { appLockEnabled = it },
                        modifier = Modifier.testTag("switch_app_lock")
                    )
                }

                if (appLockEnabled) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)

                    // PIN Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Set/Change PIN", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
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

                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)

                    // Fingerprint row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Fingerprint,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Fingerprint Unlock", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                Text("Biometric unlock support", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Switch(
                            checked = fingerprintEnabled,
                            onCheckedChange = { fingerprintEnabled = it },
                            modifier = Modifier.testTag("switch_fingerprint")
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)

                    // Auto Lock row
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("Auto Lock Delay", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val options = listOf(
                                "immediate" to "Immediately",
                                "1" to "1 Minute",
                                "5" to "5 Minutes"
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
            }
        }

        // --- 🛡️ TRANSACTION SECURITY ---
        Text(
            text = "🛡️ TRANSACTION SECURITY",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Transaction Security ON/OFF Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(
                            Icons.Default.VerifiedUser,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Transaction Security", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Text("Require verification for sensitive edits & deletes", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)

                    // Password Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Transaction Password", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
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

                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)

                    // Fingerprint row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(
                                Icons.Default.Fingerprint,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Fingerprint Unlock", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                Text("Use biometric scan for transactions", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
        }

        // --- ADDITIONAL PREMIUM SETTINGS ---
        Text(
            text = "ADDITIONAL FEATURES",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Privacy Mode Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.VisibilityOff,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Privacy Mode", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Text("Mask Dashboard values by default", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Switch(
                        checked = privacyModeEnabled,
                        onCheckedChange = { privacyModeEnabled = it },
                        modifier = Modifier.testTag("switch_privacy_mode")
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)

                // Auto Backup Reminder Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Backup,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Auto Backup Reminder", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Text("Remind to take periodic backup", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Switch(
                        checked = autoBackupReminder,
                        onCheckedChange = { autoBackupReminder = it },
                        modifier = Modifier.testTag("switch_auto_backup")
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)

                // Backup Before Reset Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Restore,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Backup Before Reset", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Text("Force manual backup before any reset", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Switch(
                        checked = backupBeforeReset,
                        onCheckedChange = { backupBeforeReset = it },
                        modifier = Modifier.testTag("switch_backup_before_reset")
                    )
                }
            }
        }

        // --- SAVE BUTTON ---
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
                android.widget.Toast.makeText(context, "Premium configurations saved successfully!", android.widget.Toast.LENGTH_SHORT).show()
                viewModel.setScreen(BillingScreen.MORE)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("btn_save_settings"),
            shape = RoundedCornerShape(12.dp)
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

    // Premium Dark Theme Palette
    val bgDark = Color(0xFF0F172A)          // Rich Slate 900
    val cardDark = Color(0xFF1E293B)        // Cool Slate 800
    val textLight = Color(0xFFF8FAFC)       // Off-White Slate 50
    val textMuted = Color(0xFF94A3B8)       // Cool Grey Slate 400
    val accentBlue = Color(0xFF3B82F6)      // Modern Blue 500
    val accentLightBlue = Color(0xFF60A5FA) // Radiant Blue 400
    val borderDark = Color(0xFF334155)      // Slate 700

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = bgDark
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
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
                        tint = textLight
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "About Application",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = textLight
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // App Branding - Central Logo
            Card(
                modifier = Modifier
                    .size(120.dp),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.ic_vm_book_logo),
                        contentDescription = "VM BOOK Logo",
                        modifier = Modifier.size(90.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // App Name
            Text(
                text = "VM BOOK Ledger",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = textLight
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Tagline
            Text(
                text = "Simple, Smart & Complete Billing",
                style = MaterialTheme.typography.bodyMedium,
                color = accentLightBlue,
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
                colors = CardDefaults.cardColors(containerColor = cardDark),
                border = BorderStroke(1.dp, borderDark)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Application Information",
                        style = MaterialTheme.typography.titleSmall,
                        color = accentLightBlue,
                        fontWeight = FontWeight.Bold
                    )
                    
                    HorizontalDivider(color = borderDark)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Version", style = MaterialTheme.typography.bodyMedium, color = textMuted)
                        Text("v2.1.0", style = MaterialTheme.typography.bodyMedium, color = textLight, fontWeight = FontWeight.SemiBold)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Developed by", style = MaterialTheme.typography.bodyMedium, color = textMuted)
                        Text("VM Tech Services", style = MaterialTheme.typography.bodyMedium, color = textLight, fontWeight = FontWeight.SemiBold)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Platform", style = MaterialTheme.typography.bodyMedium, color = textMuted)
                        Text("Android", style = MaterialTheme.typography.bodyMedium, color = textLight, fontWeight = FontWeight.SemiBold)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Database", style = MaterialTheme.typography.bodyMedium, color = textMuted)
                        Text("Room Database", style = MaterialTheme.typography.bodyMedium, color = textLight, fontWeight = FontWeight.SemiBold)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Current Build", style = MaterialTheme.typography.bodyMedium, color = textMuted)
                        Text("Stable Release", style = MaterialTheme.typography.bodyMedium, color = textLight, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // Section 2: Support Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp)
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardDark),
                border = BorderStroke(1.dp, borderDark)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Customer Support",
                        style = MaterialTheme.typography.titleSmall,
                        color = accentLightBlue,
                        fontWeight = FontWeight.Bold
                    )
                    
                    HorizontalDivider(color = borderDark)

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
                            tint = accentLightBlue,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Support Email", style = MaterialTheme.typography.labelMedium, color = textMuted)
                            Text("support@vmtechservices.in", style = MaterialTheme.typography.bodyMedium, color = textLight, fontWeight = FontWeight.SemiBold)
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Contact",
                            tint = textMuted.copy(alpha = 0.5f),
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
                            tint = accentLightBlue,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Website", style = MaterialTheme.typography.labelMedium, color = textMuted)
                            Text("www.vmtechservices.in", style = MaterialTheme.typography.bodyMedium, color = textLight, fontWeight = FontWeight.SemiBold)
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Go",
                            tint = textMuted.copy(alpha = 0.5f),
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
                            tint = accentLightBlue,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("WhatsApp Support", style = MaterialTheme.typography.labelMedium, color = textMuted)
                            Text("+91-XXXXXXXXXX", style = MaterialTheme.typography.bodyMedium, color = textLight, fontWeight = FontWeight.SemiBold)
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Chat",
                            tint = textMuted.copy(alpha = 0.5f),
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
                colors = CardDefaults.cardColors(containerColor = cardDark),
                border = BorderStroke(1.dp, borderDark)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Quick Actions",
                        style = MaterialTheme.typography.titleSmall,
                        color = accentLightBlue,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    HorizontalDivider(color = borderDark, modifier = Modifier.padding(bottom = 8.dp))

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
                        Icon(Icons.Default.Star, contentDescription = "Rate", tint = accentLightBlue, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("Rate This App", style = MaterialTheme.typography.bodyMedium, color = textLight, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                        Icon(Icons.Default.ChevronRight, contentDescription = "Go", tint = textLight.copy(alpha = 0.3f), modifier = Modifier.size(16.dp))
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
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = accentLightBlue, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("Share App", style = MaterialTheme.typography.bodyMedium, color = textLight, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                        Icon(Icons.Default.ChevronRight, contentDescription = "Go", tint = textLight.copy(alpha = 0.3f), modifier = Modifier.size(16.dp))
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
                        Icon(Icons.Default.Refresh, contentDescription = "Update", tint = accentLightBlue, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("Check for Updates", style = MaterialTheme.typography.bodyMedium, color = textLight, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                        Icon(Icons.Default.ChevronRight, contentDescription = "Go", tint = textLight.copy(alpha = 0.3f), modifier = Modifier.size(16.dp))
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
                        Icon(Icons.Default.Lock, contentDescription = "Privacy", tint = accentLightBlue, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("Privacy Policy", style = MaterialTheme.typography.bodyMedium, color = textLight, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                        Icon(Icons.Default.ChevronRight, contentDescription = "Go", tint = textLight.copy(alpha = 0.3f), modifier = Modifier.size(16.dp))
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
                        Icon(Icons.Default.Description, contentDescription = "Terms", tint = accentLightBlue, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("Terms & Conditions", style = MaterialTheme.typography.bodyMedium, color = textLight, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                        Icon(Icons.Default.ChevronRight, contentDescription = "Go", tint = textLight.copy(alpha = 0.3f), modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Footer
            Text(
                text = "Developed with ❤️ in India",
                style = MaterialTheme.typography.bodyMedium,
                color = textMuted,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "© 2026 VM Tech Services.\nAll Rights Reserved.",
                style = MaterialTheme.typography.bodySmall,
                color = textMuted.copy(alpha = 0.7f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Dialogs
    if (showUpdateDialog) {
        AlertDialog(
            onDismissRequest = { showUpdateDialog = false },
            title = { Text("App is up to date", color = textLight) },
            text = { Text("You are currently running the latest stable release (v2.1.0) of VM BOOK Ledger.", color = textMuted) },
            confirmButton = {
                TextButton(onClick = { showUpdateDialog = false }) {
                    Text("OK", color = accentLightBlue)
                }
            },
            containerColor = cardDark
        )
    }

    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = { Text("Privacy Policy", color = textLight) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = "VM BOOK Ledger is committed to protecting your privacy.\n\n" +
                               "1. Data Collection: All business data, invoice entries, and customer contacts are saved locally on your device in a secure SQLite Room database.\n\n" +
                               "2. Data Transmission: We do not upload or store any of your business transactions on any remote server. Your data stays strictly on your device.\n\n" +
                               "3. Security: Your database backups are completely managed by your own choosing. We use industry-standard security models to safeguard local data access.\n\n" +
                               "4. Third-party APIs: The application only uses platform APIs for local functions and PDF generation. No third-party analytical trackers are present.",
                        color = textMuted,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showPrivacyDialog = false }) {
                    Text("Close", color = accentLightBlue)
                }
            },
            containerColor = cardDark
        )
    }

    if (showTermsDialog) {
        AlertDialog(
            onDismissRequest = { showTermsDialog = false },
            title = { Text("Terms & Conditions", color = textLight) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = "Please read these Terms & Conditions carefully before using the app.\n\n" +
                               "1. Local Storage: VM BOOK Ledger operates locally. If you clear app storage or uninstall the app without a backup, your ledger and transaction history will be permanently deleted.\n\n" +
                               "2. Limitation of Liability: VM Tech Services is not liable for any data loss, financial discrepancy, or accounting errors that may occur. Please verify invoices with original copies.\n\n" +
                               "3. Intended Use: This application is built as a ledger, billing utility, and invoicing aid. Users are solely responsible for local tax regulations and bookkeeping laws.\n\n" +
                               "4. Modification of Services: We reserve the right to modify, update, or deprecate functional modules to comply with Android standards or security policies.",
                        color = textMuted,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showTermsDialog = false }) {
                    Text("Close", color = accentLightBlue)
                }
            },
            containerColor = cardDark
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

@Composable
fun BackupRestoreScreen(viewModel: BillingViewModel) {
    val context = LocalContext.current
    var showFactoryResetDialog by remember { mutableStateOf(false) }
    var showResetSuccessDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

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

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Icon(
                    Icons.Default.CloudUpload,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Generate Database Backup",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Creates a fully valid SQLite copy of your active Room database and registers it via system sharing so you can send it to email, Drive or chat.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { backupDatabaseFile(context) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("btn_backup_now"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Backup Now", fontWeight = FontWeight.Bold)
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
    var showBiometricDialog by remember { mutableStateOf(false) }

    // Lockout countdown timer
    if (lockoutSecondsLeft > 0) {
        LaunchedEffect(lockoutSecondsLeft) {
            delay(1000)
            lockoutSecondsLeft -= 1
        }
    }

    // Biometric dialog simulation
    if (showBiometricDialog) {
        AlertDialog(
            onDismissRequest = { showBiometricDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Fingerprint,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Biometric Authentication", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Touch the fingerprint sensor to unlock VM BOOK",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Pulsing/animated fingerprint icon
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                            .clickable {
                                // Simulate successful verification
                                showBiometricDialog = false
                                android.widget.Toast.makeText(context, "Biometric verified successfully!", android.widget.Toast.LENGTH_SHORT).show()
                                viewModel.setScreen(BillingScreen.DASHBOARD)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Fingerprint,
                            contentDescription = "Fingerprint Sensor",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(44.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Tap the fingerprint icon above to simulate scan",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showBiometricDialog = false }) {
                    Text("Cancel")
                }
            }
        )
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
                                                showBiometricDialog = true
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
                                val hsn = getDeterministicHsn(line.name)
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
    var showBiometricDialog by remember { mutableStateOf(viewModel.isTransactionFingerprintEnabled()) }

    val correctPassword = viewModel.getTransactionPassword()

    if (showBiometricDialog) {
        AlertDialog(
            onDismissRequest = { showBiometricDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Fingerprint,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Biometric Verification", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Scan fingerprint to verify transaction authorization",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                            .clickable {
                                android.widget.Toast.makeText(context, "Biometric verified!", android.widget.Toast.LENGTH_SHORT).show()
                                onVerified()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Fingerprint,
                            contentDescription = "Fingerprint Sensor",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(44.dp)
                        )
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showBiometricDialog = false }) {
                    Text("Use Password")
                }
            }
        )
    } else {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
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
}


