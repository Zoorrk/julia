# Obtenir l'APK sans rien installer (GitHub)

GitHub compile l'APK dans le cloud et te le donne à télécharger.
Il te faut seulement un compte GitHub gratuit. Aucune installation sur ton PC.

---

## Étape 1 — Créer un compte GitHub (si tu n'en as pas)
- Va sur https://github.com → **Sign up** (gratuit).

## Étape 2 — Créer un dépôt vide
1. En haut à droite, clique **+** → **New repository**.
2. Nom : `famiguard-m1` (par exemple).
3. Laisse **Private** ou **Public**, peu importe.
4. **NE coche PAS** « Add a README ».
5. Clique **Create repository**.

## Étape 3 — Déposer le projet (glisser-déposer, sans logiciel)
1. Sur la page du dépôt vide, clique le lien **uploading an existing file**
   (ou **Add file → Upload files**).
2. Décompresse d'abord le zip du projet sur ton PC.
3. Sélectionne **tout le contenu du dossier `android-m1`**
   (les dossiers `app`, `gradle`, `.github`, et les fichiers `build.gradle.kts`,
   `settings.gradle.kts`, `gradle.properties`, etc.) et glisse-les dans la page.

   > Important : dépose le **contenu** du dossier, pas le dossier `android-m1`
   > lui-même. À la racine du dépôt on doit voir `app/`, `gradle/`, `.github/`,
   > `settings.gradle.kts`…
   >
   > Le dossier `.github` commence par un point : s'il n'apparaît pas quand tu
   > sélectionnes les fichiers, active « afficher les fichiers cachés » dans ton
   > explorateur, ou glisse le dossier `.github` séparément.

4. En bas, clique **Commit changes**.

## Étape 4 — GitHub compile tout seul
1. Clique l'onglet **Actions** en haut du dépôt.
2. Tu vois une tâche **Build APK** qui tourne (rond orange). Attends ~3–5 min
   qu'elle passe au vert ✓.
   - Si GitHub demande d'autoriser les workflows la première fois, clique le
     bouton vert **I understand my workflows, go ahead and enable them**.

## Étape 5 — Télécharger l'APK
1. Clique sur la tâche terminée (ligne verte **Build APK**).
2. En bas de la page, section **Artifacts**, clique **app-debug-apk**.
3. Ça télécharge un fichier `app-debug-apk.zip`. Décompresse-le :
   dedans il y a **`app-debug.apk`**.

## Étape 6 — Installer sur le téléphone
1. Envoie `app-debug.apk` sur le téléphone Android (Drive, WhatsApp, câble…).
2. Ouvre le fichier sur le téléphone.
3. Android dit « sources inconnues » → **Paramètres** → autorise cette source →
   **Installer**.
4. Ouvre **FamiGuard**, puis donne les 2 autorisations demandées
   (données d'usage + accessibilité). Voir le README principal.

---

### Si la tâche échoue (rond rouge)
Ouvre la tâche, clique l'étape en rouge, copie le message d'erreur et
envoie-le-moi : je corrige le projet.
