package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.data.local.ActionTriggerType
import com.example.data.local.ChatMessage
import com.example.data.local.MessageSender
import com.example.data.local.PersonalityMode
import com.example.ui.components.ArcReactorOrb
import com.example.ui.components.CyberActionChip
import com.example.ui.components.CyberBadge
import com.example.ui.components.HudAlertBanner
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatScreen(
    viewModel: MayaViewModel,
    onVoiceInputClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val messages by viewModel.messages.collectAsState()
    val mode by viewModel.personalityMode.collectAsState()
    val isThinking by viewModel.isThinking.collectAsState()
    val isVoiceMuted by viewModel.isVoiceMuted.collectAsState()
    val hudNotification by viewModel.hudNotification.collectAsState()

    var inputQuery by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Auto-scroll when new message arrives
    LaunchedEffect(messages.size, isThinking) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBackground)
    ) {
        // System Telemetry HUD Bar
        TelemetryHeaderStrip()

        // HUD Alert Banner
        HudAlertBanner(message = hudNotification)

        // Top Control Header: Maya Avatar & Personality controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Mode switch button
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(CyberSurfaceVariant)
                    .border(1.dp, (if (mode == PersonalityMode.JARVIS) NeonCyan else NeonMagenta).copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                    .clickable { viewModel.togglePersonalityMode() }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.SwapHoriz,
                    contentDescription = "Switch Protocol",
                    tint = if (mode == PersonalityMode.JARVIS) NeonCyan else NeonMagenta,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = if (mode == PersonalityMode.JARVIS) "JARVIS MODE" else "COMPANION",
                        color = TextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "Tap to Switch",
                        color = TextSecondary,
                        fontSize = 9.sp
                    )
                }
            }

            // Central Arc Reactor Visualizer
            ArcReactorOrb(
                mode = mode,
                isThinking = isThinking,
                onClick = { viewModel.togglePersonalityMode() }
            )

            // Right icons: TTS toggle & Clear chat
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { viewModel.toggleVoiceMute() },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(CyberSurfaceVariant)
                ) {
                    Icon(
                        imageVector = if (isVoiceMuted) Icons.AutoMirrored.Filled.VolumeMute else Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "Toggle Audio Voice",
                        tint = if (isVoiceMuted) TextSecondary else NeonCyan,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(
                    onClick = { viewModel.clearChat() },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(CyberSurfaceVariant)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = "Purge Chat",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Quick Action Command Chips Carousel
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 14.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CyberActionChip("🚀 Open YouTube", NeonCyan) {
                viewModel.executeQuickAction("Open YouTube")
            }
            CyberActionChip("📸 Open Instagram", NeonMagenta) {
                viewModel.executeQuickAction("Open Instagram")
            }
            CyberActionChip("🎬 Viral Reel Script", NeonPurple) {
                viewModel.executeQuickAction("Reel Script")
            }
            CyberActionChip("💡 5 Viral Content Ideas", CyberAmber) {
                viewModel.executeQuickAction("Give me 5 viral video ideas for tech creators")
            }
            CyberActionChip("⚡ System Status", MatrixGreen) {
                viewModel.executeQuickAction("Diagnostics")
            }
            CyberActionChip("📝 Add Task for Boss", NeonCyan) {
                viewModel.executeQuickAction("Task")
            }
            CyberActionChip("🔍 Search Google", NeonMagenta) {
                viewModel.executeQuickAction("Search")
            }
        }

        // Messages Stream
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(vertical = 10.dp)
        ) {
            items(messages, key = { it.id }) { message ->
                MessageBubble(
                    message = message,
                    onActionClick = { action, payload ->
                        when (action) {
                            ActionTriggerType.OPEN_YOUTUBE -> viewModel.commandDispatcher.openYouTube(payload)
                            ActionTriggerType.OPEN_INSTAGRAM -> viewModel.commandDispatcher.openInstagram(payload)
                            ActionTriggerType.SEARCH_WEB -> viewModel.commandDispatcher.searchWeb(payload)
                            else -> {}
                        }
                    },
                    onCopyClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Maya Response", message.text))
                        Toast.makeText(context, "Copied to clipboard, Boss!", Toast.LENGTH_SHORT).show()
                    },
                    onSpeakClick = {
                        viewModel.voiceManager.speak(message.text, message.mode)
                    }
                )
            }

            if (isThinking) {
                item {
                    MayaThinkingIndicator(mode = mode)
                }
            }
        }

        // Bottom Input Console
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(CyberSurface)
                .border(width = 1.dp, color = NeonCyan.copy(alpha = 0.35f))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Speech Input Mic Button
                IconButton(
                    onClick = onVoiceInputClick,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(NeonMagenta.copy(alpha = 0.25f), NeonCyan.copy(alpha = 0.25f))
                            )
                        )
                        .border(1.dp, NeonCyan.copy(alpha = 0.7f), CircleShape)
                        .testTag("voice_input_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Voice Speech Recognition",
                        tint = NeonCyan,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Input Text Field
                OutlinedTextField(
                    value = inputQuery,
                    onValueChange = { inputQuery = it },
                    placeholder = {
                        Text(
                            text = if (mode == PersonalityMode.JARVIS) "Command Maya (e.g. Open YouTube, Script...)" else "Baat karo Maya se (Hinglish/English)...",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_input_field"),
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = CyberSurfaceHighlight,
                        unfocusedContainerColor = CyberSurfaceVariant,
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = Color(0xFF263859),
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    maxLines = 4
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Send Button
                IconButton(
                    onClick = {
                        if (inputQuery.isNotBlank()) {
                            val msg = inputQuery
                            inputQuery = ""
                            viewModel.sendMessage(msg)
                        }
                    },
                    enabled = inputQuery.isNotBlank() && !isThinking,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            if (inputQuery.isNotBlank()) {
                                Brush.linearGradient(listOf(NeonCyan, NeonPurple))
                            } else {
                                Brush.linearGradient(listOf(Color(0xFF23324E), Color(0xFF1B263C)))
                            }
                        )
                        .testTag("send_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send Command",
                        tint = if (inputQuery.isNotBlank()) Color.White else TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun MessageBubble(
    message: ChatMessage,
    onActionClick: (ActionTriggerType, String) -> Unit,
    onCopyClick: () -> Unit,
    onSpeakClick: () -> Unit
) {
    val isUser = message.sender == MessageSender.USER
    val timeStr = remember(message.timestamp) {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp))
    }

    val bubbleBorder = if (isUser) {
        NeonCyan.copy(alpha = 0.5f)
    } else if (message.mode == PersonalityMode.JARVIS) {
        NeonPurple.copy(alpha = 0.6f)
    } else {
        NeonMagenta.copy(alpha = 0.6f)
    }

    val bubbleBackground = if (isUser) {
        CyberSurfaceHighlight
    } else {
        CyberSurfaceVariant
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        // Sender Badge & Time
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 3.dp)
        ) {
            if (isUser) {
                Text(text = timeStr, color = TextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                Spacer(modifier = Modifier.width(6.dp))
                CyberBadge("BOSS", NeonCyan)
            } else {
                CyberBadge(
                    text = if (message.mode == PersonalityMode.JARVIS) "MAYA [JARVIS]" else "MAYA [COMPANION]",
                    color = if (message.mode == PersonalityMode.JARVIS) NeonCyan else NeonMagenta
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = timeStr, color = TextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
            }
        }

        // Bubble Box
        Box(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        topStart = 14.dp,
                        topEnd = 14.dp,
                        bottomStart = if (isUser) 14.dp else 2.dp,
                        bottomEnd = if (isUser) 2.dp else 14.dp
                    )
                )
                .background(bubbleBackground)
                .border(
                    width = 1.dp,
                    color = bubbleBorder,
                    shape = RoundedCornerShape(
                        topStart = 14.dp,
                        topEnd = 14.dp,
                        bottomStart = if (isUser) 14.dp else 2.dp,
                        bottomEnd = if (isUser) 2.dp else 14.dp
                    )
                )
                .padding(12.dp)
                .animateContentSize()
        ) {
            Column {
                Text(
                    text = message.text,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )

                // Render Action Trigger Card if an action was executed by Maya
                if (!isUser && message.actionType != ActionTriggerType.NONE) {
                    Spacer(modifier = Modifier.height(10.dp))
                    ExecutedActionCard(
                        actionType = message.actionType,
                        payload = message.actionPayload,
                        onClick = { onActionClick(message.actionType, message.actionPayload) }
                    )
                }

                // Action Bar below Maya's message (Copy, Speak)
                if (!isUser) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onSpeakClick,
                            modifier = Modifier.size(26.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Listen to Maya",
                                tint = TextSecondary,
                                modifier = Modifier.size(15.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        IconButton(
                            onClick = onCopyClick,
                            modifier = Modifier.size(26.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy message",
                                tint = TextSecondary,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExecutedActionCard(
    actionType: ActionTriggerType,
    payload: String,
    onClick: () -> Unit
) {
    val (label, icon, color) = when (actionType) {
        ActionTriggerType.OPEN_YOUTUBE -> Triple("YouTube Launched $payload", Icons.Default.PlayArrow, CyberAmber)
        ActionTriggerType.OPEN_INSTAGRAM -> Triple("Instagram Feed Launched", Icons.AutoMirrored.Filled.OpenInNew, NeonMagenta)
        ActionTriggerType.SEARCH_WEB -> Triple("Web Intelligence Queried", Icons.Default.AutoAwesome, NeonCyan)
        ActionTriggerType.ADD_TASK -> Triple("Task Registered into Matrix", Icons.Default.CheckCircle, MatrixGreen)
        ActionTriggerType.CREATE_SCRIPT -> Triple("Content Script Generated & Saved", Icons.Default.AutoAwesome, NeonPurple)
        ActionTriggerType.SYSTEM_DIAGNOSTICS -> Triple("Diagnostics Telemetry Refreshed", Icons.Default.CheckCircle, MatrixGreen)
        ActionTriggerType.NONE -> Triple("", Icons.Default.CheckCircle, MatrixGreen)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.12f))
            .border(1.dp, color.copy(alpha = 0.45f), RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            color = color,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
            contentDescription = "Open",
            tint = color.copy(alpha = 0.7f),
            modifier = Modifier.size(14.dp)
        )
    }
}

@Composable
fun MayaThinkingIndicator(mode: PersonalityMode) {
    val accent = if (mode == PersonalityMode.JARVIS) NeonCyan else NeonMagenta

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(CyberSurfaceVariant)
            .border(1.dp, accent.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(16.dp),
            color = accent,
            strokeWidth = 2.dp
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = if (mode == PersonalityMode.JARVIS) "Neural Core computing tactical response..." else "Maya soch rahi hai Boss, bas 1 second...",
            color = TextSecondary,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}
