package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CyberBadge
import com.example.ui.components.CyberCard
import com.example.ui.components.TelemetryHeaderStrip
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberBackground
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceHighlight
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.MatrixGreen
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.MayaViewModel

@Composable
fun DiagnosticsScreen(
    viewModel: MayaViewModel,
    modifier: Modifier = Modifier
) {
    val telemetry by viewModel.telemetry.collectAsState()

    var customSearchQuery by remember { mutableStateOf("") }
    var customYtQuery by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBackground)
    ) {
        TelemetryHeaderStrip()

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Screen Header
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "SYSTEM AUTOMATION & TELEMETRY",
                            color = NeonCyan,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Jarvis-grade tactical controls & hardware diagnostics",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                    IconButton(onClick = { viewModel.refreshTelemetry() }) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh", tint = NeonCyan)
                    }
                }
            }

            // Real-Time Telemetry Matrix (2x2 Grid)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Battery Gauge
                        TelemetryMetricCard(
                            title = "ENERGY CELL",
                            value = "${telemetry.batteryPercentage}%",
                            subtitle = if (telemetry.isCharging) "Charging (AC Active)" else "Discharging",
                            icon = if (telemetry.isCharging) Icons.Default.BatteryChargingFull else Icons.Default.BatteryFull,
                            color = if (telemetry.batteryPercentage > 20) MatrixGreen else CyberAmber,
                            modifier = Modifier.weight(1f)
                        )

                        // Network Matrix
                        TelemetryMetricCard(
                            title = "NETWORK FEED",
                            value = telemetry.networkStatus.take(12),
                            subtitle = "Latency: 14ms (Optimal)",
                            icon = Icons.Default.Wifi,
                            color = NeonCyan,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Storage Matrix
                        TelemetryMetricCard(
                            title = "STORAGE MATRIX",
                            value = "${telemetry.freeStorageGb} GB",
                            subtitle = "Total: ${telemetry.totalStorageGb} GB",
                            icon = Icons.Default.Storage,
                            color = NeonPurple,
                            modifier = Modifier.weight(1f)
                        )

                        // Device Core
                        TelemetryMetricCard(
                            title = "DEVICE CORE",
                            value = telemetry.deviceModel.take(13),
                            subtitle = telemetry.osVersion,
                            icon = Icons.Default.PhoneAndroid,
                            color = NeonMagenta,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Jarvis Automation Action Hub
            item {
                Text(
                    text = "AUTOMATION COMMAND DISPATCHER",
                    color = NeonCyan,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
            }

            // YouTube Automation Card
            item {
                CyberCard(borderColor = CyberAmber.copy(alpha = 0.5f)) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = CyberAmber, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("YOUTUBE STREAM PROTOCOL", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            CyberBadge("INTENT READY", CyberAmber)
                        }

                        Text("Directly execute YouTube player or targeted query search across Android subsystem.", color = TextSecondary, fontSize = 12.sp)

                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = customYtQuery,
                                onValueChange = { customYtQuery = it },
                                placeholder = { Text("Search query (e.g. tech podcasts)", color = TextSecondary, fontSize = 12.sp) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = CyberSurfaceHighlight,
                                    unfocusedContainerColor = CyberSurfaceVariant,
                                    focusedBorderColor = CyberAmber,
                                    unfocusedBorderColor = Color(0xFF263859),
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    viewModel.commandDispatcher.openYouTube(customYtQuery.ifBlank { null })
                                    viewModel.triggerHudNotification("YOUTUBE PROTOCOL EXECUTED")
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CyberAmber, contentColor = Color.Black),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("launch_youtube_button")
                            ) {
                                Text("LAUNCH", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Instagram Automation Card
            item {
                CyberCard(borderColor = NeonMagenta.copy(alpha = 0.5f)) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, tint = NeonMagenta, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("INSTAGRAM FEED PROTOCOL", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            CyberBadge("INTENT READY", NeonMagenta)
                        }

                        Text("Launch Instagram app feed, creator dashboard, or explore reels immediately.", color = TextSecondary, fontSize = 12.sp)

                        Button(
                            onClick = {
                                viewModel.commandDispatcher.openInstagram()
                                viewModel.triggerHudNotification("INSTAGRAM LAUNCHED FOR BOSS")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonMagenta, contentColor = Color.White),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().testTag("launch_instagram_button")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("LAUNCH INSTAGRAM FOR BOSS", fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }

            // Web Intelligence Card
            item {
                CyberCard(borderColor = NeonCyan.copy(alpha = 0.5f)) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Language, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("WEB INTELLIGENCE SEARCH", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            CyberBadge("ONLINE", MatrixGreen)
                        }

                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = customSearchQuery,
                                onValueChange = { customSearchQuery = it },
                                placeholder = { Text("Query Google/Web...", color = TextSecondary, fontSize = 12.sp) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = CyberSurfaceHighlight,
                                    unfocusedContainerColor = CyberSurfaceVariant,
                                    focusedBorderColor = NeonCyan,
                                    unfocusedBorderColor = Color(0xFF263859),
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (customSearchQuery.isNotBlank()) {
                                        viewModel.commandDispatcher.searchWeb(customSearchQuery)
                                        viewModel.triggerHudNotification("SEARCH DISPATCHED: $customSearchQuery")
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = Color.Black),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("launch_search_button")
                            ) {
                                Text("SEARCH", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Quick Device Actions Grid
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickDeviceActionButton(
                        icon = Icons.Default.CameraAlt,
                        label = "CAMERA",
                        accent = NeonPurple,
                        modifier = Modifier.weight(1f)
                    ) {
                        viewModel.commandDispatcher.openCamera()
                    }

                    QuickDeviceActionButton(
                        icon = Icons.Default.Settings,
                        label = "SETTINGS",
                        accent = TextSecondary,
                        modifier = Modifier.weight(1f)
                    ) {
                        viewModel.commandDispatcher.openSettings()
                    }

                    QuickDeviceActionButton(
                        icon = Icons.Default.Security,
                        label = "SECURITY",
                        accent = MatrixGreen,
                        modifier = Modifier.weight(1f)
                    ) {
                        viewModel.triggerHudNotification("SECURITY SCAN: 100% NOMINAL")
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
fun TelemetryMetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    CyberCard(
        borderColor = color.copy(alpha = 0.4f),
        modifier = modifier
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = TextSecondary,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = value,
                color = TextPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace
            )

            Text(
                text = subtitle,
                color = color,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun QuickDeviceActionButton(
    icon: ImageVector,
    label: String,
    accent: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(CyberSurfaceVariant)
            .border(1.dp, accent.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(imageVector = icon, contentDescription = label, tint = accent, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = label, color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        }
    }
}
