package tn.famiguard.m1

/** Données d'exemple pour la démonstration côté parent (Sofia, 14 ans). */

data class Alert(
    val id: Int,
    val category: String,
    val app: String,
    val sender: String,
    val excerpt: String,
    val time: String,
    val severity: String,          // "Élevé" | "Moyen" | "Faible"
    val whatDetected: List<String>,
    val whatItMeans: String,
    val recommendations: List<String>,
    val script: String
)

data class ChildLocation(
    val address: String,
    val area: String,
    val time: String,
    val accuracyM: Int,
    val live: Boolean,             // true = temps réel, false = dernière position connue (GPS off)
    val x: Float,                  // position sur la mini-carte (0..1)
    val y: Float
)

object Demo {

    val location = ChildLocation(
        address = "Avenue Habib Bourguiba",
        area = "Tunis Centre",
        time = "il y a 2 min",
        accuracyM = 12,
        live = true,
        x = 0.54f, y = 0.47f
    )

    val lastKnown = ChildLocation(
        address = "Lycée de la rue de Marseille",
        area = "Tunis",
        time = "aujourd'hui 13:42",
        accuracyM = 30,
        live = false,
        x = 0.38f, y = 0.62f
    )

    val alerts = listOf(
        Alert(
            id = 1, category = "Harcèlement", app = "WhatsApp", sender = "Groupe « Classe 3B »",
            excerpt = "t'es vraiment nul arrête de parler personne t'aime",
            time = "il y a 2 h", severity = "Élevé",
            whatDetected = listOf(
                "Messages répétés et humiliants dans un groupe",
                "3 incidents similaires en 7 jours",
                "Même groupe à chaque fois"
            ),
            whatItMeans = "Ces messages répétés et dévalorisants dans un groupe de camarades sont un signe de harcèlement scolaire. La répétition est l'élément le plus important.",
            recommendations = listOf(
                "Parlez à votre enfant sans jugement : demandez-lui comment il vit ce groupe.",
                "Écoutez avant de conseiller. Ne retirez pas le téléphone d'emblée, cela isole davantage.",
                "Conservez des captures comme preuves, et signalez au besoin à l'établissement.",
                "Si la détresse persiste, consultez un professionnel (psychologue scolaire)."
            ),
            script = "« J'ai remarqué que l'ambiance dans le groupe de classe n'est pas facile en ce moment. Tu veux m'en parler ? On peut trouver ensemble comment réagir, tu n'es pas seul. »"
        ),
        Alert(
            id = 2, category = "Menace", app = "Instagram", sender = "Message privé",
            excerpt = "fais attention à toi à la sortie",
            time = "hier 18:20", severity = "Élevé",
            whatDetected = listOf("Message privé à caractère menaçant", "Expéditeur hors des contacts habituels"),
            whatItMeans = "Une menace directe en message privé, surtout d'un expéditeur inconnu, mérite une attention immédiate.",
            recommendations = listOf(
                "Rassurez votre enfant et prenez la menace au sérieux.",
                "Bloquez et signalez le compte sur Instagram.",
                "Si la menace est précise, envisagez un signalement aux autorités."
            ),
            script = "« Merci de me montrer ce message. Tu as bien fait. On va bloquer cette personne ensemble et décider de la suite, calmement. »"
        ),
        Alert(
            id = 3, category = "Contact inconnu", app = "TikTok", sender = "Compte non identifié",
            excerpt = "envoie ta photo et garde le secret",
            time = "il y a 3 j", severity = "Élevé",
            whatDetected = listOf("Demande de photo + demande de secret", "Schéma d'approche d'un adulte (indice de grooming)"),
            whatItMeans = "La combinaison « envoie une photo » + « garde le secret » est un indicateur classique d'approche inappropriée (grooming). À traiter avec sérieux.",
            recommendations = listOf(
                "N'alarmez pas brutalement l'enfant, mais expliquez pourquoi ce message est dangereux.",
                "Ne répondez pas, bloquez et signalez le compte.",
                "Expliquez la règle : on n'envoie jamais de photo et on ne garde pas de secret imposé par un adulte.",
                "En cas de doute, contactez une association de protection de l'enfance."
            ),
            script = "« Tu peux toujours tout me dire, même si quelqu'un te demande de garder un secret. Un adulte qui demande ça à un enfant n'est pas un ami. On bloque cette personne ensemble. »"
        ),
        Alert(
            id = 4, category = "Harcèlement", app = "WhatsApp", sender = "Groupe « Classe 3B »",
            excerpt = "encore toi ? personne veut de toi ici",
            time = "il y a 5 j", severity = "Moyen",
            whatDetected = listOf("Message d'exclusion dans le même groupe"),
            whatItMeans = "Message d'exclusion, cohérent avec un harcèlement de groupe déjà repéré.",
            recommendations = listOf(
                "Rappelez à votre enfant que ce n'est pas de sa faute.",
                "Notez l'incident : il s'ajoute à un schéma répété."
            ),
            script = "« Ce qu'ils écrivent ne dit rien de qui tu es vraiment. Je suis là, on gère ça ensemble. »"
        )
    )

    // Ressource locale (à vérifier avant mise en production)
    const val helpline = "Ligne verte de protection de l'enfance (Tunisie) — numéro à confirmer avant diffusion"

    // Statistiques (7 derniers jours)
    val weekDays = listOf("L", "M", "M", "J", "V", "S", "D")
    val alertsPerDay = listOf(0, 1, 0, 2, 1, 0, 3)           // incidents détectés
    val byCategory = listOf(
        "Harcèlement" to 6,
        "Menace" to 2,
        "Contact inconnu" to 1,
        "Autre" to 1
    )
    val screenTimePerDay = listOf(185, 210, 160, 240, 225, 300, 252) // minutes
}
