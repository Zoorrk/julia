package tn.famiguard.m1

import android.content.Context

/** Réglages locaux : rôle de ce téléphone (parent / enfant) et apps bloquées. */
object Prefs {
    private const val P = "famiguard_prefs"
    const val ROLE_PARENT = "parent"
    const val ROLE_CHILD = "child"

    private fun sp(c: Context) = c.getSharedPreferences(P, Context.MODE_PRIVATE)

    fun role(c: Context): String? = sp(c).getString("role", null)
    fun setRole(c: Context, r: String) { sp(c).edit().putString("role", r).apply() }

    fun blocked(c: Context): Set<String> = sp(c).getStringSet("blocked", emptySet()) ?: emptySet()
    fun toggleBlocked(c: Context, pkg: String): Boolean {
        val set = blocked(c).toMutableSet()
        val now = if (set.contains(pkg)) { set.remove(pkg); false } else { set.add(pkg); true }
        sp(c).edit().putStringSet("blocked", set).apply()
        return now
    }

    fun reset(c: Context) = sp(c).edit().clear().apply()
}
