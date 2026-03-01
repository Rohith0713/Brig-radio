package com.campuswave.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import kotlin.random.Random

@Composable
fun WaveformVisualizer(
    modifier: Modifier = Modifier,
    isSpeaking: Boolean,
    volumeLevel: Int, // 0-255
    color: Color
) {
    val barCount = 7
    
    // Create animations for each bar with different durations for variety
    val barAnimations = remember {
        (0 until barCount).map { index ->
            Animatable(0.3f)
        }
    }
    
    // Animate each bar independently
    barAnimations.forEachIndexed { index, anim ->
        val targetValue = if (isSpeaking && volumeLevel > 10) {
            // Real audio: scale by volume
            0.3f + (volumeLevel / 255f) * 0.7f * (0.5f + Random.nextFloat() * 0.5f)
        } else {
            // Idle state: gentle pulse animation
            0.2f + (index % 3) * 0.1f
        }
        
        androidx.compose.runtime.LaunchedEffect(isSpeaking, volumeLevel, index) {
            while (true) {
                val animTarget = if (isSpeaking && volumeLevel > 10) {
                    0.3f + (volumeLevel / 255f) * 0.7f * Random.nextFloat()
                } else {
                    0.15f + Random.nextFloat() * 0.15f
                }
                anim.animateTo(
                    targetValue = animTarget,
                    animationSpec = tween(
                        durationMillis = if (isSpeaking) 100 + Random.nextInt(100) else 300 + Random.nextInt(400),
                        easing = FastOutSlowInEasing
                    )
                )
            }
        }
    }

    Canvas(modifier = modifier) {
        val barWidth = size.width / (barCount * 1.5f)
        val spacing = barWidth * 0.5f

        barAnimations.forEachIndexed { index, anim ->
            val heightRatio = anim.value.coerceIn(0.1f, 1f)
            val barHeight = size.height * heightRatio
            
            drawLine(
                color = color,
                start = Offset(x = index * (barWidth + spacing) + barWidth / 2, y = size.height / 2 - barHeight / 2),
                end = Offset(x = index * (barWidth + spacing) + barWidth / 2, y = size.height / 2 + barHeight / 2),
                strokeWidth = barWidth,
                cap = StrokeCap.Round
            )
        }
    }
}
