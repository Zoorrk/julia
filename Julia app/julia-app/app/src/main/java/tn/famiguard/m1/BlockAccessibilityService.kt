package tn.famiguard.m1

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.text.TextUtils
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.google.firebase.firestore.ListenerRegistration

/**
 * Téléphone ENFANT :
 *  - applique à distance les apps bloquées par le parent (Firestore, marche hors ligne via cache) ;
 *  - lit le texte affiché à l'écran et le classe LOCALEMENT (Detector) — aucune donnée brute
 *    n'est envoyée ; seule une alerte (catégorie + court extrait) remonte au parent.
 */
class BlockAccessibilityService : AccessibilityService() {

    private var blocked: Set<String> = emptySet()
    private var reg: ListenerRegistration? = null
    private var lastShownFor: String? = null
    private val recentAlerts = HashMap<String, Long>()

    override fun onServiceConnected() {
        super.onServiceConnected()
        blocked = Prefs.blocked(this)
        val code = Prefs.familyCode(this) ?: return
        reg = Fb.rulesDoc(code).addSnapshotListener { snap, _ ->
            @Suppress("UNCHECKED_CAST")
            val list = (snap?.get("blocked") as? List<String>) ?: return@addSnapshotListener
            blocked = list.toSet()
            Prefs.setBlocked(this, blocked)
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return
        val pkg = event.packageName?.toString() ?: return
        if (pkg == packageName) return
        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                if (blocked.contains(pkg)) { if (pkg != lastShownFor) { lastShownFor = pkg; showBlock(pkg) } }
                else lastShownFor = null
            }
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> scanContent(pkg)
            else -> {}
        }
    }

    // ---- Blocage ----
    private fun showBlock(pkg: String) {
        performGlobalAction(GLOBAL_ACTION_HOME)
        startActivity(Intent(this, BlockActivity::class.java)
            .putExtra(BlockActivity.EXTRA_APP, labelOf(pkg))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
    }

    // ---- Détection locale ----
    private fun scanContent(pkg: String) {
        val code = Prefs.familyCode(this) ?: return
        val root = rootInActiveWindow ?: return
        val sb = StringBuilder()
        collectText(root, sb, 0)
        val text = sb.toString()
        val category = Detector.classify(text) ?: return
        val key = "$pkg|$category"
        val now = System.currentTimeMillis()
        if (now - (recentAlerts[key] ?: 0L) < 60_000) return
        recentAlerts[key] = now
        Fb.pushAlert(code, labelOf(pkg), category, text.take(60).replace("\n", " ").trim())
    }

    private fun collectText(node: AccessibilityNodeInfo?, sb: StringBuilder, depth: Int) {
        node ?: return
        if (depth > 40 || sb.length > 2000) return
        node.text?.let { if (it.isNotBlank()) sb.append(it).append('\n') }
        for (i in 0 until node.childCount) collectText(node.getChild(i), sb, depth + 1)
    }

    private fun labelOf(pkg: String): String = runCatching {
        packageManager.getApplicationLabel(packageManager.getApplicationInfo(pkg, 0)).toString()
    }.getOrDefault(pkg)

    override fun onInterrupt() {}
    override fun onDestroy() { reg?.remove(); super.onDestroy() }

    companion object {
        fun isEnabled(ctx: Context): Boolean {
            val expected = "${ctx.packageName}/${BlockAccessibilityService::class.java.name}"
            val enabled = Settings.Secure.getString(ctx.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES) ?: return false
            val s = TextUtils.SimpleStringSplitter(':'); s.setString(enabled)
            while (s.hasNext()) if (s.next().equals(expected, true)) return true
            return false
        }
        fun openAccessibilitySettings(ctx: Context) {
            ctx.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
    }
}
