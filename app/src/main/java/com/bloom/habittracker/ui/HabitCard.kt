package com.bloom.habittracker.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.bloom.habittracker.R
import com.bloom.habittracker.data.Habit
import java.time.Instant
import java.time.ZoneId

private const val HOUR_STAGE_SECONDS = 3600L

private val MONTHS_GENITIVE_RU = listOf(
    "ЯНВАРЯ", "ФЕВРАЛЯ", "МАРТА", "АПРЕЛЯ", "МАЯ", "ИЮНЯ",
    "ИЮЛЯ", "АВГУСТА", "СЕНТЯБРЯ", "ОКТЯБРЯ", "НОЯБРЯ", "ДЕКАБРЯ"
)

private fun formatTimer(totalSeconds: Long): String {
    val days = totalSeconds / 86400
    val hours = (totalSeconds % 86400) / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    fun two(v: Long) = v.toString().padStart(2, '0')
    return "${two(days)} : ${two(hours)} : ${two(minutes)} : ${two(seconds)}"
}

private fun formatDateLabel(ms: Long): String {
    val d = Instant.ofEpochMilli(ms).atZone(ZoneId.systemDefault())
    return "ДАТА: ${d.dayOfMonth} ${MONTHS_GENITIVE_RU[d.monthValue - 1]} ${d.year} ГОД"
}

private fun formatTimeLabel(ms: Long): String {
    val d = Instant.ofEpochMilli(ms).atZone(ZoneId.systemDefault())
    val h = d.hour.toString().padStart(2, '0')
    val m = d.minute.toString().padStart(2, '0')
    return "ВРЕМЯ: $h:$m"
}

@Composable
fun HabitCard(
    habit: Habit,
    nowMs: Long,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardShape = RoundedCornerShape(28.dp)
    val elapsedSec = maxOf(0L, (nowMs - habit.startTimeMs) / 1000)
    val ringRatio = (elapsedSec % HOUR_STAGE_SECONDS).toFloat() / HOUR_STAGE_SECONDS
    val ringSize = if (LocalConfiguration.current.screenWidthDp <= 390) 184.dp else 204.dp

    Column(
        modifier = modifier
            .fillMaxSize()
            .clip(cardShape)
            .background(BloomColors.SurfaceContainer)
            .border(1.dp, BloomColors.OutlineVariant, cardShape)
            .padding(start = 18.dp, end = 18.dp, top = 20.dp, bottom = 22.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().heightIn(min = 34.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Spacer(Modifier.size(34.dp))
            Text(
                text = habit.title,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                color = habit.color,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontFamily = Montserrat,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.06.em
            )
            DeleteButton(onClick = onDelete)
        }

        Box(modifier = Modifier.padding(top = 8.dp)) {
            ProgressRing(ratio = ringRatio, color = habit.color, ringSize = ringSize)
        }

        Box(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
            TimerCard(text = formatTimer(elapsedSec))
        }

        Column(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "ПРИВЫЧКА СОЗДАНА",
                color = BloomColors.OnBackground,
                fontFamily = Montserrat,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.1.em
            )
            Spacer(Modifier.height(14.dp))
            MetaStack(startTimeMs = habit.startTimeMs)
        }
    }
}

@Composable
private fun DeleteButton(onClick: () -> Unit) {
    val shape = RoundedCornerShape(10.dp)
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(shape)
            .background(BloomColors.SurfaceContainerHigh)
            .border(1.dp, BloomColors.OutlineVariant, shape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_trash),
            contentDescription = "Удалить",
            tint = BloomColors.OnSurfaceVariant,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun ProgressRing(ratio: Float, color: Color, ringSize: Dp) {
    val animated by animateFloatAsState(
        targetValue = ratio,
        animationSpec = tween(350, easing = LinearEasing),
        label = "ring"
    )
    Box(
        modifier = Modifier.size(ringSize),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(R.drawable.logo),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(ringSize * (160f / 204f))
                .clip(CircleShape)
        )
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 13f / 204f * size.width
            val radius = 88f / 204f * size.width
            val center = Offset(size.width / 2f, size.height / 2f)
            drawCircle(
                color = BloomColors.OutlineVariant,
                radius = radius,
                center = center,
                style = Stroke(width = strokeWidth)
            )
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 360f * animated,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2f, radius * 2f),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
    }
}

@Composable
private fun TimerCard(text: String) {
    val shape = RoundedCornerShape(20.dp)
    var fontSizeSp by remember { mutableFloatStateOf(21f) }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(BloomColors.SurfaceContainerHigh)
            .border(1.dp, BloomColors.OutlineVariant, shape)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = BloomColors.OnBackground,
            fontFamily = Montserrat,
            fontSize = fontSizeSp.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.06.em,
            maxLines = 1,
            softWrap = false,
            style = TextStyle(fontFeatureSettings = "tnum"),
            onTextLayout = {
                if (it.didOverflowWidth && fontSizeSp > 12f) fontSizeSp -= 1f
            }
        )
    }
}

@Composable
private fun MetaStack(startTimeMs: Long) {
    val shape = RoundedCornerShape(18.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(BloomColors.SurfaceContainerHigh)
            .border(1.dp, BloomColors.OutlineVariant, shape)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MetaRow(text = formatDateLabel(startTimeMs)) { CalendarIcon() }
        MetaRow(text = formatTimeLabel(startTimeMs)) { ClockIcon() }
    }
}

@Composable
private fun MetaRow(text: String, icon: @Composable () -> Unit) {
    Row(
        modifier = Modifier.padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        icon()
        Text(
            text = text,
            color = BloomColors.OnSurfaceVariant,
            fontFamily = Montserrat,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.02.em,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = TextStyle(fontFeatureSettings = "tnum")
        )
    }
}

@Composable
private fun CalendarIcon() {
    Canvas(modifier = Modifier.size(14.dp)) {
        val u = size.width / 24f
        val color = BloomColors.Primary
        val stroke = Stroke(width = 2f * u, cap = StrokeCap.Round, join = StrokeJoin.Round)
        drawRoundRect(
            color = color,
            topLeft = Offset(3f * u, 4f * u),
            size = Size(18f * u, 18f * u),
            cornerRadius = CornerRadius(2f * u),
            style = stroke
        )
        drawLine(color, Offset(16f * u, 2f * u), Offset(16f * u, 6f * u), 2f * u, StrokeCap.Round)
        drawLine(color, Offset(8f * u, 2f * u), Offset(8f * u, 6f * u), 2f * u, StrokeCap.Round)
        drawLine(color, Offset(3f * u, 10f * u), Offset(21f * u, 10f * u), 2f * u, StrokeCap.Round)
    }
}

@Composable
private fun ClockIcon() {
    Canvas(modifier = Modifier.size(14.dp)) {
        val u = size.width / 24f
        val color = BloomColors.Primary
        val stroke = Stroke(width = 2f * u, cap = StrokeCap.Round, join = StrokeJoin.Round)
        drawCircle(
            color = color,
            radius = 10f * u,
            center = Offset(12f * u, 12f * u),
            style = stroke
        )
        val hands = Path().apply {
            moveTo(12f * u, 6f * u)
            lineTo(12f * u, 12f * u)
            lineTo(16f * u, 14f * u)
        }
        drawPath(hands, color, style = stroke)
    }
}

@Composable
fun CarouselDots(count: Int, current: Int, onSelect: (Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(count) { index ->
            val active = index == current
            val width by animateDpAsState(
                targetValue = if (active) 20.dp else 7.dp,
                animationSpec = tween(280),
                label = "dotWidth"
            )
            val color by animateColorAsState(
                targetValue = if (active) BloomColors.Primary else Color(0x42C4C6D0),
                animationSpec = tween(250),
                label = "dotColor"
            )
            Box(
                modifier = Modifier
                    .size(width = width, height = 7.dp)
                    .clip(CircleShape)
                    .background(color)
                    .clickable { onSelect(index) }
            )
        }
    }
}
