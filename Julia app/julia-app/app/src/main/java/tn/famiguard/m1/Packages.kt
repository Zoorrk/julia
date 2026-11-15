package tn.famiguard.m1

/** Paquets connus — pour appliquer « bloquer le navigateur / les réseaux sociaux ». */
object Packages {
    val browsers = setOf(
        "com.android.chrome", "com.sec.android.app.sbrowser", "org.mozilla.firefox",
        "com.opera.browser", "com.opera.mini.native", "com.microsoft.emmx",
        "com.brave.browser", "com.duckduckgo.mobile.android", "com.android.browser",
        "com.UCMobile.intl", "com.kiwibrowser.browser"
    )
    val social = setOf(
        "com.zhiliaoapp.musically", "com.ss.android.ugc.trill",  // TikTok
        "com.instagram.android", "com.facebook.katana", "com.facebook.lite",
        "com.snapchat.android", "com.twitter.android", "com.x.android",
        "com.whatsapp", "org.telegram.messenger", "com.discord"
    )
}
