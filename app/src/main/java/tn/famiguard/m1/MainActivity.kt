package tn.famiguard.m1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = FamiColors) {
                Surface(Modifier.fillMaxSize(), color = Brand.bg) { AppRoot() }
            }
        }
    }
}

@Composable
fun AppRoot() {
    val ctx = LocalContext.current
    var role by remember { mutableStateOf(Prefs.role(ctx)) }
    when (role) {
        null -> RolePicker { r -> Prefs.setRole(ctx, r); role = r }
        Prefs.ROLE_PARENT -> ParentApp { Prefs.reset(ctx); role = null }
        else -> ChildReal { Prefs.reset(ctx); role = null }
    }
}

/* ============ composants ============ */
@Composable
private fun Shield(size: Int = 44) = Box(
    Modifier.size(size.dp).clip(RoundedCornerShape((size * 0.3).dp)).background(Brand.teal),
    contentAlignment = Alignment.Center
) { Text("🛡", fontSize = (size * 0.5).sp) }

@Composable
private fun TopBar(title: String, subtitle: String, trailing: @Composable (() -> Unit)? = null) =
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Shield(44); Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Brand.ink)
            Text(subtitle, fontSize = 12.5.sp, color = Brand.muted)
        }
        trailing?.invoke()
    }

@Composable
private fun StatusPill(text: String, dot: Color, bg: Color, fg: Color) = Row(
    Modifier.clip(RoundedCornerShape(50)).background(bg).padding(horizontal = 10.dp, vertical = 5.dp),
    verticalAlignment = Alignment.CenterVertically
) {
    Box(Modifier.size(7.dp).clip(RoundedCornerShape(50)).background(dot)); Spacer(Modifier.width(6.dp))
    Text(text, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = fg)
}

@Composable
private fun SoftCard(bg: Color = Brand.surface, onClick: (() -> Unit)? = null, content: @Composable ColumnScope.() -> Unit) =
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(bg)
            .border(1.dp, Brand.line, RoundedCornerShape(18.dp))
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(16.dp), content = content
    )

@Composable
private fun UsageBar(fraction: Float, color: Color, h: Int = 6) =
    Box(Modifier.fillMaxWidth().height(h.dp).clip(RoundedCornerShape(50)).background(Brand.line)) {
        Box(Modifier.fillMaxWidth(fraction.coerceIn(0.02f, 1f)).fillMaxHeight()
            .clip(RoundedCornerShape(50)).background(color))
    }

@Composable
private fun Eyebrow(text: String) = Text(
    text.uppercase(), fontSize = 11.sp, letterSpacing = 1.2.sp,
    fontWeight = FontWeight.Bold, color = Brand.muted
)

private fun sevColor(s: String) = when (s) { "Élevé" -> Brand.red; "Moyen" -> Brand.amber; else -> Brand.muted }
private fun sevBg(s: String) = when (s) { "Élevé" -> Brand.redBg; "Moyen" -> Brand.amberBg; else -> Brand.surface2 }

private fun todayLabel(): String =
    SimpleDateFormat("EEEE d MMMM", Locale.FRENCH).format(Date()).replaceFirstChar { it.uppercase() }

/* ============ Choix du rôle ============ */
@Composable
fun RolePicker(onPick: (String) -> Unit) = Column(
    Modifier.fillMaxSize().padding(horizontal = 24.dp),
    verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally
) {
    Shield(64); Spacer(Modifier.height(16.dp))
    Text("Julia", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Brand.ink)
    Text("Protection numérique de la famille", fontSize = 13.sp, color = Brand.muted)
    Spacer(Modifier.height(36.dp))
    RoleCard("👨‍👩‍👧", "Téléphone du parent", "Suivre et protéger", Brand.teal) { onPick(Prefs.ROLE_PARENT) }
    Spacer(Modifier.height(14.dp))
    RoleCard("🧒", "Téléphone de l'enfant", "Appareil à protéger", Brand.amber) { onPick(Prefs.ROLE_CHILD) }
    Spacer(Modifier.height(28.dp))
    Text("Installez Julia sur les deux téléphones et choisissez le rôle sur chacun.",
        fontSize = 12.sp, color = Brand.muted, textAlign = TextAlign.Center)
}

@Composable
private fun RoleCard(emoji: String, title: String, sub: String, accent: Color, onClick: () -> Unit) = Row(
    Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Brand.surface)
        .border(1.dp, Brand.line, RoundedCornerShape(18.dp)).clickable { onClick() }.padding(18.dp),
    verticalAlignment = Alignment.CenterVertically
) {
    Box(Modifier.size(46.dp).clip(RoundedCornerShape(13.dp)).background(accent.copy(alpha = 0.16f)),
        contentAlignment = Alignment.Center) { Text(emoji, fontSize = 22.sp) }
    Spacer(Modifier.width(14.dp))
    Column(Modifier.weight(1f)) {
        Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Brand.ink)
        Text(sub, fontSize = 12.5.sp, color = Brand.muted)
    }
    Text("→", fontSize = 20.sp, color = accent)
}

/* ============ PARENT ============ */
private data class AppItem(val label: String, val time: String, val frac: Float, var blocked: Boolean)

@Composable
fun ParentApp(onReset: () -> Unit) {
    var tab by remember { mutableStateOf(0) }
    var selected by remember { mutableStateOf<Alert?>(null) }
    var showApps by remember { mutableStateOf(false) }

    val apps = remember {
        mutableStateListOf(
            AppItem("TikTok", "2h05", 1.0f, false),
            AppItem("Instagram", "1h20", 0.64f, false),
            AppItem("WhatsApp", "47 min", 0.37f, false),
            AppItem("YouTube", "38 min", 0.30f, false),
            AppItem("Snapchat", "22 min", 0.18f, false),
            AppItem("Chrome", "15 min", 0.12f, false),
        )
    }
    val net = remember { mutableStateMapOf("cut" to false, "social" to false, "browser" to false, "bedtime" to true) }

    if (selected != null) { AlertDetail(selected!!) { selected = null }; return }
    if (showApps) { AppsScreen(apps) { showApps = false }; return }

    Scaffold(
        containerColor = Brand.bg,
        bottomBar = {
            NavigationBar(containerColor = Brand.surface) {
                val items = listOf("Accueil" to "🏠", "Alertes" to "🔔", "Carte" to "📍", "Stats" to "📊")
                items.forEachIndexed { i, (label, emoji) ->
                    NavigationBarItem(
                        selected = tab == i, onClick = { tab = i },
                        icon = { Text(emoji, fontSize = 18.sp) },
                        label = { Text(label, fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Brand.teal, selectedTextColor = Brand.teal,
                            indicatorColor = Brand.surface2, unselectedIconColor = Brand.muted,
                            unselectedTextColor = Brand.muted
                        )
                    )
                }
            }
        }
    ) { pad ->
        Box(Modifier.padding(pad)) {
            when (tab) {
                0 -> ParentHome(apps, net, { selected = it }, { showApps = true }, { tab = 2 }, onReset)
                1 -> AlertsList { selected = it }
                2 -> LocationScreen()
                else -> StatsScreen()
            }
        }
    }
}

@Composable
private fun ParentHome(
    apps: List<AppItem>, net: MutableMap<String, Boolean>,
    onOpenAlert: (Alert) -> Unit, onOpenApps: () -> Unit, onLocation: () -> Unit, onReset: () -> Unit
) {
    val last = Demo.alerts.first()
    val blockedCount = apps.count { it.blocked }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp, 20.dp, 16.dp, 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { TopBar("Sofia · 14 ans", todayLabel()) { StatusPill("En ligne", Brand.green, Brand.greenBg, Brand.green) } }
        item {
            SoftCard(onClick = onOpenApps) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Eyebrow("Temps d'écran aujourd'hui"); Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text("4h12", fontSize = 40.sp, fontWeight = FontWeight.Bold, color = Brand.ink)
                            Spacer(Modifier.width(10.dp))
                            Text("+45 min", fontSize = 12.5.sp, color = Brand.red, modifier = Modifier.padding(bottom = 8.dp))
                        }
                    }
                    Text("›", fontSize = 30.sp, color = Brand.muted)
                }
                Spacer(Modifier.height(10.dp))
                Text("Voir les applications · ${apps.size} apps" + if (blockedCount > 0) " · $blockedCount bloquée(s)" else "",
                    fontSize = 12.5.sp, color = Brand.teal)
            }
        }
        item { InternetCard(net) }
        item { Eyebrow("Dernière alerte") }
        item { AlertRow(last) { onOpenAlert(last) } }
        item { Eyebrow("Localisation") }
        item { LocationCard(Demo.location, compact = true, onClick = onLocation) }
        item {
            Spacer(Modifier.height(4.dp))
            Text("Démonstration — données d'exemple.", fontSize = 11.5.sp, color = Brand.muted, textAlign = TextAlign.Center)
            TextButton(onReset, Modifier.fillMaxWidth()) { Text("Changer de rôle", color = Brand.muted) }
        }
    }
}

@Composable
private fun InternetCard(net: MutableMap<String, Boolean>) {
    val cut = net["cut"] == true
    SoftCard(if (cut) Brand.redBg else Brand.surface) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("🌐", fontSize = 18.sp); Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                Text("Accès Internet", fontWeight = FontWeight.Bold, color = Brand.ink)
                Text(if (cut) "Internet coupé pour l'enfant" else "Internet autorisé", fontSize = 12.sp,
                    color = if (cut) Brand.red else Brand.muted)
            }
            Switch(checked = cut, onCheckedChange = { net["cut"] = it })
        }
        if (!cut) {
            Spacer(Modifier.height(6.dp))
            NetRow("Bloquer les réseaux sociaux", net, "social")
            NetRow("Bloquer le navigateur", net, "browser")
            NetRow("Couper Internet la nuit (21h–7h)", net, "bedtime")
        }
    }
}

@Composable
private fun NetRow(label: String, net: MutableMap<String, Boolean>, key: String) = Row(
    Modifier.fillMaxWidth().padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically
) {
    Text(label, fontSize = 13.sp, color = Brand.ink, modifier = Modifier.weight(1f))
    Switch(checked = net[key] == true, onCheckedChange = { net[key] = it })
}

@Composable
private fun AppsScreen(apps: MutableList<AppItem>, onBack: () -> Unit) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp, 18.dp, 16.dp, 28.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { TextButton(onBack, contentPadding = PaddingValues(0.dp)) { Text("← Retour", color = Brand.teal) } }
        item {
            Column {
                Text("Applications utilisées", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Brand.ink)
                Text("Appuie sur « Bloquer » pour mettre une appli en pause sur le téléphone de Sofia", fontSize = 12.5.sp, color = Brand.muted)
            }
        }
        items(apps.size) { i ->
            val a = apps[i]
            SoftCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(42.dp).clip(RoundedCornerShape(11.dp)).background(Brand.surface2),
                        contentAlignment = Alignment.Center) { Text(a.label.take(1), fontWeight = FontWeight.Bold, color = Brand.teal) }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(a.label, fontWeight = FontWeight.SemiBold, color = Brand.ink)
                            if (a.blocked) { Spacer(Modifier.width(8.dp)); StatusPill("Bloquée", Brand.red, Brand.redBg, Brand.red) }
                        }
                        Text(a.time, fontSize = 12.5.sp, color = Brand.muted)
                    }
                    Spacer(Modifier.width(8.dp))
                    if (a.blocked) OutlinedButton({ apps[i] = a.copy(blocked = false) }, shape = RoundedCornerShape(11.dp)) { Text("Débloquer") }
                    else Button({ apps[i] = a.copy(blocked = true) }, shape = RoundedCornerShape(11.dp)) { Text("Bloquer") }
                }
                Spacer(Modifier.height(10.dp))
                UsageBar(a.frac, if (a.blocked) Brand.red else Brand.teal)
            }
        }
    }
}

@Composable
private fun AlertsList(onOpenAlert: (Alert) -> Unit) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp, 20.dp, 16.dp, 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Column {
                Text("Alertes", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Brand.ink)
                Text("${Demo.alerts.size} incidents détectés · 7 derniers jours", fontSize = 12.5.sp, color = Brand.muted)
            }
        }
        items(Demo.alerts, key = { it.id }) { a -> AlertRow(a) { onOpenAlert(a) } }
    }
}

@Composable
private fun AlertRow(a: Alert, onClick: () -> Unit) = Column(
    Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Brand.surface)
        .border(1.dp, Brand.line, RoundedCornerShape(18.dp)).clickable { onClick() }.padding(14.dp)
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        StatusPill(a.category, sevColor(a.severity), sevBg(a.severity), sevColor(a.severity))
        Spacer(Modifier.weight(1f)); Text(a.time, fontSize = 11.5.sp, color = Brand.muted)
    }
    Spacer(Modifier.height(8.dp))
    Text("« ${a.excerpt} »", fontSize = 14.sp, color = Brand.ink)
    Spacer(Modifier.height(6.dp))
    Text("${a.app} · ${a.sender}", fontSize = 12.sp, color = Brand.muted)
}

@Composable
private fun AlertDetail(a: Alert, onBack: () -> Unit) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp, 18.dp, 16.dp, 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { TextButton(onBack, contentPadding = PaddingValues(0.dp)) { Text("← Retour", color = Brand.teal) } }
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                StatusPill(a.severity, sevColor(a.severity), sevBg(a.severity), sevColor(a.severity))
                Spacer(Modifier.width(8.dp)); Text(a.time, fontSize = 12.sp, color = Brand.muted)
            }
        }
        item { Text(a.category, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Brand.ink) }
        item {
            SoftCard(sevBg(a.severity)) {
                Eyebrow("Message détecté"); Spacer(Modifier.height(8.dp))
                Text("« ${a.excerpt} »", fontSize = 15.sp, color = Brand.ink)
                Spacer(Modifier.height(8.dp)); Text("${a.app} · ${a.sender}", fontSize = 12.sp, color = Brand.muted)
            }
        }
        item {
            SoftCard {
                Eyebrow("Ce que Julia a détecté"); Spacer(Modifier.height(8.dp))
                a.whatDetected.forEach { Row(Modifier.padding(vertical = 3.dp)) { Text("•  ", color = Brand.teal); Text(it, fontSize = 13.5.sp, color = Brand.ink) } }
            }
        }
        item {
            SoftCard { Eyebrow("Ce que cela peut signifier"); Spacer(Modifier.height(8.dp)); Text(a.whatItMeans, fontSize = 13.5.sp, color = Brand.ink) }
        }
        item {
            SoftCard(Brand.greenBg) {
                Eyebrow("Recommandations"); Spacer(Modifier.height(10.dp))
                a.recommendations.forEachIndexed { i, r ->
                    Row(Modifier.padding(vertical = 5.dp), verticalAlignment = Alignment.Top) {
                        Box(Modifier.size(22.dp).clip(RoundedCornerShape(50)).background(Brand.green),
                            contentAlignment = Alignment.Center) { Text("${i + 1}", color = Brand.tealInk, fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                        Spacer(Modifier.width(10.dp)); Text(r, fontSize = 13.5.sp, color = Brand.ink)
                    }
                }
            }
        }
        item {
            SoftCard {
                Eyebrow("Comment en parler"); Spacer(Modifier.height(8.dp))
                Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(Brand.surface2).padding(12.dp)) {
                    Text(a.script, fontSize = 13.5.sp, color = Brand.ink)
                }
            }
        }
        item { SoftCard { Eyebrow("Besoin d'aide ?"); Spacer(Modifier.height(6.dp)); Text(Demo.helpline, fontSize = 12.5.sp, color = Brand.muted) } }
    }
}

/* ============ Localisation ============ */
@Composable
private fun LocationScreen() {
    var gpsOff by remember { mutableStateOf(false) }
    val loc = if (gpsOff) Demo.lastKnown else Demo.location
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp, 20.dp, 16.dp, 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Localisation", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Brand.ink)
                    Text("Position de Sofia", fontSize = 12.5.sp, color = Brand.muted)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("GPS coupé", fontSize = 11.sp, color = Brand.muted); Spacer(Modifier.width(6.dp))
                    Switch(checked = gpsOff, onCheckedChange = { gpsOff = it })
                }
            }
        }
        item { MapPanel(loc) }
        item { LocationCard(loc, compact = false, onClick = null) }
        item {
            Text("Démonstration. Dans la vraie version, la position vient du GPS du téléphone de l'enfant ; sans GPS, la dernière position connue est affichée.",
                fontSize = 11.5.sp, color = Brand.muted)
        }
    }
}

@Composable
private fun MapPanel(loc: ChildLocation) {
    val road = Color(0xFF32415C); val block = Color(0xFF1E2A3E); val water = Color(0xFF123247); val park = Color(0xFF17351F)
    BoxWithConstraints(
        Modifier.fillMaxWidth().height(250.dp).clip(RoundedCornerShape(18.dp)).border(1.dp, Brand.line, RoundedCornerShape(18.dp))
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width; val h = size.height
            drawRect(Color(0xFF151D2C))
            val cx = 5; val cy = 4; val gx = w / cx; val gy = h / cy
            for (i in 0 until cx) for (j in 0 until cy) {
                drawRect(block, topLeft = Offset(i * gx + 5f, j * gy + 5f), size = Size(gx - 10f, gy - 10f))
            }
            drawRect(water, topLeft = Offset(0f, h * 0.80f), size = Size(w, h * 0.20f))
            drawRect(park, topLeft = Offset(w * 0.62f, h * 0.08f), size = Size(w * 0.30f, h * 0.26f))
            // avenues
            drawLine(road, Offset(0f, h * 0.5f), Offset(w, h * 0.5f), strokeWidth = 7f)
            drawLine(road, Offset(w * 0.5f, 0f), Offset(w * 0.5f, h), strokeWidth = 7f)
            // itinéraire
            drawLine(Brand.teal, Offset(w * 0.18f, h * 0.92f), Offset(w * 0.5f, h * 0.5f), strokeWidth = 5f)
            drawLine(Brand.teal, Offset(w * 0.5f, h * 0.5f), Offset(w * loc.x, h * loc.y), strokeWidth = 5f)
        }
        val mColor = if (loc.live) Brand.teal else Brand.amber
        Box(Modifier.offset(x = maxWidth * loc.x - 18.dp, y = maxHeight * loc.y - 18.dp)) {
            Box(Modifier.size(36.dp).clip(RoundedCornerShape(50)).background(mColor.copy(alpha = 0.25f)))
            Box(Modifier.size(16.dp).align(Alignment.Center).clip(RoundedCornerShape(50)).background(mColor)
                .border(2.dp, Brand.bg, RoundedCornerShape(50)))
        }
        Box(Modifier.align(Alignment.TopStart).padding(10.dp)) {
            StatusPill(if (loc.live) "Position en temps réel" else "Dernière position connue", mColor, Brand.surface, mColor)
        }
    }
}

@Composable
private fun LocationCard(loc: ChildLocation, compact: Boolean, onClick: (() -> Unit)?) {
    val mod = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Brand.surface)
        .border(1.dp, Brand.line, RoundedCornerShape(18.dp))
        .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier).padding(16.dp)
    Column(mod) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("📍", fontSize = 20.sp); Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(loc.address, fontWeight = FontWeight.SemiBold, color = Brand.ink)
                Text("${loc.area} · ${loc.time}", fontSize = 12.sp, color = Brand.muted)
            }
            if (!compact) StatusPill(if (loc.live) "Live" else "GPS off",
                if (loc.live) Brand.green else Brand.amber, if (loc.live) Brand.greenBg else Brand.amberBg,
                if (loc.live) Brand.green else Brand.amber)
        }
        if (!compact) { Spacer(Modifier.height(10.dp)); Text("Précision ± ${loc.accuracyM} m", fontSize = 12.sp, color = Brand.muted) }
    }
}

/* ============ Statistiques ============ */
@Composable
private fun StatsScreen() {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp, 20.dp, 16.dp, 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Column {
                Text("Statistiques", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Brand.ink)
                Text("7 derniers jours", fontSize = 12.5.sp, color = Brand.muted)
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatTile("10", "Alertes", Brand.red, Modifier.weight(1f))
                StatTile("6", "Harcèlement", Brand.amber, Modifier.weight(1f))
                StatTile("3h30", "Écran / jour", Brand.teal, Modifier.weight(1f))
            }
        }
        item {
            SoftCard { Eyebrow("Alertes par jour"); Spacer(Modifier.height(14.dp)); BarChart(Demo.alertsPerDay.map { it.toFloat() }, Demo.weekDays, Brand.red) }
        }
        item {
            SoftCard {
                Eyebrow("Par catégorie"); Spacer(Modifier.height(12.dp))
                val max = Demo.byCategory.maxOf { it.second }.toFloat()
                Demo.byCategory.forEach { (name, n) ->
                    Row(Modifier.padding(vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(name, fontSize = 12.5.sp, color = Brand.ink, modifier = Modifier.width(120.dp))
                        Box(Modifier.weight(1f)) { UsageBar(n / max, Brand.teal, 10) }
                        Spacer(Modifier.width(8.dp)); Text("$n", fontSize = 12.sp, color = Brand.muted)
                    }
                }
            }
        }
        item {
            SoftCard { Eyebrow("Temps d'écran par jour (min)"); Spacer(Modifier.height(14.dp)); BarChart(Demo.screenTimePerDay.map { it.toFloat() }, Demo.weekDays, Brand.teal) }
        }
    }
}

@Composable
private fun StatTile(value: String, label: String, accent: Color, modifier: Modifier = Modifier) =
    Column(modifier.clip(RoundedCornerShape(16.dp)).background(Brand.surface)
        .border(1.dp, Brand.line, RoundedCornerShape(16.dp)).padding(14.dp)) {
        Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = accent)
        Text(label, fontSize = 11.sp, color = Brand.muted)
    }

@Composable
private fun BarChart(values: List<Float>, labels: List<String>, color: Color) {
    val max = (values.maxOrNull() ?: 1f).coerceAtLeast(1f)
    Row(Modifier.fillMaxWidth().height(120.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Bottom) {
        values.forEachIndexed { i, v ->
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom) {
                Box(Modifier.fillMaxWidth().fillMaxHeight((v / max).coerceIn(0.03f, 1f)).clip(RoundedCornerShape(6.dp)).background(color))
                Spacer(Modifier.height(6.dp)); Text(labels.getOrElse(i) { "" }, fontSize = 10.sp, color = Brand.muted)
            }
        }
    }
}

/* ============ ENFANT (réel) ============ */
@Composable
fun ChildReal(onReset: () -> Unit) {
    val ctx = LocalContext.current
    var tick by remember { mutableStateOf(0) }
    val owner = LocalLifecycleOwner.current
    DisposableEffect(owner) {
        val obs = LifecycleEventObserver { _, e -> if (e == Lifecycle.Event.ON_RESUME) tick++ }
        owner.lifecycle.addObserver(obs); onDispose { owner.lifecycle.removeObserver(obs) }
    }
    val hasUsage = remember(tick) { UsageRepository.hasUsageAccess(ctx) }
    val accOn = remember(tick) { BlockAccessibilityService.isEnabled(ctx) }
    val apps = remember(tick, hasUsage) { if (hasUsage) UsageRepository.todayUsage(ctx) else emptyList() }
    val total = apps.sumOf { it.millisToday }
    val maxMs = (apps.maxOfOrNull { it.millisToday } ?: 1L).coerceAtLeast(1L)
    var blocked by remember(tick) { mutableStateOf(Prefs.blocked(ctx)) }
    val ok = hasUsage && accOn

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp, 20.dp, 16.dp, 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            TopBar("Cet appareil", todayLabel()) {
                if (ok) StatusPill("Protégé", Brand.green, Brand.greenBg, Brand.green)
                else StatusPill("À configurer", Brand.amber, Brand.amberBg, Brand.amber)
            }
        }
        if (!ok) item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (!hasUsage) SetupCard("1", "Accès aux données d'usage", "Nécessaire pour mesurer le temps d'écran.") { UsageRepository.openUsageAccessSettings(ctx) }
                if (!accOn) SetupCard("2", "Activer le blocage", "Nécessaire pour mettre une application en pause.") { BlockAccessibilityService.openAccessibilitySettings(ctx) }
            }
        }
        item {
            SoftCard {
                Eyebrow("Temps d'écran aujourd'hui"); Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(UsageRepository.formatDuration(total), fontSize = 40.sp, fontWeight = FontWeight.Bold, color = Brand.ink)
                    Spacer(Modifier.width(10.dp)); Text("${apps.size} apps", fontSize = 13.sp, color = Brand.muted, modifier = Modifier.padding(bottom = 8.dp))
                }
                if (apps.isNotEmpty()) { Spacer(Modifier.height(10.dp)); Text("La plus utilisée : ${apps.first().label}", fontSize = 12.5.sp, color = Brand.muted) }
            }
        }
        if (hasUsage && apps.isEmpty()) item { Text("Aucun usage mesuré pour l'instant aujourd'hui.", color = Brand.muted, fontSize = 13.sp) }
        if (apps.isNotEmpty()) item { Eyebrow("Applications") }
        items(apps, key = { it.packageName }) { app ->
            val isBlocked = blocked.contains(app.packageName)
            SoftCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    app.icon?.let { Image(it.toBitmap(80, 80).asImageBitmap(), null, Modifier.size(42.dp).clip(RoundedCornerShape(11.dp))); Spacer(Modifier.width(12.dp)) }
                    Column(Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(app.label, fontWeight = FontWeight.SemiBold, color = Brand.ink)
                            if (isBlocked) { Spacer(Modifier.width(8.dp)); StatusPill("Bloquée", Brand.red, Brand.redBg, Brand.red) }
                        }
                        Text(UsageRepository.formatDuration(app.millisToday), fontSize = 12.5.sp, color = Brand.muted)
                    }
                    Spacer(Modifier.width(8.dp))
                    if (isBlocked) OutlinedButton({ Prefs.toggleBlocked(ctx, app.packageName); blocked = Prefs.blocked(ctx) }, shape = RoundedCornerShape(11.dp)) { Text("Débloquer") }
                    else Button({ Prefs.toggleBlocked(ctx, app.packageName); blocked = Prefs.blocked(ctx) }, shape = RoundedCornerShape(11.dp)) { Text("Bloquer") }
                }
                Spacer(Modifier.height(10.dp)); UsageBar(app.millisToday.toFloat() / maxMs, if (isBlocked) Brand.red else Brand.teal)
            }
        }
        item {
            Spacer(Modifier.height(6.dp))
            Text("Le contenu des messages et les photos ne sont jamais lus. Seul le temps d'usage est mesuré, sur l'appareil.",
                fontSize = 11.5.sp, color = Brand.muted, textAlign = TextAlign.Center)
            TextButton(onReset, Modifier.fillMaxWidth()) { Text("Changer de rôle", color = Brand.muted) }
        }
    }
}

@Composable
private fun SetupCard(num: String, title: String, body: String, onClick: () -> Unit) = SoftCard(Brand.amberBg) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(26.dp).clip(RoundedCornerShape(50)).background(Brand.amber), contentAlignment = Alignment.Center) {
            Text(num, color = Brand.tealInk, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, color = Brand.ink, fontSize = 14.5.sp)
            Text(body, fontSize = 12.sp, color = Brand.muted)
        }
    }
    Spacer(Modifier.height(10.dp))
    Button(onClick, Modifier.fillMaxWidth(), shape = RoundedCornerShape(11.dp)) { Text("Autoriser") }
}
