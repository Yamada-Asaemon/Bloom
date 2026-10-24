package com.bloom.app

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale as drawScale
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
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
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.floor

// ───────────── Цвета (из :root в Bloom.html) ─────────────
private val Bg = Color(0xFF16161D)
private val Panel = Color(0xFF1E1E28)
private val Accent = Color(0xFF008000)
private val Muted = Color(0x99FFFFFF)
private val HabitRed = Color(0xFFFF5C5C)
private val HabitYellow = Color(0xFFFFD700)
private val HabitGreen = Color(0xFF008000)

private const val MAX_LEN = 25
private val milestones = listOf(1, 7, 14, 30, 60, 90, 120, 150, 180, 210, 240, 270, 300, 330, 365)

// ───────────── Модель и хранение (вместо localStorage) ─────────────
data class Habit(val name: String, val color: Int, val startTime: Long)

private const val PREFS = "bloom"
private const val KEY = "tracker_habits_array"

fun loadHabits(ctx: Context): List<Habit> {
    val raw = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY, null)
        ?: return emptyList()
    return try {
        val arr = JSONArray(raw)
        List(arr.length()) { i ->
            val o = arr.getJSONObject(i)
            Habit(o.getString("name"), o.getInt("color"), o.getLong("startTime"))
        }
    } catch (e: Exception) {
        emptyList()
    }
}

fun saveHabits(ctx: Context, list: List<Habit>) {
    val arr = JSONArray()
    list.forEach {
        arr.put(JSONObject().put("name", it.name).put("color", it.color).put("startTime", it.startTime))
    }
    ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY, arr.toString()).apply()
}

// ───────────── Логика кругов прогресса (вехи) ─────────────
private fun cycleOf(elapsedDays: Double): Pair<Double, Double> {
    if (elapsedDays < 1) return 0.0 to 1.0
    for (i in milestones.indices) {
        if (elapsedDays < milestones[i]) return milestones[i - 1].toDouble() to milestones[i].toDouble()
    }
    val extra = floor((elapsedDays - 365) / 30)
    val from = 365 + extra * 30
    return from to from + 30
}

private fun two(n: Long) = n.toString().padStart(2, '0')

private fun formatDateTime(ts: Long): String =
    SimpleDateFormat("dd.MM.yyyy // HH:mm", Locale.getDefault()).format(Date(ts))

// ───────────── Activity ─────────────
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        setContent { BloomApp() }
    }
}

// ───────────── Корневой экран ─────────────
@Composable
fun BloomApp() {
    val ctx = LocalContext.current
    val focusManager = LocalFocusManager.current
    val density = LocalDensity.current

    val habits = remember { mutableStateListOf<Habit>().also { it.addAll(loadHabits(ctx)) } }
    var index by remember { mutableIntStateOf(0) }
    var showAdd by remember { mutableStateOf(false) }
    var showDelete by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var picked by remember { mutableStateOf<Color?>(null) }
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    val inputFocus = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        while (true) {
            now = System.currentTimeMillis()
            delay(1000)
        }
    }

    fun closeAdd() {
        focusManager.clearFocus()
        showAdd = false
        name = ""
        picked = null
    }

    BackHandler(enabled = showAdd || showDelete) {
        if (showDelete) showDelete = false else closeAdd()
    }

    val swipePx = with(density) { 50.dp.toPx() }

    Box(Modifier.fillMaxSize().background(Bg)) {

        // ── основной слой ──
        Box(Modifier.fillMaxSize().systemBarsPadding()) {
            if (habits.isNotEmpty()) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .pointerInput(index, habits.size) {
                            var total = 0f
                            detectHorizontalDragGestures(
                                onDragStart = { total = 0f },
                                onDragCancel = { total = 0f },
                                onDragEnd = {
                                    if (abs(total) >= swipePx) {
                                        if (total < 0 && index < habits.size - 1) index++
                                        else if (total > 0 && index > 0) index--
                                    }
                                }
                            ) { _, dx -> total += dx }
                        }
                ) {
                    AnimatedContent(
                        targetState = index,
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.TopCenter,
                        transitionSpec = {
                            val dir = if (targetState > initialState) 1 else -1
                            (slideInHorizontally(tween(250)) { 30 * dir } + fadeIn(tween(250))) togetherWith
                                (slideOutHorizontally(tween(250)) { -30 * dir } + fadeOut(tween(250)))
                        },
                        label = "habit"
                    ) { i ->
                        val habit = habits.getOrNull(i)
                        if (habit != null) {
                            HabitContent(
                                habit = habit,
                                now = now,
                                count = habits.size,
                                position = i,
                                onDelete = { showDelete = true }
                            )
                        }
                    }
                }
            }

            // кнопка «добавить привычку»
            Box(
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 40.dp)
                    .fillMaxWidth(0.6f)
                    .bounceClick { showAdd = true }
                    .clip(RoundedCornerShape(14.dp))
                    .background(Accent)
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("добавить привычку", color = Color.White, fontSize = 13.sp, letterSpacing = 1.5.sp)
            }
        }

        // ── затемнение + окно добавления ──
        Scrim(visible = showAdd, onClick = { closeAdd() })

        AnimatedVisibility(
            visible = showAdd,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = slideInVertically(tween(350)) { it },
            exit = slideOutVertically(tween(350)) { it }
        ) {
            AddPanel(
                name = name,
                onNameChange = { if (it.length <= MAX_LEN) name = it.lowercase() },
                picked = picked,
                onPick = { picked = it },
                focusRequester = inputFocus,
                onSubmit = {
                    val trimmed = name.trim()
                    if (trimmed.isEmpty()) {
                        inputFocus.requestFocus()
                    } else {
                        habits.add(
                            Habit(trimmed, (picked ?: Color.White).toArgb(), System.currentTimeMillis())
                        )
                        index = habits.size - 1
                        saveHabits(ctx, habits.toList())
                        closeAdd()
                    }
                }
            )
        }

        // ── окно удаления ──
        Scrim(visible = showDelete, onClick = { showDelete = false })

        AnimatedVisibility(
            visible = showDelete,
            modifier = Modifier.align(Alignment.Center),
            enter = fadeIn(tween(250)) + scaleIn(tween(250), initialScale = 0.9f),
            exit = fadeOut(tween(250)) + scaleOut(tween(250), targetScale = 0.9f)
        ) {
            DeletePanel(
                onYes = {
                    if (habits.isNotEmpty()) {
                        habits.removeAt(index.coerceIn(0, habits.size - 1))
                        index = if (habits.isEmpty()) 0 else index.coerceIn(0, habits.size - 1)
                        saveHabits(ctx, habits.toList())
                    }
                    showDelete = false
                },
                onNo = { showDelete = false }
            )
        }
    }
}

// ───────────── Экран одной привычки ─────────────
@Composable
private fun HabitContent(habit: Habit, now: Long, count: Int, position: Int, onDelete: () -> Unit) {
    val habitColor = Color(habit.color)
    val elapsedMs = (now - habit.startTime).coerceAtLeast(0)
    val totalSec = elapsedMs / 1000
    val days = elapsedMs / 86_400_000.0
    val (from, to) = cycleOf(days)
    val target = ((days - from) / (to - from)).coerceIn(0.0, 1.0).toFloat()
    val progress by animateFloatAsState(target, tween(500, easing = LinearEasing), label = "progress")

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(30.dp))

        // заголовок + корзина
        Box(Modifier.fillMaxWidth().padding(horizontal = 30.dp).heightIn(min = 32.dp)) {
            Text(
                text = habit.name.lowercase(),
                color = habitColor,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 3.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.align(Alignment.Center).padding(horizontal = 50.dp)
            )
            TrashIcon(
                Modifier
                    .align(Alignment.CenterEnd)
                    .size(22.dp)
                    .alpha(0.8f)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onDelete
                    )
            )
        }

        Spacer(Modifier.height(20.dp))

        // круг прогресса с картинкой
        Box(Modifier.size(200.dp), contentAlignment = Alignment.Center) {
            Image(
                painter = painterResource(R.drawable.app_icon),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(170.dp).clip(CircleShape).background(Bg)
            )
            Canvas(Modifier.size(200.dp)) {
                val stroke = 12.dp.toPx()
                val r = 92.dp.toPx()
                val topLeft = Offset(size.width / 2 - r, size.height / 2 - r)
                drawCircle(Color(0x40808080), radius = r, style = Stroke(width = stroke))
                if (progress > 0f) {
                    drawArc(
                        color = habitColor,
                        startAngle = -90f,
                        sweepAngle = 360f * progress,
                        useCenter = false,
                        topLeft = topLeft,
                        size = Size(2 * r, 2 * r),
                        style = Stroke(width = stroke, cap = StrokeCap.Round)
                    )
                }
            }
        }

        // таймер
        Spacer(Modifier.height(40.dp))
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.Center) {
            TimerUnit("дни", two(totalSec / 86400))
            TimerColon()
            TimerUnit("часы", two((totalSec % 86400) / 3600))
            TimerColon()
            TimerUnit("минуты", two((totalSec % 3600) / 60))
            TimerColon()
            TimerUnit("секунды", two(totalSec % 60))
        }

        // точки
        if (count > 1) {
            Spacer(Modifier.height(15.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                repeat(count) { i ->
                    val active = i == position
                    val s by animateFloatAsState(if (active) 1.3f else 1f, tween(300), label = "dot")
                    Box(
                        Modifier
                            .size(8.dp)
                            .scale(s)
                            .clip(CircleShape)
                            .background(if (active) Color.White else Color(0x66808080))
                    )
                }
            }
        }

        // дата создания
        Spacer(Modifier.height(30.dp))
        Box(
            Modifier
                .padding(horizontal = 30.dp)
                .widthIn(max = 300.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0x1A808080))
                .border(1.dp, Accent, RoundedCornerShape(8.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = Muted.copy(alpha = 0.51f))) { append("привычка создана: ") }
                    withStyle(SpanStyle(color = Color.White, fontWeight = FontWeight.SemiBold, letterSpacing = 1.4.sp)) {
                        append(formatDateTime(habit.startTime))
                    }
                },
                fontSize = 12.sp,
                letterSpacing = 1.3.sp,
                lineHeight = 17.sp,
                textAlign = TextAlign.Center
            )
        }

        Spacer(Modifier.height(120.dp)) // запас под кнопку
    }
}

@Composable
private fun TimerUnit(label: String, value: String) {
    Column(
        Modifier.widthIn(min = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(label, color = Muted, fontSize = 10.sp, letterSpacing = 1.5.sp)
        Text(
            value,
            color = Color.White,
            fontSize = 32.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.5.sp,
            lineHeight = 32.sp
        )
    }
}

@Composable
private fun TimerColon() {
    Text(
        ":",
        color = Color.White.copy(alpha = 0.7f),
        fontSize = 32.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 32.sp,
        modifier = Modifier.padding(horizontal = 4.dp).offset(y = (-4).dp)
    )
}

// ───────────── Иконка корзины (SVG-пути из HTML) ─────────────
private val trashPaths = listOf(
    "M10 11v6",
    "M14 11v6",
    "M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6",
    "M3 6h18",
    "M8 6V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"
)

@Composable
private fun TrashIcon(modifier: Modifier) {
    val paths = remember { trashPaths.map { PathParser().parsePathString(it).toPath() } }
    Canvas(modifier) {
        val k = size.width / 24f
        drawScale(k, pivot = Offset.Zero) {
            paths.forEach {
                drawPath(it, Color.White, style = Stroke(width = 2f, cap = StrokeCap.Round, join = StrokeJoin.Round))
            }
        }
    }
}

// ───────────── Окна ─────────────
@Composable
private fun Scrim(visible: Boolean, onClick: () -> Unit) {
    AnimatedVisibility(visible, enter = fadeIn(tween(300)), exit = fadeOut(tween(300))) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick)
        )
    }
}

@Composable
private fun AddPanel(
    name: String,
    onNameChange: (String) -> Unit,
    picked: Color?,
    onPick: (Color) -> Unit,
    focusRequester: FocusRequester,
    onSubmit: () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    val buttonWidth = (LocalConfiguration.current.screenWidthDp * 0.6f).dp

    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(Panel)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {}
            .navigationBarsPadding()
            .imePadding()
            .padding(30.dp)
    ) {
        Text("название привычки", color = Color.White, fontSize = 13.sp, letterSpacing = 1.5.sp)
        Spacer(Modifier.height(12.dp))

        BasicTextField(
            value = name,
            onValueChange = onNameChange,
            singleLine = true,
            interactionSource = interaction,
            textStyle = TextStyle(color = Color.White, fontSize = 13.sp, letterSpacing = 1.5.sp),
            cursorBrush = SolidColor(Color.White),
            modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
            decorationBox = { inner ->
                Box(Modifier.fillMaxWidth()) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0x26808080))
                            .border(1.dp, if (focused) Accent else Color(0xFF888888), RoundedCornerShape(12.dp))
                            .padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 56.dp)
                    ) {
                        if (name.isEmpty()) {
                            Text("введите название", color = Color.White.copy(alpha = 0.6f), fontSize = 13.sp, letterSpacing = 1.5.sp)
                        }
                        inner()
                    }
                    Text(
                        "${name.length}/$MAX_LEN",
                        color = if (name.length >= MAX_LEN) HabitRed else Muted,
                        fontSize = 12.sp,
                        letterSpacing = 1.sp,
                        modifier = Modifier.align(Alignment.CenterEnd).padding(end = 14.dp)
                    )
                }
            }
        )

        Spacer(Modifier.height(25.dp))
        Text("цвет", color = Color.White, fontSize = 13.sp, letterSpacing = 1.5.sp)
        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(15.dp)) {
            listOf(HabitRed, HabitYellow, HabitGreen).forEach { c ->
                Box(
                    Modifier
                        .size(28.dp)
                        .bounceClick { onPick(c) }
                        .clip(CircleShape)
                        .background(c)
                        .border(2.dp, if (picked == c) Color.White else Color.Transparent, CircleShape)
                )
            }
        }

        Spacer(Modifier.height(25.dp))
        Box(
            Modifier
                .align(Alignment.CenterHorizontally)
                .width(buttonWidth)
                .bounceClick(onSubmit)
                .clip(RoundedCornerShape(14.dp))
                .background(Accent)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("добавить", color = Color.White, fontSize = 13.sp, letterSpacing = 1.5.sp)
        }
    }
}

@Composable
private fun DeletePanel(onYes: () -> Unit, onNo: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth(0.8f)
            .widthIn(max = 340.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Panel)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {}
            .padding(start = 26.dp, top = 24.dp, end = 26.dp, bottom = 22.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "вы действительно хотите удалить привычку?",
            color = Color.White,
            fontSize = 14.sp,
            letterSpacing = 1.2.sp,
            lineHeight = 21.sp,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(22.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            DeleteButton("да", onYes, Modifier.weight(1f))
            DeleteButton("нет", onNo, Modifier.weight(1f))
        }
    }
}

@Composable
private fun DeleteButton(label: String, onClick: () -> Unit, modifier: Modifier) {
    Box(
        modifier
            .bounceClick(onClick)
            .clip(RoundedCornerShape(12.dp))
            .background(Accent)
            .padding(horizontal = 18.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = Color.White, fontSize = 13.sp, letterSpacing = 1.5.sp)
    }
}

// Эффект нажатия (scale 0.96, как :active в CSS)
@Composable
private fun Modifier.bounceClick(onClick: () -> Unit): Modifier {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val s by animateFloatAsState(if (pressed) 0.96f else 1f, tween(100), label = "press")
    return this
        .scale(s)
        .clickable(interactionSource = source, indication = null, onClick = onClick)
}
