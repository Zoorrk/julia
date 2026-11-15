package tn.famiguard.m1

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.LocationManager
import androidx.core.content.ContextCompat
import java.util.Locale

/** Téléphone ENFANT : envoie sa vraie position au parent (via Firebase). */
object LocationUploader {

    fun hasPermission(ctx: Context): Boolean =
        ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

    fun upload(ctx: Context) {
        val code = Prefs.familyCode(ctx) ?: return
        if (!hasPermission(ctx)) return
        val lm = ctx.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return
        val loc = try {
            val providers = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER, LocationManager.PASSIVE_PROVIDER)
            providers.mapNotNull { runCatching { lm.getLastKnownLocation(it) }.getOrNull() }
                .maxByOrNull { it.time }
        } catch (e: SecurityException) { null } ?: return

        val address = runCatching {
            @Suppress("DEPRECATION")
            Geocoder(ctx, Locale.FRENCH).getFromLocation(loc.latitude, loc.longitude, 1)
                ?.firstOrNull()?.let { listOfNotNull(it.thoroughfare, it.locality).joinToString(", ") }
        }.getOrNull().orEmpty()

        Fb.setLocation(code, loc.latitude, loc.longitude, address.ifBlank { "Position" })
    }
}
