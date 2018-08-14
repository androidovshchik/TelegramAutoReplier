@file:Suppress("DEPRECATION")

package io.androidovshchik.tgautoreply

import android.annotation.SuppressLint
import android.os.Bundle
import android.preference.Preference
import android.preference.PreferenceFragment
import com.github.androidovshchik.core.utils.context.*
import com.github.androidovshchik.core.utils.getBoolean

import timber.log.Timber

class SettingsFragment : PreferenceFragment() {

    @SuppressLint("BinaryOperationInTimber")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.settings)
        val context = activity.appContext
        if (context.preferences.getBoolean(PREFERENCE_ENABLE_BOT) &&
            !context.isServiceRunning(TelegramService::class.java)) {
            context.forceRestartForegroundService(TelegramService::class.java)
        }
        findPreference(PREFERENCE_ENABLE_BOT)
            .setOnPreferenceChangeListener { preference: Preference, newValue: Any ->
                Timber.d(preference.key + ": " + newValue)
                if (newValue as Boolean) {
                    context.forceRestartForegroundService(TelegramService::class.java)
                } else {
                    context.stopService(TelegramService::class.java)
                }
                true
            }
        findPreference(PREFERENCE_TELEGRAM_API_ID).onPreferenceChangeListener = Preference.OnPreferenceChangeListener {
            preference, newValue -> onPreferenceChange(preference, newValue)
        }
        findPreference(PREFERENCE_TELEGRAM_API_HASH).onPreferenceChangeListener = Preference.OnPreferenceChangeListener {
            preference, newValue -> onPreferenceChange(preference, newValue)
        }
        findPreference(PREFERENCE_TELEGRAM_PHONE).onPreferenceChangeListener = Preference.OnPreferenceChangeListener {
            preference, newValue -> onPreferenceChange(preference, newValue)
        }
    }

    @SuppressLint("BinaryOperationInTimber")
    private fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
        Timber.d(preference.key + ": " + newValue)
        if (context.preferences.getBoolean(PREFERENCE_ENABLE_BOT)) {
            context.forceRestartForegroundService(TelegramService::class.java)
        }
        return true
    }
}