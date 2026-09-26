package com.android.tweaker.ui.screens

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import com.android.tweaker.data.AdbManager
import com.android.tweaker.data.LogcatManager
import com.android.tweaker.model.SavedLogItem
import com.android.tweaker.model.Strings
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogcatScreen(
    adbManager: AdbManager,
    strings: Strings
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val logcatManager = remember { LogcatManager(context) }

    var isGranted by remember { mutableStateOf(logcatManager.isReadLogsGranted()) }
    var selectedTab by remember { mutableStateOf(0) } // 0 = App Logs, 1 = System Logs

    var targetPackage by remember { mutableStateOf("") }
    var isRecording by remember { mutableStateOf(false) }
    var recordingStatusText by remember { mutableStateOf("") }

    var showPermissionInfoDialog by remember { mutableStateOf(!isGranted) }
    var grantOutput by remember { mutableStateOf("") }

    fun sendRecordingNotif(pkg: String) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("log_recording", "Logcat Recording", NotificationManager.IMPORTANCE_HIGH)
            nm.createNotificationChannel(channel)
        }
        val notif = NotificationCompat.Builder(context, "log_recording")
            .setSmallIcon(android.R.drawable.ic_menu_save)
            .setContentTitle("Registrazione log per ${if (pkg.isNotBlank()) pkg else "System"}")
            .setContentText("Registrazione logcat in corso...")
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        nm.notify(2001, notif)
    }

    fun stopNotifAndNotifySuccess(pkg: String) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.cancel(2001)
        val notifSuccess = NotificationCompat.Builder(context, "log_recording")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Log salvato con successo!")
            .setContentText("Il file di log è stato salvato nel menù Log Salvati.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        nm.notify(2002, notifSuccess)
    }

    fun startLogging() {
        isRecording = true
        sendRecordingNotif(targetPackage)

        if (selectedTab == 0 && targetPackage.isNotBlank()) {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(targetPackage.trim())
            if (launchIntent != null) {
                context.startActivity(launchIntent)
            }
        }

        recordingStatusText = "Registrazione avviata..."
    }

    fun stopAndSaveLogging() {
        isRecording = false
        val isSys = (selectedTab == 1)
        val pkg = targetPackage.trim()
        stopNotifAndNotifySuccess(pkg)

        scope.launch {
            val rawLogs = adbManager.executeShellCommand("logcat -d -t 300")
            val name = if (isSys) "System_Log_${System.currentTimeMillis()}" else "${pkg}_Log_${System.currentTimeMillis()}"
            val item = SavedLogItem(
                id = UUID.randomUUID().toString(),
                fileName = name,
                timestamp = System.currentTimeMillis(),
                logContent = rawLogs,
                isSystemLog = isSys,
                targetPackage = pkg
            )
            logcatManager.saveLogItem(item)
            recordingStatusText = "Log salvato con successo in Log Salvati!"
        }
    }

    if (showPermissionInfoDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionInfoDialog = false },
            icon = {
                Icon(Icons.Default.Security, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            },
            title = {
                Text("Permesso READ_LOGS Richiesto", fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text(
                        text = "Per utilizzare la funzione Logcat, una tantum devi essere connesso ad ADB ed eseguire il comando per concedere il permesso di lettura log.\n\n" +
                                "Una volta concesso, se riavvii, chiudi o aggiorni l'app, il permesso RIMANE. Se formatti, disinstalli o rimuovi manualmente il permesso, dovrai concederlo di nuovo.",
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 20.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (grantOutput.isNotBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(grantOutput, modifier = Modifier.padding(8.dp), fontSize = 12.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            val res = adbManager.executeShellCommand("pm grant com.android.tweaker android.permission.READ_LOGS")
                            grantOutput = res
                            if (logcatManager.isReadLogsGranted()) {
                                isGranted = true
                                showPermissionInfoDialog = false
                            }
                        }
                    }
                ) {
                    Text("Esegui Grant READ_LOGS", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showPermissionInfoDialog = false }) {
                    Text(strings.close)
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        TabRow(selectedTabIndex = selectedTab, modifier = Modifier.fillMaxWidth()) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("App Logs", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("System Logs", fontWeight = FontWeight.Bold) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (selectedTab == 0) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Registrazione Log App Specifica", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Inserisci il nome pacchetto (es. com.instagram.android) e clicca 'Inizia registrazione'. L'app verrà aperta automaticamente e la registrazione catturerà i log di sessione.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = targetPackage,
                        onValueChange = { targetPackage = it },
                        label = { Text("Nome Pacchetto Applicazione") },
                        placeholder = { Text("com.example.app") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { startLogging() },
                            enabled = !isRecording && targetPackage.isNotBlank(),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Inizia registrazione")
                        }

                        if (isRecording) {
                            Button(
                                onClick = { stopAndSaveLogging() },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Stop, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Stop & Salva Log")
                            }
                        }
                    }
                }
            }
        } else {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Registrazione Log di Sistema", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Cattura tutti i log del sistema operativo Android in tempo reale.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { startLogging() },
                            enabled = !isRecording,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Inizia registrazione")
                        }

                        if (isRecording) {
                            Button(
                                onClick = { stopAndSaveLogging() },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Stop, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Stop & Salva Log")
                            }
                        }
                    }
                }
            }
        }

        if (recordingStatusText.isNotBlank()) {
            Spacer(modifier = Modifier.height(16.dp))
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = recordingStatusText,
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}
