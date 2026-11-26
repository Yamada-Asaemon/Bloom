package com.bloom.habittracker.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

@Composable
fun ConfirmDeleteModal(
    onYes: () -> Unit,
    onNo: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(22.dp)
    Column(
        modifier = modifier
            .padding(horizontal = 28.dp)
            .widthIn(max = 340.dp)
            .fillMaxWidth()
            .clip(shape)
            .background(BloomColors.SurfaceContainerHigh)
            .border(1.dp, BloomColors.OutlineVariant, shape)
            .padding(horizontal = 22.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "ВЫ ДЕЙСТВИТЕЛЬНО ХОТИТЕ УДАЛИТЬ ПРИВЫЧКУ?",
            textAlign = TextAlign.Center,
            color = BloomColors.OnBackground,
            fontFamily = Montserrat,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.07.em,
            lineHeight = 18.6.sp
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 22.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            BloomButton(
                text = "ДА",
                onClick = onYes,
                containerColor = BloomColors.Error,
                contentColor = Color(0xFF1C1010),
                fontWeight = FontWeight.Bold,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                modifier = Modifier.weight(1f)
            )
            BloomButton(
                text = "НЕТ",
                onClick = onNo,
                containerColor = BloomColors.SurfaceContainer,
                contentColor = BloomColors.OnBackground,
                fontWeight = FontWeight.Bold,
                border = BorderStroke(1.dp, BloomColors.OutlineVariant),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                modifier = Modifier.weight(1f)
            )
        }
    }
}
