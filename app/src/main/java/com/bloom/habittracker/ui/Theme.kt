package com.bloom.habittracker.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.bloom.habittracker.R

object BloomColors {
    val Background = Color(0xFF121318)
    val OnBackground = Color(0xFFE2E2E9)
    val Primary = Color(0xFFB6C4FF)
    val OnPrimary = Color(0xFF1A285C)
    val SurfaceContainer = Color(0xFF1E2026)
    val SurfaceContainerHigh = Color(0xFF282A31)
    val OutlineVariant = Color(0x24C4C6D0)
    val OnSurfaceVariant = Color(0xFFC4C6D0)
    val Error = Color(0xFFF28B82)
}

val Montserrat = FontFamily(
    Font(R.font.montserrat_medium, FontWeight.Medium),
    Font(R.font.montserrat_semibold, FontWeight.SemiBold),
    Font(R.font.montserrat_bold, FontWeight.Bold)
)

@Composable
fun BloomTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = BloomColors.Primary,
            onPrimary = BloomColors.OnPrimary,
            background = BloomColors.Background,
            onBackground = BloomColors.OnBackground,
            surface = BloomColors.Background,
            onSurface = BloomColors.OnBackground,
            error = BloomColors.Error
        ),
        content = content
    )
}
