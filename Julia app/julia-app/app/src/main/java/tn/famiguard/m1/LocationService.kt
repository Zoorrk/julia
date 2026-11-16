package tn.famiguard.m1

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import androidx.core.app.NotificationCompat

/**
 * Service permanent (téléphone ENFANT) : partage la position en temps réel avec le parent.
 * Android impose une notification visible pour ce type de service — c'est voulu et transparent.
 * Écoute GPS + réseau et envoie chaque nouvelle position à Firebase (le parent la voit en direct).
 */
class LocationService : Service() {

    private var lm: LocationManager? = null

    private val listener = object : LocationListener {
        override fun onLocationChanged(loc: Location) {
            runCatching { LocationUploader.push(applicationContext, loc) }
        }
        override fun onProviderDisabled(provider: String) {}
        override fun onProviderEnabled(provider: String) {}
        @Deprecated("deprecated in API 29")
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
    }

    override fun onCreate() {
        super.onCreate()
        startForeground(NOTIF_ID, buildNotification())
        startUpdates()
    }

    private fun startUpdates() {
        if (!LocationUploader.hasPermission(this)) return
        val manager = getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return
        lm = manager
        // Au moins toutes les ~60 s ou tous les ~25 m, selon ce qui arrive en premier.
        val minTimeMs = 60_000L
        val minDistM = 25f
        try {
            if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER))
                manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTimeMs, minDistM, listener)
            if (manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
                manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, minTimeMs, minDistM, listener)
            // Envoie tout de suite la dernière position connue pour ne pas attendre le 1er fix.
            LocationUploader.upload(this)
        } catch (e: SecurityException) {
            // Permission retirée entre-temps : on ignore, le service reste inoffensif.
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        runCatching { lm?.removeUpdates(listener) }
        super.onDestroy()
    }

    private fun buildNotification(): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(NotificationManager::class.java)
            if (nm.getNotificationChannel(CHANNEL) == null) {
                nm.createNotificationChannel(
                    NotificationChannel(CHANNEL, "Protection Julia", NotificationManager.IMPORTANCE_LOW)
                        .apply { description = "Partage de position avec les parents" }
                )
            }
        }
        return NotificationCompat.Builder(this, CHANNEL)
            .setContentTitle("Protection active")
            .setContentText("Ta position est partagée avec tes parents.")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    companion object {
        private const val CHANNEL = "julia_location"
        private const val NOTIF_ID = 42

        /** Démarre le service (sans planter si la permission manque). */
        fun start(ctx: Context) {
            if (!LocationUploader.hasPermission(ctx)) return
            val i = Intent(ctx, LocationService::class.java)
            runCatching {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) ctx.startForegroundService(i)
                else ctx.startService(i)
            }
        }
    }
}
