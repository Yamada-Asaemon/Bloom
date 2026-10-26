package com.bloom.app

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import kotlin.math.floor

const val CHANNEL_ID = "milestones"
private const val DAY_MS = 86_400_000L

fun ensureChannel(ctx: Context) {
    if (Build.VERSION.SDK_INT >= 26) {
        val channel = NotificationChannel(CHANNEL_ID, "Достижения", NotificationManager.IMPORTANCE_DEFAULT)
        channel.description = "Уведомления о достигнутых целях"
        ctx.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }
}

// Уведомления: через 1 день, затем каждые 7 дней (7, 14, 21, 28...)
fun nextNotifyDay(afterDays: Double): Int =
    if (afterDays < 1.0) 1 else (floor(afterDays / 7).toInt() + 1) * 7

private fun daysWord(n: Int): String {
    val m100 = n % 100
    val m10 = n % 10
    return when {
        m100 in 11..14 -> "дней"
        m10 == 1 -> "день"
        m10 in 2..4 -> "дня"
        else -> "дней"
    }
}

private fun alarmIntent(ctx: Context, startTime: Long, name: String, days: Int): PendingIntent {
    val intent = Intent(ctx, MilestoneReceiver::class.java)
        .putExtra("name", name)
        .putExtra("start", startTime)
        .putExtra("days", days)
    return PendingIntent.getBroadcast(
        ctx,
        startTime.hashCode(),
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
}

fun scheduleNext(
    ctx: Context,
    habit: Habit,
    afterDays: Double = (System.currentTimeMillis() - habit.startTime) / DAY_MS.toDouble()
) {
    val days = nextNotifyDay(afterDays.coerceAtLeast(0.0))
    val triggerAt = habit.startTime + days * DAY_MS
    val am = ctx.getSystemService(AlarmManager::class.java)
    am.setAndAllowWhileIdle(
        AlarmManager.RTC_WAKEUP,
        triggerAt,
        alarmIntent(ctx, habit.startTime, habit.name, days)
    )
}

fun scheduleAll(ctx: Context, habits: List<Habit>) {
    habits.forEach { scheduleNext(ctx, it) }
}

fun cancelFor(ctx: Context, habit: Habit) {
    val am = ctx.getSystemService(AlarmManager::class.java)
    am.cancel(alarmIntent(ctx, habit.startTime, habit.name, 0))
}

// Круглая аватарка из app_icon.png
private fun avatar(ctx: Context): Bitmap {
    val size = 192
    val out = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)

    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeResource(ctx.resources, R.drawable.app_icon, bounds)
    var sample = 1
    while (bounds.outWidth / (sample * 2) >= size && bounds.outHeight / (sample * 2) >= size) sample *= 2

    val src = BitmapFactory.decodeResource(
        ctx.resources,
        R.drawable.app_icon,
        BitmapFactory.Options().apply { inSampleSize = sample }
    ) ?: return out

    val canvas = Canvas(out)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)
    paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
    val side = minOf(src.width, src.height)
    val srcRect = Rect(
        (src.width - side) / 2, (src.height - side) / 2,
        (src.width + side) / 2, (src.height + side) / 2
    )
    canvas.drawBitmap(src, srcRect, Rect(0, 0, size, size), paint)
    return out
}

class MilestoneReceiver : BroadcastReceiver() {
    override fun onReceive(ctx: Context, intent: Intent) {
        val start = intent.getLongExtra("start", 0L)
        val days = intent.getIntExtra("days", 0)
        if (days <= 0) return

        // если привычку уже удалили — ничего не делаем
        val habit = loadHabits(ctx).firstOrNull { it.startTime == start } ?: return

        ensureChannel(ctx)

        val text = "Поздравляю! Вы держитесь $days ${daysWord(days)}!"

        val views = RemoteViews(ctx.packageName, R.layout.notification_milestone)
        views.setImageViewBitmap(R.id.avatar, avatar(ctx))
        views.setTextViewText(R.id.message, text)

        val builder = NotificationCompat.Builder(ctx, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notif)
            .setContentText(text)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomContentView(views)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        ctx.packageManager.getLaunchIntentForPackage(ctx.packageName)?.let { open ->
            builder.setContentIntent(
                PendingIntent.getActivity(
                    ctx, 0, open,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            )
        }

        ctx.getSystemService(NotificationManager::class.java)
            .notify(start.hashCode() + days, builder.build())

        // следующее уведомление
        scheduleNext(ctx, habit, days.toDouble())
    }
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(ctx: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            scheduleAll(ctx, loadHabits(ctx))
        }
    }
}
