package com.bloom.app

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.floor

// Цвета привычек = сила зависимости
val HABIT_RED: Int = 0xFFFF5C5C.toInt()
val HABIT_YELLOW: Int = 0xFFFFD700.toInt()
val HABIT_GREEN: Int = 0xFF5CFF5C.toInt()
val HABIT_DEFAULT: Int = 0xFFFFFFFF.toInt()

data class Habit(val name: String, val color: Int, val startTime: Long)

data class CycleInfo(val from: Int, val to: Int)

private val milestones = listOf(1, 7, 14, 30, 60, 90, 120, 150, 180, 210, 240, 270, 300, 330, 365)

/** Вехи круга: 1/7/14/30/60/90/…/365 дней, дальше каждые 30. */
fun getCycleInfo(elapsedDays: Double): CycleInfo {
    if (elapsedDays < 1) return CycleInfo(0, 1)
    for (i in milestones.indices) {
        if (elapsedDays < milestones[i]) {
            return CycleInfo(milestones[i - 1], milestones[i])
        }
    }
    val daysAfterYear = elapsedDays - 365
    val extraCycles = floor(daysAfterYear / 30).toInt()
    val from = 365 + extraCycles * 30
    return CycleInfo(from, from + 30)
}

data class TimeParts(val d: String, val h: String, val m: String, val s: String)

fun getTimeParts(ms: Long): TimeParts {
    val total = ms / 1000
    return TimeParts(
        d = (total / 86400).toString().padStart(2, '0'),
        h = ((total % 86400) / 3600).toString().padStart(2, '0'),
        m = ((total % 3600) / 60).toString().padStart(2, '0'),
        s = (total % 60).toString().padStart(2, '0')
    )
}

fun formatDateTime(ts: Long): String =
    SimpleDateFormat("dd.MM.yyyy // HH:mm", Locale.ROOT).format(Date(ts))

class BloomViewModel(app: Application) : AndroidViewModel(app) {

    private val prefs = app.getSharedPreferences("bloom", Context.MODE_PRIVATE)

    val habits = mutableStateListOf<Habit>()

    var currentIndex by mutableIntStateOf(0)
        private set

    var isLight by mutableStateOf(prefs.getBoolean(KEY_THEME, false))
        private set

    var lang by mutableStateOf(prefs.getString(KEY_LANG, "ru") ?: "ru")
        private set

    init {
        load()
    }

    private fun load() {
        try {
            val raw = prefs.getString(KEY_HABITS, null) ?: return
            val arr = JSONArray(raw)
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                habits.add(Habit(o.getString("name"), o.getInt("color"), o.getLong("startTime")))
            }
            currentIndex = if (habits.isEmpty()) 0 else habits.size - 1
        } catch (e: Exception) {
            habits.clear()
            currentIndex = 0
        }
    }

    private fun save() {
        val arr = JSONArray()
        habits.forEach {
            arr.put(
                JSONObject()
                    .put("name", it.name)
                    .put("color", it.color)
                    .put("startTime", it.startTime)
            )
        }
        prefs.edit().putString(KEY_HABITS, arr.toString()).apply()
    }

    fun addHabit(name: String, color: Int) {
        habits.add(Habit(name, color, System.currentTimeMillis()))
        currentIndex = habits.size - 1
        save()
    }

    fun deleteCurrent() {
        if (habits.isEmpty()) return
        habits.removeAt(currentIndex)
        currentIndex = when {
            habits.isEmpty() -> 0
            currentIndex >= habits.size -> habits.size - 1
            else -> currentIndex
        }
        save()
    }

    fun next() {
        if (currentIndex < habits.size - 1) currentIndex++
    }

    fun prev() {
        if (currentIndex > 0) currentIndex--
    }

    fun toggleTheme() {
        isLight = !isLight
        prefs.edit().putBoolean(KEY_THEME, isLight).apply()
    }

    fun setLanguage(code: String) {
        lang = code
        prefs.edit().putString(KEY_LANG, code).apply()
    }

    private companion object {
        const val KEY_HABITS = "bloom_habits_list"
        const val KEY_THEME = "light_theme"
        const val KEY_LANG = "bloom_language"
    }
}
