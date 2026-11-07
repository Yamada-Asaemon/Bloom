package com.bloom.app

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID

data class Habit(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val color: Long,
    val createdAt: Long = System.currentTimeMillis(),
    val imagePath: String? = null
)

object Store {
    private const val PREFS = "bloom"
    private const val KEY = "habits"

    fun load(ctx: Context): List<Habit> {
        val raw = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY, null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).map {
                val o = arr.getJSONObject(it)
                Habit(
                    id = o.getString("id"),
                    name = o.getString("name"),
                    color = o.getLong("color"),
                    createdAt = o.getLong("createdAt"),
                    imagePath = if (o.isNull("imagePath")) null else o.getString("imagePath")
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun save(ctx: Context, list: List<Habit>) {
        val arr = JSONArray()
        list.forEach {
            arr.put(JSONObject().apply {
                put("id", it.id)
                put("name", it.name)
                put("color", it.color)
                put("createdAt", it.createdAt)
                put("imagePath", it.imagePath ?: JSONObject.NULL)
            })
        }
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY, arr.toString()).apply()
    }

    /** Копирует выбранную картинку во внутреннюю память (уменьшая её) и возвращает путь. */
    fun saveImage(ctx: Context, uri: Uri, habitId: String): String? {
        return try {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            ctx.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
            var sample = 1
            while (bounds.outWidth / sample > 1024 || bounds.outHeight / sample > 1024) sample *= 2
            val bmp = ctx.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample })
            } ?: return null
            val file = File(ctx.filesDir, "habit_${habitId}_${System.currentTimeMillis()}.jpg")
            file.outputStream().use { bmp.compress(Bitmap.CompressFormat.JPEG, 90, it) }
            file.absolutePath
        } catch (e: Exception) {
            null
        }
    }
}
