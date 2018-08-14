package io.androidovshchik.tgautoreply.triggers

import android.content.BroadcastReceiver
import android.content.Intent
import android.support.v4.app.RemoteInput

abstract class ReplyReceiver : BroadcastReceiver() {

    @Suppress("IfThenToSafeAccess")
    fun getReplyMessage(intent: Intent): String? {
        val bundle = RemoteInput.getResultsFromIntent(intent)
        return if (bundle == null) {
            null
        } else {
            bundle.getCharSequence(KEY_REPLY).toString()
        }
    }

    companion object {

        const val KEY_REPLY = "keyReply"

        const val ACTION_REPLY = "actionReply"
    }
}