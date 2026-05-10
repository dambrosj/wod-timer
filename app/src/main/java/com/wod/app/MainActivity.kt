package com.wod.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wod.app.ui.WodNavGraph
import com.wod.app.ui.theme.LocalWindowSizeClass
import com.wod.app.ui.theme.WodTheme

class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val preferencesRepository = (application as WodApp).preferencesRepository
        setContent {
            val themeMode by preferencesRepository.themeModeFlow
                .collectAsStateWithLifecycle(initialValue = com.wod.app.ui.theme.ThemeMode.DARK)
            val windowSizeClass = calculateWindowSizeClass(this)
            CompositionLocalProvider(LocalWindowSizeClass provides windowSizeClass) {
                WodTheme(themeMode = themeMode) {
                    Scaffold { padding ->
                        Box(Modifier.fillMaxSize().padding(padding)) {
                            WodNavGraph()
                        }
                    }
                }
            }
        }
    }
}
