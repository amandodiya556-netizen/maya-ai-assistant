package com.example.automation

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.provider.MediaStore
import android.provider.Settings
import android.widget.Toast

data class SystemTelemetry(
    val batteryPercentage: Int,
    val isCharging: Boolean,
    val networkStatus: String,
    val freeStorageGb: String,
    val totalStorageGb: String,
    val osVersion: String,
    val deviceModel: String,
    val neuralSyncRate: String = "99.8%"
)

class CommandDispatcher(private val context: Context) {

    fun openYouTube(query: String? = null): Boolean {
        return try {
            val intent = if (!query.isNullOrBlank()) {
                val encodedQuery = Uri.encode(query)
                Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/results?search_query=$encodedQuery")).apply {
                    setPackage("com.google.android.youtube")
                }
            } else {
                val launchIntent = context.packageManager.getLaunchIntentForPackage("com.google.android.youtube")
                launchIntent ?: Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com"))
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            // Fallback to browser
            try {
                val url = if (!query.isNullOrBlank()) {
                    "https://www.youtube.com/results?search_query=${Uri.encode(query)}"
                } else {
                    "https://www.youtube.com"
                }
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(browserIntent)
                true
            } catch (fallbackEx: Exception) {
                Toast.makeText(context, "Could not launch YouTube: ${fallbackEx.message}", Toast.LENGTH_SHORT).show()
                false
            }
        }
    }

    fun openInstagram(profileOrQuery: String? = null): Boolean {
        return try {
            val url = if (!profileOrQuery.isNullOrBlank()) {
                val clean = profileOrQuery.trim().removePrefix("@")
                "https://www.instagram.com/$clean"
            } else {
                "https://www.instagram.com"
            }
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                setPackage("com.instagram.android")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            // Fallback to browser
            try {
                val url = "https://www.instagram.com"
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(browserIntent)
                true
            } catch (fallbackEx: Exception) {
                Toast.makeText(context, "Could not launch Instagram: ${fallbackEx.message}", Toast.LENGTH_SHORT).show()
                false
            }
        }
    }

    fun searchWeb(query: String): Boolean {
        if (query.isBlank()) return false
        return try {
            val intent = Intent(Intent.ACTION_WEB_SEARCH).apply {
                putExtra(SearchManager.QUERY, query)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            try {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=${Uri.encode(query)}")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(browserIntent)
                true
            } catch (ex: Exception) {
                Toast.makeText(context, "Unable to execute web search", Toast.LENGTH_SHORT).show()
                false
            }
        }
    }

    fun openSettings(): Boolean {
        return try {
            val intent = Intent(Settings.ACTION_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun openCamera(): Boolean {
        return try {
            val intent = Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun shareContent(title: String, text: String): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, title)
                putExtra(Intent.EXTRA_TEXT, text)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val chooser = Intent.createChooser(intent, "Share via Maya").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun getSystemTelemetry(): SystemTelemetry {
        // Battery status
        var batteryPct = 85
        var isCharging = false
        try {
            val ifilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            val batteryStatus: Intent? = context.registerReceiver(null, ifilter)
            val level: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            if (level >= 0 && scale > 0) {
                batteryPct = ((level / scale.toFloat()) * 100).toInt()
            }
            val status: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
            isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL
        } catch (e: Exception) {
            // fallback
        }

        // Network status
        var netStatus = "Offline"
        try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            if (cm != null) {
                val activeNet = cm.activeNetwork
                val caps = cm.getNetworkCapabilities(activeNet)
                netStatus = when {
                    caps == null -> "Offline"
                    caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "Wi-Fi (High Speed)"
                    caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Cellular 5G/4G"
                    else -> "Connected"
                }
            }
        } catch (e: Exception) {
            netStatus = "Online"
        }

        // Storage status
        var freeStorageStr = "128.0"
        var totalStorageStr = "256.0"
        try {
            val path = Environment.getDataDirectory()
            val stat = StatFs(path.path)
            val blockSize = stat.blockSizeLong
            val availableBlocks = stat.availableBlocksLong
            val totalBlocks = stat.blockCountLong
            val freeGb = (availableBlocks * blockSize) / (1024.0 * 1024.0 * 1024.0)
            val totalGb = (totalBlocks * blockSize) / (1024.0 * 1024.0 * 1024.0)
            freeStorageStr = "%.1f".format(freeGb)
            totalStorageStr = "%.1f".format(totalGb)
        } catch (e: Exception) {
            // fallback
        }

        return SystemTelemetry(
            batteryPercentage = batteryPct,
            isCharging = isCharging,
            networkStatus = netStatus,
            freeStorageGb = freeStorageStr,
            totalStorageGb = totalStorageStr,
            osVersion = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
            deviceModel = "${Build.MANUFACTURER.replaceFirstChar { it.uppercase() }} ${Build.MODEL}",
            neuralSyncRate = "99.8%"
        )
    }
}
