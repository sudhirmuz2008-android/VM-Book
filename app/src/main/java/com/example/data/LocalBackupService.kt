package com.example.data

import android.content.Context
import android.net.Uri
import org.json.JSONObject
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

object LocalBackupService {

    /**
     * Closes the active Room database to commit all pending transactions and checkpoints,
     * serializes SharedPreferences to JSON, and bundles both into a .vmbook ZIP format at [uri].
     */
    fun performBackupToUri(context: Context, uri: Uri): Boolean {
        return try {
            // Close the database to checkpoint WAL & SHM files safely
            AppDatabase.closeDatabase()

            // Read SharedPreferences of "business_profile_prefs"
            val prefs = context.getSharedPreferences("business_profile_prefs", Context.MODE_PRIVATE)
            val allEntries = prefs.all
            val jsonObj = JSONObject()
            for ((key, value) in allEntries) {
                if (value != null) {
                    jsonObj.put(key, value)
                }
            }
            val prefsJsonStr = jsonObj.toString(4)

            // Get standard database file path
            val dbFile = context.getDatabasePath("billing_database")

            // Create output stream to the Uri and write zip archive
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                ZipOutputStream(outputStream).use { zipOut ->
                    // 1. Write SharedPreferences JSON
                    val prefsEntry = ZipEntry("shared_prefs.json")
                    zipOut.putNextEntry(prefsEntry)
                    zipOut.write(prefsJsonStr.toByteArray(Charsets.UTF_8))
                    zipOut.closeEntry()

                    // 2. Write Database file if it exists
                    if (dbFile.exists()) {
                        val dbEntry = ZipEntry("billing_database")
                        zipOut.putNextEntry(dbEntry)
                        dbFile.inputStream().use { input ->
                            input.copyTo(zipOut)
                        }
                        zipOut.closeEntry()
                    }
                }
            }

            // Re-open/reinitialize the database for ongoing session use
            AppDatabase.getDatabase(context)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            // Make sure database is open even if backup encountered issues
            try { AppDatabase.getDatabase(context) } catch (ex: Exception) { /* ignore */ }
            false
        }
    }

    /**
     * Verifies if the selected file [uri] contains the expected backup signatures:
     * "billing_database" and "shared_prefs.json".
     */
    fun validateBackupFile(context: Context, uri: Uri): Boolean {
        return try {
            var hasDb = false
            var hasPrefs = false
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                ZipInputStream(inputStream).use { zipIn ->
                    var entry = zipIn.nextEntry
                    while (entry != null) {
                        if (entry.name == "billing_database") {
                            hasDb = true
                        } else if (entry.name == "shared_prefs.json") {
                            hasPrefs = true
                        }
                        zipIn.closeEntry()
                        entry = zipIn.nextEntry
                    }
                }
            }
            hasDb && hasPrefs
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Restores the Room Database and SharedPreferences from the backup archive [uri].
     * Wipes any existing database files and updates SharedPreferences, prompting a reboot.
     */
    fun performRestoreFromUri(context: Context, uri: Uri): Boolean {
        return try {
            // Close the database connection
            AppDatabase.closeDatabase()

            val dbFile = context.getDatabasePath("billing_database")
            val dbWalFile = context.getDatabasePath("billing_database-wal")
            val dbShmFile = context.getDatabasePath("billing_database-shm")

            // Delete existing database files to prevent merge corruption/conflicts
            if (dbFile.exists()) dbFile.delete()
            if (dbWalFile.exists()) dbWalFile.delete()
            if (dbShmFile.exists()) dbShmFile.delete()

            var jsonPrefsStr: String? = null

            // Extract files from backup ZIP
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                ZipInputStream(inputStream).use { zipIn ->
                    var entry = zipIn.nextEntry
                    while (entry != null) {
                        if (entry.name == "billing_database") {
                            dbFile.parentFile?.mkdirs()
                            dbFile.outputStream().use { out ->
                                zipIn.copyTo(out)
                            }
                        } else if (entry.name == "shared_prefs.json") {
                            jsonPrefsStr = zipIn.bufferedReader(Charsets.UTF_8).use { it.readText() }
                        }
                        zipIn.closeEntry()
                        entry = zipIn.nextEntry
                    }
                }
            }

            // Repopulate SharedPreferences if found in the backup
            if (jsonPrefsStr != null) {
                val prefs = context.getSharedPreferences("business_profile_prefs", Context.MODE_PRIVATE)
                val editor = prefs.edit()
                editor.clear() // Remove existing keys to ensure exact backup state

                val jsonObj = JSONObject(jsonPrefsStr!!)
                val keys = jsonObj.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val value = jsonObj.get(key)
                    when (value) {
                        is Boolean -> editor.putBoolean(key, value)
                        is Int -> editor.putInt(key, value)
                        is Long -> editor.putLong(key, value)
                        is Double -> editor.putFloat(key, value.toFloat())
                        is Float -> editor.putFloat(key, value)
                        is String -> editor.putString(key, value)
                        else -> editor.putString(key, value.toString())
                    }
                }
                editor.apply()
            }

            // Re-open database with newly copied files
            AppDatabase.getDatabase(context)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            try { AppDatabase.getDatabase(context) } catch (ex: Exception) { /* ignore */ }
            false
        }
    }
}
