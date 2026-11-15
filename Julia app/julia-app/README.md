# FamiGuard — Démo (une seule app, parent OU enfant)

Application Android de démonstration. **Sans Firebase, sans Internet, sans serveur.**
Au premier lancement, elle demande le rôle de ce téléphone :

- **PARENT** → tableau de bord d'exemple (« Sofia ») : temps d'écran, alerte, blocage.
  Sert à montrer à quoi ressemble le produit.
- **ENFANT** → fonctionnement réel sur CE téléphone : son vrai temps d'écran par
  application + blocage réel (si tu bloques une appli et que tu l'ouvres,
  l'écran « application en pause » apparaît).

> Le côté « le parent voit le téléphone de l'enfant à distance » n'est pas dans
> cette démo : il demande un serveur (Firebase). Ici, chaque rôle se montre sur
> un téléphone, ce qui suffit pour une présentation.

---

## Obtenir l'APK (GitHub, sans Android Studio)

Si le dépôt `famiguard-m1` existe déjà :
1. **Add file → Upload files** → glisse le **dossier `app`** (remplace l'ancien) et le
   fichier **`build.gradle.kts`** de la racine → **Commit changes**.
2. Onglet **Actions** → attends le ✓ vert → **Artifacts → app-debug-apk** → télécharge.

Détails complets dans `INSTALLER_SANS_ANDROID_STUDIO.md`.

## Installer
- Envoie `app-debug.apk` sur le téléphone → ouvre-le → autorise les sources inconnues → Installe.
- Play Protect bloque ? → Play Store → profil → Play Protect → ⚙️ → désactive l'analyse,
  installe, puis réactive.

## Première utilisation
- Choisis **Parent** ou **Enfant**.
- En mode Enfant, donne les 2 autorisations (données d'usage + accessibilité) pour voir
  le vrai temps d'écran et bloquer des applications.
- Bouton **« Changer de rôle »** en bas pour rebasculer.
