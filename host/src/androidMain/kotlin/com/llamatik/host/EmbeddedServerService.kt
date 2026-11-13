package com.llamatik.host

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.llamatik.network.discovery.NodeDiscovery
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * Keeps [LlamatikHost.server] alive while the app is in the background or the screen is off.
 * Started/stopped through [start] / [stop]; the notification has its own Stop action.
 */
class EmbeddedServerService : Service() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var observer: Job? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var wifiLock: WifiManager.WifiLock? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val server = LlamatikHost.server
        if (intent?.action == ACTION_STOP || server == null) {
            server?.stop()
            shutDown()
            return START_NOT_STICKY
        }

        createChannel()
        ServiceCompat.startForeground(
            this, NOTIFICATION_ID, buildNotification(getString(R.string.llamatik_host_starting)),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            } else {
                0
            },
        )
        acquireLocks()
        NodeDiscovery.init(this)

        val config = HostConfig(
            port = intent?.getIntExtra(EXTRA_PORT, HostConfig.DEFAULT_PORT) ?: HostConfig.DEFAULT_PORT,
            serviceName = intent?.getStringExtra(EXTRA_NAME) ?: Build.MODEL,
            shareText = intent?.getBooleanExtra(EXTRA_TEXT, true) ?: true,
            shareEmbeddings = intent?.getBooleanExtra(EXTRA_EMBEDDINGS, true) ?: true,
        )
        server.start(config)

        observer?.cancel()
        observer = scope.launch {
            combine(server.state, server.pendingRequests) { state, pending -> state to pending }
                .collect { (state, pending) ->
                    when (state) {
                        is EmbeddedServerState.Running -> {
                            val address = state.addresses.firstOrNull()?.let { "$it:${state.port}" } ?: "${state.port}"
                            val text = if (pending.isNotEmpty()) {
                                getString(R.string.llamatik_host_pending, pending.first().clientName)
                            } else {
                                getString(R.string.llamatik_host_running, address)
                            }
                            notify(buildNotification(text))
                        }
                        is EmbeddedServerState.Error -> {
                            notify(buildNotification(state.message))
                            shutDown()
                        }
                        EmbeddedServerState.Stopped -> shutDown()
                        EmbeddedServerState.Starting -> Unit
                    }
                }
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        observer?.cancel()
        scope.cancel()
        releaseLocks()
        super.onDestroy()
    }

    private fun shutDown() {
        observer?.cancel()
        releaseLocks()
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun acquireLocks() {
        if (wakeLock == null) {
            wakeLock = getSystemService(PowerManager::class.java)
                ?.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "llamatik:host")
                ?.apply { setReferenceCounted(false); acquire() }
        }
        if (wifiLock == null) {
            @Suppress("DEPRECATION")
            wifiLock = applicationContext.getSystemService(WifiManager::class.java)
                ?.createWifiLock(
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        WifiManager.WIFI_MODE_FULL_LOW_LATENCY
                    } else {
                        WifiManager.WIFI_MODE_FULL_HIGH_PERF
                    },
                    "llamatik:host",
                )
                ?.apply { setReferenceCounted(false); acquire() }
        }
    }

    private fun releaseLocks() {
        wakeLock?.takeIf { it.isHeld }?.release()
        wifiLock?.takeIf { it.isHeld }?.release()
        wakeLock = null
        wifiLock = null
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getSystemService(NotificationManager::class.java).createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    getString(R.string.llamatik_host_channel),
                    NotificationManager.IMPORTANCE_LOW,
                ),
            )
        }
    }

    private fun notify(notification: Notification) {
        getSystemService(NotificationManager::class.java).notify(NOTIFICATION_ID, notification)
    }

    private fun buildNotification(text: String): Notification {
        val stop = PendingIntent.getService(
            this, 0,
            Intent(this, EmbeddedServerService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val open = packageManager.getLaunchIntentForPackage(packageName)?.let {
            PendingIntent.getActivity(this, 1, it, PendingIntent.FLAG_IMMUTABLE)
        }
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setContentTitle(getString(R.string.llamatik_host_title))
            .setContentText(text)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(open)
            .addAction(0, getString(R.string.llamatik_host_stop), stop)
            .build()
    }

    companion object {
        private const val CHANNEL_ID = "llamatik_host"
        private const val NOTIFICATION_ID = 7421
        private const val ACTION_STOP = "com.llamatik.host.STOP"
        private const val EXTRA_PORT = "port"
        private const val EXTRA_NAME = "name"
        private const val EXTRA_TEXT = "text"
        private const val EXTRA_EMBEDDINGS = "embeddings"

        fun start(context: Context, config: HostConfig) {
            val intent = Intent(context, EmbeddedServerService::class.java)
                .putExtra(EXTRA_PORT, config.port)
                .putExtra(EXTRA_NAME, config.serviceName)
                .putExtra(EXTRA_TEXT, config.shareText)
                .putExtra(EXTRA_EMBEDDINGS, config.shareEmbeddings)
            ContextCompat.startForegroundService(context, intent)
        }

        fun stop(context: Context) {
            context.startService(Intent(context, EmbeddedServerService::class.java).setAction(ACTION_STOP))
        }
    }
}
