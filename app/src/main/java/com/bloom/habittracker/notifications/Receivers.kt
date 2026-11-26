package com.bloom.habittracker.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.bloom.habittracker.data.HabitStore

class MilestoneReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val habitId = intent.getStringExtra(Notifications.EXTRA_HABIT_ID) ?: return
        val days = intent.getIntExtra(Notifications.EXTRA_DAYS, 0)
        val habit = HabitStore(context).load().firstOrNull { it.id == habitId } ?: return
        Notifications.show(context, habit, days)
        Notifications.schedule(context, habit)
    }
}

class RescheduleReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            Notifications.rescheduleAll(context, HabitStore(context).load())
        }
    }
}
