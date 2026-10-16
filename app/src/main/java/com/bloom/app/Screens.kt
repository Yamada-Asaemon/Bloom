package com.bloom.app

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

// ===== Пустой экран =====

@Composable
fun EmptyScreen(lang: String) {
    val c = LocalBloom.current
    Column(
        Modifier
            .fillMaxSize()
            .padding(top = 25.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(R.drawable.empty),
            contentDescription = null,
            modifier = Modifier
                .size(180.dp)
                .alpha(0.5f),
            contentScale = ContentScale.Fit
        )
        Spacer(Modifier.height(20.dp))
        BText(
            tr(lang, "emptyText"),
            size = 12,
            color = c.text.copy(alpha = 0.5f),
            align = TextAlign.Center
        )
        Spacer(Modifier.height(20.dp))
        BloomLine(Modifier.fillMaxWidth(0.6f))
    }
}

// ===== Экран привычки =====

@Composable
fun HabitScreen(
    habit: Habit,
    lang: String,
    index: Int,
    count: Int,
    onMenu: () -> Unit,
    onDelete: () -> Unit,
    onPrev: () -> Unit,
    onNext: () -> Unit
) {
    val c = LocalBloom.current

    val now by produceState(System.currentTimeMillis()) {
        while (true) {
            value = System.currentTimeMillis()
            delay(1000)
        }
    }

    val elapsed = (now - habit.startTime).coerceAtLeast(0L)
    val elapsedDays = elapsed / 86_400_000.0
    val cycle = getCycleInfo(elapsedDays)
    val progress = ((elapsedDays - cycle.from) / (cycle.to - cycle.from))
        .coerceIn(0.0, 1.0).toFloat()
    val parts = getTimeParts(elapsed)
    val habitColor = Color(habit.color)

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = 30.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Шапка: меню — название — удаление
        Box(
            Modifier
                .fillMaxWidth()
                .heightIn(min = 44.dp)
                .padding(bottom = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 8.dp)
                    .size(44.dp)
                    .noRipple(onMenu),
                contentAlignment = Alignment.Center
            ) {
                Ico(R.drawable.ic_menu, c.text.copy(alpha = 0.8f), 22)
            }

            BText(
                habit.name,
                modifier = Modifier.padding(horizontal = 56.dp),
                size = 22,
                color = habitColor,
                weight = FontWeight.Bold,
                spacing = 3f,
                align = TextAlign.Center
            )

            Box(
                Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 8.dp)
                    .size(44.dp)
                    .noRipple(onDelete),
                contentAlignment = Alignment.Center
            ) {
                Ico(R.drawable.ic_trash, c.text.copy(alpha = 0.8f), 22)
            }
        }

        Spacer(Modifier.height(10.dp))

        ProgressRing(
            progress = progress,
            color = habitColor,
            canSwipe = count > 1,
            hasPrev = index > 0,
            hasNext = index < count - 1,
            onPrev = onPrev,
            onNext = onNext
        )

        // Таймер дни : часы : минуты : секунды
        Row(
            Modifier.padding(top = 40.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            TimerUnit(tr(lang, "days"), parts.d)
            TimerColon()
            TimerUnit(tr(lang, "hours"), parts.h)
            TimerColon()
            TimerUnit(tr(lang, "minutes"), parts.m)
            TimerColon()
            TimerUnit(tr(lang, "seconds"), parts.s)
        }

        // Точки-индикатор
        if (count > 1) {
            Row(
                Modifier.padding(top = 30.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (i in 0 until count) {
                    val active = i == index
                    val s by animateFloatAsState(if (active) 1.3f else 1f, tween(300), label = "dot")
                    Box(
                        Modifier
                            .size(8.dp)
                            .scale(s)
                            .clip(CircleShape)
                            .background(if (active) c.text else Color(0x66808080))
                    )
                }
            }
        }

        // История
        Column(
            Modifier
                .fillMaxWidth()
                .padding(start = 30.dp, end = 30.dp, top = 40.dp)
        ) {
            BText(
                tr(lang, "history"),
                size = 13,
                weight = FontWeight.SemiBold,
                spacing = 2f
            )
            Spacer(Modifier.height(12.dp))
            BloomLine(Modifier.fillMaxWidth())
            Spacer(Modifier.height(16.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(c.panel)
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                BasicText(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = c.muted)) {
                            append(tr(lang, "created").lowercase())
                        }
                        append(" ")
                        withStyle(SpanStyle(color = c.text, fontWeight = FontWeight.SemiBold)) {
                            append(formatDateTime(habit.startTime))
                        }
                    },
                    style = TextStyle(
                        color = c.muted,
                        fontSize = 13.sp,
                        letterSpacing = 1.2.sp,
                        lineHeight = 18.sp
                    )
                )
            }
        }

        // Место под кнопку «добавить привычку»
        Spacer(Modifier.height(120.dp))
    }
}

@Composable
private fun ProgressRing(
    progress: Float,
    color: Color,
    canSwipe: Boolean,
    hasPrev: Boolean,
    hasNext: Boolean,
    onPrev: () -> Unit,
    onNext: () -> Unit
) {
    val animated by animateFloatAsState(progress, tween(500, easing = LinearEasing), label = "ring")
    var drag by remember { mutableFloatStateOf(0f) }

    Box(
        Modifier
            .size(200.dp)
            .pointerInput(canSwipe, hasPrev, hasNext) {
                if (!canSwipe) return@pointerInput
                detectHorizontalDragGestures(
                    onDragStart = { drag = 0f },
                    onDragCancel = { drag = 0f },
                    onDragEnd = {
                        val threshold = 50.dp.toPx()
                        if (drag < -threshold && hasNext) onNext()
                        else if (drag > threshold && hasPrev) onPrev()
                        drag = 0f
                    }
                ) { _, amount -> drag += amount }
            },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(R.drawable.app_icon),
            contentDescription = null,
            modifier = Modifier
                .size(180.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        Canvas(Modifier.size(200.dp)) {
            val stroke = 12.dp.toPx()
            val inset = 8.dp.toPx()
            val d = 184.dp.toPx()
            drawArc(
                color = Color(0x40808080),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = Size(d, d),
                style = Stroke(width = stroke)
            )
            if (animated > 0f) {
                drawArc(
                    color = color,
                    startAngle = -90f,
                    sweepAngle = 360f * animated,
                    useCenter = false,
                    topLeft = Offset(inset, inset),
                    size = Size(d, d),
                    style = Stroke(width = stroke, cap = StrokeCap.Round)
                )
            }
        }
    }
}

@Composable
private fun TimerUnit(label: String, value: String) {
    val c = LocalBloom.current
    Column(
        Modifier.widthIn(min = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        BText(label, size = 10, color = c.muted, spacing = 1.5f)
        BText(value, size = 32, weight = FontWeight.SemiBold, spacing = 1.5f)
    }
}

@Composable
private fun TimerColon() {
    val c = LocalBloom.current
    BText(
        ":",
        modifier = Modifier
            .padding(horizontal = 4.dp)
            .offset(y = (-4).dp),
        size = 32,
        color = c.text.copy(alpha = 0.7f),
        weight = FontWeight.SemiBold,
        spacing = 0f
    )
}
