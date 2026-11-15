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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.firebase.firestore.ListenerRegistration
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

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
    var code by remember { mutableStateOf(Prefs.familyCode(ctx)) }
    when {
        role == null -> RolePicker { r -> Prefs.setRole(ctx, r); role = r }
        code == null && role == Prefs.ROLE_PARENT -> ParentPairing { c -> Prefs.setFamilyCode(ctx, c); code = c }
        code == null -> ChildPairing { c -> Prefs.setFamilyCode(ctx, c); code = c }
        role == Prefs.ROLE_PARENT -> ParentApp(code!!) { Prefs.reset(ctx); role = null; code = null }
        else -> ChildReal(code!!) { Prefs.reset(ctx); role = null; code = null }
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
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier).padding(16.dp), content = content
    )

@Composable
private fun UsageBar(fraction: Float, color: Color, h: Int = 6) =
    Box(Modifier.fillMaxWidth().height(h.dp).clip(RoundedCornerShape(50)).background(Brand.line)) {
        Box(Modifier.fillMaxWidth(fraction.coerceIn(0.02f, 1f)).fillMaxHeight().clip(RoundedCornerShape(50)).background(color))
    }

@Composable
private fun Eyebrow(text: String) = Text(text.uppercase(), fontSize = 11.sp, letterSpacing = 1.2.sp, fontWeight = FontWeight.Bold, color = Brand.muted)

private fun sevColor(s: String) = when (s) { "Élevé" -> Brand.red; "Moyen" -> Brand.amber; else -> Brand.muted }
private fun sevBg(s: String) = when (s) { "Élevé" -> Brand.redBg; "Moyen" -> Brand.amberBg; else -> Brand.surface2 }
private fun todayLabel(): String = SimpleDateFormat("EEEE d MMMM", Locale.FRENCH).format(Date()).replaceFirstChar { it.uppercase() }
private fun fmt(ms: Long): String { val m = ms / 60000; val h = m / 60; return if (h > 0) "${h}h${(m % 60).toString().padStart(2, '0')}" else "${m} min" }

/* ============ Choix du rôle ============ */
@Composable
fun RolePicker(onPick: (String) -> Unit) = Column(
    Modifier.fillMaxSize().padding(horizontal = 24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally
) {
    Shield(64); Spacer(Modifier.height(16.dp))
    Text("Julia", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Brand.ink)
    Text("Protection numérique de la famille", fontSize = 13.sp, color = Brand.muted)
    Spacer(Modifier.height(36.dp))
    RoleCard("👨‍👩‍👧", "Téléphone du parent", "Suivre et protéger", Brand.teal) { onPick(Prefs.ROLE_PARENT) }
    Spacer(Modifier.height(14.dp))
    RoleCard("🧒", "Téléphone de l'enfant", "Appareil à protéger", Brand.amber) { onPick(Prefs.ROLE_CHILD) }
    Spacer(Modifier.height(28.dp))
    Text("Installez Julia sur les deux téléphones et choisissez le rôle sur chacun.", fontSize = 12.sp, color = Brand.muted, textAlign = TextAlign.Center)
}

@Composable
private fun RoleCard(emoji: String, title: String, sub: String, accent: Color, onClick: () -> Unit) = Row(
    Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Brand.surface)
        .border(1.dp, Brand.line, RoundedCornerShape(18.dp)).clickable { onClick() }.padding(18.dp),
    verticalAlignment = Alignment.CenterVertically
) {
    Box(Modifier.size(46.dp).clip(RoundedCornerShape(13.dp)).background(accent.copy(alpha = 0.16f)), contentAlignment = Alignment.Center) { Text(emoji, fontSize = 22.sp) }
    Spacer(Modifier.width(14.dp))
    Column(Modifier.weight(1f)) {
        Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Brand.ink)
        Text(sub, fontSize = 12.5.sp, color = Brand.muted)
    }
    Text("→", fontSize = 20.sp, color = accent)
}

/* ============ Jumelage ============ */
private fun newCode(): String {
    val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
    return (1..6).map { chars[Random.nextInt(chars.length)] }.joinToString("")
}

@Composable
fun ParentPairing(onReady: (String) -> Unit) {
    val code = remember { newCode() }
    LaunchedEffect(code) { Fb.createFamily(code) }
    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Code de jumelage", fontSize = 16.sp, color = Brand.muted)
        Spacer(Modifier.height(16.dp))
        Text(code, fontSize = 46.sp, fontWeight = FontWeight.Bold, color = Brand.teal, fontFamily = FontFamily.Monospace)
        Spacer(Modifier.height(16.dp))
        Text("Sur le téléphone de l'enfant : ouvre Julia, choisis « enfant » et tape ce code.", color = Brand.muted, textAlign = TextAlign.Center)
        Spacer(Modifier.height(28.dp))
        Button({ onReady(code) }, Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) { Text("J'ai noté le code → continuer") }
    }
}

@Composable
fun ChildPairing(onReady: (String) -> Unit) {
    var input by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Entre le code du parent", fontSize = 18.sp, color = Brand.ink)
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(value = input, onValueChange = { if (it.length <= 6) input = it.uppercase() }, singleLine = true,
            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 26.sp, fontFamily = FontFamily.Monospace, color = Brand.ink),
            label = { Text("Code à 6 caractères") })
        Spacer(Modifier.height(24.dp))
        Button({ if (input.length == 6) onReady(input) }, enabled = input.length == 6, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) { Text("Relier ce téléphone") }
    }
}

/* ============ PARENT ============ */
@Composable
fun ParentApp(code: String, onReset: () -> Unit) {
    var tab by remember { mutableStateOf(0) }
    var selected by remember { mutableStateOf<Alert?>(null) }
    var showApps by remember { mutableStateOf(false) }

    var usageApps by remember { mutableStateOf<List<Triple<String, String, Long>>>(emptyList()) }
    var total by remember { mutableStateOf(0L) }
    var model by remember { mutableStateOf("") }
    var blocked by remember { mutableStateOf<Set<String>>(emptySet()) }
    val net = remember { mutableStateMapOf<String, Boolean>() }
    var alerts by remember { mutableStateOf<List<Alert>>(emptyList()) }

    DisposableEffect(code) {
        val regs = mutableListOf<ListenerRegistration>()
        regs += Fb.usageDoc(code).addSnapshotListener { s, _ ->
            if (s != null && s.exists()) {
                total = s.getLong("totalMs") ?: 0L
                model = s.getString("model") ?: ""
                @Suppress("UNCHECKED_CAST")
                val raw = (s.get("apps") as? List<Map<String, Any>>) ?: emptyList()
                usageApps = raw.mapNotNull {
                    val p = it["pkg"] as? String ?: return@mapNotNull null
                    Triple(p, it["label"] as? String ?: p, (it["ms"] as? Number)?.toLong() ?: 0L)
                }
            }
        }
        regs += Fb.rulesDoc(code).addSnapshotListener { s, _ ->
            @Suppress("UNCHECKED_CAST")
            blocked = ((s?.get("blocked") as? List<String>) ?: emptyList()).toSet()
            @Suppress("UNCHECKED_CAST")
            val n = (s?.get("net") as? Map<String, Boolean>) ?: emptyMap()
            net.clear(); net.putAll(n)
        }
        regs += Fb.recentAlerts(code).addSnapshotListener { s, _ ->
            alerts = s?.documents?.mapNotNull { d ->
                val cat = d.getString("category") ?: return@mapNotNull null
                alertFrom(cat, d.getString("app") ?: "", d.getString("snippet") ?: "", d.id.hashCode())
            } ?: emptyList()
        }
        onDispose { regs.forEach { it.remove() } }
    }

    fun toggleBlock(pkg: String) {
        val next = blocked.toMutableSet().apply { if (contains(pkg)) remove(pkg) else add(pkg) }
        Fb.setBlocked(code, next.toList())
    }
    fun setNet(key: String, v: Boolean) { net[key] = v; Fb.setNet(code, net.toMap()) }

    if (selected != null) { AlertDetail(selected!!) { selected = null }; return }
    if (showApps) { AppsScreen(usageApps, blocked, ::toggleBlock) { showApps = false }; return }

    Scaffold(containerColor = Brand.bg, bottomBar = {
        NavigationBar(containerColor = Brand.surface) {
            listOf("Accueil" to "🏠", "Alertes" to "🔔", "Carte" to "📍", "Stats" to "📊").forEachIndexed { i, (label, emoji) ->
                NavigationBarItem(selected = tab == i, onClick = { tab = i },
                    icon = { Text(emoji, fontSize = 18.sp) }, label = { Text(label, fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = Brand.teal, selectedTextColor = Brand.teal,
                        indicatorColor = Brand.surface2, unselectedIconColor = Brand.muted, unselectedTextColor = Brand.muted))
            }
        }
    }) { pad ->
        Box(Modifier.padding(pad)) {
            when (tab) {
                0 -> ParentHome(code, model, total, usageApps.size, blocked.size, net, ::setNet, alerts.firstOrNull(),
                    { selected = it }, { showApps = true }, { tab = 2 }, onReset)
                1 -> AlertsList(alerts) { selected = it }
                2 -> LocationScreen()
                else -> StatsScreen()
            }
        }
    }
}

@Composable
private fun ParentHome(
    code: String, model: String, total: Long, appCount: Int, blockedCount: Int,
    net: Map<String, Boolean>, setNet: (String, Boolean) -> Unit, lastAlert: Alert?,
    onOpenAlert: (Alert) -> Unit, onOpenApps: () -> Unit, onLocation: () -> Unit, onReset: () -> Unit
) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp, 20.dp, 16.dp, 24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            TopBar("Téléphone de l'enfant", if (model.isBlank()) "En attente · code $code" else "$model · code $code") {
                StatusPill(if (model.isBlank()) "En attente" else "Relié", if (model.isBlank()) Brand.amber else Brand.green,
                    if (model.isBlank()) Brand.amberBg else Brand.greenBg, if (model.isBlank()) Brand.amber else Brand.green)
            }
        }
        item {
            SoftCard(onClick = onOpenApps) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Eyebrow("Temps d'écran aujourd'hui"); Spacer(Modifier.height(8.dp))
                        Text(fmt(total), fontSize = 40.sp, fontWeight = FontWeight.Bold, color = Brand.ink)
                    }
                    Text("›", fontSize = 30.sp, color = Brand.muted)
                }
                Spacer(Modifier.height(10.dp))
                Text("Voir les applications · $appCount apps" + if (blockedCount > 0) " · $blockedCount bloquée(s)" else "", fontSize = 12.5.sp, color = Brand.teal)
            }
        }
        item { InternetCard(net, setNet) }
        item { Eyebrow("Dernière alerte") }
        item {
            if (lastAlert != null) AlertRow(lastAlert) { onOpenAlert(lastAlert) }
            else SoftCard { Text("Aucune alerte pour l'instant.", color = Brand.muted, fontSize = 13.sp) }
        }
        item { Eyebrow("Localisation") }
        item { LocationCard(Demo.location, true, onLocation) }
        item {
            Spacer(Modifier.height(4.dp))
            Text("Localisation et statistiques : exemples pour l'instant (prochaines étapes).", fontSize = 11.5.sp, color = Brand.muted, textAlign = TextAlign.Center)
            TextButton(onReset, Modifier.fillMaxWidth()) { Text("Délier / changer de rôle", color = Brand.muted) }
        }
    }
}

@Composable
private fun InternetCard(net: Map<String, Boolean>, setNet: (String, Boolean) -> Unit) {
    val cut = net["cut"] == true
    SoftCard(if (cut) Brand.redBg else Brand.surface) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("🌐", fontSize = 18.sp); Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                Text("Accès Internet", fontWeight = FontWeight.Bold, color = Brand.ink)
                Text(if (cut) "Coupé pour l'enfant" else "Autorisé", fontSize = 12.sp, color = if (cut) Brand.red else Brand.muted)
            }
            Switch(checked = cut, onCheckedChange = { setNet("cut", it) })
        }
        if (!cut) {
            Spacer(Modifier.height(6.dp))
            NetRow("Bloquer les réseaux sociaux", net["social"] == true) { setNet("social", it) }
            NetRow("Bloquer le navigateur", net["browser"] == true) { setNet("browser", it) }
            NetRow("Couper Internet la nuit (21h–7h)", net["bedtime"] == true) { setNet("bedtime", it) }
        }
    }
}

@Composable
private fun NetRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) = Row(
    Modifier.fillMaxWidth().padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically
) {
    Text(label, fontSize = 13.sp, color = Brand.ink, modifier = Modifier.weight(1f))
    Switch(checked = checked, onCheckedChange = onChange)
}

@Composable
private fun AppsScreen(apps: List<Triple<String, String, Long>>, blocked: Set<String>, onToggle: (String) -> Unit, onBack: () -> Unit) {
    val maxMs = (apps.maxOfOrNull { it.third } ?: 1L).coerceAtLeast(1L)
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp, 18.dp, 16.dp, 28.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { TextButton(onBack, contentPadding = PaddingValues(0.dp)) { Text("← Retour", color = Brand.teal) } }
        item {
            Column {
                Text("Applications utilisées", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Brand.ink)
                Text("« Bloquer » met l'appli en pause sur le téléphone de l'enfant", fontSize = 12.5.sp, color = Brand.muted)
            }
        }
        if (apps.isEmpty()) item { Text("En attente des données du téléphone de l'enfant…", color = Brand.muted, fontSize = 13.sp) }
        items(apps, key = { it.first }) { (pkg, label, ms) ->
            val isBlocked = blocked.contains(pkg)
            SoftCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(42.dp).clip(RoundedCornerShape(11.dp)).background(Brand.surface2), contentAlignment = Alignment.Center) { Text(label.take(1), fontWeight = FontWeight.Bold, color = Brand.teal) }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(label, fontWeight = FontWeight.SemiBold, color = Brand.ink)
                            if (isBlocked) { Spacer(Modifier.width(8.dp)); StatusPill("Bloquée", Brand.red, Brand.redBg, Brand.red) }
                        }
                        Text(fmt(ms), fontSize = 12.5.sp, color = Brand.muted)
                    }
                    Spacer(Modifier.width(8.dp))
                    if (isBlocked) OutlinedButton({ onToggle(pkg) }, shape = RoundedCornerShape(11.dp)) { Text("Débloquer") }
                    else Button({ onToggle(pkg) }, shape = RoundedCornerShape(11.dp)) { Text("Bloquer") }
                }
                Spacer(Modifier.height(10.dp)); UsageBar(ms.toFloat() / maxMs, if (isBlocked) Brand.red else Brand.teal)
            }
        }
    }
}

@Composable
private fun AlertsList(alerts: List<Alert>, onOpenAlert: (Alert) -> Unit) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp, 20.dp, 16.dp, 24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Column {
                Text("Alertes", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Brand.ink)
                Text("${alerts.size} incident(s) détecté(s)", fontSize = 12.5.sp, color = Brand.muted)
            }
        }
        if (alerts.isEmpty()) item {
            SoftCard { Text("Aucune alerte. La détection de harcèlement (FR / anglais / tunisien) s'active à l'étape suivante.", color = Brand.muted, fontSize = 13.sp) }
        }
        items(alerts, key = { it.id }) { a -> AlertRow(a) { onOpenAlert(a) } }
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
    Spacer(Modifier.height(8.dp)); Text("« ${a.excerpt} »", fontSize = 14.sp, color = Brand.ink)
    Spacer(Modifier.height(6.dp)); Text("${a.app} · ${a.sender}", fontSize = 12.sp, color = Brand.muted)
}

@Composable
private fun AlertDetail(a: Alert, onBack: () -> Unit) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp, 18.dp, 16.dp, 28.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
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
                Text("« ${a.excerpt} »", fontSize = 15.sp, color = Brand.ink); Spacer(Modifier.height(8.dp))
                Text("${a.app} · ${a.sender}", fontSize = 12.sp, color = Brand.muted)
            }
        }
        item { SoftCard { Eyebrow("Ce que cela peut signifier"); Spacer(Modifier.height(8.dp)); Text(a.whatItMeans, fontSize = 13.5.sp, color = Brand.ink) } }
        item {
            SoftCard(Brand.greenBg) {
                Eyebrow("Recommandations"); Spacer(Modifier.height(10.dp))
                a.recommendations.forEachIndexed { i, r ->
                    Row(Modifier.padding(vertical = 5.dp), verticalAlignment = Alignment.Top) {
                        Box(Modifier.size(22.dp).clip(RoundedCornerShape(50)).background(Brand.green), contentAlignment = Alignment.Center) { Text("${i + 1}", color = Brand.tealInk, fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                        Spacer(Modifier.width(10.dp)); Text(r, fontSize = 13.5.sp, color = Brand.ink)
                    }
                }
            }
        }
        item {
            SoftCard {
                Eyebrow("Comment en parler"); Spacer(Modifier.height(8.dp))
                Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(Brand.surface2).padding(12.dp)) { Text(a.script, fontSize = 13.5.sp, color = Brand.ink) }
            }
        }
    }
}

/* ============ Localisation (démo) ============ */
@Composable
private fun LocationScreen() {
    var gpsOff by remember { mutableStateOf(false) }
    val loc = if (gpsOff) Demo.lastKnown else Demo.location
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp, 20.dp, 16.dp, 24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Localisation", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Brand.ink)
                    Text("Position de l'enfant", fontSize = 12.5.sp, color = Brand.muted)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("GPS coupé", fontSize = 11.sp, color = Brand.muted); Spacer(Modifier.width(6.dp))
                    Switch(checked = gpsOff, onCheckedChange = { gpsOff = it })
                }
            }
        }
        item { MapPanel(loc) }
        item { LocationCard(loc, false, null) }
        item { Text("Démonstration. La localisation réelle (GPS du téléphone enfant) arrive dans une prochaine étape.", fontSize = 11.5.sp, color = Brand.muted) }
    }
}

@Composable
private fun MapPanel(loc: ChildLocation) {
    val road = Color(0xFF32415C); val block = Color(0xFF1E2A3E); val water = Color(0xFF123247); val park = Color(0xFF17351F)
    BoxWithConstraints(Modifier.fillMaxWidth().height(250.dp).clip(RoundedCornerShape(18.dp)).border(1.dp, Brand.line, RoundedCornerShape(18.dp))) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width; val h = size.height
            drawRect(Color(0xFF151D2C))
            val gx = w / 5; val gy = h / 4
            for (i in 0 until 5) for (j in 0 until 4) drawRect(block, topLeft = Offset(i * gx + 5f, j * gy + 5f), size = Size(gx - 10f, gy - 10f))
            drawRect(water, topLeft = Offset(0f, h * 0.80f), size = Size(w, h * 0.20f))
            drawRect(park, topLeft = Offset(w * 0.62f, h * 0.08f), size = Size(w * 0.30f, h * 0.26f))
            drawLine(road, Offset(0f, h * 0.5f), Offset(w, h * 0.5f), strokeWidth = 7f)
            drawLine(road, Offset(w * 0.5f, 0f), Offset(w * 0.5f, h), strokeWidth = 7f)
            drawLine(Brand.teal, Offset(w * 0.18f, h * 0.92f), Offset(w * 0.5f, h * 0.5f), strokeWidth = 5f)
            drawLine(Brand.teal, Offset(w * 0.5f, h * 0.5f), Offset(w * loc.x, h * loc.y), strokeWidth = 5f)
        }
        val mColor = if (loc.live) Brand.teal else Brand.amber
        Box(Modifier.offset(x = maxWidth * loc.x - 18.dp, y = maxHeight * loc.y - 18.dp)) {
            Box(Modifier.size(36.dp).clip(RoundedCornerShape(50)).background(mColor.copy(alpha = 0.25f)))
            Box(Modifier.size(16.dp).align(Alignment.Center).clip(RoundedCornerShape(50)).background(mColor).border(2.dp, Brand.bg, RoundedCornerShape(50)))
        }
        Box(Modifier.align(Alignment.TopStart).padding(10.dp)) { StatusPill(if (loc.live) "Temps réel" else "Dernière position", mColor, Brand.surface, mColor) }
    }
}

@Composable
private fun LocationCard(loc: ChildLocation, compact: Boolean, onClick: (() -> Unit)?) {
    val mod = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Brand.surface)
        .border(1.dp, Brand.line, RoundedCornerShape(18.dp)).then(if (onClick != null) Modifier.clickable { onClick() } else Modifier).padding(16.dp)
    Column(mod) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("📍", fontSize = 20.sp); Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(loc.address, fontWeight = FontWeight.SemiBold, color = Brand.ink)
                Text("${loc.area} · ${loc.time}", fontSize = 12.sp, color = Brand.muted)
            }
            if (!compact) StatusPill(if (loc.live) "Live" else "GPS off", if (loc.live) Brand.green else Brand.amber, if (loc.live) Brand.greenBg else Brand.amberBg, if (loc.live) Brand.green else Brand.amber)
        }
        if (!compact) { Spacer(Modifier.height(10.dp)); Text("Précision ± ${loc.accuracyM} m", fontSize = 12.sp, color = Brand.muted) }
    }
}

/* ============ Stats (démo) ============ */
@Composable
private fun StatsScreen() {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp, 20.dp, 16.dp, 24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Column {
                Text("Statistiques", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Brand.ink)
                Text("Exemple — 7 derniers jours", fontSize = 12.5.sp, color = Brand.muted)
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatTile("10", "Alertes", Brand.red, Modifier.weight(1f))
                StatTile("6", "Harcèlement", Brand.amber, Modifier.weight(1f))
                StatTile("3h30", "Écran / jour", Brand.teal, Modifier.weight(1f))
            }
        }
        item { SoftCard { Eyebrow("Alertes par jour"); Spacer(Modifier.height(14.dp)); BarChart(Demo.alertsPerDay.map { it.toFloat() }, Demo.weekDays, Brand.red) } }
        item { SoftCard { Eyebrow("Temps d'écran par jour (min)"); Spacer(Modifier.height(14.dp)); BarChart(Demo.screenTimePerDay.map { it.toFloat() }, Demo.weekDays, Brand.teal) } }
    }
}

@Composable
private fun StatTile(value: String, label: String, accent: Color, modifier: Modifier = Modifier) =
    Column(modifier.clip(RoundedCornerShape(16.dp)).background(Brand.surface).border(1.dp, Brand.line, RoundedCornerShape(16.dp)).padding(14.dp)) {
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
fun ChildReal(code: String, onReset: () -> Unit) {
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

    LaunchedEffect(ok) { if (hasUsage) { UsageUploadWorker.schedule(ctx); UsageUploadWorker.runNow(ctx) } }

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp, 20.dp, 16.dp, 28.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            TopBar("Téléphone protégé", "Relié · code $code") {
                if (ok) StatusPill("Protégé", Brand.green, Brand.greenBg, Brand.green) else StatusPill("À configurer", Brand.amber, Brand.amberBg, Brand.amber)
            }
        }
        if (!ok) item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (!hasUsage) SetupCard("1", "Accès aux données d'usage", "Pour mesurer le temps d'écran.") { UsageRepository.openUsageAccessSettings(ctx) }
                if (!accOn) SetupCard("2", "Activer le blocage", "Pour appliquer les règles du parent.") { BlockAccessibilityService.openAccessibilitySettings(ctx) }
            }
        }
        item {
            SoftCard {
                Eyebrow("Temps d'écran aujourd'hui"); Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(fmt(total), fontSize = 40.sp, fontWeight = FontWeight.Bold, color = Brand.ink)
                    Spacer(Modifier.width(10.dp)); Text("${apps.size} apps", fontSize = 13.sp, color = Brand.muted, modifier = Modifier.padding(bottom = 8.dp))
                }
                if (apps.isNotEmpty()) { Spacer(Modifier.height(10.dp)); Text("Envoyé au parent automatiquement.", fontSize = 12.sp, color = Brand.teal) }
            }
        }
        item {
            SoftCard(Brand.greenBg) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("\uD83D\uDEE1", fontSize = 18.sp); Spacer(Modifier.width(10.dp))
                    Column {
                        Text("Détection active (sur l'appareil)", fontWeight = FontWeight.SemiBold, color = Brand.ink, fontSize = 14.sp)
                        Text("Harcèlement \u00b7 " + Detector.languages, fontSize = 11.5.sp, color = Brand.muted)
                    }
                }
            }
        }
        item { OutlinedButton({ UsageUploadWorker.runNow(ctx) }, Modifier.fillMaxWidth(), shape = RoundedCornerShape(11.dp)) { Text("Envoyer maintenant") } }
        if (apps.isNotEmpty()) item { Eyebrow("Applications") }
        items(apps, key = { it.packageName }) { app ->
            val isBlocked = blocked.contains(app.packageName)
            SoftCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    app.icon?.let { Image(it.toBitmap(80, 80).asImageBitmap(), null, Modifier.size(42.dp).clip(RoundedCornerShape(11.dp))); Spacer(Modifier.width(12.dp)) }
                    Column(Modifier.weight(1f)) {
                        Text(app.label, fontWeight = FontWeight.SemiBold, color = Brand.ink)
                        Text(fmt(app.millisToday), fontSize = 12.5.sp, color = Brand.muted)
                    }
                    if (isBlocked) StatusPill("Bloquée", Brand.red, Brand.redBg, Brand.red)
                }
                Spacer(Modifier.height(10.dp)); UsageBar(app.millisToday.toFloat() / maxMs, if (isBlocked) Brand.red else Brand.teal)
            }
        }
        item {
            Spacer(Modifier.height(6.dp))
            Text("Ce téléphone est protégé par tes parents. Le contenu des messages et les photos ne sont pas lus pour le suivi du temps.", fontSize = 11.5.sp, color = Brand.muted, textAlign = TextAlign.Center)
            TextButton(onReset, Modifier.fillMaxWidth()) { Text("Délier / changer de rôle", color = Brand.muted) }
        }
    }
}

@Composable
private fun SetupCard(num: String, title: String, body: String, onClick: () -> Unit) = SoftCard(Brand.amberBg) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(26.dp).clip(RoundedCornerShape(50)).background(Brand.amber), contentAlignment = Alignment.Center) { Text(num, color = Brand.tealInk, fontWeight = FontWeight.Bold, fontSize = 13.sp) }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, color = Brand.ink, fontSize = 14.5.sp)
            Text(body, fontSize = 12.sp, color = Brand.muted)
        }
    }
    Spacer(Modifier.height(10.dp))
    Button(onClick, Modifier.fillMaxWidth(), shape = RoundedCornerShape(11.dp)) { Text("Autoriser") }
}
