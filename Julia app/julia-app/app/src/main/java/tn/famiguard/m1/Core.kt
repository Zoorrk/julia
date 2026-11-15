package tn.famiguard.m1

import android.app.Application
import android.content.Context
import com.google.firebase.auth.FirebaseAuth

/** Application : connexion anonyme à Firebase au démarrage (nécessaire pour Firestore). */
class FamiApp : Application() {
    override fun onCreate() {
        super.onCreate()
        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser == null) auth.signInAnonymously()
    }
}

/** Réglages locaux : rôle du téléphone, code de la famille, apps bloquées (cache hors ligne). */
object Prefs {
    private const val P = "famiguard_prefs"
    const val ROLE_PARENT = "parent"
    const val ROLE_CHILD = "child"

    private fun sp(c: Context) = c.getSharedPreferences(P, Context.MODE_PRIVATE)

    fun role(c: Context): String? = sp(c).getString("role", null)
    fun setRole(c: Context, r: String) { sp(c).edit().putString("role", r).apply() }

    fun familyCode(c: Context): String? = sp(c).getString("family", null)
    fun setFamilyCode(c: Context, code: String) { sp(c).edit().putString("family", code).apply() }

    fun blocked(c: Context): Set<String> = sp(c).getStringSet("blocked", emptySet()) ?: emptySet()
    fun setBlocked(c: Context, pkgs: Set<String>) { sp(c).edit().putStringSet("blocked", pkgs).apply() }
    fun toggleBlocked(c: Context, pkg: String): Boolean {
        val set = blocked(c).toMutableSet()
        val now = if (set.contains(pkg)) { set.remove(pkg); false } else { set.add(pkg); true }
        setBlocked(c, set)
        return now
    }

    fun reset(c: Context) = sp(c).edit().clear().apply()
}
