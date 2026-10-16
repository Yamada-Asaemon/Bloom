package com.bloom.app

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

@Composable
fun BloomApp(vm: BloomViewModel) {
    val c = if (vm.isLight) LightColors else DarkColors
    val lang = vm.lang

    val view = LocalView.current
    SideEffect {
        val window = (view.context as Activity).window
        val controller = WindowCompat.getInsetsController(window, view)
        controller.isAppearanceLightStatusBars = vm.isLight
        controller.isAppearanceLightNavigationBars = vm.isLight
    }

    var menuOpen by rememberSaveable { mutableStateOf(false) }
    var langOpen by rememberSaveable { mutableStateOf(false) }
    var addOpen by rememberSaveable { mutableStateOf(false) }
    var infoOpen by rememberSaveable { mutableStateOf(false) }
    var deleteOpen by rememberSaveable { mutableStateOf(false) }
    var nameInput by rememberSaveable { mutableStateOf("") }
    var nameError by rememberSaveable { mutableStateOf(false) }
    var selectedColor by rememberSaveable { mutableStateOf(0) } // 0 = цвет не выбран

    val focusManager = LocalFocusManager.current

    val closeAdd: () -> Unit = {
        addOpen = false
        nameInput = ""
        nameError = false
        selectedColor = 0
        focusManager.clearFocus()
    }

    val closeMenu: () -> Unit = {
        menuOpen = false
        langOpen = false
    }

    val submit: () -> Unit = {
        val name = nameInput.trim()
        if (name.isNotEmpty()) {
            vm.addHabit(name, if (selectedColor != 0) selectedColor else HABIT_DEFAULT)
            closeAdd()
        }
    }

    BackHandler(enabled = deleteOpen || infoOpen || menuOpen || addOpen) {
        when {
            deleteOpen -> deleteOpen = false
            infoOpen -> infoOpen = false
            menuOpen -> closeMenu()
            addOpen -> closeAdd()
            else -> {}
        }
    }

    CompositionLocalProvider(LocalBloom provides c) {
        Box(
            Modifier
                .fillMaxSize()
                .background(c.bg)
        ) {
            // ===== Основной экран =====
            Box(
                Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
            ) {
                val habit = vm.habits.getOrNull(vm.currentIndex)
                if (habit == null) {
                    EmptyScreen(lang)
                } else {
                    HabitScreen(
                        habit = habit,
                        lang = lang,
                        index = vm.currentIndex,
                        count = vm.habits.size,
                        onMenu = { menuOpen = true },
                        onDelete = { deleteOpen = true },
                        onPrev = { vm.prev() },
                        onNext = { vm.next() }
                    )
                }

                BloomButton(
                    text = tr(lang, "addHabit"),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 40.dp)
                        .fillMaxWidth(0.6f),
                    onClick = { addOpen = true }
                )
            }

            // ===== Модалка добавления привычки =====
            Scrim(visible = addOpen, alpha = 0.6f) { closeAdd() }
            AnimatedVisibility(
                visible = addOpen,
                modifier = Modifier.align(Alignment.BottomCenter),
                enter = slideInVertically(tween(350)) { it },
                exit = slideOutVertically(tween(350)) { it }
            ) {
                AddSheet(
                    lang = lang,
                    name = nameInput,
                    showError = nameError,
                    selectedColor = selectedColor,
                    onNameChange = {
                        if (it.length > 25) {
                            nameInput = it.substring(0, 25)
                            nameError = true
                        } else {
                            nameInput = it
                            nameError = false
                        }
                    },
                    onColor = { selectedColor = it },
                    onInfo = { infoOpen = true },
                    onSubmit = submit
                )
            }

            // ===== Модалка информации о цветах =====
            Scrim(visible = infoOpen, alpha = 0.6f) { infoOpen = false }
            AnimatedVisibility(
                visible = infoOpen,
                modifier = Modifier.align(Alignment.Center),
                enter = fadeIn(tween(250)) + scaleIn(tween(250), initialScale = 0.9f),
                exit = fadeOut(tween(250)) + scaleOut(tween(250), targetScale = 0.9f)
            ) {
                InfoCard(lang)
            }

            // ===== Боковое меню =====
            Scrim(visible = menuOpen, alpha = 0.5f) { closeMenu() }
            AnimatedVisibility(
                visible = menuOpen,
                modifier = Modifier.align(Alignment.CenterStart),
                enter = slideInHorizontally(tween(300)) { -it },
                exit = slideOutHorizontally(tween(300)) { -it }
            ) {
                SideMenu(
                    lang = lang,
                    isLight = vm.isLight,
                    langOpen = langOpen,
                    onToggleTheme = { vm.toggleTheme() },
                    onToggleLang = { langOpen = !langOpen },
                    onPickLang = {
                        vm.setLanguage(it)
                        langOpen = false
                    }
                )
            }

            // ===== Подтверждение удаления =====
            Scrim(visible = deleteOpen, alpha = 0.6f) { deleteOpen = false }
            AnimatedVisibility(
                visible = deleteOpen,
                modifier = Modifier.align(Alignment.Center),
                enter = fadeIn(tween(250)) + scaleIn(tween(250), initialScale = 0.9f),
                exit = fadeOut(tween(250)) + scaleOut(tween(250), targetScale = 0.9f)
            ) {
                DeleteCard(
                    lang = lang,
                    onYes = {
                        vm.deleteCurrent()
                        deleteOpen = false
                    },
                    onNo = { deleteOpen = false }
                )
            }
        }
    }
}

// ===== Добавление привычки =====

@Composable
private fun AddSheet(
    lang: String,
    name: String,
    showError: Boolean,
    selectedColor: Int,
    onNameChange: (String) -> Unit,
    onColor: (Int) -> Unit,
    onInfo: () -> Unit,
    onSubmit: () -> Unit
) {
    val c = LocalBloom.current
    var focused by remember { mutableStateOf(false) }
    val inputShape = RoundedCornerShape(12.dp)

    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(c.panel)
            .noRipple {}
            .windowInsetsPadding(WindowInsets.navigationBars.union(WindowInsets.ime))
            .padding(30.dp)
    ) {
        BText(tr(lang, "habitName"), modifier = Modifier.padding(bottom = 12.dp))

        BasicTextField(
            value = name,
            onValueChange = onNameChange,
            singleLine = true,
            textStyle = TextStyle(color = c.text, fontSize = 13.sp, letterSpacing = 1.5.sp),
            cursorBrush = SolidColor(c.accent),
            modifier = Modifier.onFocusChanged { focused = it.isFocused },
            decorationBox = { inner ->
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(inputShape)
                        .background(Color(0x26808080))
                        .border(1.dp, if (focused) c.accent else Color(0xFF888888), inputShape)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    if (name.isEmpty()) {
                        BText(tr(lang, "enterName"), color = c.muted)
                    }
                    inner()
                }
            }
        )

        if (showError) {
            BText(
                tr(lang, "charLimit"),
                modifier = Modifier.padding(top = 8.dp),
                size = 10,
                color = Color(0xFFFF4444),
                spacing = 1f
            )
        }

        Row(
            Modifier.padding(top = 25.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            BText(tr(lang, "color"))
            Ico(R.drawable.ic_info, c.text, 14, Modifier.noRipple(onInfo))
        }

        Row(horizontalArrangement = Arrangement.spacedBy(15.dp)) {
            listOf(HABIT_RED, HABIT_YELLOW, HABIT_GREEN).forEach { col ->
                val selected = selectedColor == col
                Box(
                    Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color(col))
                        .border(2.dp, if (selected) c.accent else Color.Transparent, CircleShape)
                        .noRipple { onColor(col) }
                )
            }
        }

        BloomButton(
            text = tr(lang, "add"),
            modifier = Modifier
                .padding(top = 25.dp)
                .fillMaxWidth(0.6f)
                .align(Alignment.CenterHorizontally),
            onClick = onSubmit
        )
    }
}

// ===== Информация о цветах =====

@Composable
private fun InfoCard(lang: String) {
    val c = LocalBloom.current
    Column(
        Modifier
            .fillMaxWidth(0.65f)
            .clip(RoundedCornerShape(14.dp))
            .background(c.panel)
            .noRipple {}
            .padding(start = 30.dp, end = 30.dp, top = 24.dp, bottom = 30.dp)
    ) {
        BText(tr(lang, "infoTitle"), modifier = Modifier.padding(bottom = 20.dp))
        InfoRow(HABIT_RED, tr(lang, "strongDep"))
        Spacer(Modifier.height(16.dp))
        InfoRow(HABIT_YELLOW, tr(lang, "mediumDep"))
        Spacer(Modifier.height(16.dp))
        InfoRow(HABIT_GREEN, tr(lang, "lightDep"))
    }
}

@Composable
private fun InfoRow(color: Int, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            Modifier
                .size(14.dp)
                .clip(CircleShape)
                .background(Color(color))
        )
        BText("—", size = 12)
        BText(text, size = 12, spacing = 1.2f)
    }
}

// ===== Удаление =====

@Composable
private fun DeleteCard(lang: String, onYes: () -> Unit, onNo: () -> Unit) {
    val c = LocalBloom.current
    Column(
        Modifier
            .fillMaxWidth(0.8f)
            .widthIn(max = 340.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(c.panel)
            .noRipple {}
            .padding(start = 26.dp, end = 26.dp, top = 24.dp, bottom = 22.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BText(
            tr(lang, "deleteConfirm"),
            modifier = Modifier.padding(bottom = 22.dp),
            size = 14,
            spacing = 1.2f,
            align = TextAlign.Center
        )
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            BloomButton(
                tr(lang, "yes"),
                modifier = Modifier.weight(1f),
                radius = 12,
                vertical = 12,
                horizontal = 18,
                onClick = onYes
            )
            BloomButton(
                tr(lang, "no"),
                modifier = Modifier.weight(1f),
                radius = 12,
                vertical = 12,
                horizontal = 18,
                onClick = onNo
            )
        }
    }
}

// ===== Боковое меню =====

@Composable
private fun SideMenu(
    lang: String,
    isLight: Boolean,
    langOpen: Boolean,
    onToggleTheme: () -> Unit,
    onToggleLang: () -> Unit,
    onPickLang: (String) -> Unit
) {
    val c = LocalBloom.current
    val rowBg = Color(0x1A808080)
    val arrow by animateFloatAsState(if (langOpen) 180f else 0f, tween(250), label = "arrow")

    Column(
        Modifier
            .fillMaxHeight()
            .fillMaxWidth(0.75f)
            .widthIn(max = 320.dp)
            .background(c.panel)
            .noRipple {}
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 30.dp)
    ) {
        BText(tr(lang, "theme"), size = 16, weight = FontWeight.SemiBold, spacing = 2f)
        Spacer(Modifier.height(16.dp))
        BloomLine(Modifier.fillMaxWidth())
        Spacer(Modifier.height(16.dp))

        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(rowBg)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Ico(R.drawable.ic_moon, c.text.copy(alpha = 0.9f), 20)
                BText("/", size = 14, color = c.text.copy(alpha = 0.5f), spacing = 0f)
                Ico(R.drawable.ic_sun, c.text.copy(alpha = 0.9f), 20)
            }
            ThemeSwitch(on = isLight, onClick = onToggleTheme)
        }

        Spacer(Modifier.height(24.dp))

        BText(tr(lang, "language"), size = 16, weight = FontWeight.SemiBold, spacing = 2f)
        Spacer(Modifier.height(16.dp))
        BloomLine(Modifier.fillMaxWidth())
        Spacer(Modifier.height(16.dp))

        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(rowBg)
                .noRipple(onToggleLang)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            BText(languageNames[lang] ?: "русский", size = 14)
            Ico(
                R.drawable.ic_chevron_down,
                c.text.copy(alpha = 0.8f),
                16,
                Modifier.rotate(arrow)
            )
        }

        AnimatedVisibility(
            visible = langOpen,
            enter = expandVertically(tween(300)) + fadeIn(tween(300)),
            exit = shrinkVertically(tween(300)) + fadeOut(tween(300))
        ) {
            Column(Modifier.padding(top = 8.dp)) {
                languageNames.filterKeys { it != lang }.forEach { (code, label) ->
                    Box(
                        Modifier
                            .padding(bottom = 8.dp)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(rowBg)
                            .noRipple { onPickLang(code) }
                            .padding(horizontal = 18.dp, vertical = 14.dp)
                    ) {
                        BText(label)
                    }
                }
            }
        }
    }
}
