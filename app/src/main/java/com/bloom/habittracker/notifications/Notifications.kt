package com.bloom.habittracker.notifications

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.bloom.habittracker.MainActivity
import com.bloom.habittracker.R
import com.bloom.habittracker.data.Habit

object Notifications {
    const val EXTRA_HABIT_ID = "habit_id"
    const val EXTRA_DAYS = "days"

    private const val CHANNEL_ID = "bloom_milestones"
    private const val HOUR_MS = 3_600_000L
    private const val WEEK_MS = 7 * 24 * HOUR_MS

    /** Возвращает время ближайшего уведомления и число дней (0 = первый час). */
    fun nextMilestone(startMs: Long, nowMs: Long): Pair<Long, Int> {
        val first = startMs + HOUR_MS
        if (nowMs < first) return first to 0
        val weeks = (nowMs - startMs) / WEEK_MS + 1
        return (startMs + weeks * WEEK_MS) to (weeks * 7).toInt()
    }

    fun schedule(context: Context, habit: Habit) {
        val (triggerAt, days) = nextMilestone(habit.startTimeMs, System.currentTimeMillis())
        val intent = Intent(context, MilestoneReceiver::class.java)
            .putExtra(EXTRA_HABIT_ID, habit.id)
            .putExtra(EXTRA_DAYS, days)
        val pending = PendingIntent.getBroadcast(
            context,
            habit.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        context.getSystemService(AlarmManager::class.java)
            .setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pending)
    }

    fun cancel(context: Context, habitId: String) {
        val pending = PendingIntent.getBroadcast(
            context,
            habitId.hashCode(),
            Intent(context, MilestoneReceiver::class.java),
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        ) ?: return
        context.getSystemService(AlarmManager::class.java).cancel(pending)
        pending.cancel()
    }

    fun rescheduleAll(context: Context, habits: List<Habit>) {
        habits.forEach { schedule(context, it) }
    }

    @SuppressLint("MissingPermission")
    fun show(context: Context, habit: Habit, days: Int) {
        val manager = NotificationManagerCompat.from(context)
        if (!manager.areNotificationsEnabled()) return
        ensureChannel(context)

        val openApp = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_bloom)
            .setLargeIcon(loadLargeIcon(context))
            .setContentText(message(days))
            .setContentIntent(openApp)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        manager.notify("${habit.id}-$days".hashCode(), notification)
    }

    private fun message(days: Int): String =
        if (days == 0) "Поздравляю! Ты продержался 1 час 🌴"
        else "Поздравляю! Ты продержался $days ${dayWord(days)} 🌴"

    private fun dayWord(n: Int): String {
        val mod10 = n % 10
        val mod100 = n % 100
        return when {
            mod10 == 1 && mod100 != 11 -> "день"
            mod10 in 2..4 && mod100 !in 12..14 -> "дня"
            else -> "дней"
        }
    }

    private fun ensureChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Поздравления",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        context.getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)
    }

    private fun loadLargeIcon(context: Context): Bitmap? {
        val res = context.resources
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeResource(res, R.drawable.app_icon, bounds)
        var sample = 1
        while (bounds.outWidth / (sample * 2) >= 256 && bounds.outHeight / (sample * 2) >= 256) {
            sample *= 2
        }
        val options = BitmapFactory.Options().apply { inSampleSize = sample }
        return BitmapFactory.decodeResource(res, R.drawable.app_icon, options)
    }
}
