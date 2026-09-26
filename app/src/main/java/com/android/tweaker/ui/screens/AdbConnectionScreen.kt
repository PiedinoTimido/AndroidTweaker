package com.android.tweaker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PhonelinkSetup
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SignalWifi4Bar
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.tweaker.data.AdbConnectionStatus
import com.android.tweaker.data.AdbManager
import com.android.tweaker.data.PreferencesManager
import com.android.tweaker.model.Strings
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdbConnectionScreen(
    adbManager: AdbManager,
    prefs: PreferencesManager,
    strings: Strings
) {
    val scope = rememberCoroutineScope()
    val status by adbManager.connectionStatus.collectAsState()

    var selectedTabIndex by remember { mutableStateOf(0) }

    var pairPort by remember { mutableStateOf(prefs.lastPairPort) }
    var pairCode by remember { mutableStateOf(prefs.lastPairCode) }
    var adbPort by remember { mutableStateOf(prefs.lastAdbPort) }
    var message by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Status Card
        Card(
            colors = CardDefaults.cardColors(
                containerColor = when (status) {
                    is AdbConnectionStatus.Connected -> MaterialTheme.colorScheme.primaryContainer
                    is AdbConnectionStatus.Connecting -> MaterialTheme.colorScheme.secondaryContainer
                    is AdbConnectionStatus.Error -> MaterialTheme.colorScheme.errorContainer
                    is AdbConnectionStatus.Disconnected -> MaterialTheme.colorScheme.surfaceVariant
                }
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = when (status) {
                        is AdbConnectionStatus.Connected -> Icons.Default.CheckCircle
                        is AdbConnectionStatus.Error -> Icons.Default.Warning
                        else -> Icons.Default.SignalWifi4Bar
                    },
                    contentDescription = "Status",
                    tint = when (status) {
                        is AdbConnectionStatus.Connected -> MaterialTheme.colorScheme.primary
                        is AdbConnectionStatus.Error -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = strings.statusTitle,
                        style = MaterialTheme.typography.labelMedium
                    )
                    Text(
                        text = when (val s = status) {
                            is AdbConnectionStatus.Connected -> String.format(strings.statusConnected, s.port)
                            is AdbConnectionStatus.Connecting -> strings.statusConnecting
                            is AdbConnectionStatus.Error -> s.message
                            is AdbConnectionStatus.Disconnected -> strings.statusDisconnected
                        },
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Connection Method Tab Selector
        TabRow(selectedTabIndex = selectedTabIndex, modifier = Modifier.fillMaxWidth()) {
            Tab(
                selected = selectedTabIndex == 0,
                onClick = { selectedTabIndex = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.SignalWifi4Bar, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Wireless (No PC)", fontWeight = FontWeight.SemiBold)
                    }
                }
            )
            Tab(
                selected = selectedTabIndex == 1,
                onClick = { selectedTabIndex = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Usb, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Via PC (USB)", fontWeight = FontWeight.SemiBold)
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (selectedTabIndex == 0) {
            // --- WIRELESS DEBUGGING MODE ---
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = "Info", tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = strings.guideTitle,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = strings.guideSteps,
                        style = MaterialTheme.typography.bodySmall,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Pairing Section
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = strings.pairHeader,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = pairPort,
                            onValueChange = {
                                pairPort = it
                                prefs.lastPairPort = it
                            },
                            label = { Text(strings.pairPortLabel) },
                            placeholder = { Text("e.g. 38451") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = pairCode,
                            onValueChange = {
                                pairCode = it
                                prefs.lastPairCode = it
                            },
                            label = { Text(strings.pairCodeLabel) },
                            placeholder = { Text("6 digits") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            val portInt = pairPort.toIntOrNull()
                            if (portInt == null || pairCode.length < 5) {
                                message = "Please enter valid Pairing Port and 6-digit Code"
                                isError = true
                                return@Button
                            }
                            scope.launch {
                                val res = adbManager.pairDevice(portInt, pairCode)
                                if (res.isSuccess) {
                                    message = res.getOrDefault("Pairing successful!")
                                    isError = false
                                } else {
                                    message = res.exceptionOrNull()?.localizedMessage ?: "Pairing failed"
                                    isError = true
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.PhonelinkSetup, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(strings.pairButton)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ADB Wireless Connection Section
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = strings.connectHeader,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = adbPort,
                        onValueChange = {
                            adbPort = it
                            prefs.lastAdbPort = it
                        },
                        label = { Text(strings.adbPortLabel) },
                        placeholder = { Text("e.g. 5555 or Wireless Debugging Port") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                val portInt = adbPort.toIntOrNull()
                                if (portInt == null) {
                                    message = "Please enter a valid ADB Port number"
                                    isError = true
                                    return@Button
                                }
                                scope.launch {
                                    val res = adbManager.connectDevice(portInt)
                                    if (res.isSuccess) {
                                        message = res.getOrDefault("Connected!")
                                        isError = false
                                    } else {
                                        message = res.exceptionOrNull()?.localizedMessage ?: "Connection failed"
                                        isError = true
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(strings.connectButton)
                        }

                        if (status is AdbConnectionStatus.Connected) {
                            OutlinedButton(
                                onClick = { adbManager.disconnect() },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(strings.disconnectButton)
                            }
                        }
                    }
                }
            }
        } else {
            // --- VIA PC (USB DEBUGGING) MODE ---
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Computer, contentDescription = "PC", tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Guida Connessione tramite PC (USB Debugging)",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    val usbGuideSteps = """
                        1. Attiva le Opzioni Sviluppatore:
                           Vai su Impostazioni > Info sul telefono e tocca 7 volte su Numero di serie / Build number.

                        2. Attiva il Debug USB:
                           Vai in Impostazioni > Sistema > Opzioni sviluppatore e abilita lo switch Debug USB.

                        3. Collega al PC:
                           Connetti il telefono al computer tramite cavo USB.

                        4. Esegui il comando da Terminale PC:
                           Apri il terminale / PowerShell sul PC (con Platform Tools installati) ed esegui:
                           adb devices

                        5. Autorizza il PC:
                           Guarda lo schermo dello smartphone, spunta "Consenti sempre da questo computer" e premi Consenti.
                    """.trimIndent()

                    Text(
                        text = usbGuideSteps,
                        style = MaterialTheme.typography.bodySmall,
                        lineHeight = 20.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            scope.launch {
                                // Attempt ADB daemon connection on default port 5555 or custom port
                                val targetPort = adbPort.toIntOrNull() ?: 5555
                                val res = adbManager.connectDevice(targetPort)
                                if (res.isSuccess) {
                                    message = "✅ ADB daemon local connection verified on port $targetPort!"
                                    isError = false
                                } else {
                                    message = "⚠️ Could not connect to local ADB daemon on port $targetPort.\nEnsure USB Debugging is allowed and ADB is listening."
                                    isError = true
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Verifica Connessione ADB", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (message.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Surface(
                color = if (isError) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = message,
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isError) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}
