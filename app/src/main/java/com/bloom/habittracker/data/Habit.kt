package com.bloom.habittracker.data

import android.content.Context
import androidx.compose.ui.graphics.Color
import org.json.JSONArray
import org.json.JSONObject

enum class Severity(val key: String, val color: Color) {
    RED("red", Color(0xFFF28B82)),
    YELLOW("yellow", Color(0xFFFDD663)),
    GREEN("green", Color(0xFF81C995));

    companion object {
        fun fromKey(key: String?): Severity? = entries.firstOrNull { it.key == key }
    }
}

data class Habit(
    val id: String,
    val title: String,
    val severity: Severity?,
    val startTimeMs: Long
) {
    val color: Color get() = severity?.color ?: Color.White
}

class HabitStore(context: Context) {
    private val prefs = context.applicationContext
        .getSharedPreferences("bloom_habits", Context.MODE_PRIVATE)

    fun load(): List<Habit> {
        val raw = prefs.getString(KEY, null) ?: return emptyList()
        return try {
            val array = JSONArray(raw)
            List(array.length()) { i ->
                val o = array.getJSONObject(i)
                Habit(
                    id = o.getString("id"),
                    title = o.getString("title"),
                    severity = Severity.fromKey(o.optString("severity")),
                    startTimeMs = o.getLong("startTimeMs")
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun save(habits: List<Habit>) {
        val array = JSONArray()
        habits.forEach { h ->
            array.put(
                JSONObject().apply {
                    put("id", h.id)
                    put("title", h.title)
                    put("severity", h.severity?.key ?: "")
                    put("startTimeMs", h.startTimeMs)
                }
            )
        }
        prefs.edit().putString(KEY, array.toString()).apply()
    }

    private companion object {
        const val KEY = "habits"
    }
}
