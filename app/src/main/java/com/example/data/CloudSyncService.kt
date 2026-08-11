package com.example.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

object CloudSyncService {

    // Helper to serialize all database and settings entries into a JSON String
    fun serializePayload(
        customers: List<CustomerEntity>,
        customerPayments: List<CustomerPaymentEntity>,
        suppliers: List<SupplierEntity>,
        supplierPayments: List<SupplierPaymentEntity>,
        categories: List<ProductCategoryEntity>,
        items: List<ProductItemEntity>,
        invoices: List<InvoiceEntity>,
        invoiceItems: List<InvoiceItemEntity>,
        sequences: List<InvoiceSequenceEntity>,
        settings: Map<String, *>,
        firms: List<FirmEntity> = emptyList()
    ): String {
        val root = JSONObject()

        // Firms
        val firmsArray = JSONArray()
        for (f in firms) {
            val obj = JSONObject()
            obj.put("id", f.id)
            obj.put("name", f.name)
            obj.put("phone", f.phone)
            obj.put("email", f.email)
            obj.put("address", f.address)
            obj.put("gstin", f.gstin)
            obj.put("logoUri", f.logoUri)
            firmsArray.put(obj)
        }
        root.put("firms", firmsArray)

        // Customers
        val customersArray = JSONArray()
        for (c in customers) {
            val obj = JSONObject()
            obj.put("id", c.id)
            obj.put("name", c.name)
            obj.put("phone", c.phone)
            obj.put("email", c.email)
            obj.put("address", c.address)
            obj.put("notes", c.notes)
            obj.put("firmId", c.firmId)
            customersArray.put(obj)
        }
        root.put("customers", customersArray)

        // Customer Payments
        val custPaymentsArray = JSONArray()
        for (p in customerPayments) {
            val obj = JSONObject()
            obj.put("id", p.id)
            obj.put("customerId", p.customerId)
            obj.put("amount", p.amount)
            obj.put("date", p.date)
            obj.put("paymentMode", p.paymentMode)
            obj.put("referenceNo", p.referenceNo)
            obj.put("notes", p.notes)
            obj.put("firmId", p.firmId)
            custPaymentsArray.put(obj)
        }
        root.put("customerPayments", custPaymentsArray)

        // Suppliers
        val suppliersArray = JSONArray()
        for (s in suppliers) {
            val obj = JSONObject()
            obj.put("id", s.id)
            obj.put("name", s.name)
            obj.put("phone", s.phone)
            obj.put("email", s.email)
            obj.put("address", s.address)
            obj.put("notes", s.notes)
            obj.put("firmId", s.firmId)
            suppliersArray.put(obj)
        }
        root.put("suppliers", suppliersArray)

        // Supplier Payments
        val suppPaymentsArray = JSONArray()
        for (p in supplierPayments) {
            val obj = JSONObject()
            obj.put("id", p.id)
            obj.put("supplierId", p.supplierId)
            obj.put("amount", p.amount)
            obj.put("date", p.date)
            obj.put("paymentMode", p.paymentMode)
            obj.put("referenceNo", p.referenceNo)
            obj.put("notes", p.notes)
            obj.put("firmId", p.firmId)
            suppPaymentsArray.put(obj)
        }
        root.put("supplierPayments", suppPaymentsArray)

        // Categories
        val categoriesArray = JSONArray()
        for (cat in categories) {
            val obj = JSONObject()
            obj.put("id", cat.id)
            obj.put("name", cat.name)
            obj.put("firmId", cat.firmId)
            categoriesArray.put(obj)
        }
        root.put("categories", categoriesArray)

        // Product Items
        val itemsArray = JSONArray()
        for (item in items) {
            val obj = JSONObject()
            obj.put("id", item.id)
            obj.put("categoryName", item.categoryName)
            obj.put("name", item.name)
            obj.put("hsnCode", item.hsnCode ?: "")
            if (item.defaultSellingRate != null) {
                obj.put("defaultSellingRate", item.defaultSellingRate)
            }
            if (item.defaultDiscountValue != null) {
                obj.put("defaultDiscountValue", item.defaultDiscountValue)
            }
            if (item.defaultDiscountType != null) {
                obj.put("defaultDiscountType", item.defaultDiscountType)
            }
            obj.put("firmId", item.firmId)
            itemsArray.put(obj)
        }
        root.put("items", itemsArray)

        // Invoices
        val invoicesArray = JSONArray()
        for (inv in invoices) {
            val obj = JSONObject()
            obj.put("id", inv.id)
            obj.put("invoiceNumber", inv.invoiceNumber)
            obj.put("partyName", inv.partyName)
            obj.put("type", inv.type)
            obj.put("date", inv.date)
            obj.put("discount", inv.discount)
            obj.put("tax", inv.tax)
            obj.put("totalAmount", inv.totalAmount)
            obj.put("notes", inv.notes)
            obj.put("isCreditSale", inv.isCreditSale)
            obj.put("outstandingAmount", inv.outstandingAmount)
            obj.put("dueDate", inv.dueDate)
            obj.put("firmId", inv.firmId)
            invoicesArray.put(obj)
        }
        root.put("invoices", invoicesArray)

        // Invoice Items
        val invItemsArray = JSONArray()
        for (item in invoiceItems) {
            val obj = JSONObject()
            obj.put("id", item.id)
            obj.put("invoiceId", item.invoiceId)
            obj.put("name", item.name)
            obj.put("price", item.price)
            obj.put("quantity", item.quantity)
            obj.put("totalPrice", item.totalPrice)
            obj.put("hsnCode", item.hsnCode ?: "")
            invItemsArray.put(obj)
        }
        root.put("invoiceItems", invItemsArray)

        // Sequences
        val seqArray = JSONArray()
        for (seq in sequences) {
            val obj = JSONObject()
            obj.put("type", seq.type)
            obj.put("lastVal", seq.lastVal)
            obj.put("firmId", seq.firmId)
            seqArray.put(obj)
        }
        root.put("sequences", seqArray)

        // Settings Map
        val settingsObj = JSONObject()
        for ((key, value) in settings) {
            if (value != null) {
                settingsObj.put(key, value)
            }
        }
        root.put("settings", settingsObj)

        return root.toString(4)
    }

    // Real REST network push
    fun uploadToCloud(serverUrl: String, mobileNumber: String, payload: String): Boolean {
        try {
            val url = URL("$serverUrl/backup?mobile=$mobileNumber")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true
            conn.connectTimeout = 15000
            conn.readTimeout = 15000

            OutputStreamWriter(conn.outputStream).use { writer ->
                writer.write(payload)
                writer.flush()
            }

            val code = conn.responseCode
            conn.disconnect()
            return code == 200 || code == 201
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            return false
        }
    }

    // Real REST network fetch
    fun downloadFromCloud(serverUrl: String, mobileNumber: String): String? {
        try {
            val url = URL("$serverUrl/restore?mobile=$mobileNumber")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 15000
            conn.readTimeout = 15000

            if (conn.responseCode == 200) {
                val text = conn.inputStream.bufferedReader().use { it.readText() }
                conn.disconnect()
                return text
            }
            conn.disconnect()
            return null
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            return null
        }
    }

    // Local Cloud Sandbox Emulator storage
    fun saveToSandbox(context: Context, mobileNumber: String, payload: String) {
        val file = File(context.filesDir, "sandbox_cloud_$mobileNumber.json")
        file.writeText(payload)
    }

    fun loadFromSandbox(context: Context, mobileNumber: String): String? {
        val file = File(context.filesDir, "sandbox_cloud_$mobileNumber.json")
        return if (file.exists()) file.readText() else null
    }
}
