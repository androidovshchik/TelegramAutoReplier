package io.androidovshchik.tgautoreply

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.support.v4.app.RemoteInput
import com.github.androidovshchik.core.BaseService
import com.github.androidovshchik.core.QUITE_CHANNEL_ID
import com.github.androidovshchik.core.utils.context.*
import com.github.androidovshchik.core.utils.getString
import io.androidovshchik.tgautoreply.triggers.ReplyReceiver
import io.androidovshchik.tgautoreply.triggers.ReplyReceiver.Companion.ACTION_REPLY
import io.androidovshchik.tgautoreply.triggers.ReplyReceiver.Companion.KEY_REPLY
import io.androidovshchik.tgautoreply.triggers.ServiceReceiver
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import org.drinkless.td.libcore.telegram.Client
import org.drinkless.td.libcore.telegram.TdApi
import timber.log.Timber
import java.util.*

class TelegramService : BaseService() {

    private var client: Client? = null

    private val replyReceiver = object : ReplyReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val code = getReplyMessage(intent)?: ""
            Timber.d("code is $code")
            notificationManager.notify(2222, getNotification())
            // also unsafe check of reply input if auth passed previously
            client!!.send(TdApi.CheckAuthenticationCode(code, null, null)) { check ->
                Timber.d(check.toString())
                if (check.constructor == TdApi.Ok.CONSTRUCTOR) {
                    onAuthPassed()
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        acquireWakeLock()
        createSilentChannel()
        nextAlarm(SERVICE_INTERVAL, ServiceReceiver::class.java)
        registerReceiver(replyReceiver, IntentFilter(ACTION_REPLY))
        startForeground(2222, getNotification())
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        disposable.add(Observable.fromCallable {
            client = Client.create({ update ->
                when (update.constructor) {
                    TdApi.UpdateNewMessage.CONSTRUCTOR -> {
                        Timber.d(update.toString())
                        val message = (update as TdApi.UpdateNewMessage).message
                        if (message.isOutgoing) {
                            // myself message
                            return@create
                        }
                        client!!.send(TdApi.GetUser(message.senderUserId)) { user ->
                            Timber.d(user.toString())
                            if ((user as TdApi.User).type.constructor != TdApi.UserTypeRegular.CONSTRUCTOR) {
                                // otherwise it may be a bot and etc
                                return@send
                            }
                            val text = "It really works!!!"
                            val messageText = TdApi.InputMessageText(TdApi.FormattedText(text, arrayOf()), true, true)
                            client!!.send(TdApi.SendMessage(message.chatId, 0, false,
                                true, null, messageText)) { send ->
                                Timber.d(send.toString())
                            }
                        }
                    }
                }
            }, null, null)
            val tdLibParams = TdApi.TdlibParameters()
            tdLibParams.apiId = preferences.getString(PREFERENCE_TELEGRAM_API_ID).toInt()
            tdLibParams.apiHash = preferences.getString(PREFERENCE_TELEGRAM_API_HASH)
            tdLibParams.databaseDirectory = cacheDir.absolutePath
            tdLibParams.filesDirectory = filesDir.absolutePath
            tdLibParams.applicationVersion = BuildConfig.VERSION_NAME
            tdLibParams.deviceModel = Build.MODEL
            tdLibParams.systemVersion = Build.VERSION.RELEASE
            tdLibParams.systemLanguageCode = Locale.getDefault().language
            client!!.send(TdApi.SetTdlibParameters(tdLibParams)) { tdLib ->
                Timber.d(tdLib.toString())
                client!!.send(TdApi.SetDatabaseEncryptionKey("androidovshchik".toByteArray())) { db ->
                    Timber.d(db.toString())
                    client!!.send(TdApi.GetAuthorizationState()) { auth ->
                        Timber.d(auth.toString())
                        when (auth.constructor) {
                            TdApi.AuthorizationStateWaitPhoneNumber.CONSTRUCTOR -> {
                                client!!.send(TdApi.SetAuthenticationPhoneNumber(preferences.getString(PREFERENCE_TELEGRAM_PHONE),
                                    true, false)) { sms ->
                                    Timber.d(sms.toString())
                                }
                            }
                            else -> {
                                // not all cases matches correct here e.g. TdApi.AuthorizationStateWaitPassword
                                onAuthPassed()
                            }
                        }
                    }
                }
            }
        }.subscribeOn(Schedulers.io())
            .subscribe())
        return Service.START_NOT_STICKY
    }

    private fun onAuthPassed() {
        Timber.d("onAuthPassed")
        // todo custom logic on authorized user
    }

    private fun getNotification(): Notification {
        val replyLabel = "Проверочный код"
        return NotificationCompat.Builder(appContext, QUITE_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_telegram)
            .setContentTitle("Фоновая работа с Telegram")
            .setContentText("Бот не спит...ZZZzzzz")
            .setContentIntent(PendingIntent.getActivity(appContext, 0,
                Intent(appContext, MainActivity::class.java), 0))
            .addAction(NotificationCompat.Action.Builder(R.drawable.ic_telegram, replyLabel,
                PendingIntent.getBroadcast(applicationContext, 0, Intent(ACTION_REPLY),
                    PendingIntent.FLAG_UPDATE_CURRENT))
                .addRemoteInput(RemoteInput.Builder(KEY_REPLY)
                    .setLabel(replyLabel)
                    .build())
                .setAllowGeneratedReplies(true)
                .build())
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(replyReceiver)
        client?.close()
    }
}