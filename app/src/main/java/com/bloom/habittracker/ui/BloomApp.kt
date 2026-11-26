package com.bloom.habittracker.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.bloom.habittracker.data.Habit
import com.bloom.habittracker.data.HabitStore
import com.bloom.habittracker.notifications.Notifications
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun BloomApp(store: HabitStore) {
    val context = LocalContext.current
    var habits by remember { mutableStateOf(store.load()) }
    var showSheet by remember { mutableStateOf(false) }
    var pendingDeleteId by remember { mutableStateOf<String?>(null) }
    var nowMs by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var lastSize by remember { mutableIntStateOf(habits.size) }
    val pagerState = rememberPagerState { habits.size }
    val scope = rememberCoroutineScope()
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    val hasHabits = habits.isNotEmpty()
    LaunchedEffect(hasHabits) {
        if (!hasHabits) return@LaunchedEffect
        while (true) {
            nowMs = System.currentTimeMillis()
            delay(1000 - nowMs % 1000)
        }
    }

    LaunchedEffect(habits.size) {
        if (habits.size > lastSize) pagerState.scrollToPage(habits.lastIndex)
        lastSize = habits.size
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BloomColors.Background)
    ) {
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
            Text(
                text = "Bloom",
                modifier = Modifier.padding(start = 28.dp, end = 28.dp, top = 28.dp, bottom = 8.dp),
                style = TextStyle(
                    brush = Brush.linearGradient(
                        listOf(Color(0xFFF0F3FF), BloomColors.Primary)
                    ),
                    fontFamily = Montserrat,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.02).em
                )
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .navigationBarsPadding()
                    .padding(top = 12.dp, bottom = 116.dp)
            ) {
                if (habits.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "« НЕТ НИЧЕГО НЕВОЗМОЖНОГО, ЕСЛИ ТЫ ПИЗДАБОЛ »",
                            modifier = Modifier.padding(horizontal = 28.dp).widthIn(max = 280.dp),
                            textAlign = TextAlign.Center,
                            color = BloomColors.OnSurfaceVariant,
                            fontFamily = Montserrat,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 0.08.em,
                            lineHeight = 19.sp
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .widthIn(max = 596.dp)
                            .fillMaxWidth()
                            .fillMaxHeight()
                    ) {
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                            key = { habits[it].id }
                        ) { page ->
                            val habit = habits[page]
                            HabitCard(
                                habit = habit,
                                nowMs = nowMs,
                                onDelete = { pendingDeleteId = habit.id },
                                modifier = Modifier.padding(horizontal = 28.dp)
                            )
                        }
                        CarouselDots(
                            count = habits.size,
                            current = pagerState.currentPage,
                            onSelect = { scope.launch { pagerState.animateScrollToPage(it) } }
                        )
                    }
                }
            }
        }

        BloomButton(
            text = "СОЗДАТЬ ПРИВЫЧКУ",
            onClick = { showSheet = true },
            containerColor = BloomColors.Primary,
            contentColor = BloomColors.OnPrimary,
            fontWeight = FontWeight.Bold,
            shape = RoundedCornerShape(14.dp),
            contentPadding = PaddingValues(horizontal = 28.dp),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(start = 28.dp, end = 28.dp, bottom = 36.dp)
                .widthIn(max = 320.dp)
                .fillMaxWidth()
                .height(44.dp)
        )

        AnimatedVisibility(
            visible = showSheet || pendingDeleteId != null,
            enter = fadeIn(tween(280)),
            exit = fadeOut(tween(280))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x94000000))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        showSheet = false
                        pendingDeleteId = null
                    }
            )
        }

        AnimatedVisibility(
            visible = showSheet,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = slideInVertically(tween(360)) { it },
            exit = slideOutVertically(tween(360)) { it }
        ) {
            HabitSheet(
                onCreate = { title, severity ->
                    val now = System.currentTimeMillis()
                    val habit = Habit(
                        id = "habit-$now",
                        title = title,
                        severity = severity,
                        startTimeMs = now
                    )
                    habits = habits + habit
                    store.save(habits)
                    Notifications.schedule(context, habit)
                    if (Build.VERSION.SDK_INT >= 33 &&
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                    showSheet = false
                }
            )
        }

        AnimatedVisibility(
            visible = pendingDeleteId != null,
            modifier = Modifier.align(Alignment.Center),
            enter = fadeIn(tween(220)) + slideInVertically(tween(260)) { it / 8 },
            exit = fadeOut(tween(220)) + slideOutVertically(tween(260)) { it / 8 }
        ) {
            ConfirmDeleteModal(
                onYes = {
                    val id = pendingDeleteId
                    pendingDeleteId = null
                    if (id != null) {
                        habits = habits.filter { it.id != id }
                        store.save(habits)
                        Notifications.cancel(context, id)
                    }
                },
                onNo = { pendingDeleteId = null }
            )
        }
    }
}
