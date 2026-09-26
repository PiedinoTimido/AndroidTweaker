package com.android.tweaker.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.android.tweaker.data.LogcatManager
import com.android.tweaker.model.SavedLogItem
import com.android.tweaker.model.Strings
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SavedLogsScreen(strings: Strings) {
    val context = LocalContext.current
    val logcatManager = remember { LogcatManager(context) }
    var logsList by remember { mutableStateOf(logcatManager.getSavedLogs()) }

    var selectedLogForView by remember { mutableStateOf<SavedLogItem?>(null) }
    var renamingLogItem by remember { mutableStateOf<SavedLogItem?>(null) }
    var renameInputText by remember { mutableStateOf("") }

    fun refreshLogs() {
        logsList = logcatManager.getSavedLogs()
    }

    fun shareLogFile(item: SavedLogItem) {
        try {
            val file = File(context.cacheDir, "${item.fileName}.txt")
            file.writeText(item.logContent)
            val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Condividi Log"))
        } catch (e: Exception) {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, item.logContent)
            }
            context.startActivity(Intent.createChooser(intent, "Condividi Log"))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Log Salvati",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            text = "Visualizza, rinomina, metti in primo piano o condividi le registrazioni logcat salvate.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(12.dp))

        if (logsList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Nessun log salvato.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(logsList, key = { it.id }) { item ->
                    val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(item.timestamp))
                    Card(
                        onClick = { selectedLogForView = item },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = {
                                    logcatManager.toggleStar(item.id)
                                    refreshLogs()
                                }
                            ) {
                                Icon(
                                    imageVector = if (item.isStarred) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = "Star",
                                    tint = if (item.isStarred) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.fileName,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "$dateStr • ${if (item.isSystemLog) "System" else item.targetPackage}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            IconButton(
                                onClick = {
                                    renamingLogItem = item
                                    renameInputText = item.fileName
                                }
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Rename", tint = MaterialTheme.colorScheme.primary)
                            }

                            IconButton(onClick = { shareLogFile(item) }) {
                                Icon(Icons.Default.Share, contentDescription = "Share", tint = MaterialTheme.colorScheme.primary)
                            }

                            IconButton(
                                onClick = {
                                    logcatManager.deleteLogItem(item.id)
                                    refreshLogs()
                                }
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }

    // View Log Content Dialog
    selectedLogForView?.let { item ->
        AlertDialog(
            onDismissRequest = { selectedLogForView = null },
            title = { Text(item.fileName, fontWeight = FontWeight.Bold) },
            text = {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(12.dp)
                    ) {
                        Text(
                            text = item.logContent,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { shareLogFile(item) }) {
                    Text("Condividi .txt")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedLogForView = null }) {
                    Text(strings.close)
                }
            }
        )
    }

    // Rename Dialog
    renamingLogItem?.let { item ->
        AlertDialog(
            onDismissRequest = { renamingLogItem = null },
            title = { Text("Rinomina Log", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = renameInputText,
                    onValueChange = { renameInputText = it },
                    label = { Text("Nuovo Nome File") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (renameInputText.isNotBlank()) {
                            logcatManager.renameLog(item.id, renameInputText.trim())
                            refreshLogs()
                            renamingLogItem = null
                        }
                    }
                ) {
                    Text("Salva")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { renamingLogItem = null }) {
                    Text(strings.dangerCancel)
                }
            }
        )
    }
}
