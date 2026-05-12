package com.wod.app.ui.util

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext

/**
 * Forces portrait orientation for the duration of the calling composable's
 * lifetime, then restores the previous setting on disposal.
 *
 * Usage: call at the top of any screen that must stay in portrait.
 */
@Composable
fun LockPortrait() {
    val context = LocalContext.current
    DisposableEffect(Unit) {
        val activity = context as? Activity ?: return@DisposableEffect onDispose {}
        val previous = activity.requestedOrientation
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        onDispose {
            activity.requestedOrientation = previous
        }
    }
}
