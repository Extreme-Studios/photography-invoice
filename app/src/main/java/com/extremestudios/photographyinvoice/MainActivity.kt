package com.extremestudios.photographyinvoicelite

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import com.extremestudios.photographyinvoicelite.BuildConfig
import com.extremestudios.photographyinvoicelite.SecurityManager
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest
import java.text.NumberFormat
import java.util.*
import androidx.compose.ui.viewinterop.AndroidView
import android.content.ContentValues
import android.content.ComponentName
import android.provider.MediaStore
import android.content.ClipData
import android.content.ClipboardManager
/**
 * Photography Invoice
 */

object AppColors {
    val Primary = Color(0xFF6366F1)
    val Secondary = Color(0xFF4F46E5)
    val Accent = Color(0xFFF59E0B)
    val Success = Color(0xFF10B981)
    val Error = Color(0xFFEF4444)
    val Background = Color(0xFFF9FAFB)
    val Surface = Color(0xFFFFFFFF)
    val TextPrimary = Color(0xFF111827)
    val TextSecondary = Color(0xFF6B7280)
    val Border = Color(0xFFE5E7EB)
}

data class InvoiceItem(
    val description: String = "",
    val price: String = "",
    val quantity: String = ""
)

data class CompletedInvoice(
    val invoiceNo: String,
    val clientName: String,
    val clientWA: String,
    val items: List<InvoiceItem>,
    val total: Double,
    val paymentMethod: String,
    val date: String,
    val time: String
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FGInvoiceTheme {
                var showSplash by remember { mutableStateOf(true) }

                if (showSplash) {
                    SplashScreen(onFinish = { showSplash = false }, isAdReady = { true })
                } else {
                    Surface(modifier = Modifier.fillMaxSize(), color = AppColors.Background) {
                        InvoiceScreen(onExit = { finish() })
                    }
                }
            }
        }
    }
}

@Composable
fun SplashScreen(onFinish: () -> Unit, isAdReady: () -> Boolean) {
    LaunchedEffect(Unit) {
        delay(3500)
        var totalWait = 0
        while (!isAdReady() && totalWait < 4000) {
            delay(500)
            totalWait += 500
        }
        onFinish()
    }

    Box(
        modifier = Modifier.fillMaxSize().background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.splash_logo),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        
        Column(
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 64.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val versionName = "Version ${BuildConfig.VERSION_NAME}"
            
            Text(
                text = "Powered By Extreme Studios",
                style = TextStyle(
                    color = Color.Black,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = versionName,
                style = TextStyle(
                    color = Color.Black.copy(alpha = 0.6f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            )
            Spacer(modifier = Modifier.height(24.dp))
            LinearProgressIndicator(
                modifier = Modifier.width(140.dp).height(4.dp).clip(CircleShape),
                color = AppColors.Primary,
                trackColor = Color.Black.copy(0.1f)
            )
        }
    }
}

@Composable
fun InvoiceScreen(onExit: () -> Unit) {
    BackHandler { onExit() }
    
    val context = LocalContext.current
    val sharedPref = remember { context.getSharedPreferences("invoice_prefs", Context.MODE_PRIVATE) }
    val gson = Gson()
    val coroutineScope = rememberCoroutineScope()
    val graphicsLayer = rememberGraphicsLayer()
    val clipboardManager = LocalClipboardManager.current
    val focusManager = LocalFocusManager.current
    
    // Security features are fully enabled in this build.
    val isStatusVerified = true

    var showHistory by remember { mutableStateOf(false) }
    var showQueue by remember { mutableStateOf(false) }
    var showResetConfirm by remember { mutableStateOf(false) }
    var currentTime by remember { mutableStateOf(java.util.Date()) }

    var businessName by remember { mutableStateOf(sharedPref.getString("businessName", "Photography Invoice") ?: "Photography Invoice") }
    var businessWA by remember { mutableStateOf(sharedPref.getString("businessWA", "") ?: "") }
    var businessNPWP by remember { mutableStateOf(sharedPref.getString("businessNPWP", "") ?: "") }
    var waCaption by remember { mutableStateOf(sharedPref.getString("waCaption", "Halo [name], berikut Photography Invoice Anda.") ?: "Halo [name], berikut Photography Invoice Anda.") }
    var bankName by remember { mutableStateOf(sharedPref.getString("bankName", "BCA") ?: "BCA") }
    var bankAccountNumber by remember { mutableStateOf(sharedPref.getString("bankAccountNumber", "") ?: "") }
    var showBrandingDialog by remember { mutableStateOf(false) }
    var showTempWhatsAppDialog by remember { mutableStateOf(false) }
    var tempWhatsAppNumber by remember { mutableStateOf("") }

    val items = remember { 
        val json = sharedPref.getString("items", null)
        val list = try {
            if (json != null) gson.fromJson<List<InvoiceItem>>(json, object : TypeToken<List<InvoiceItem>>() {}.type) else listOf(InvoiceItem())
        } catch (e: Exception) { listOf(InvoiceItem()) }
        mutableStateListOf<InvoiceItem>().apply { addAll(list) }
    }
    var clientName by remember { mutableStateOf(sharedPref.getString("clientName", "") ?: "") }
    var clientWA by remember { mutableStateOf(sharedPref.getString("clientWA", "") ?: "") }
    var invoiceCounter by remember { mutableIntStateOf(sharedPref.getInt("invoiceCounter", 1)) }

    var selectedPaymentMethod by remember { mutableStateOf(sharedPref.getString("paymentMethod", "Transfer Bank") ?: "Transfer Bank") }
    var qrisImageUri by remember { mutableStateOf(sharedPref.getString("qris_uri", null)) }
    var isQrisFullscreen by remember { mutableStateOf(false) }

    val queue = remember {
        val json = sharedPref.getString("queue", null)
        val list = try {
            if (json != null) gson.fromJson<List<CompletedInvoice>>(json, object : TypeToken<List<CompletedInvoice>>() {}.type) else emptyList()
        } catch (_: Exception) { emptyList() }
        mutableStateListOf<CompletedInvoice>().apply { addAll(list) }
    }

    val total = items.sumOf { it.price.toDoubleOrNull() ?: 0.0 }
    var totalInvoicesSold by remember { mutableIntStateOf(sharedPref.getInt("todayInvoicesSold", 0)) }
    var totalDailyRevenue by remember { mutableDoubleStateOf(sharedPref.getString("todayDailyRevenue", "0.0")?.toDoubleOrNull() ?: 0.0) }

    var resendInvoiceTarget by remember { mutableStateOf<CompletedInvoice?>(null) }
    var editHistoryTarget by remember { mutableStateOf<CompletedInvoice?>(null) }
    var queueDetailTarget by remember { mutableStateOf<CompletedInvoice?>(null) }
    var queueSendTarget by remember { mutableStateOf<CompletedInvoice?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    
    val history = remember { 
        val json = sharedPref.getString("history", null)
        val list = try {
            if (json != null) gson.fromJson<List<CompletedInvoice>>(json, object : TypeToken<List<CompletedInvoice>>() {}.type) else emptyList()
        } catch (e: Exception) { emptyList() }
        mutableStateListOf<CompletedInvoice>().apply { addAll(list) }
    }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = java.util.Date()
            delay(1000)
        }
    }

    val qrisPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                val qrisFile = File(context.filesDir, "qris_image.jpg")
                val outputStream = FileOutputStream(qrisFile)
                inputStream?.use { input -> outputStream.use { output -> input.copyTo(output) } }
                qrisImageUri = qrisFile.absolutePath + "?t=" + System.currentTimeMillis()
                Toast.makeText(context, "QRIS Berhasil Diupdate", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) { Toast.makeText(context, "Gagal simpan QRIS: ${e.message}", Toast.LENGTH_SHORT).show() }
        }
    }

    fun saveToMediaStore(json: String, fileName: String, relativePath: String, isUpdate: Boolean = false) {
        coroutineScope.launch {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val resolver = context.contentResolver
                    val contentValues = android.content.ContentValues().apply {
                        put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                        put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "application/json")
                        put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS + "/" + relativePath)
                    }

                    var targetUri: Uri? = null
                    if (isUpdate) {
                        resolver.query(
                            android.provider.MediaStore.Files.getContentUri("external"),
                            arrayOf(android.provider.MediaStore.MediaColumns._ID),
                            "${android.provider.MediaStore.MediaColumns.DISPLAY_NAME} = ? AND ${android.provider.MediaStore.MediaColumns.RELATIVE_PATH} = ?",
                            arrayOf(fileName, Environment.DIRECTORY_DOCUMENTS + "/" + relativePath + "/"),
                            null
                        )?.use { cursor ->
                            if (cursor.moveToFirst()) {
                                val id = cursor.getLong(cursor.getColumnIndexOrThrow(android.provider.MediaStore.MediaColumns._ID))
                                targetUri = android.content.ContentUris.withAppendedId(android.provider.MediaStore.Files.getContentUri("external"), id)
                                resolver.delete(targetUri!!, null, null)
                                targetUri = null
                            }
                        }
                    }

                    if (targetUri == null) targetUri = resolver.insert(android.provider.MediaStore.Files.getContentUri("external"), contentValues)
                    targetUri?.let { uri ->
                        resolver.openOutputStream(uri, "rwt")?.use { it.write(json.toByteArray()) }
                    }
                } else {
                    val folder = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), relativePath)
                    if (!folder.exists()) folder.mkdirs()
                    val file = File(folder, fileName)
                    file.writeText(json)
                }
                
                launch(kotlinx.coroutines.Dispatchers.Main) {
                    Toast.makeText(context, "✅ Data Tersimpan di Documents", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) { 
                android.util.Log.e("MediaStore", "Error: ${e.message}")
            }
        }
    }

    fun autoSave() {
        coroutineScope.launch {
            try {
                val data = mapOf(
                    "history" to history.toList(),
                    "queue" to queue.toList(),
                    "invoiceCounter" to invoiceCounter,
                    "todayInvoicesSold" to totalInvoicesSold,
                    "todayDailyRevenue" to totalDailyRevenue,
                    "isStatusVerified" to isStatusVerified
                )
                val jsonString = gson.toJson(data)
                
                // Pastikan folder tersedia di internal untuk akses cepat
                saveToMediaStore(jsonString, "latest_invoice_autosave.json", "ExtremeStudios_Backups", true)
                
                // Backup ekstra ke internal private folder agar selalu ada cadangan
                val internalFile = File(context.filesDir, "latest_autosave_internal.json")
                internalFile.writeText(jsonString)
                
                android.util.Log.d("Autosave", "Berhasil tersimpan di MediaStore & Internal")
            } catch (e: Exception) {
                android.util.Log.e("Autosave", "Gagal: ${e.message}")
            }
        }
    }

    fun resetTodayStats() {
        history.clear()
        queue.clear()
        totalInvoicesSold = 0
        totalDailyRevenue = 0.0
        sharedPref.edit()
            .putInt("todayInvoicesSold", totalInvoicesSold)
            .putString("todayDailyRevenue", totalDailyRevenue.toString())
            .apply()
        Toast.makeText(context, "Pendapatan hari ini direset", Toast.LENGTH_SHORT).show()
    }

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                try {
                    context.contentResolver.openInputStream(uri)?.use { inputStream ->
                        val reader = inputStream.bufferedReader()
                        val jsonContent = reader.readText()
                        if (jsonContent.trim().startsWith("[")) {
                            val importedHistory: List<CompletedInvoice> = gson.fromJson(jsonContent, object : TypeToken<List<CompletedInvoice>>() {}.type)
                            history.clear(); history.addAll(importedHistory)
                            queue.clear()
                            val lastIdx = importedHistory.lastOrNull()?.invoiceNo?.substringAfterLast("-")?.toIntOrNull() ?: 0
                            invoiceCounter = lastIdx + 1
                            totalInvoicesSold = importedHistory.size
                            totalDailyRevenue = importedHistory.sumOf { it.total }
                        } else {
                            val data: Map<String, Any> = gson.fromJson(jsonContent, object : TypeToken<Map<String, Any>>() {}.type)
                            val historyJson = gson.toJson(data["history"])
                            val queueJson = gson.toJson(data["queue"])
                            val importedHistory: List<CompletedInvoice> = gson.fromJson(historyJson, object : TypeToken<List<CompletedInvoice>>() {}.type)
                            val importedQueue: List<CompletedInvoice> = gson.fromJson(queueJson, object : TypeToken<List<CompletedInvoice>>() {}.type)
                            history.clear(); history.addAll(importedHistory)
                            queue.clear(); queue.addAll(importedQueue)
                            invoiceCounter = (data["invoiceCounter"] as? Number)?.toInt() ?: (importedHistory.size + 1)
                            totalInvoicesSold = (data["todayInvoicesSold"] as? Number)?.toInt() ?: importedHistory.size
                            totalDailyRevenue = (data["todayDailyRevenue"] as? Number)?.toDouble() ?: importedHistory.sumOf { it.total }
                        }
                        Toast.makeText(context, "Impor Berhasil", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) { Toast.makeText(context, "Gagal impor: ${e.message}", Toast.LENGTH_SHORT).show() }
            }
        }
    }

    LaunchedEffect(items.toList(), clientName, clientWA, invoiceCounter, history.toList(), queue.toList(), businessName, businessWA, businessNPWP, selectedPaymentMethod, qrisImageUri, totalInvoicesSold, totalDailyRevenue) {
        sharedPref.edit().apply {
            putString("items", gson.toJson(items.toList()))
            putString("clientName", clientName); putString("clientWA", clientWA)
            putInt("invoiceCounter", invoiceCounter)
            putString("history", gson.toJson(history.toList()))
            putString("queue", gson.toJson(queue.toList()))
            putString("businessName", businessName); putString("businessWA", businessWA)
            putString("businessNPWP", businessNPWP); putBoolean("pref_status_verified", isStatusVerified)
            putString("paymentMethod", selectedPaymentMethod); putString("qris_uri", qrisImageUri)
            putInt("todayInvoicesSold", totalInvoicesSold)
            putString("todayDailyRevenue", totalDailyRevenue.toString())
            apply()
        }
    }

    Scaffold(
        containerColor = AppColors.Background,
        bottomBar = {
            NavigationBar(containerColor = AppColors.Surface, tonalElevation = 0.dp, modifier = Modifier.shadow(16.dp)) {
                NavigationBarItem(
                    selected = !showHistory && !showQueue,
                    onClick = { showHistory = false; showQueue = false },
                    icon = { Icon(if (!showHistory && !showQueue) Icons.Filled.Description else Icons.Outlined.Description, null) },
                    label = { Text("Nota Baru", fontWeight = FontWeight.Medium) },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = AppColors.Primary, selectedTextColor = AppColors.Primary, unselectedIconColor = AppColors.TextSecondary, unselectedTextColor = AppColors.TextSecondary, indicatorColor = AppColors.Primary.copy(0.1f))
                )
                NavigationBarItem(
                    selected = showQueue,
                    onClick = { showHistory = false; showQueue = true },
                    icon = { Icon(if (showQueue) Icons.Filled.List else Icons.Outlined.List, null) },
                    label = { Text("Daftar Antrian", fontWeight = FontWeight.Medium) },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = AppColors.Primary, selectedTextColor = AppColors.Primary, unselectedIconColor = AppColors.TextSecondary, unselectedTextColor = AppColors.TextSecondary, indicatorColor = AppColors.Primary.copy(0.1f))
                )
                NavigationBarItem(
                    selected = showHistory,
                    onClick = { showHistory = true; showQueue = false },
                    icon = { Icon(if (showHistory) Icons.Filled.History else Icons.Outlined.History, null) },
                    label = { Text("Riwayat", fontWeight = FontWeight.Medium) },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = AppColors.Primary, selectedTextColor = AppColors.Primary, unselectedIconColor = AppColors.TextSecondary, unselectedTextColor = AppColors.TextSecondary, indicatorColor = AppColors.Primary.copy(0.1f))
                )
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            BannerAdView(adUnitId = "ca-app-pub-8451272966172172/8350122710")
            
            if (!showHistory && !showQueue) {
                BannerAdView(adUnitId = "ca-app-pub-8451272966172172/8350122710")
                Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Detail Tagihan",
                            modifier = Modifier.weight(1f),
                            style = TextStyle(color = AppColors.TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        )
                        IconButton(
                            onClick = { showBrandingDialog = true },
                            modifier = Modifier.background(AppColors.Primary, CircleShape).size(44.dp)
                        ) { Icon(Icons.Outlined.Settings, null, tint = Color.White, modifier = Modifier.size(20.dp)) }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = AppColors.Surface,
                        shape = RoundedCornerShape(20.dp),
                        border = borderStroke(),
                        shadowElevation = 4.dp
                    ) {
                        // Wrapper Box untuk Capture dengan Padding agar tidak terpotong
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .drawWithContent {
                                    graphicsLayer.record {
                                        this@drawWithContent.drawContent()
                                    }
                                    drawLayer(graphicsLayer)
                                }
                                .background(Color.White)
                                .padding(24.dp) // Margin internal agar gambar tidak mepet tepi
                        ) {
                            InvoiceContent(
                                businessName = businessName, businessWA = businessWA, businessNPWP = businessNPWP,
                                clientName = clientName, clientWA = clientWA, invoiceNo = "#INV-${String.format("%03d", invoiceCounter)}",
                                date = java.text.SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(currentTime),
                                items = items, total = total, paymentMethod = selectedPaymentMethod,
                                onUpdateClientName = { clientName = it }, onUpdateClientWA = { clientWA = it },
                                onAddItem = { items.add(InvoiceItem()) }, onUpdateItem = { idx, item -> items[idx] = item }, onDeleteItem = { idx -> items.removeAt(idx) },
                                onPaymentMethodChange = { selectedPaymentMethod = it },
                                qrisImageUri = qrisImageUri,
                                onUploadQris = { qrisPickerLauncher.launch("image/*") },
                                onQrisFullscreen = { isQrisFullscreen = true },
                                bankName = bankName,
                                bankAccountNumber = bankAccountNumber,
                                onBankNameChange = { bankName = it },
                                onBankAccountNumberChange = { bankAccountNumber = it },
                                onCopyBankNumber = { clipboardManager.setText(AnnotatedString(bankAccountNumber)); Toast.makeText(context, "Nomor rekening disalin", Toast.LENGTH_SHORT).show() },
                                focusManager = focusManager
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
    // Area Tombol Aksi
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { showResetConfirm = true },
                            modifier = Modifier.weight(0.9f).height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AppColors.TextSecondary)
                        ) {
                            Icon(Icons.Default.Refresh, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp)); Text("RESET", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        Button(
                            onClick = { 
                                val targetNumber = when {
                                    clientWA.isNotBlank() -> clientWA
                                    tempWhatsAppNumber.isNotBlank() -> tempWhatsAppNumber
                                    else -> ""
                                }
                                if (targetNumber.isBlank()) {
                                    showTempWhatsAppDialog = true
                                } else {
                                    executeSendInvoice(
                                        context = context,
                                        layer = graphicsLayer,
                                        scope = coroutineScope,
                                        name = clientName,
                                        wa = targetNumber,
                                        no = "#INV-${String.format("%03d", invoiceCounter)}",
                                        captionTemplate = waCaption,
                                        paymentMethod = selectedPaymentMethod
                                    )
                                }
                            },
                            modifier = Modifier.weight(1f).height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AppColors.Success)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp)); Text("KIRIM", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        
                        Button(
                            onClick = {
                                val now = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())
                                val split = now.split(" ")
                                queue.add(CompletedInvoice("#INV-${String.format("%03d", invoiceCounter)}", clientName.ifEmpty { "Tanpa Nama" }, clientWA.ifEmpty { "-" }, items.toList(), total, selectedPaymentMethod, split[0], split[1]))
                                invoiceCounter++
                                autoSave()
                                clientName = ""; clientWA = ""
                                Toast.makeText(context, "Masuk daftar antrian", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1.1f).height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary)
                        ) { Text("SIMPAN", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                    }
                    Spacer(modifier = Modifier.height(40.dp))
                }
            } else if (showQueue) {
                BannerAdView(adUnitId = "ca-app-pub-8451272966172172/8350122710")
                QueueScreen(
                    queue = queue,
                    onDone = { queuedInvoice ->
                        queue.remove(queuedInvoice)
                        history.add(queuedInvoice)
                        totalInvoicesSold += 1
                        totalDailyRevenue += queuedInvoice.total
                        autoSave()
                        Toast.makeText(context, "Masuk riwayat penjualan", Toast.LENGTH_SHORT).show()
                    },
                    onOpenDetails = { queueDetailTarget = it },
                    onSend = { queueSendTarget = it },
                    padding = padding
                )
            } else {
                BannerAdView(adUnitId = "ca-app-pub-8451272966172172/8350122710")
                HistoryScreen(
                    history = history, query = searchQuery, onSearchQueryChange = { searchQuery = it },
                    totalInvoicesSold = totalInvoicesSold,
                    totalDailyRevenue = totalDailyRevenue,
                    onResend = { resendInvoiceTarget = it },
                    onEdit = { editHistoryTarget = it },
                    onBackup = {
                        val json = gson.toJson(
                            mapOf(
                                "history" to history.toList(),
                                "queue" to queue.toList(),
                                "invoiceCounter" to invoiceCounter,
                                "todayInvoicesSold" to totalInvoicesSold,
                                "todayDailyRevenue" to totalDailyRevenue,
                                "isStatusVerified" to isStatusVerified
                            )
                        )
                        saveToMediaStore(json, "Backup_${System.currentTimeMillis()}.json", "ExtremeStudios_Backups/Manual_Backups")
                        Toast.makeText(context, "✅ Backup Berhasil", Toast.LENGTH_SHORT).show()
                    },
                    onImport = {
                        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply { addCategory(Intent.CATEGORY_OPENABLE); type = "application/json" }
                        importLauncher.launch(intent)
                    },
                    padding = padding
                )
            }
        }
    }


    if (showBrandingDialog) {
        BrandingSettingsDialog(
            businessName, businessWA, businessNPWP, waCaption,
            onUpdateName = { businessName = it }, onUpdateWA = { businessWA = it }, onUpdateNPWP = { businessNPWP = it }, onUpdateCaption = { waCaption = it },
            onDismiss = { showBrandingDialog = false }
        )
    }

    if (showResetConfirm) {
        Dialog(onDismissRequest = { showResetConfirm = false }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = AppColors.Surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Reset Data", fontSize = 18.sp, fontWeight = FontWeight.Black, color = AppColors.TextPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Riwayat penjualan dan daftar antrian akan dikosongkan. Nomor invoice tetap lanjut.",
                        color = AppColors.TextSecondary,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { showResetConfirm = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("BATAL", fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = {
                                showResetConfirm = false
                                resetTodayStats()
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = AppColors.Error)
                        ) {
                            Text("RESET", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    queueDetailTarget?.let { queuedInvoice ->
        Dialog(onDismissRequest = { queueDetailTarget = null }) {
            Surface(shape = RoundedCornerShape(20.dp), color = AppColors.Surface) {
                Column(modifier = Modifier.padding(20.dp).verticalScroll(rememberScrollState())) {
                    Text("Detail Nota", fontSize = 18.sp, fontWeight = FontWeight.Black, color = AppColors.TextPrimary)
                    Spacer(modifier = Modifier.height(12.dp))
                    DetailLine("Invoice", queuedInvoice.invoiceNo)
                    DetailLine("Nama", queuedInvoice.clientName)
                    DetailLine("No. WA", queuedInvoice.clientWA)
                    DetailLine("Keterangan", queuedInvoice.items.joinToString(separator = "\n") { item ->
                        val qty = item.quantity.ifBlank { "1" }
                        val desc = item.description.ifBlank { "-" }
                        val price = item.price.ifBlank { "0" }
                        "• $desc x$qty @ $price"
                    })
                    DetailLine("Total Tagihan", formatCurrency(queuedInvoice.total))
                    DetailLine("Sistem Pembayaran", queuedInvoice.paymentMethod)
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = { queueDetailTarget = null },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("TUTUP", fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            queueDetailTarget = null
                            queueSendTarget = queuedInvoice
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.Success)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("KIRIM", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    editHistoryTarget?.let { invoiceToEdit ->
        EditHistoryInvoiceDialog(
            invoice = invoiceToEdit,
            onDismiss = { editHistoryTarget = null },
            onSave = { updatedInvoice ->
                val index = history.indexOf(invoiceToEdit)
                if (index >= 0) {
                    history[index] = updatedInvoice
                    totalDailyRevenue += updatedInvoice.total - invoiceToEdit.total
                    autoSave()
                    Toast.makeText(context, "Riwayat nota diperbarui", Toast.LENGTH_SHORT).show()
                }
                editHistoryTarget = null
            }
        )
    }
    if (showTempWhatsAppDialog) {
        Dialog(onDismissRequest = { showTempWhatsAppDialog = false }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = AppColors.Surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "Nomor WhatsApp sementara",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = AppColors.TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Isi nomor baru di sini kalau belum ada di daftar. Nomor ini tidak disimpan.",
                        fontSize = 12.sp,
                        color = AppColors.TextSecondary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = tempWhatsAppNumber,
                        onValueChange = { tempWhatsAppNumber = it.filter { c -> c.isDigit() || c == '+' } },
                        label = { Text("No. WhatsApp") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            val phone = normalizeWhatsappNumber(tempWhatsAppNumber)
                            if (phone.isBlank()) {
                                Toast.makeText(context, "Nomor WhatsApp belum diisi", Toast.LENGTH_SHORT).show()
                            } else {
                                showTempWhatsAppDialog = false
                                val openIntent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://api.whatsapp.com/send?phone=$phone")
                                ).apply {
                                    setPackage("com.whatsapp")
                                }
                                try {
                                    context.startActivity(openIntent)
                                } catch (_: Exception) {
                                    context.startActivity(Intent.createChooser(openIntent, "Buka WhatsApp"))
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary)
                    ) {
                        Text("SIAPKAN & BUKA WA", fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(18.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { showTempWhatsAppDialog = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("BATAL", fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = {
                                val number = tempWhatsAppNumber.trim()
                                if (number.isBlank()) {
                                    Toast.makeText(context, "Nomor WhatsApp belum diisi", Toast.LENGTH_SHORT).show()
                                } else {
                                    executeSendInvoice(
                                        context = context,
                                        layer = graphicsLayer,
                                        scope = coroutineScope,
                                        name = clientName,
                                        wa = number,
                                        no = "#INV-${String.format("%03d", invoiceCounter)}",
                                        captionTemplate = waCaption,
                                        paymentMethod = selectedPaymentMethod
                                    )
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = AppColors.Success)
                        ) {
                            Text("KIRIM", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
    resendInvoiceTarget?.let { historicalInv ->
        SendInvoiceDialog(
            title = "Kirim Ulang Nota",
            invoice = historicalInv,
            businessName = businessName,
            businessWA = businessWA,
            businessNPWP = businessNPWP,
            focusManager = focusManager,
            onDismiss = { resendInvoiceTarget = null },
            onSend = { resendLayer ->
                executeSendInvoice(
                    context = context,
                    layer = resendLayer,
                    scope = coroutineScope,
                    name = historicalInv.clientName,
                    wa = historicalInv.clientWA,
                    no = historicalInv.invoiceNo,
                    captionTemplate = waCaption,
                    paymentMethod = historicalInv.paymentMethod
                )
                resendInvoiceTarget = null
            }
        )
    }

    queueSendTarget?.let { queuedInvoice ->
        SendInvoiceDialog(
            title = "Kirim Nota Antrian",
            invoice = queuedInvoice,
            businessName = businessName,
            businessWA = businessWA,
            businessNPWP = businessNPWP,
            focusManager = focusManager,
            onDismiss = { queueSendTarget = null },
            onSend = { sendLayer ->
                executeSendInvoice(
                    context = context,
                    layer = sendLayer,
                    scope = coroutineScope,
                    name = queuedInvoice.clientName,
                    wa = queuedInvoice.clientWA,
                    no = queuedInvoice.invoiceNo,
                    captionTemplate = waCaption,
                    paymentMethod = queuedInvoice.paymentMethod
                )
                queueSendTarget = null
            }
        )
    }

    if (isQrisFullscreen && qrisImageUri != null) {
        Dialog(onDismissRequest = { isQrisFullscreen = false }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.9f)).clickable { isQrisFullscreen = false }, contentAlignment = Alignment.Center) {
                val bitmap = remember(qrisImageUri) {
                    try {
                        val actualPath = qrisImageUri?.substringBefore("?t=") ?: ""
                        android.graphics.BitmapFactory.decodeFile(actualPath)?.asImageBitmap()
                    } catch (e: Exception) { null }
                }
                bitmap?.let { Image(bitmap = it, contentDescription = null, modifier = Modifier.fillMaxSize(0.9f), contentScale = ContentScale.Fit) }
                Text("Ketuk untuk menutup", color = Color.White, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 32.dp), fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun EditHistoryInvoiceDialog(invoice: CompletedInvoice, onDismiss: () -> Unit, onSave: (CompletedInvoice) -> Unit) {
    var clientName by remember(invoice) { mutableStateOf(invoice.clientName) }
    var clientWA by remember(invoice) { mutableStateOf(invoice.clientWA) }
    var paymentMethod by remember(invoice) { mutableStateOf(invoice.paymentMethod) }
    val editableItems = remember(invoice) {
        mutableStateListOf<InvoiceItem>().apply {
            addAll(invoice.items.ifEmpty { listOf(InvoiceItem()) })
        }
    }
    val total = editableItems.sumOf { it.price.toDoubleOrNull() ?: 0.0 }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.94f).fillMaxHeight(0.9f),
            shape = RoundedCornerShape(20.dp),
            color = AppColors.Surface
        ) {
            Column(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState())) {
                Text("Edit Riwayat Nota", fontSize = 18.sp, fontWeight = FontWeight.Black, color = AppColors.TextPrimary)
                Spacer(modifier = Modifier.height(4.dp))
                Text(invoice.invoiceNo, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AppColors.Primary)
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = clientName,
                    onValueChange = { clientName = it },
                    label = { Text("Nama Klien") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = clientWA,
                    onValueChange = { clientWA = it.filter { c -> c.isDigit() || c == '+' } },
                    label = { Text("No. WhatsApp") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )

                Spacer(modifier = Modifier.height(16.dp))
                Text("Keterangan Nota", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AppColors.TextSecondary)
                Spacer(modifier = Modifier.height(8.dp))
                editableItems.forEachIndexed { index, item ->
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = AppColors.Background,
                        border = borderStroke()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            OutlinedTextField(
                                value = item.description,
                                onValueChange = { editableItems[index] = item.copy(description = it) },
                                label = { Text("Keterangan") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                OutlinedTextField(
                                    value = item.quantity,
                                    onValueChange = { editableItems[index] = item.copy(quantity = it.filter { c -> c.isDigit() }) },
                                    label = { Text("Qty") },
                                    modifier = Modifier.weight(0.7f),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                )
                                OutlinedTextField(
                                    value = item.price,
                                    onValueChange = { editableItems[index] = item.copy(price = it.filter { c -> c.isDigit() }) },
                                    label = { Text("Harga") },
                                    modifier = Modifier.weight(1.3f),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                )
                                IconButton(
                                    onClick = {
                                        if (editableItems.size > 1) editableItems.removeAt(index)
                                    }
                                ) {
                                    Icon(Icons.Default.Delete, null, tint = AppColors.Error)
                                }
                            }
                        }
                    }
                }

                OutlinedButton(
                    onClick = { editableItems.add(InvoiceItem()) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("TAMBAH ITEM", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Sistem Pembayaran", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AppColors.TextSecondary)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    listOf("Transfer Bank", "QRIS", "Cash").forEach { option ->
                        FilterChip(
                            selected = paymentMethod == option,
                            onClick = { paymentMethod = option },
                            label = { Text(option, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text("Total: ${formatCurrency(total)}", fontSize = 16.sp, fontWeight = FontWeight.Black, color = AppColors.Primary)
                Spacer(modifier = Modifier.height(20.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("BATAL", fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = {
                            onSave(
                                invoice.copy(
                                    clientName = clientName.ifBlank { "Tanpa Nama" },
                                    clientWA = clientWA.ifBlank { "-" },
                                    items = editableItems.toList(),
                                    total = total,
                                    paymentMethod = paymentMethod
                                )
                            )
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary)
                    ) {
                        Text("SIMPAN", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
@Composable
fun HistoryScreen(history: List<CompletedInvoice>, query: String, onSearchQueryChange: (String) -> Unit, totalInvoicesSold: Int, totalDailyRevenue: Double, onResend: (CompletedInvoice) -> Unit, onEdit: (CompletedInvoice) -> Unit, onBackup: () -> Unit, onImport: () -> Unit, padding: PaddingValues) {
    val context = LocalContext.current
    val sharedPref = remember { context.getSharedPreferences("invoice_prefs", Context.MODE_PRIVATE) }
    val isStatusVerified = SecurityManager.isPremiumAccessAuthorized(sharedPref)
    
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = AppColors.Surface,
            shape = RoundedCornerShape(20.dp),
            border = borderStroke(),
            shadowElevation = 2.dp
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Laporan Hari Ini", style = TextStyle(color = AppColors.TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(formatCurrency(totalDailyRevenue), style = TextStyle(color = AppColors.TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Black))
                }
                Surface(color = AppColors.Success.copy(0.1f), shape = RoundedCornerShape(8.dp)) {
                    Text("$totalInvoicesSold Nota Terjual", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), color = AppColors.Success, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            ActionButton(Icons.Default.Save, "SIMPAN", AppColors.Primary) { onBackup() }
            Spacer(Modifier.width(8.dp))
            ActionButton(Icons.Default.FileDownload, "IMPORT", AppColors.TextSecondary) { onImport() }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Text("RIWAYAT NOTA", style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Black, color = AppColors.TextPrimary))
        }
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = query, onValueChange = onSearchQueryChange, placeholder = { Text("Cari klien...") },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = AppColors.Surface, focusedContainerColor = AppColors.Surface)
        )
        Spacer(modifier = Modifier.height(16.dp))

        val filtered = history.filter { it.clientName.contains(query, true) || it.invoiceNo.contains(query, true) }.reversed()
        if (filtered.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Tidak ada riwayat", color = AppColors.TextSecondary) }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
                items(filtered) { inv ->
                    Surface(
                        color = AppColors.Surface, shape = RoundedCornerShape(16.dp), border = borderStroke(),
                        modifier = Modifier.fillMaxWidth().clickable { onResend(inv) }
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(inv.invoiceNo, fontWeight = FontWeight.Bold, color = AppColors.Primary, fontSize = 14.sp)
                                Text(inv.clientName, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary, fontSize = 16.sp)
                                Text("${inv.date} • ${inv.time}", color = AppColors.TextSecondary, fontSize = 12.sp)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(formatCurrency(inv.total), fontWeight = FontWeight.Black, color = AppColors.TextPrimary, fontSize = 16.sp)
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    IconButton(
                                        onClick = { onEdit(inv) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, null, tint = AppColors.Primary, modifier = Modifier.size(16.dp))
                                    }
                                    Icon(Icons.AutoMirrored.Filled.Send, null, tint = AppColors.Success, modifier = Modifier.size(16.dp))
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
fun QueueScreen(queue: List<CompletedInvoice>, onDone: (CompletedInvoice) -> Unit, onOpenDetails: (CompletedInvoice) -> Unit, onSend: (CompletedInvoice) -> Unit, padding: PaddingValues) {
    val ordered = queue.sortedBy { it.invoiceNo.substringAfterLast("-").toIntOrNull() ?: Int.MAX_VALUE }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("DAFTAR ANTRIAN", style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Black, color = AppColors.TextPrimary))
        Spacer(modifier = Modifier.height(16.dp))
        if (ordered.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Belum ada antrian", color = AppColors.TextSecondary)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
                items(ordered) { inv ->
                    Surface(
                        color = AppColors.Surface,
                        shape = RoundedCornerShape(16.dp),
                        border = borderStroke(),
                        modifier = Modifier.fillMaxWidth().clickable { onOpenDetails(inv) }
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(inv.invoiceNo, fontWeight = FontWeight.Bold, color = AppColors.Primary, fontSize = 14.sp)
                                Text(inv.clientName, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary, fontSize = 16.sp)
                                Text(inv.paymentMethod, color = AppColors.TextSecondary, fontSize = 12.sp)
                            }
                            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Button(
                                    onClick = { onDone(inv) },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.Success)
                                ) {
                                    Text("DONE", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                                Button(
                                    onClick = { onSend(inv) },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary)
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.Send, null, modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("KIRIM", fontWeight = FontWeight.Bold, fontSize = 11.sp)
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
fun DetailLine(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AppColors.TextSecondary)
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = AppColors.TextPrimary)
        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
fun SendInvoiceDialog(
    title: String,
    invoice: CompletedInvoice,
    businessName: String,
    businessWA: String,
    businessNPWP: String,
    focusManager: FocusManager,
    onDismiss: () -> Unit,
    onSend: (GraphicsLayer) -> Unit
) {
    val sendLayer = rememberGraphicsLayer()
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(modifier = Modifier.fillMaxWidth(0.95f).verticalScroll(rememberScrollState()), shape = RoundedCornerShape(24.dp), color = AppColors.Background) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(title, fontWeight = FontWeight.Black, fontSize = 20.sp, modifier = Modifier.padding(bottom = 12.dp))
                Surface(color = AppColors.Surface, shape = RoundedCornerShape(16.dp), border = borderStroke()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .drawWithContent {
                                sendLayer.record {
                                    this@drawWithContent.drawContent()
                                }
                                drawLayer(sendLayer)
                            }
                            .background(Color.White)
                            .padding(24.dp)
                    ) {
                        InvoiceContent(
                            businessName = businessName,
                            businessWA = businessWA,
                            businessNPWP = businessNPWP,
                            clientName = invoice.clientName,
                            clientWA = invoice.clientWA,
                            invoiceNo = invoice.invoiceNo,
                            date = invoice.date,
                            items = invoice.items,
                            total = invoice.total,
                            paymentMethod = invoice.paymentMethod,
                            onUpdateClientName = {},
                            onUpdateClientWA = {},
                            onAddItem = {},
                            onUpdateItem = { _, _ -> },
                            onDeleteItem = {},
                            focusManager = focusManager,
                            isReadOnly = true
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(0.7f)) {
                        Text("TUTUP", color = AppColors.TextSecondary)
                    }
                    Button(
                        onClick = { onSend(sendLayer) },
                        modifier = Modifier.weight(1f).height(44.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("WA", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun ActionButton(icon: ImageVector, label: String, color: Color, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.height(40.dp).clickable { onClick() },
        color = color.copy(0.1f), shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(0.2f))
    ) {
        Row(modifier = Modifier.padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, modifier = Modifier.size(16.dp), tint = color)
            Spacer(Modifier.width(6.dp)); Text(label, color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun QrisSection(uri: String?, onUpload: () -> Unit, onFullscreen: () -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth(), color = AppColors.Surface, shape = RoundedCornerShape(16.dp), border = borderStroke()) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("PEMBAYARAN QRIS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AppColors.Primary)
            Spacer(Modifier.height(8.dp))
            if (uri != null) {
                val bitmap = remember(uri) {
                    try {
                        val actualPath = uri.substringBefore("?t=")
                        android.graphics.BitmapFactory.decodeFile(actualPath)?.asImageBitmap()
                    } catch (e: Exception) { null }
                }
                bitmap?.let {
                    Image(bitmap = it, contentDescription = "QRIS", modifier = Modifier.fillMaxWidth().heightIn(max = 180.dp).padding(horizontal = 8.dp).border(2.dp, AppColors.Primary, RoundedCornerShape(8.dp)).padding(4.dp).clickable { onFullscreen() })
                }
            } else {
                Box(modifier = Modifier.fillMaxWidth().height(150.dp).background(AppColors.Background, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                    Text("QRIS BELUM DIUPLOAD", color = AppColors.TextSecondary, fontSize = 11.sp)
                }
            }
            TextButton(onClick = onUpload) {
                Text(if (uri == null) "UPLOAD QRIS" else "GANTI QRIS", color = AppColors.Primary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun BankSection(name: String, number: String, onNameChange: (String) -> Unit, onNumberChange: (String) -> Unit, onCopy: () -> Unit, focusManager: FocusManager) {
    Surface(modifier = Modifier.fillMaxWidth(), color = AppColors.Surface, shape = RoundedCornerShape(16.dp), border = borderStroke()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("INFO REKENING BANK", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AppColors.Primary, modifier = Modifier.align(Alignment.CenterHorizontally))
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextField(
                    value = name, onValueChange = onNameChange, label = { Text("NAMA BANK", fontSize = 9.sp) }, modifier = Modifier.weight(1f),
                    colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent)
                )
                Spacer(Modifier.width(8.dp))
                TextField(
                    value = number, onValueChange = onNumberChange, label = { Text("NO. REKENING", fontSize = 9.sp) }, modifier = Modifier.weight(1.5f),
                    colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    trailingIcon = { IconButton(onClick = onCopy) { Icon(Icons.Default.ContentCopy, null, tint = AppColors.Primary, modifier = Modifier.size(18.dp)) } }
                )
            }
        }
    }
}

@Composable
fun InvoiceContent(
    businessName: String, businessWA: String, businessNPWP: String,
    clientName: String, clientWA: String, invoiceNo: String, date: String,
    items: List<InvoiceItem>, total: Double, paymentMethod: String,
    onUpdateClientName: (String) -> Unit, onUpdateClientWA: (String) -> Unit,
    onAddItem: () -> Unit, onUpdateItem: (Int, InvoiceItem) -> Unit, onDeleteItem: (Int) -> Unit,
    onPaymentMethodChange: (String) -> Unit = {},
    qrisImageUri: String? = null,
    onUploadQris: () -> Unit = {},
    onQrisFullscreen: () -> Unit = {},
    bankName: String = "",
    bankAccountNumber: String = "",
    onBankNameChange: (String) -> Unit = {},
    onBankAccountNumberChange: (String) -> Unit = {},
    onCopyBankNumber: () -> Unit = {},
    focusManager: FocusManager, isReadOnly: Boolean = false
) {
    val context = LocalContext.current
    val sharedPref = remember { context.getSharedPreferences("invoice_prefs", Context.MODE_PRIVATE) }
    val isStatusVerified = SecurityManager.isPremiumAccessAuthorized(sharedPref)
    Column(modifier = Modifier.fillMaxWidth().background(Color.White)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(if (isStatusVerified) businessName.uppercase() else "INVOICE PENJUALAN", fontWeight = FontWeight.Black, fontSize = 20.sp, color = AppColors.Primary)
                if (businessNPWP.isNotEmpty()) Text("NPWP: $businessNPWP", fontSize = 10.sp, color = AppColors.TextSecondary)
                Text("WA: $businessWA", fontSize = 10.sp, color = AppColors.Success, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.height(16.dp)); HorizontalDivider(color = AppColors.Primary, thickness = 2.dp); Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
                if (isReadOnly) {
                    Text("KEPADA:", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = AppColors.TextSecondary)
                    Text(clientName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(clientWA, fontSize = 12.sp, color = AppColors.TextSecondary)
                } else {
                    CompactInput(value = clientName, onValueChange = onUpdateClientName, label = "NAMA KLIEN")
                    CompactInput(value = clientWA, onValueChange = onUpdateClientWA, label = "NO WHATSAPP", keyboardType = KeyboardType.Phone)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("NO. NOTA", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = AppColors.TextSecondary)
                Text(invoiceNo, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(date, fontSize = 11.sp, color = AppColors.TextSecondary)
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        Surface(color = AppColors.Primary, shape = RoundedCornerShape(4.dp)) {
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("KETERANGAN", modifier = Modifier.weight(1f).padding(start = 8.dp), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text("QTY", modifier = Modifier.width(40.dp), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                Text("HARGA", modifier = Modifier.width(80.dp).padding(end = 8.dp), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
            }
        }
        items.forEachIndexed { idx, item ->
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                if (isReadOnly) {
                    Text(item.description, modifier = Modifier.weight(1f).padding(start = 8.dp), fontSize = 12.sp)
                    Text(item.quantity, modifier = Modifier.width(40.dp), fontSize = 12.sp, textAlign = TextAlign.Center)
                    Text(formatCurrency(item.price.toDoubleOrNull() ?: 0.0), modifier = Modifier.width(80.dp).padding(end = 8.dp), fontSize = 12.sp, textAlign = TextAlign.End)
                } else {
                    InvoiceRow(idx, item, onUpdate = { onUpdateItem(idx, it) }, onDelete = { onDeleteItem(idx) }, focusManager)
                }
            }
            HorizontalDivider(color = AppColors.Border, thickness = 0.5.dp)
        }
        if (!isReadOnly) {
            TextButton(onClick = onAddItem, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(14.dp)); Text("Tambah Item", fontSize = 12.sp)
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Surface(color = AppColors.Primary.copy(0.05f), shape = RoundedCornerShape(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("TOTAL TAGIHAN", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AppColors.TextPrimary)
                Text(formatCurrency(total), fontWeight = FontWeight.Black, fontSize = 18.sp, color = AppColors.Primary)
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        var showPaymentMenuLocal by remember { mutableStateOf(false) }
        if (isReadOnly) {
            Text("METODE PEMBAYARAN", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = AppColors.TextSecondary)
            Text(paymentMethod, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary, fontSize = 14.sp)
        } else {
            Text("Pilih Metode Bayar", style = TextStyle(color = AppColors.TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(8.dp))
            Box {
                Surface(
                    modifier = Modifier.fillMaxWidth().clickable { showPaymentMenuLocal = true },
                    color = AppColors.Surface, shape = RoundedCornerShape(12.dp), border = borderStroke()
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Payment, null, tint = AppColors.Primary)
                        Spacer(Modifier.width(12.dp))
                        Text(paymentMethod, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary, modifier = Modifier.weight(1f))
                        Icon(Icons.Default.ArrowDropDown, null)
                    }
                }
                DropdownMenu(expanded = showPaymentMenuLocal, onDismissRequest = { showPaymentMenuLocal = false }) {
                    val methods = listOf("Transfer Bank", "Dana", "Shopee", "Qris", "Gopay", "Cash")
                    methods.forEach { method ->
                        DropdownMenuItem(text = { Text(method) }, onClick = { onPaymentMethodChange(method); showPaymentMenuLocal = false })
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            if (paymentMethod == "Qris") {
                QrisSection(qrisImageUri, onUploadQris, onQrisFullscreen)
            } else if (paymentMethod == "Transfer Bank") {
                BankSection(bankName, bankAccountNumber, onBankNameChange, onBankAccountNumberChange, onCopyBankNumber, focusManager)
            }
        }
    }
}

@Composable
fun CompactInput(value: String, onValueChange: (String) -> Unit, label: String, keyboardType: KeyboardType = KeyboardType.Text) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(label, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = AppColors.Primary)
        BasicTextField(
            value = value, onValueChange = onValueChange,
            textStyle = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType, capitalization = KeyboardCapitalization.Words),
            modifier = Modifier.fillMaxWidth()
        )
        Box(Modifier.fillMaxWidth().height(1.dp).background(AppColors.Border))
    }
}

@Composable
fun InvoiceRow(index: Int, item: InvoiceItem, onUpdate: (InvoiceItem) -> Unit, onDelete: () -> Unit, focusManager: FocusManager) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        BasicTextField(
            value = item.description, onValueChange = { onUpdate(item.copy(description = it)) },
            modifier = Modifier.weight(1f).padding(start = 4.dp),
            textStyle = TextStyle(fontSize = 12.sp, color = AppColors.TextPrimary),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
        )
        BasicTextField(
            value = item.quantity, onValueChange = { onUpdate(item.copy(quantity = it.filter { c -> c.isDigit() })) },
            modifier = Modifier.width(40.dp),
            textStyle = TextStyle(fontSize = 12.sp, color = AppColors.TextPrimary, textAlign = TextAlign.Center),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        BasicTextField(
            value = item.price, onValueChange = { onUpdate(item.copy(price = it.filter { c -> c.isDigit() })) },
            modifier = Modifier.width(80.dp).padding(end = 4.dp),
            textStyle = TextStyle(fontSize = 12.sp, color = AppColors.TextPrimary, textAlign = TextAlign.End),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
        )
        IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
            Icon(Icons.Default.Close, null, tint = AppColors.Error.copy(0.5f), modifier = Modifier.size(14.dp))
        }
    }
}

@Composable
fun BrandingSettingsDialog(name: String, wa: String, npwp: String, caption: String, onUpdateName: (String) -> Unit, onUpdateWA: (String) -> Unit, onUpdateNPWP: (String) -> Unit, onUpdateCaption: (String) -> Unit, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val sharedPref = remember { context.getSharedPreferences("invoice_prefs", Context.MODE_PRIVATE) }
    val isStatusVerified = SecurityManager.isPremiumAccessAuthorized(sharedPref)
    
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(24.dp), color = AppColors.Background) {
            Column(modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState())) {
                Text("Pengaturan Branding", style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Black, color = AppColors.TextPrimary))
                Spacer(modifier = Modifier.height(16.dp))
                StartupInput(value = name, onValueChange = onUpdateName, label = "Nama Bisnis", enabled = isStatusVerified, isLocked = !isStatusVerified)
                StartupInput(value = wa, onValueChange = onUpdateWA, label = "Nomor WhatsApp")
                StartupInput(value = npwp, onValueChange = onUpdateNPWP, label = "NPWP Bisnis")
                StartupInput(value = caption, onValueChange = onUpdateCaption, label = "Template Caption WA", singleLine = false)
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(12.dp)) { Text("SIMPAN PERUBAHAN", fontWeight = FontWeight.Bold) }
            }
        }
    }
}



@Composable
fun StartupInput(value: String, onValueChange: (String) -> Unit, label: String, enabled: Boolean = true, isLocked: Boolean = false, singleLine: Boolean = true) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AppColors.TextSecondary)
            if (isLocked) { Spacer(Modifier.width(4.dp)); Icon(Icons.Default.Lock, null, modifier = Modifier.size(10.dp), tint = AppColors.Accent) }
        }
        OutlinedTextField(
            value = value, onValueChange = onValueChange, modifier = Modifier.fillMaxWidth().padding(top = 4.dp), enabled = enabled,
            shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = AppColors.Surface, focusedContainerColor = AppColors.Surface),
            singleLine = singleLine, minLines = if (singleLine) 1 else 3
        )
    }
}

fun formatCurrency(amount: Double): String {
    return try {
        val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        format.format(amount).replace("Rp", "Rp ")
    } catch (e: Exception) { "Rp $amount" }
}

fun formatPrice(input: String): String {
    if (input.isEmpty()) return ""
    return try { NumberFormat.getNumberInstance(Locale("id", "ID")).format(input.toLong()) } catch (e: Exception) { input }
}

fun borderStroke() = androidx.compose.foundation.BorderStroke(1.dp, AppColors.Border)

fun saveImageToGallery(context: Context, bitmap: Bitmap, no: String): Uri? {
    val filename = "Invoice_${no.replace("#", "")}_${System.currentTimeMillis()}.jpg"
    val resolver = context.contentResolver
    
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/PhotographyInvoices")
        }
        val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        imageUri?.let { uri ->
            try {
                resolver.openOutputStream(uri)?.use { output ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, output)
                }
            } catch (e: Exception) {
                android.util.Log.e("SaveToGallery", "Failed to save: ${e.message}")
            }
        }
        imageUri
    } else {
        val picturesFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val appFolder = File(picturesFolder, "PhotographyInvoices").apply { mkdirs() }
        val file = File(appFolder, filename)
        try {
            FileOutputStream(file).use { output ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, output)
            }
            val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            mediaScanIntent.data = Uri.fromFile(file)
            context.sendBroadcast(mediaScanIntent)
            Uri.fromFile(file)
        } catch (e: Exception) {
            null
        }
    }
}

fun copyImageToClipboard(context: Context, uri: Uri) {
    try {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newUri(context.contentResolver, "Invoice Image", uri)
        clipboard.setPrimaryClip(clip)
    } catch (e: Exception) {
        android.util.Log.e("Clipboard", "Failed to copy image: ${e.message}")
    }
}

fun normalizeWhatsappNumber(raw: String): String {
    val digits = raw.filter { it.isDigit() }
    if (digits.isBlank()) return ""
    return when {
        digits.startsWith("62") -> digits
        digits.startsWith("0") -> "62" + digits.drop(1)
        digits.startsWith("8") -> "62$digits"
        else -> digits
    }
}

fun executeSendInvoice(
    context: Context, 
    layer: GraphicsLayer, 
    scope: CoroutineScope, 
    name: String, 
    wa: String, 
    no: String, 
    captionTemplate: String, 
    paymentMethod: String
) {
    scope.launch {
        try {
            val bitmap = layer.toImageBitmap().asAndroidBitmap()
            val imagesFolder = File(context.cacheDir, "images").apply { mkdirs() }
            val file = File(imagesFolder, "invoice_${no.replace("#", "")}.jpg")
            FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it) }
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

            val finalCaption = buildString {
                append(captionTemplate.replace("[name]", name))
                append("\nMetode pembayaran: ")
                append(paymentMethod)
            }
            
            val pm = context.packageManager
            val isWhatsappInstalled = isPackageInstalled("com.whatsapp", pm)
            val isWhatsappBusinessInstalled = isPackageInstalled("com.whatsapp.w4b", pm)
            
            // Salin gambar invoice ke clipboard agar bisa di-paste di WhatsApp
            copyImageToClipboard(context, uri)
            
            val phone = normalizeWhatsappNumber(wa)

            if (phone.isNotBlank()) {
                Toast.makeText(
                    context,
                    "Membuka chat WhatsApp...\nPaste gambar invoice dari clipboard.",
                    Toast.LENGTH_LONG
                ).show()

                // Buka langsung ke chat nomor tujuan dengan caption yang sudah tertulis
                val encodedCaption = java.net.URLEncoder.encode(finalCaption, "UTF-8")
                val openIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=$phone&text=$encodedCaption")).apply {
                    if (isWhatsappBusinessInstalled) {
                        setPackage("com.whatsapp.w4b")
                    } else if (isWhatsappInstalled) {
                        setPackage("com.whatsapp")
                    }
                }
                try {
                    context.startActivity(openIntent)
                } catch (_: Exception) {
                    try {
                        context.startActivity(Intent.createChooser(openIntent, "Buka WhatsApp"))
                    } catch (e: Exception) {
                        Toast.makeText(context, "Gagal membuka WhatsApp: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                // Tidak ada nomor WA: buka share chooser dengan gambar
                Toast.makeText(
                    context,
                    "Tidak ada nomor WA. Membuka share...",
                    Toast.LENGTH_SHORT
                ).show()

                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/jpeg"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_TEXT, finalCaption)
                    clipData = ClipData.newRawUri("Invoice Image", uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(intent, "Bagikan invoice"))
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Gagal: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}


fun isPackageInstalled(packageName: String, packageManager: android.content.pm.PackageManager): Boolean {
    return try {
        packageManager.getPackageInfo(packageName, 0)
        true
    } catch (e: Exception) {
        false
    }
}

@Composable
fun BannerAdView(adUnitId: String) {
    val context = LocalContext.current
    val sharedPref = remember { context.getSharedPreferences("invoice_prefs", Context.MODE_PRIVATE) }
    if (SecurityManager.isPremiumAccessAuthorized(sharedPref)) return
    
    // Memberikan area khusus di bagian atas agar tidak menumpuk dengan konten
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        AndroidView(
            modifier = Modifier.fillMaxWidth(),
            factory = { context ->
                AdView(context).apply {
                    setAdSize(AdSize.BANNER)
                    this.adUnitId = adUnitId
                    loadAd(AdRequest.Builder().build())
                    adListener = object : AdListener() {
                        override fun onAdFailedToLoad(error: LoadAdError) {
                            android.util.Log.e("BannerAd", "Gagal muat iklan: ${error.message}")
                            // Coba muat ulang otomatis setelah 15 detik
                            postDelayed({ loadAd(AdRequest.Builder().build()) }, 15000)
                        }
                        override fun onAdLoaded() {
                            android.util.Log.d("BannerAd", "Berhasil muat iklan")
                        }
                    }
                }
            }
        )
    }
}

@Composable
fun FGInvoiceTheme(content: @Composable () -> Unit) {
    val colorScheme = lightColorScheme(
        primary = AppColors.Primary,
        secondary = AppColors.Secondary,
        background = AppColors.Background,
        surface = AppColors.Surface
    )
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(
            bodyLarge = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                letterSpacing = 0.5.sp
            ),
            titleLarge = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Black,
                fontSize = 22.sp,
                letterSpacing = 1.sp
            )
        ),
        content = content
    )
}

@Preview(showBackground = true)
@Composable
fun InvoicePreview() {
    FGInvoiceTheme { 
        InvoiceScreen(onExit = {})
    }
}
