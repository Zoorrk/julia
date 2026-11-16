package tn.famiguard.m1

/**
 * Moteur de détection LOCAL (sur le téléphone de l'enfant) — aucune donnée envoyée au cloud
 * pour l'analyse. Le texte affiché à l'écran est classé ici même.
 *
 * MVP : détection par mots-clés + motifs, normalisée pour le français, l'anglais,
 * l'arabe et l'arabizi (chiffres-lettres : 3=ع, 7=ح, 9=ق, 5=خ...).
 * Conçu pour être remplacé plus tard par un modèle on-device (TFLite) sans changer le reste.
 */
object Detector {

    // Chaque catégorie : liste de termes/motifs (déjà normalisés plus bas à la comparaison).
    private val lexicon: Map<String, List<String>> = mapOf(
        "Harcèlement" to listOf(
            // français
            "tu es nul", "t es nul", "personne t aime", "ferme la", "degage", "tocard", "grosse", "boloss", "sale",
            // anglais
            "you are stupid", "nobody likes you", "kill yourself", "loser", "shut up", "ugly", "freak",
            // arabe / tunisien / arabizi
            "7mar", "bhim", "5ayeb", "saket", "mhabbel", "ya 5ra", "3eyech", "حمار", "بهيم", "مخنث", "اسكت"
        ),
        "Menace" to listOf(
            "fais attention a toi", "je vais te frapper", "tu vas voir", "on va te",
            "i will hurt you", "watch your back", "you are dead",
            "bech ndhorbek", "twa3ed", "bech tara", "باش نضربك", "تهديد"
        ),
        "Contact inconnu" to listOf(
            "envoie ta photo", "envoie une photo", "garde le secret", "ne dis pas a tes parents",
            "on se voit", "tu habites ou", "t as quel age",
            "send me a photo", "keep it secret", "don t tell your parents", "where do you live", "how old are you",
            "3malli photo", "ab3athli taswira", "ma t9olech l omok"
        ),
        // Contenu adulte / pornographique : sites connus + termes. S'applique aux adresses
        // et pages affichées dans le navigateur (lues en direct), comme à tout autre écran.
        "Contenu adulte" to listOf(
            // sites connus (détectés dès que l'adresse ou un lien apparaît à l'écran)
            "pornhub", "xvideos", "xnxx", "xhamster", "redtube", "youporn", "onlyfans",
            "brazzers", "chaturbate", "stripchat", "spankbang", "hqporner", "porn300",
            "youjizz", "tube8", "sex.com", "livejasmin", "fansly",
            // termes génériques FR / EN
            "porno", "pornographie", "pornographique", "porn", "hardcore porn", "sexe explicite",
            "nude", "nudes", "hentai", "camgirl", "escort", "x rated", "adult video",
            // arabizi / arabe
            "jins", "9a7ba", "fضيحة", "sa7ki jinsi", "film jins"
        ),
        // Drogue / violence : signaux d'alerte supplémentaires.
        "Drogue / violence" to listOf(
            "drogue", "zatla", "joint", "cannabis", "cocaine", "ecstasy", "weed", "deal de",
            "je vais me battre", "ramene un couteau", "bring a knife", "buy weed", "acheter de la drogue"
        )
    )

    /** Normalise : minuscules, sans accents, arabizi -> lettres, espaces compactés. */
    private fun norm(s: String): String {
        val sb = StringBuilder(s.length)
        for (c in s.lowercase()) {
            val r = when (c) {
                'é', 'è', 'ê', 'ë' -> 'e'; 'à', 'â', 'ä' -> 'a'; 'î', 'ï' -> 'i'
                'ô', 'ö' -> 'o'; 'ù', 'û', 'ü' -> 'u'; 'ç' -> 'c'
                '3' -> 'a'; '7' -> 'h'; '9' -> 'q'; '5' -> 'k'; '2' -> 'a'; '8' -> 'g'
                else -> c
            }
            sb.append(r)
        }
        return sb.toString().replace(Regex("[^\\p{L}\\p{N} ]"), " ").replace(Regex(" +"), " ").trim()
    }

    private val normLexicon: Map<String, List<String>> by lazy {
        lexicon.mapValues { (_, v) -> v.map { norm(it) } }
    }

    /** Renvoie la première catégorie détectée, ou null. */
    fun classify(text: String): String? {
        if (text.isBlank()) return null
        val n = norm(text)
        for ((cat, words) in normLexicon) {
            if (words.any { it.isNotBlank() && n.contains(it) }) return cat
        }
        return null
    }

    /** Langues couvertes (pour l'affichage). */
    const val languages = "français, anglais, tunisien (arabizi + arabe)"
}
