package com.bloom.app

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

object Milestones {
    const val DAY = 24L * 60 * 60 * 1000

    /** Следующая веха в днях: 1, 7, 14, 21, 28 ... */
    fun next(elapsedMs: Long): Int {
        val d = (elapsedMs / DAY).toInt()
        return if (d < 1) 1 else (d / 7 + 1) * 7
    }
}

fun daysWord(n: Int): String {
    val m10 = n % 10
    val m100 = n % 100
    return when {
        m10 == 1 && m100 != 11 -> "день"
        m10 in 2..4 && m100 !in 12..14 -> "дня"
        else -> "дней"
    }
}

object Notifier {
    private const val CHANNEL = "milestones"

    fun ensureChannel(ctx: Context) {
        if (Build.VERSION.SDK_INT >= 26) {
            ctx.getSystemService(NotificationManager::class.java).createNotificationChannel(
                NotificationChannel(CHANNEL, "Достижения", NotificationManager.IMPORTANCE_DEFAULT)
            )
        }
    }

    private fun pending(ctx: Context, h: Habit, days: Int): PendingIntent {
        val i = Intent(ctx, MilestoneReceiver::class.java)
            .putExtra("id", h.id)
            .putExtra("days", days)
        return PendingIntent.getBroadcast(
            ctx, h.id.hashCode(), i,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun schedule(ctx: Context, h: Habit, now: Long = System.currentTimeMillis()) {
        val days = Milestones.next(now - h.createdAt)
        val at = h.createdAt + days * Milestones.DAY
        ctx.getSystemService(AlarmManager::class.java)
            .setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pending(ctx, h, days))
    }

    fun cancel(ctx: Context, h: Habit) {
        ctx.getSystemService(AlarmManager::class.java).cancel(pending(ctx, h, 0))
    }

    fun show(ctx: Context, h: Habit, days: Int) {
        ensureChannel(ctx)
        val open = PendingIntent.getActivity(
            ctx, 0, Intent(ctx, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE
        )
        val n = NotificationCompat.Builder(ctx, CHANNEL)
            .setSmallIcon(R.drawable.ic_lotus)
            .setContentTitle("Поздравляю!")
            .setContentText("Вы держитесь $days ${daysWord(days)}.")
            .setContentIntent(open)
            .setAutoCancel(true)
            .build()
        ctx.getSystemService(NotificationManager::class.java).notify(h.id.hashCode() + days, n)
    }
}

class MilestoneReceiver : BroadcastReceiver() {
    override fun onReceive(ctx: Context, intent: Intent) {
        val id = intent.getStringExtra("id") ?: return
        val days = intent.getIntExtra("days", 0)
        val habit = Store.load(ctx).firstOrNull { it.id == id } ?: return
        Notifier.show(ctx, habit, days)
        Notifier.schedule(ctx, habit)
    }
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(ctx: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Store.load(ctx).forEach { Notifier.schedule(ctx, it) }
        }
    }
}
