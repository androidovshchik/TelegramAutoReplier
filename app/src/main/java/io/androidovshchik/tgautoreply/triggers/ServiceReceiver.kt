package io.androidovshchik.tgautoreply.triggers

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.github.androidovshchik.core.utils.context.forceRestartForegroundService
import com.github.androidovshchik.core.utils.context.preferences
import com.github.androidovshchik.core.utils.getBoolean
import io.androidovshchik.tgautoreply.PREFERENCE_ENABLE_BOT
import io.androidovshchik.tgautoreply.TelegramService
import timber.log.Timber

class ServiceReceiver : BroadcastReceiver() {

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    override fun onReceive(context: Context, intent: Intent?) {
        Timber.d("ServiceReceiver")
        if (context.preferences.getBoolean(PREFERENCE_ENABLE_BOT)) {
            context.forceRestartForegroundService(TelegramService::class.java)
        }
    }
}