package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.data.AppDatabase
import com.example.data.InvoiceRepository
import com.example.ui.BillingAppContent
import com.example.ui.BillingViewModel
import com.example.ui.BillingViewModelFactory
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

  private val database by lazy { AppDatabase.getDatabase(applicationContext) }
  private val repository by lazy { 
    InvoiceRepository(
      database.invoiceDao(),
      database.customerDao(),
      database.supplierDao()
    ) 
  }
  private val viewModel: BillingViewModel by viewModels {
    BillingViewModelFactory(repository, application)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
        val permission = android.Manifest.permission.POST_NOTIFICATIONS
        if (checkSelfPermission(permission) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(permission), 101)
        }
    }
    
    // Check and trigger system notifications on startup
    viewModel.checkAndSendDueReminders()

    setContent {
      MyApplicationTheme {
        BillingAppContent(viewModel = viewModel)
      }
    }
  }

  override fun onPause() {
    super.onPause()
    viewModel.onAppBackgrounded()
  }

  override fun onResume() {
    super.onResume()
    viewModel.onAppResumed()
  }
}
