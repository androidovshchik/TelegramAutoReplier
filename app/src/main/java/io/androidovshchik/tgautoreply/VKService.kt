package io.androidovshchik.tgautoreply

import android.app.Service
import android.content.Intent
import com.github.androidovshchik.core.BaseService
import com.github.androidovshchik.core.utils.context.createSilentChannel
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import rf.androidovshchik.vk_bot_android_sdk.clients.Client
import rf.androidovshchik.vk_bot_android_sdk.clients.User
import rf.androidovshchik.vk_bot_android_sdk.objects.Message
import rf.androidovshchik.vk_bot_android_sdk.utils.vkapi.API
import java.util.*
import java.util.concurrent.Executors
import java.util.regex.Pattern
import android.app.PendingIntent
import android.support.v4.app.NotificationCompat
import com.github.androidovshchik.core.QUITE_CHANNEL_ID
import com.github.androidovshchik.core.utils.context.appContext
import com.github.androidovshchik.core.utils.context.nextAlarm
import com.github.androidovshchik.core.utils.context.preferences
import com.github.androidovshchik.core.utils.getString
import io.androidovshchik.tgautoreply.triggers.ServiceReceiver

class VKService : BaseService() {

    private var client: User? = null

    // this may be used to control requests per user
    private var requests: ArrayList<Int> = ArrayList()

    // custom condition and etc
    private val patternCode = Pattern.compile("^[0-9a-z]+\$", Pattern.CASE_INSENSITIVE)

    override fun onCreate() {
        super.onCreate()
        acquireWakeLock()
        Client.service = Executors.newCachedThreadPool()
        Client.scheduler = Executors.newSingleThreadScheduledExecutor()
        API.executionStarted = false
        createSilentChannel()
        nextAlarm(SERVICE_INTERVAL, ServiceReceiver::class.java)
        val intent = Intent(appContext, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(appContext, 0, intent, 0)
        startForeground(1111, NotificationCompat.Builder(appContext, QUITE_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_vk)
            .setContentTitle("Фоновая работа с VK")
            .setContentText("Бот не спит...ZZZzzzz")
            .setContentIntent(pendingIntent)
            .build())
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        disposable.add(Observable.fromCallable {
            // here may be also group instead of user
            client = User(preferences.getString(PREFERENCE_VK_ID).toInt(),
                preferences.getString(PREFERENCE_VK_TOKEN))
            client!!.enableLoggingUpdates(true)
            client!!.enableTyping(true)
            client!!.onMessage { message ->
                if (message.isMessageFromChat) {
                    Message().from(client)
                        .to(message.chatIdLong)
                        .text("Sorry, i'm currently dislike groups chat")
                        .send()
                    client!!.chat(message.chatIdLong)
                        .kickUser(client!!.id)
                    return@onMessage
                }
                val text = message.text.trim()
                if (text.length == 6 && patternCode.matcher(text).matches()) {
                    Message().from(client)
                        .to(message.authorId())
                        .text("Custom condition also works!")
                        .forwardedMessages(message.messageId)
                        .send()
                } else {
                    Message().from(client)
                        .to(message.authorId())
                        .text("It really works!!!")
                        .send()
                }
            }
        }.subscribeOn(Schedulers.io())
            .subscribe())
        return Service.START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        client?.longPoll()?.off()
        Client.service.shutdownNow()
        Client.scheduler.shutdownNow()
        Client.service = null
        Client.scheduler = null
        Client.api = null
        API.executor = null
    }
}