package com.campuswave.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow

@Composable
fun MarqueeComponent(
    message: String,
    backgroundColor: Color = Color(0xFF6366F1).copy(alpha = 0.1f),
    textColor: Color = Color(0xFF6366F1),
    velocity: Int = 100,
    fontWeight: FontWeight = FontWeight.Bold,
    fontStyle: FontStyle = FontStyle.Normal,
    fontSize: Float = 12f,
    gradientEndColor: Color? = null,
    textAlignment: TextAlign = TextAlign.Left
) {
    if (message.isBlank()) return

    val density = LocalDensity.current
    var containerWidth by remember { mutableStateOf(0) }
    var textWidth by remember { mutableStateOf(0) }
    
    val infiniteTransition = rememberInfiniteTransition(label = "marquee")
    
    val xOffset = if (textWidth > 0 && containerWidth > 0) {
        val totalDistance = textWidth + containerWidth
        val duration = (totalDistance * 1000 / velocity).coerceAtLeast(3000)
        
        infiniteTransition.animateFloat(
            initialValue = containerWidth.toFloat(),
            targetValue = -textWidth.toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(duration, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "xOffset"
        ).value
    } else {
        0f
    }

    val bgModifier = if (gradientEndColor != null) {
        Modifier.background(
            Brush.horizontalGradient(listOf(backgroundColor, gradientEndColor))
        )
    } else {
        Modifier.background(backgroundColor)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(36.dp)
            .then(bgModifier)
            .clipToBounds()
            .onGloballyPositioned { containerWidth = it.size.width },
        contentAlignment = if (textAlignment == TextAlign.Center) Alignment.Center else Alignment.CenterStart
    ) {
        Text(
            text = message,
            color = textColor,
            fontSize = fontSize.sp,
            fontWeight = fontWeight,
            fontStyle = fontStyle,
            maxLines = 1,
            overflow = TextOverflow.Visible,
            modifier = Modifier
                .offset(x = with(density) { xOffset.toDp() })
                .onGloballyPositioned { textWidth = it.size.width }
                .wrapContentWidth(unbounded = true)
        )
    }
}
