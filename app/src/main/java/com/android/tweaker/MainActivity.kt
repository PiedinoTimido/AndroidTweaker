package com.android.tweaker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.android.tweaker.data.AdbManager
import com.android.tweaker.data.PreferencesManager
import com.android.tweaker.ui.AndroidTweakerApp
import com.android.tweaker.ui.theme.AndroidTweakerTheme

class MainActivity : ComponentActivity() {

    private lateinit var adbManager: AdbManager
    private lateinit var prefs: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        adbManager = AdbManager()
        prefs = PreferencesManager(this)

        setContent {
            AndroidTweakerTheme {
                AndroidTweakerApp(
                    adbManager = adbManager,
                    prefs = prefs
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        adbManager.disconnect()
    }
}