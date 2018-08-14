package io.androidovshchik.tgautoreply.triggers

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.github.androidovshchik.core.utils.context.forceRestartForegroundService
import io.androidovshchik.tgautoreply.VKService
import timber.log.Timber

class RebootReceiver : BroadcastReceiver() {

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    override fun onReceive(context: Context, intent: Intent?) {
        Timber.d("RebootReceiver")
        context.forceRestartForegroundService(VKService::class.java)
    }
}