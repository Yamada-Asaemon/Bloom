package com.bloom.app

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.BasicText

// ===== Цвета темы =====

data class BloomColors(
    val bg: Color,
    val panel: Color,
    val text: Color,
    val muted: Color,
    val accent: Color
)

val DarkColors = BloomColors(
    bg = Color(0xFF16161D),
    panel = Color(0xFF1E1E28),
    text = Color.White,
    muted = Color.White.copy(alpha = 0.5f),
    accent = Color(0xFF6A1FB0)
)

val LightColors = BloomColors(
    bg = Color(0xFFF2F2F7),
    panel = Color.White,
    text = Color(0xFF16161D),
    muted = Color.Black.copy(alpha = 0.5f),
    accent = Color(0xFF6A1FB0)
)

val LocalBloom = staticCompositionLocalOf { DarkColors }

// ===== Утилиты =====

fun Modifier.noRipple(onClick: () -> Unit): Modifier = composed {
    clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = onClick
    )
}

/** Весь текст в приложении — в нижнем регистре, как в HTML (text-transform: lowercase). */
@Composable
fun BText(
    text: String,
    modifier: Modifier = Modifier,
    size: Int = 13,
    color: Color = LocalBloom.current.text,
    weight: FontWeight = FontWeight.Normal,
    spacing: Float = 1.5f,
    align: TextAlign = TextAlign.Start
) {
    BasicText(
        text = text.lowercase(),
        modifier = modifier,
        style = TextStyle(
            color = color,
            fontSize = size.sp,
            fontWeight = weight,
            letterSpacing = spacing.sp,
            textAlign = align,
            fontFeatureSettings = "smcp"
        )
    )
}

@Composable
fun Ico(res: Int, tint: Color, size: Int, modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(res),
        contentDescription = null,
        modifier = modifier.size(size.dp),
        colorFilter = ColorFilter.tint(tint)
    )
}

@Composable
fun BloomLine(modifier: Modifier = Modifier) {
    val c = LocalBloom.current
    Box(
        modifier
            .height(5.dp)
            .clip(RoundedCornerShape(5.dp))
            .background(c.text.copy(alpha = 0.5f))
    )
}

@Composable
fun BloomButton(
    text: String,
    modifier: Modifier = Modifier,
    radius: Int = 14,
    vertical: Int = 16,
    horizontal: Int = 24,
    onClick: () -> Unit
) {
    val c = LocalBloom.current
    Box(
        modifier
            .clip(RoundedCornerShape(radius.dp))
            .background(c.accent)
            .noRipple(onClick)
            .padding(horizontal = horizontal.dp, vertical = vertical.dp),
        contentAlignment = Alignment.Center
    ) {
        BText(text, color = Color.White, align = TextAlign.Center)
    }
}

@Composable
fun Scrim(
    visible: Boolean,
    alpha: Float,
    modifier: Modifier = Modifier.fillMaxSize(),
    onClick: () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = fadeIn(tween(300)),
        exit = fadeOut(tween(300))
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = alpha))
                .noRipple(onClick)
        )
    }
}

@Composable
fun ThemeSwitch(on: Boolean, onClick: () -> Unit) {
    val c = LocalBloom.current
    val x by animateDpAsState(if (on) 25.dp else 3.dp, tween(250), label = "knob")
    Box(
        Modifier
            .width(52.dp)
            .height(30.dp)
            .clip(RoundedCornerShape(15.dp))
            .background(if (on) c.accent else Color(0xFF888888))
            .noRipple(onClick)
    ) {
        Box(
            Modifier
                .offset(x = x, y = 3.dp)
                .size(24.dp)
                .clip(CircleShape)
                .background(Color.White)
        )
    }
}
