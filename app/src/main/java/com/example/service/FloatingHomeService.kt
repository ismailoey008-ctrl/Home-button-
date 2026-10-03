package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import com.example.HomeCircleApplication
import com.example.MainActivity
import com.example.R
import com.example.data.model.CircleSettings
import com.example.view.CircleHomeView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class FloatingHomeService : Service(), CircleHomeView.Listener {

    private var windowManager: WindowManager? = null
    private var circleView: CircleHomeView? = null
    private var layoutParams: WindowManager.LayoutParams? = null

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    private val repository by lazy {
        (application as HomeCircleApplication).repository
    }

    private var currentSettings = CircleSettings()

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        isRunning = true
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification())

        if (Settings.canDrawOverlays(this)) {
            initOverlayView()
            observeSettings()
        } else {
            stopSelf()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP_SERVICE -> {
                serviceScope.launch {
                    repository.setEnabled(false)
                }
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_RELOAD_SETTINGS -> {
                currentSettings = repository.getQuickSettings()
                updateViewFromSettings(currentSettings)
            }
        }
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notification_channel_desc)
                setShowBadge(false)
                enableLights(false)
                enableVibration(false)
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        val stopIntent = Intent(this, FloatingHomeService::class.java).apply {
            action = ACTION_STOP_SERVICE
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_home_circle_1791028763245)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_content))
            .setContentIntent(openAppPendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                getString(R.string.notification_action_stop),
                stopPendingIntent
            )
            .build()
    }

    private fun initOverlayView() {
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        currentSettings = repository.getQuickSettings()

        val density = resources.displayMetrics.density
        val sizePx = (currentSettings.sizeDp * density).toInt()

        val windowType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        layoutParams = WindowManager.LayoutParams(
            sizePx,
            sizePx,
            windowType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                    WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = currentSettings.posX
            y = currentSettings.posY
        }

        circleView = CircleHomeView(this).apply {
            this.settings = currentSettings
            this.windowManager = this@FloatingHomeService.windowManager
            this.layoutParams = this@FloatingHomeService.layoutParams
            this.listener = this@FloatingHomeService
        }

        try {
            windowManager?.addView(circleView, layoutParams)
        } catch (_: Exception) {
        }
    }

    private fun observeSettings() {
        serviceScope.launch {
            repository.settingsFlow.collectLatest { settings ->
                currentSettings = settings
                updateViewFromSettings(settings)
            }
        }
    }

    private fun updateViewFromSettings(settings: CircleSettings) {
        circleView?.let { view ->
            view.settings = settings
            val density = resources.displayMetrics.density
            val sizePx = (settings.sizeDp * density).toInt()

            layoutParams?.let { lp ->
                if (lp.width != sizePx || lp.height != sizePx) {
                    lp.width = sizePx
                    lp.height = sizePx
                    try {
                        windowManager?.updateViewLayout(view, lp)
                    } catch (_: Exception) {
                    }
                }
            }
        }
    }

    // Listener callbacks from CircleHomeView
    override fun onSingleTap() {
        executeAction(currentSettings.singleTapAction)
    }

    override fun onDoubleTap() {
        executeAction(currentSettings.doubleTapAction)
    }

    override fun onLongPress() {
        executeAction(currentSettings.longPressAction)
    }

    override fun onPositionChanged(x: Int, y: Int) {
        serviceScope.launch {
            repository.updatePosition(x, y)
        }
    }

    private fun executeAction(actionType: String) {
        when (actionType) {
            CircleSettings.ACTION_HOME -> {
                // Try accessibility first for instant action without activity transition
                val handled = HomeAccessibilityService.performHome()
                if (!handled) {
                    // Fallback to native Home Intent (100% reliable)
                    val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                        addCategory(Intent.CATEGORY_HOME)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                    }
                    startActivity(homeIntent)
                }
                serviceScope.launch {
                    repository.recordHomeClick()
                }
            }
            CircleSettings.ACTION_RECENTS -> {
                HomeAccessibilityService.performRecents()
            }
            CircleSettings.ACTION_NOTIFICATIONS -> {
                HomeAccessibilityService.performNotifications()
            }
            CircleSettings.ACTION_LOCK -> {
                HomeAccessibilityService.performLock()
            }
            CircleSettings.ACTION_OPEN_SETTINGS -> {
                val intent = Intent(this, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                }
                startActivity(intent)
            }
            else -> {
                // None
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        serviceJob.cancel()
        circleView?.let { view ->
            try {
                windowManager?.removeView(view)
            } catch (_: Exception) {
            }
        }
        circleView = null
    }

    companion object {
        private const val CHANNEL_ID = "home_circle_fg_service"
        private const val NOTIFICATION_ID = 1001
        const val ACTION_STOP_SERVICE = "com.example.action.STOP_SERVICE"
        const val ACTION_RELOAD_SETTINGS = "com.example.action.RELOAD_SETTINGS"

        var isRunning: Boolean = false
            private set

        fun start(context: Context) {
            val intent = Intent(context, FloatingHomeService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, FloatingHomeService::class.java)
            context.stopService(intent)
        }
    }
}
