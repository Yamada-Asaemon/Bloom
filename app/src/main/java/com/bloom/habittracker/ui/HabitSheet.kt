package com.bloom.habittracker.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.bloom.habittracker.data.Severity

const val MAX_HABIT_CHARS = 25

@Composable
fun HabitSheet(
    onCreate: (title: String, severity: Severity?) -> Unit,
    modifier: Modifier = Modifier
) {
    var field by remember { mutableStateOf(TextFieldValue("")) }
    var exceeded by remember { mutableStateOf(false) }
    var severity by remember { mutableStateOf<Severity?>(null) }
    var focused by remember { mutableStateOf(false) }
    val sheetShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    val inputShape = RoundedCornerShape(10.dp)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .imePadding()
            .clip(sheetShape)
            .background(BloomColors.SurfaceContainerHigh)
            .border(1.dp, BloomColors.OutlineVariant, sheetShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {}
            )
            .heightIn(min = 348.dp)
            .navigationBarsPadding()
            .padding(start = 28.dp, end = 28.dp, top = 12.dp, bottom = 34.dp)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .size(width = 36.dp, height = 4.dp)
                .clip(CircleShape)
                .background(Color(0x59C4C6D0))
        )
        Spacer(Modifier.height(22.dp))

        Text(
            text = "НОВАЯ ПРИВЫЧКА",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            color = BloomColors.OnBackground,
            fontFamily = Montserrat,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.08.em
        )
        Spacer(Modifier.height(24.dp))

        FieldLabel("НАЗВАНИЕ ПРИВЫЧКИ")
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(inputShape)
                .background(BloomColors.SurfaceContainer)
                .border(
                    1.dp,
                    if (focused) BloomColors.Primary else BloomColors.OutlineVariant,
                    inputShape
                )
                .padding(start = 16.dp, end = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = field,
                onValueChange = { v ->
                    val upper = v.text.uppercase()
                    val limited = upper.take(MAX_HABIT_CHARS)
                    exceeded = if (upper.length > MAX_HABIT_CHARS) true
                    else exceeded && upper.length >= MAX_HABIT_CHARS
                    field = TextFieldValue(
                        text = limited,
                        selection = TextRange(
                            v.selection.start.coerceAtMost(limited.length),
                            v.selection.end.coerceAtMost(limited.length)
                        )
                    )
                },
                singleLine = true,
                textStyle = TextStyle(
                    color = BloomColors.OnBackground,
                    fontFamily = Montserrat,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.08.em
                ),
                cursorBrush = SolidColor(BloomColors.Primary),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 13.dp)
                    .onFocusChanged { focused = it.isFocused },
                decorationBox = { inner ->
                    Box {
                        if (field.text.isEmpty()) {
                            Text(
                                text = "...",
                                color = Color(0x6BC4C6D0),
                                fontFamily = Montserrat,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium,
                                letterSpacing = 0.16.em
                            )
                        }
                        inner()
                    }
                }
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = "${field.text.length}/$MAX_HABIT_CHARS",
                color = if (exceeded) BloomColors.Error else BloomColors.OnSurfaceVariant,
                fontFamily = Montserrat,
                fontSize = 10.sp,
                fontWeight = if (exceeded) FontWeight.SemiBold else FontWeight.Medium,
                letterSpacing = 0.06.em
            )
        }
        Spacer(Modifier.height(24.dp))

        FieldLabel("УРОВЕНЬ ЗАВИСИМОСТИ")
        Spacer(Modifier.height(12.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Severity.entries.forEach { item ->
                SeverityDot(
                    severity = item,
                    selected = severity == item,
                    onClick = { severity = if (severity == item) null else item }
                )
            }
        }
        Spacer(Modifier.height(26.dp))

        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            BloomButton(
                text = "СОЗДАТЬ",
                onClick = { onCreate(field.text.trim(), severity) },
                containerColor = BloomColors.Primary,
                contentColor = BloomColors.OnPrimary,
                enabled = field.text.isNotBlank(),
                modifier = Modifier.widthIn(min = 184.dp)
            )
        }
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        color = BloomColors.OnSurfaceVariant,
        fontFamily = Montserrat,
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.08.em
    )
}

@Composable
private fun SeverityDot(severity: Severity, selected: Boolean, onClick: () -> Unit) {
    val scale by animateFloatAsState(if (selected) 1.08f else 1f, label = "dot")
    Box(
        modifier = Modifier
            .size(28.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .drawBehind {
                if (selected) {
                    drawCircle(Color(0x38B6C4FF), radius = size.minDimension / 2 + 3.dp.toPx())
                }
            }
            .clip(CircleShape)
            .background(severity.color)
            .border(
                2.dp,
                if (selected) BloomColors.OnBackground else Color.Transparent,
                CircleShape
            )
            .clickable(onClick = onClick)
    )
}
