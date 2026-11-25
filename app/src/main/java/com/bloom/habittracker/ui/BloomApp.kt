package com.bloom.habittracker.ui

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.bloom.habittracker.data.Habit
import com.bloom.habittracker.data.HabitStore

@Composable
fun BloomApp(store: HabitStore) {
    var habits by remember { mutableStateOf(store.load()) }
    var showSheet by remember { mutableStateOf(false) }

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
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.Center
            ) {
                if (habits.isEmpty()) {
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
                } else {
                    // Временно: заменим на экран привычки на этапе 3
                    val habit = habits.last()
                    Text(
                        text = habit.title,
                        color = habit.color,
                        fontFamily = Montserrat,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.06.em
                    )
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
            visible = showSheet,
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
                    ) { showSheet = false }
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
                    habits = habits + Habit(
                        id = "habit-$now",
                        title = title,
                        severity = severity,
                        startTimeMs = now
                    )
                    store.save(habits)
                    showSheet = false
                }
            )
        }
    }
}
