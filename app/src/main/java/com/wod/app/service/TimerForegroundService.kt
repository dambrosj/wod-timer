package com.wod.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.wod.app.MainActivity
import com.wod.app.R
import com.wod.app.WodApp
import com.wod.app.domain.engine.AmrapEngine
import com.wod.app.domain.engine.CustomEngine
import com.wod.app.domain.engine.EmomEngine
import com.wod.app.domain.engine.ForTimeEngine
import com.wod.app.domain.engine.TabataEngine
import com.wod.app.domain.engine.TimerEngine
import com.wod.app.domain.model.TimerConfig
import com.wod.app.domain.model.TimerPhase
import com.wod.app.domain.model.TimerType
import com.wod.app.domain.model.WorkoutLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Keeps the active timer alive when the screen is off or the app is backgrounded.
 *
 * Command flow:
 *   → [ACTION_START]   : deserialise TimerConfig, create engine, start collection jobs
 *   → [ACTION_PAUSE]   : engine.pause()
 *   → [ACTION_RESUME]  : engine.resume()
 *   → [ACTION_SKIP]    : engine.skip()
 *   → [ACTION_STOP]    : stopSelf()
 *
 * The service holds a PARTIAL_WAKE_LOCK for the duration of the workout so
 * the coroutine ticker keeps firing with the screen off.
 */
class TimerForegroundService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var engine: TimerEngine? = null
    private var engineJobs: Job? = null
    private var wakeLock: PowerManager.WakeLock? = null

    // Workout tracking for CompletionScreen
    private var startTimeMs: Long = 0L
    private var currentConfigType: String = ""
    private var currentConfigJson: String = ""

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        ensureChannel()
        when (intent?.action) {
            ACTION_START  -> handleStart(intent)
            ACTION_PAUSE  -> engine?.pause()
            ACTION_RESUME -> engine?.resume()
            ACTION_SKIP   -> engine?.skip()
            ACTION_STOP   -> stopSelf()
            else          -> startForeground(NOTIF_ID, buildNotification(getString(R.string.app_name)))
        }
        return START_NOT_STICKY
    }

    private fun handleStart(intent: Intent) {
        val configJson = intent.getStringExtra(EXTRA_CONFIG_JSON) ?: return
        val configType = intent.getStringExtra(EXTRA_CONFIG_TYPE) ?: return

        engineJobs?.cancel()
        engine?.stop()

        val newEngine: TimerEngine = when (configType) {
            CONFIG_TABATA   -> TabataEngine(Json.decodeFromString<TimerConfig.Tabata>(configJson), serviceScope)
            CONFIG_AMRAP    -> AmrapEngine(Json.decodeFromString<TimerConfig.Amrap>(configJson), serviceScope)
            CONFIG_FOR_TIME -> ForTimeEngine(Json.decodeFromString<TimerConfig.ForTime>(configJson), serviceScope)
            CONFIG_EMOM     -> EmomEngine(Json.decodeFromString<TimerConfig.Emom>(configJson), serviceScope)
            CONFIG_CUSTOM   -> CustomEngine(Json.decodeFromString<TimerConfig.Custom>(configJson), serviceScope)
            else -> return
        }

        engine = newEngine
        startTimeMs = System.currentTimeMillis()
        currentConfigType = configType
        currentConfigJson = configJson
        acquireWakeLock()
        startForeground(NOTIF_ID, buildNotification(getString(R.string.app_name)))

        val app = application as WodApp

        engineJobs = serviceScope.launch {
            launch { newEngine.phase.collect { phase ->
                updateNotification(phase)
                app.activePhase.value = phase
            }}
            launch { newEngine.cues.collect { cue -> app.audioCueManager.play(cue) } }
            launch { newEngine.isCompleted.collect { done ->
                if (done) {
                    val durationSecs = ((System.currentTimeMillis() - startTimeMs) / 1000).toInt()
                    val timerType = when (currentConfigType) {
                        CONFIG_TABATA   -> TimerType.TABATA
                        CONFIG_AMRAP    -> TimerType.AMRAP
                        CONFIG_FOR_TIME -> TimerType.FOR_TIME
                        CONFIG_EMOM     -> TimerType.EMOM
                        CONFIG_CUSTOM   -> TimerType.CUSTOM
                        else            -> TimerType.TABATA
                    }
                    app.pendingWorkoutLog = WorkoutLog(
                        type            = timerType,
                        configJson      = currentConfigJson,
                        durationSeconds = durationSecs,
                        completedAt     = System.currentTimeMillis(),
                    )
                    app.timerCompleted.emit(Unit)
                    stopSelf()
                }
            }}
        }

        newEngine.start()
    }

    override fun onDestroy() {
        engineJobs?.cancel()
        engine?.stop()
        engine = null
        releaseWakeLock()
        (application as WodApp).activePhase.value = null
        serviceScope.cancel()
        super.onDestroy()
    }

    // ── Notification ──────────────────────────────────────────────────────────

    private fun updateNotification(phase: TimerPhase) {
        val timeStr = formatSeconds(phase.remainingSeconds)
        val text = if (phase.isPaused)
            getString(R.string.notif_timer_paused, timeStr)
        else
            getString(R.string.notif_timer_running, "${phase.label} · $timeStr")
        val mgr = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mgr.notify(NOTIF_ID, buildNotification(text, phase.isPaused))
    }

    private fun buildNotification(text: String, isPaused: Boolean = false): Notification {
        val openAppIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_SINGLE_TOP },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        fun actionIntent(action: String, requestCode: Int): PendingIntent =
            PendingIntent.getService(
                this, requestCode,
                Intent(this, TimerForegroundService::class.java).apply { this.action = action },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )

        val toggleAction = if (isPaused) {
            NotificationCompat.Action(
                android.R.drawable.ic_media_play,
                getString(R.string.timer_action_resume),
                actionIntent(ACTION_RESUME, 1),
            )
        } else {
            NotificationCompat.Action(
                android.R.drawable.ic_media_pause,
                getString(R.string.timer_action_pause),
                actionIntent(ACTION_PAUSE, 2),
            )
        }

        val skipAction = NotificationCompat.Action(
            android.R.drawable.ic_media_next,
            getString(R.string.timer_action_skip),
            actionIntent(ACTION_SKIP, 3),
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(openAppIntent)
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .addAction(toggleAction)
            .addAction(skipAction)
            .build()
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val mgr = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (mgr.getNotificationChannel(CHANNEL_ID) != null) return
        mgr.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notif_channel_timer),
                NotificationManager.IMPORTANCE_LOW,
            )
        )
    }

    // ── WakeLock ──────────────────────────────────────────────────────────────

    private fun acquireWakeLock() {
        releaseWakeLock()
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKE_LOCK_TAG)
            .also { it.acquire(MAX_WORKOUT_MS) }
    }

    private fun releaseWakeLock() {
        wakeLock?.let { if (it.isHeld) it.release() }
        wakeLock = null
    }

    private fun formatSeconds(s: Int) = "%d:%02d".format(s / 60, s % 60)

    // ── Public API ────────────────────────────────────────────────────────────

    companion object {
        const val ACTION_START  = "com.wod.app.action.START_TIMER"
        const val ACTION_PAUSE  = "com.wod.app.action.PAUSE_TIMER"
        const val ACTION_RESUME = "com.wod.app.action.RESUME_TIMER"
        const val ACTION_SKIP   = "com.wod.app.action.SKIP_TIMER"
        const val ACTION_STOP   = "com.wod.app.action.STOP_TIMER"

        const val EXTRA_CONFIG_JSON = "config_json"
        const val EXTRA_CONFIG_TYPE = "config_type"
        const val CONFIG_TABATA     = "TABATA"
        const val CONFIG_AMRAP      = "AMRAP"
        const val CONFIG_FOR_TIME   = "FOR_TIME"
        const val CONFIG_EMOM       = "EMOM"
        const val CONFIG_CUSTOM     = "CUSTOM"

        private const val CHANNEL_ID      = "wod_timer"
        private const val NOTIF_ID        = 1001
        private const val WAKE_LOCK_TAG   = "WodApp:TimerWakeLock"
        private const val MAX_WORKOUT_MS  = 4L * 60 * 60 * 1000 // 4 h

        fun startTabata(context: Context, config: TimerConfig.Tabata) =
            startConfig(context, Json.encodeToString(config), CONFIG_TABATA)

        fun startAmrap(context: Context, config: TimerConfig.Amrap) =
            startConfig(context, Json.encodeToString(config), CONFIG_AMRAP)

        fun startForTime(context: Context, config: TimerConfig.ForTime) =
            startConfig(context, Json.encodeToString(config), CONFIG_FOR_TIME)

        fun startEmom(context: Context, config: TimerConfig.Emom) =
            startConfig(context, Json.encodeToString(config), CONFIG_EMOM)

        fun startCustom(context: Context, config: TimerConfig.Custom) =
            startConfig(context, Json.encodeToString(config), CONFIG_CUSTOM)

        private fun startConfig(context: Context, json: String, type: String) {
            val intent = Intent(context, TimerForegroundService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_CONFIG_JSON, json)
                putExtra(EXTRA_CONFIG_TYPE, type)
            }
            context.startForegroundServiceCompat(intent)
        }

        fun pause(context: Context)  = sendAction(context, ACTION_PAUSE)
        fun resume(context: Context) = sendAction(context, ACTION_RESUME)
        fun skip(context: Context)   = sendAction(context, ACTION_SKIP)

        fun stop(context: Context) =
            context.stopService(Intent(context, TimerForegroundService::class.java))

        private fun sendAction(context: Context, action: String) =
            context.startService(
                Intent(context, TimerForegroundService::class.java).apply { this.action = action }
            )

        private fun Context.startForegroundServiceCompat(intent: Intent) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(intent)
            else startService(intent)
        }
    }
}
