package com.android.tweaker.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.tweaker.model.Strings

data class OpenSourceLicense(
    val name: String,
    val author: String,
    val license: String,
    val description: String,
    val githubUrl: String
)

@Composable
fun LicensesScreen(strings: Strings) {
    val context = LocalContext.current
    val licenses = listOf(
        OpenSourceLicense(
            name = "dadb",
            author = "Mobile Native Foundation",
            license = "Apache License 2.0",
            description = "A Kotlin ADB client for Android to execute commands over TCP sockets.",
            githubUrl = "https://github.com/mobile-native-foundation/dadb"
        ),
        OpenSourceLicense(
            name = "Jetpack Compose & Material 3",
            author = "Google LLC",
            license = "Apache License 2.0",
            description = "Android UI toolkit for building modern user interfaces.",
            githubUrl = "https://github.com/androidx/androidx"
        ),
        OpenSourceLicense(
            name = "Kotlin Coroutines",
            author = "JetBrains s.r.o.",
            license = "Apache License 2.0",
            description = "Asynchronous programming framework for Kotlin.",
            githubUrl = "https://github.com/Kotlin/kotlinx.coroutines"
        ),
        OpenSourceLicense(
            name = "AndroidX Core KTX & Activity",
            author = "Google LLC",
            license = "Apache License 2.0",
            description = "Core Jetpack libraries for Android app development.",
            githubUrl = "https://github.com/androidx/androidx"
        )
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = strings.navLicenses,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = "Android Tweaker uses the following third-party open source libraries:",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        items(licenses) { item ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "By ${item.author} • ${item.license}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = item.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    TextButton(
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(item.githubUrl))
                                context.startActivity(intent)
                            } catch (_: Exception) {}
                        },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Visita la repo originale", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
