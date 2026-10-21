package com.azhdar.app10.core.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import com.azhdar.app10.App10Application
import com.azhdar.app10.MainActivity
import com.azhdar.app10.R
import com.azhdar.app10.core.flashlight.TorchController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Keeps the torch alive while the app is in the background.
 *
 * A camera2 torch belongs to the process that switched it on: the camera service turns the
 * torch off automatically when that process dies, and Android is free to kill a backgrounded
 * process at any time. A foreground service is therefore the only way to guarantee that the
 * light stays on while the user is in another app or the screen is off.
 *
 * Design notes:
 *  - The service does not own the torch. [TorchController] stays the single source of truth
 *    and lives in the application scope, so the service and the UI never disagree about the
 *    state.
 *  - The `specialUse` foreground service type is used on purpose. The `camera` type would
 *    require the `CAMERA` runtime permission to be granted on Android 14+, but the torch API
 *    itself needs no permission at all, so demanding it would be a needless and alarming
 *    permission prompt.
 *  - The service is not sticky: if the process is killed the torch is off anyway, so
 *    resurrecting the service would only show a misleading notification.
 */
class TorchService : Service() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var observeJob: Job? = null

    private val controller: TorchController
        get() = (application as App10Application).container.torchController

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_TURN_OFF -> {
                controller.safeSetTorchOff()
                stopSelf()
                return START_NOT_STICKY
            }
        }

        startForegroundCompat()

        // Keep the notification in sync with the actual torch state.
        observeJob?.cancel()
        observeJob = scope.launch {
            controller.torchState.collectLatest { state ->
                updateNotification(state.isOn)
            }
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        observeJob?.cancel()
        // The torch must never outlive the service, otherwise it would stay on with no way
        // for the user to switch it off.
        controller.safeSetTorchOff()
        controller.onServiceStopped()
        super.onDestroy()
    }

    private fun startForegroundCompat() {
        val notification = buildNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ServiceCompat.startForeground(
                this,
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE,
            )
        } else {
            // Before Android 14 the type is taken from the manifest, and the specialUse
            // flag is not part of the platform yet.
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun buildNotification(isOn: Boolean = true): Notification {
        val contentIntent = PendingIntent.getActivity(
            this,
            REQUEST_CONTENT,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        val turnOffIntent = PendingIntent.getService(
            this,
            REQUEST_TURN_OFF,
            Intent(this, TorchService::class.java).setAction(ACTION_TURN_OFF),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(
                getString(
                    if (isOn) R.string.notification_torch_title
                    else R.string.notification_torch_title_off,
                ),
            )
            .setContentText(getString(R.string.notification_torch_text))
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(contentIntent)
            .setOngoing(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .addAction(0, getString(R.string.notification_turn_off), turnOffIntent)
            .build()
    }

    private fun updateNotification(isOn: Boolean) {
        val manager = NotificationManagerCompat.from(this)
        if (!manager.areNotificationsEnabled()) return
        runCatching {
            manager.notify(NOTIFICATION_ID, buildNotification(isOn))
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = getString(R.string.notification_channel_description)
            setShowBadge(false)
        }
        getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
    }

    companion object {
        private const val CHANNEL_ID = "torch_service"
        private const val NOTIFICATION_ID = 4711
        private const val REQUEST_CONTENT = 1
        private const val REQUEST_TURN_OFF = 2

        const val ACTION_TURN_OFF = "com.azhdar.app10.action.TURN_OFF_TORCH"

        /** Starts the service so the torch survives the app being backgrounded. */
        fun start(context: Context) {
            val intent = Intent(context, TorchService::class.java)
            runCatching {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            }
        }

        /** Stops the service and switches the torch off. */
        fun stop(context: Context) {
            runCatching { context.stopService(Intent(context, TorchService::class.java)) }
        }
    }
}
