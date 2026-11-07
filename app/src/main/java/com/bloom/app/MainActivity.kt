package com.bloom.app

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Bg = Color(0xFF121212)
private val CardBg = Color(0xFF1E1E26)
private val Track = Color(0xFF2C2C34)
private val Violet = Color(0xFF6A1FB0)
private val VioletLight = Color(0xFFB57CFF)
private val Gray = Color(0xFF8A8A94)
private val HabitRed = Color(0xFFFF5C5C)
private val HabitYellow = Color(0xFFFFD600)
private val HabitGreen = Color(0xFF008000)

private val ButtonShape = RoundedCornerShape(14.dp)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Notifier.ensureChannel(this)
        setContent { MainScreen() }
    }
}

@Composable
fun PrimaryButton(text: String, modifier: Modifier = Modifier, enabled: Boolean = true, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth(0.55f).height(46.dp),
        shape = ButtonShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = Violet, contentColor = Color.White,
            disabledContainerColor = Violet.copy(alpha = 0.4f),
            disabledContentColor = Color.White.copy(alpha = 0.6f)
        )
    ) {
        Text(text, fontSize = 12.sp, letterSpacing = 2.sp)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val ctx = LocalContext.current
    var habits by remember { mutableStateOf(Store.load(ctx)) }
    var showAdd by remember { mutableStateOf(false) }
    var deleteId by remember { mutableStateOf<String?>(null) }
    var pickFor by rememberSaveable { mutableStateOf<String?>(null) }
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    val pagerState = rememberPagerState { habits.size }

    LaunchedEffect(Unit) {
        while (true) {
            now = System.currentTimeMillis()
            delay(1000 - now % 1000)
        }
    }

    val permLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) permLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        Store.load(ctx).forEach { Notifier.schedule(ctx, it) }
    }

    var lastCount by remember { mutableIntStateOf(habits.size) }
    LaunchedEffect(habits.size) {
        if (habits.size > lastCount) pagerState.animateScrollToPage(habits.size - 1)
        lastCount = habits.size
    }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        val id = pickFor
        if (uri != null && id != null) {
            val path = Store.saveImage(ctx, uri, id)
            if (path != null) {
                habits.firstOrNull { it.id == id }?.imagePath?.let { File(it).delete() }
                habits = habits.map { if (it.id == id) it.copy(imagePath = path) else it }
                Store.save(ctx, habits)
            }
        }
    }

    Box(Modifier.fillMaxSize().background(Bg).systemBarsPadding()) {
        if (habits.isNotEmpty()) {
            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { i ->
                val h = habits[i]
                HabitPage(
                    h = h, now = now,
                    onDelete = { deleteId = h.id },
                    onPhoto = { pickFor = h.id; picker.launch("image/*") }
                )
            }
            if (habits.size > 1) {
                Row(
                    Modifier.align(Alignment.BottomCenter).padding(bottom = 80.dp),
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    habits.indices.forEach { i ->
                        Box(
                            Modifier.size(7.dp).clip(CircleShape)
                                .background(if (i == pagerState.currentPage) VioletLight else Track)
                        )
                    }
                }
            }
        }

        PrimaryButton(
            text = "ДОБАВИТЬ ПРИВЫЧКУ",
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 20.dp),
            onClick = { showAdd = true }
        )
    }

    if (showAdd) {
        AddSheet(
            onDismiss = { showAdd = false },
            onAdd = { name, color ->
                val h = Habit(name = name, color = color.toArgb().toLong() and 0xFFFFFFFFL)
                habits = habits + h
                Store.save(ctx, habits)
                Notifier.schedule(ctx, h)
                showAdd = false
            }
        )
    }

    deleteId?.let { id ->
        AlertDialog(
            onDismissRequest = { deleteId = null },
            containerColor = CardBg,
            title = { Text("Удалить привычку?", color = Color.White, fontSize = 18.sp) },
            confirmButton = {
                TextButton(onClick = {
                    habits.firstOrNull { it.id == id }?.let {
                        Notifier.cancel(ctx, it)
                        it.imagePath?.let { p -> File(p).delete() }
                    }
                    habits = habits.filter { it.id != id }
                    Store.save(ctx, habits)
                    deleteId = null
                }) { Text("УДАЛИТЬ", color = HabitRed) }
            },
            dismissButton = {
                TextButton(onClick = { deleteId = null }) { Text("ОТМЕНА", color = Gray) }
            }
        )
    }
}

@Composable
fun HabitPage(h: Habit, now: Long, onDelete: () -> Unit, onPhoto: () -> Unit) {
    val color = Color(h.color)
    val elapsed = (now - h.createdAt).coerceAtLeast(0)

    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier.fillMaxSize().padding(bottom = 96.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(32.dp))
            Text(
                h.name.uppercase(), color = color, fontSize = 20.sp,
                fontWeight = FontWeight.Bold, letterSpacing = 3.sp,
                textAlign = TextAlign.Center, maxLines = 2,
                modifier = Modifier.padding(horizontal = 68.dp)
            )
            Spacer(Modifier.height(28.dp))
            Ring(h, color, elapsed)
            Spacer(Modifier.height(36.dp))
            TimerRow(elapsed)
            Spacer(Modifier.height(24.dp))
            DateCard(h.createdAt)
        }

        Column(
            Modifier.align(Alignment.TopEnd).padding(top = 22.dp, end = 14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            IconButton(onClick = onDelete) {
                Icon(painterResource(R.drawable.ic_trash), null, Modifier.size(26.dp), tint = Color.White)
            }
            IconButton(onClick = onPhoto) {
                Icon(painterResource(R.drawable.ic_camera), null, Modifier.size(26.dp), tint = Color.White)
            }
        }
    }
}

@Composable
fun Ring(h: Habit, color: Color, elapsed: Long) {
    val d = (elapsed / Milestones.DAY).toInt()
    val next = Milestones.next(elapsed)
    val prev = if (d < 1) 0 else if (d < 7) 1 else (d / 7) * 7
    val progress = ((elapsed - prev * Milestones.DAY).toFloat() /
            ((next - prev) * Milestones.DAY).toFloat()).coerceIn(0f, 1f)

    val total = 236.dp
    val stroke = 12.dp

    Box(Modifier.size(total), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val sw = stroke.toPx()
            val inset = sw / 2
            val r = size.minDimension / 2 - inset
            drawCircle(Track, radius = r, style = Stroke(sw))
            drawArc(
                color = color, startAngle = -90f, sweepAngle = 360f * progress,
                useCenter = false, topLeft = Offset(inset, inset),
                size = Size(size.width - sw, size.height - sw),
                style = Stroke(sw, cap = StrokeCap.Round)
            )
            val a = Math.toRadians((-90f + 360f * progress).toDouble())
            drawCircle(
                color, radius = sw * 0.62f,
                center = Offset(
                    center.x + r * Math.cos(a).toFloat(),
                    center.y + r * Math.sin(a).toFloat()
                )
            )
        }
        val inner = total - stroke * 2 - 8.dp
        Box(
            Modifier.size(inner).clip(CircleShape).background(
                Brush.radialGradient(listOf(Color(0xFF1B1D36), Color(0xFF14141C)))
            ),
            contentAlignment = Alignment.Center
        ) {
            val bmp = remember(h.imagePath) {
                h.imagePath?.let { BitmapFactory.decodeFile(it)?.asImageBitmap() }
            }
            if (bmp != null) {
                Image(bmp, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            } else {
                Image(painterResource(R.drawable.ic_lotus), null, Modifier.size(inner * 0.5f))
            }
        }
    }
}

@Composable
fun TimerRow(elapsed: Long) {
    val s = elapsed / 1000
    val parts = listOf(
        "ДНИ" to s / 86400,
        "ЧАСЫ" to (s / 3600) % 24,
        "МИНУТЫ" to (s / 60) % 60,
        "СЕКУНДЫ" to s % 60
    )
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        parts.forEachIndexed { i, (label, v) ->
            if (i > 0) {
                Text(
                    ":", color = Violet, fontSize = 22.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 3.dp).padding(bottom = 10.dp)
                )
            }
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(label, color = Gray, fontSize = 8.sp, letterSpacing = 1.sp, maxLines = 1)
                Spacer(Modifier.height(6.dp))
                Box(
                    Modifier.fillMaxWidth().height(52.dp)
                        .clip(RoundedCornerShape(14.dp)).background(CardBg)
                        .border(1.5.dp, Violet, RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        v.toString().padStart(2, '0'), color = VioletLight,
                        fontSize = 26.sp, fontWeight = FontWeight.Bold,
                        maxLines = 1, softWrap = false
                    )
                }
            }
        }
    }
}

@Composable
fun DateCard(ts: Long) {
    val date = remember(ts) {
        SimpleDateFormat("d MMMM yyyy", Locale.forLanguageTag("ru")).format(Date(ts)).uppercase()
    }
    val time = remember(ts) { SimpleDateFormat("HH:mm", Locale.US).format(Date(ts)) }
    val shape = RoundedCornerShape(20.dp)

    Row(
        Modifier.fillMaxWidth().padding(horizontal = 28.dp)
            .clip(shape)
            .background(Brush.horizontalGradient(listOf(Color(0xFF26203A), Color(0xFF1A1A22))))
            .border(1.dp, Violet.copy(alpha = 0.45f), shape)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // тональный круг с иконкой календаря
        Box(
            Modifier.size(42.dp).clip(CircleShape).background(Violet.copy(alpha = 0.28f)),
            contentAlignment = Alignment.Center
        ) {
            Canvas(Modifier.size(20.dp)) {
                val sw = 1.8.dp.toPx()
                val c = VioletLight
                drawRoundRect(
                    c, topLeft = Offset(sw, size.height * 0.18f),
                    size = Size(size.width - 2 * sw, size.height * 0.74f),
                    cornerRadius = CornerRadius(4.dp.toPx()), style = Stroke(sw)
                )
                drawLine(c, Offset(sw, size.height * 0.42f), Offset(size.width - sw, size.height * 0.42f), sw)
                drawLine(c, Offset(size.width * 0.3f, sw / 2), Offset(size.width * 0.3f, size.height * 0.3f), sw, StrokeCap.Round)
                drawLine(c, Offset(size.width * 0.7f, sw / 2), Offset(size.width * 0.7f, size.height * 0.3f), sw, StrokeCap.Round)
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text("СОЗДАНА", color = Gray, fontSize = 9.sp, letterSpacing = 2.sp)
            Spacer(Modifier.height(2.dp))
            Text(
                date, color = Color.White, fontSize = 14.sp,
                fontWeight = FontWeight.Bold, letterSpacing = 1.sp, maxLines = 1
            )
        }
        Spacer(Modifier.width(8.dp))
        // плашка со временем
        Box(
            Modifier.clip(RoundedCornerShape(10.dp))
                .background(Violet.copy(alpha = 0.28f))
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Text(time, color = VioletLight, fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSheet(onDismiss: () -> Unit, onAdd: (String, Color) -> Unit) {
    var name by remember { mutableStateOf("") }
    var color by remember { mutableStateOf<Color?>(null) }
    val ready = name.isNotBlank() && color != null
    val fieldShape = RoundedCornerShape(14.dp)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = CardBg,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = null
    ) {
        Column(
            Modifier.fillMaxWidth().navigationBarsPadding().imePadding()
                .padding(horizontal = 24.dp, vertical = 24.dp)
        ) {
            Text("НАЗВАНИЕ ПРИВЫЧКИ", color = Color.White, fontSize = 12.sp, letterSpacing = 2.sp)
            Spacer(Modifier.height(14.dp))
            BasicTextField(
                value = name,
                onValueChange = { if (it.length <= 25) name = it },
                singleLine = true,
                textStyle = TextStyle(color = Color.White, fontSize = 14.sp, letterSpacing = 1.sp),
                cursorBrush = SolidColor(VioletLight),
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { inner ->
                    Row(
                        Modifier.fillMaxWidth().height(46.dp).clip(fieldShape)
                            .background(Track)
                            .border(1.dp, Color(0xFF6B6B75), fieldShape)
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(Modifier.weight(1f)) {
                            if (name.isEmpty()) {
                                Text("ВВЕДИТЕ НАЗВАНИЕ", color = Gray, fontSize = 12.sp, letterSpacing = 2.sp)
                            }
                            inner()
                        }
                        Text("${name.length}/25", color = Gray, fontSize = 12.sp)
                    }
                }
            )
            Spacer(Modifier.height(22.dp))
            Text("ЦВЕТ", color = Color.White, fontSize = 12.sp, letterSpacing = 2.sp)
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                listOf(HabitRed, HabitYellow, HabitGreen).forEach { c ->
                    Box(
                        Modifier.size(28.dp).clip(CircleShape).background(c)
                            .then(if (color == c) Modifier.border(2.5.dp, Color.White, CircleShape) else Modifier)
                            .clickable { color = c }
                    )
                }
            }
            Spacer(Modifier.height(22.dp))
            PrimaryButton(
                text = "ДОБАВИТЬ",
                modifier = Modifier.align(Alignment.CenterHorizontally),
                enabled = ready,
                onClick = { color?.let { onAdd(name.trim(), it) } }
            )
        }
    }
}
