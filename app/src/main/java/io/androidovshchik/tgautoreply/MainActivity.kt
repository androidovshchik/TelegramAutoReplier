package io.androidovshchik.tgautoreply

import android.annotation.SuppressLint
import android.os.Bundle
import com.github.androidovshchik.support.BaseV7PActivity

@SuppressLint("ExportedPreferenceActivity")
class MainActivity : BaseV7PActivity() {

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val settings = SettingsFragment()
        fragmentManager.beginTransaction()
            .replace(android.R.id.content, settings)
            .commit()
    }
}
