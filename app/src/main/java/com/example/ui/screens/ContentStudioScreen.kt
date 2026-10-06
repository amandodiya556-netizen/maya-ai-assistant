package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ContentScript
import com.example.data.local.ScriptPlatform
import com.example.ui.components.CyberBadge
import com.example.ui.components.CyberCard
import com.example.ui.components.TelemetryHeaderStrip
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberBackground
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceHighlight
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.MayaViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ContentStudioScreen(
    viewModel: MayaViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scripts by viewModel.scripts.collectAsState()
    val isThinking by viewModel.isThinking.collectAsState()

    var topicInput by remember { mutableStateOf("") }
    var selectedPlatform by remember { mutableStateOf(ScriptPlatform.INSTAGRAM_REEL) }

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
            // Header
            item {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "CONTENT STUDIO",
                            color = NeonMagenta,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "High-retention scripts, viral hooks & Hinglish pacing",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                    CyberBadge("${scripts.size} SCRIPTS", NeonPurple)
                }
            }

            // Quick Generator Card
            item {
                CyberCard(borderColor = NeonMagenta.copy(alpha = 0.5f)) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "AI",
                                tint = NeonMagenta,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "SCRIPT & HOOK GENERATOR",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        // Preset Topic Ideas Chips
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            PresetChip("🔥 3 AI Websites to 10x Productivity") {
                                topicInput = "3 Secret AI Websites to 10x Daily Productivity"
                                selectedPlatform = ScriptPlatform.INSTAGRAM_REEL
                            }
                            PresetChip("📱 iPhone vs Android Hinglish Review") {
                                topicInput = "iPhone 16 Pro vs S24 Ultra Camera Blind Test"
                                selectedPlatform = ScriptPlatform.TECH_REVIEW
                            }
                            PresetChip("🎬 5 Psychological Hooks for Reels") {
                                topicInput = "5 Psychological Viral Hooks that stop scrolling immediately"
                                selectedPlatform = ScriptPlatform.VIRAL_HOOK
                            }
                            PresetChip("💡 YouTube Long: How to Build an AI App") {
                                topicInput = "Complete Roadmap: How to Build and Launch an Android AI App"
                                selectedPlatform = ScriptPlatform.YOUTUBE_LONG
                            }
                        }

                        // Topic TextField
                        OutlinedTextField(
                            value = topicInput,
                            onValueChange = { topicInput = it },
                            placeholder = { Text("Enter video topic, niche, or idea...", color = TextSecondary) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("script_topic_input"),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = CyberSurfaceHighlight,
                                unfocusedContainerColor = CyberSurfaceVariant,
                                focusedBorderColor = NeonMagenta,
                                unfocusedBorderColor = Color(0xFF263859),
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            maxLines = 3
                        )

                        // Platform Selector
                        Text("Target Format:", color = TextSecondary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            ScriptPlatform.values().forEach { platform ->
                                val isSelected = platform == selectedPlatform
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) NeonMagenta.copy(alpha = 0.2f) else CyberSurfaceVariant)
                                        .border(1.dp, if (isSelected) NeonMagenta else Color(0xFF263859), RoundedCornerShape(8.dp))
                                        .clickable { selectedPlatform = platform }
                                        .padding(horizontal = 9.dp, vertical = 5.dp)
                                ) {
                                    Text(
                                        text = platform.displayName,
                                        color = if (isSelected) NeonMagenta else TextSecondary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Generate Button
                        Button(
                            onClick = {
                                if (topicInput.isNotBlank()) {
                                    viewModel.generateNewScript(topicInput, selectedPlatform)
                                    topicInput = ""
                                }
                            },
                            enabled = topicInput.isNotBlank() && !isThinking,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("generate_script_button"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NeonMagenta,
                                contentColor = Color.White
                            )
                        ) {
                            if (isThinking) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("SYNTHESIZING SCRIPT IN HINGLISH...", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            } else {
                                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("CRAFT SCRIPT WITH MAYA", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }
                }
            }

            // Saved Scripts section
            item {
                Text(
                    text = "VAULT / SAVED SCRIPTS",
                    color = NeonCyan,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }

            items(scripts, key = { it.id }) { script ->
                ScriptCard(
                    script = script,
                    onCopy = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText(script.title, "${script.title}\n\n${script.scriptBody}"))
                        Toast.makeText(context, "Full script copied, Boss!", Toast.LENGTH_SHORT).show()
                    },
                    onShare = {
                        viewModel.commandDispatcher.shareContent(script.title, script.scriptBody)
                    },
                    onDelete = {
                        viewModel.deleteScript(script)
                    },
                    onLaunchApp = {
                        if (script.platform == ScriptPlatform.INSTAGRAM_REEL) {
                            viewModel.commandDispatcher.openInstagram()
                        } else {
                            viewModel.commandDispatcher.openYouTube()
                        }
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
fun ScriptCard(
    script: ContentScript,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
    onLaunchApp: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    val timeStr = remember(script.createdAt) {
        SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(Date(script.createdAt))
    }

    val platformColor = when (script.platform) {
        ScriptPlatform.INSTAGRAM_REEL -> NeonMagenta
        ScriptPlatform.YOUTUBE_SHORTS, ScriptPlatform.YOUTUBE_LONG -> CyberAmber
        ScriptPlatform.TECH_REVIEW -> NeonCyan
        ScriptPlatform.VIRAL_HOOK -> NeonPurple
    }

    CyberCard(
        borderColor = platformColor.copy(alpha = 0.4f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CyberBadge(text = script.platform.displayName, color = platformColor)
                Text(text = timeStr, color = TextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
            }

            // Title
            Text(
                text = script.title,
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )

            // Body preview / full
            Text(
                text = if (isExpanded) script.scriptBody else script.scriptBody.take(160) + if (script.scriptBody.length > 160) "..." else "",
                color = TextSecondary,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )

            // Tags
            if (script.tags.isNotBlank()) {
                Text(
                    text = script.tags,
                    color = NeonCyan.copy(alpha = 0.8f),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Bottom Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Expand toggle
                Text(
                    text = if (isExpanded) "Show Less ▲" else "Read Full Script ▼",
                    color = platformColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { isExpanded = !isExpanded }
                )

                // Tool buttons
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onLaunchApp, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Test in App", tint = platformColor, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onCopy, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy Script", tint = TextSecondary, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onShare, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = "Share", tint = TextSecondary, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = TextSecondary.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun PresetChip(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(CyberSurfaceHighlight)
            .border(0.5.dp, NeonMagenta.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(text = label, color = TextPrimary, fontSize = 10.sp)
    }
}
