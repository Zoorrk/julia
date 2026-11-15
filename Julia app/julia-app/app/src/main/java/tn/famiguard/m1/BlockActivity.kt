package tn.famiguard.m1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Écran plein affiché quand l'enfant ouvre une application bloquée. */
class BlockActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = intent.getStringExtra(EXTRA_APP) ?: "Cette application"
        setContent {
            MaterialTheme(colorScheme = FamiColors) {
                Surface(Modifier.fillMaxSize(), color = Brand.bg) {
                    Column(
                        Modifier.fillMaxSize().padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(Modifier.size(84.dp).clip(RoundedCornerShape(24.dp)).background(Brand.teal),
                            contentAlignment = Alignment.Center) { Text("🛡", fontSize = 44.sp) }
                        Spacer(Modifier.height(24.dp))
                        Text("$app est en pause", fontSize = 23.sp, fontWeight = FontWeight.Bold,
                            color = Brand.ink, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(12.dp))
                        Text("Cette application est mise en pause par tes parents en ce moment.",
                            textAlign = TextAlign.Center, color = Brand.muted, fontSize = 14.sp)
                        Spacer(Modifier.height(30.dp))
                        Button(onClick = { finish() }, shape = RoundedCornerShape(12.dp)) {
                            Text("J'ai compris", modifier = Modifier.padding(horizontal = 12.dp))
                        }
                    }
                }
            }
        }
    }

    companion object { const val EXTRA_APP = "app_label" }
}
