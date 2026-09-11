package com.yourorg.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yourorg.app.Data.Remote.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

/**
 * Phase 1 skeleton screen for the KMP migration — proves the composeApp module builds, runs on
 * Android, and can reach the real Supabase backend, before any real screens move over. Replaced by
 * the actual navigation graph (RootApp/AppNavHost) in Phase 6.
 */
@Serializable
private data class ProbeRow(val id: String)

@Composable
fun App() {
    MaterialTheme {
        Surface(Modifier.fillMaxSize()) {
            var statusText by remember { mutableStateOf("Not checked yet") }
            val scope = rememberCoroutineScope()

            Column(
                Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Today in History — KMP skeleton", style = MaterialTheme.typography.titleLarge)
                Text(statusText)
                Button(onClick = {
                    scope.launch {
                        statusText = try {
                            val rows = SupabaseClient.client.postgrest.from("year_guess_items")
                                .select { limit(1) }
                                .decodeList<ProbeRow>()
                            "Connected — got ${rows.size} row(s) from year_guess_items"
                        } catch (e: Exception) {
                            "Failed: ${e.message}"
                        }
                    }
                }) {
                    Text("Test Supabase connection")
                }
            }
        }
    }
}
