package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.PersonalityMode
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.TextPrimary

@Composable
fun ArcReactorOrb(
    mode: PersonalityMode,
    isThinking: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "arc_reactor")

    // Rotation for outer tech ring
    val outerRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isThinking) 2500 else 9000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "outer_ring"
    )

    // Reverse rotation for inner ring
    val innerRotation by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isThinking) 3000 else 12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "inner_ring"
    )

    // Pulse scale for core breathing
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = if (isThinking) 1.15f else 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isThinking) 600 else 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val primaryAccent = if (mode == PersonalityMode.JARVIS) NeonCyan else NeonMagenta
    val secondaryAccent = if (mode == PersonalityMode.JARVIS) NeonPurple else CyberAmber

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(110.dp)
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            // Glowing aura backdrop
            Box(
                modifier = Modifier
                    .size(105.dp)
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                primaryAccent.copy(alpha = if (isThinking) 0.5f else 0.25f),
                                secondaryAccent.copy(alpha = 0.1f),
                                Color.Transparent
                            )
                        )
                    )
            )

            // Outer segmented ring
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .rotate(outerRotation)
                    .clip(CircleShape)
                    .border(
                        width = 2.dp,
                        brush = Brush.sweepGradient(
                            listOf(
                                primaryAccent,
                                Color.Transparent,
                                secondaryAccent,
                                Color.Transparent,
                                primaryAccent
                            )
                        ),
                        shape = CircleShape
                    )
            )

            // Inner holographic ring
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .rotate(innerRotation)
                    .clip(CircleShape)
                    .border(
                        width = 1.5.dp,
                        brush = Brush.sweepGradient(
                            listOf(
                                secondaryAccent,
                                Color.Transparent,
                                primaryAccent,
                                Color.Transparent,
                                secondaryAccent
                            )
                        ),
                        shape = CircleShape
                    )
            )

            // Maya's holographic portrait / core avatar
            Box(
                modifier = Modifier
                    .size(66.dp)
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .border(1.5.dp, primaryAccent, CircleShape)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_maya_avatar),
                    contentDescription = "Maya Neural AI Avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(66.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Futuristic mode pill
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(primaryAccent.copy(alpha = 0.15f))
                .border(1.dp, primaryAccent.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                .clickable { onClick() }
                .padding(horizontal = 10.dp, vertical = 3.dp)
        ) {
            Text(
                text = if (isThinking) "⚡ PROCESSING BOSS QUERY..." else if (mode == PersonalityMode.JARVIS) "⚡ JARVIS PROTOCOL" else "💖 COMPANION MODE",
                color = if (isThinking) CyberAmber else TextPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )
        }
    }
}
