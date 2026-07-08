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
  private val repository by lazy { InvoiceRepository(database.invoiceDao()) }
  private val viewModel: BillingViewModel by viewModels {
    BillingViewModelFactory(repository)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        BillingAppContent(viewModel = viewModel)
      }
    }
  }
}
