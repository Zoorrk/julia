package tn.famiguard.m1

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.text.TextUtils
import android.view.accessibility.AccessibilityEvent

/**
 * Sur le téléphone de l'ENFANT : détecte l'application au premier plan et, si elle est
 * bloquée, affiche l'écran "application en pause". Fonctionne en local, sans Internet.
 */
class BlockAccessibilityService : AccessibilityService() {

    private var lastShownFor: String? = null

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val pkg = event.packageName?.toString() ?: return
        if (pkg == packageName) return

        if (Prefs.blocked(this).contains(pkg)) {
            if (pkg != lastShownFor) { lastShownFor = pkg; showBlockScreen(pkg) }
        } else {
            lastShownFor = null
        }
    }

    private fun showBlockScreen(blockedPkg: String) {
        performGlobalAction(GLOBAL_ACTION_HOME)
        val label = runCatching {
            packageManager.getApplicationLabel(packageManager.getApplicationInfo(blockedPkg, 0)).toString()
        }.getOrDefault(blockedPkg)
        startActivity(
            Intent(this, BlockActivity::class.java)
                .putExtra(BlockActivity.EXTRA_APP, label)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        )
    }

    override fun onInterrupt() {}

    companion object {
        fun isEnabled(ctx: Context): Boolean {
            val expected = "${ctx.packageName}/${BlockAccessibilityService::class.java.name}"
            val enabled = Settings.Secure.getString(
                ctx.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: return false
            val s = TextUtils.SimpleStringSplitter(':'); s.setString(enabled)
            while (s.hasNext()) if (s.next().equals(expected, true)) return true
            return false
        }

        fun openAccessibilitySettings(ctx: Context) {
            ctx.startActivity(
                Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }
}
