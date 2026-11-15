package tn.famiguard.m1

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

/**
 * Pont Firestore entre téléphone parent et téléphone enfant.
 *   families/{code}
 *     state/usage  -> temps d'écran (enfant écrit, parent lit)
 *     state/rules  -> apps bloquées + réglages internet (parent écrit, enfant lit)
 *     alerts/{id}  -> contenus détectés (enfant écrit, parent lit)
 */
object Fb {
    val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    fun family(code: String) = db.collection("families").document(code)
    fun usageDoc(code: String) = family(code).collection("state").document("usage")
    fun rulesDoc(code: String) = family(code).collection("state").document("rules")
    fun alerts(code: String) = family(code).collection("alerts")

    fun createFamily(code: String) {
        family(code).set(mapOf("createdAt" to FieldValue.serverTimestamp()))
        rulesDoc(code).set(mapOf("blocked" to emptyList<String>(), "net" to emptyMap<String, Boolean>()))
    }

    fun uploadUsage(code: String, date: String, total: Long, apps: List<Map<String, Any>>, model: String) {
        usageDoc(code).set(
            mapOf(
                "date" to date, "totalMs" to total, "apps" to apps,
                "model" to model, "updatedAt" to FieldValue.serverTimestamp()
            )
        )
        family(code).update("childLastSeen", FieldValue.serverTimestamp())
    }

    fun setBlocked(code: String, pkgs: List<String>) {
        rulesDoc(code).set(mapOf("blocked" to pkgs), com.google.firebase.firestore.SetOptions.merge())
    }

    fun setNet(code: String, net: Map<String, Boolean>) {
        rulesDoc(code).set(mapOf("net" to net), com.google.firebase.firestore.SetOptions.merge())
    }

    fun pushAlert(code: String, app: String, category: String, snippet: String) {
        alerts(code).add(
            mapOf("app" to app, "category" to category, "snippet" to snippet, "at" to FieldValue.serverTimestamp())
        )
    }

    fun recentAlerts(code: String) =
        alerts(code).orderBy("at", Query.Direction.DESCENDING).limit(30)
}
