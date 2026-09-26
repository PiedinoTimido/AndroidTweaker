package com.android.tweaker.ui.screens

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.PhonelinkSetup
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SignalWifi4Bar
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
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
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val status by adbManager.connectionStatus.collectAsState()

    val isAndroid11OrHigher = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
    var selectedTabIndex by remember { mutableStateOf(if (isAndroid11OrHigher) 0 else 1) }

    var isFullScreenWirelessFlow by remember { mutableStateOf(false) }

    var pairPort by remember { mutableStateOf(prefs.lastPairPort) }
    var pairCode by remember { mutableStateOf(prefs.lastPairCode) }
    var adbPort by remember { mutableStateOf(prefs.lastAdbPort) }
    var message by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    fun sendPairingNotifications() {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "adb_pairing",
                "ADB Pairing Setup",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notif1 = NotificationCompat.Builder(context, "adb_pairing")
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentTitle("IP & Port Config")
            .setContentText("Premi per inserire IP e Porta per la connessione ADB")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        val notif2 = NotificationCompat.Builder(context, "adb_pairing")
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentTitle("PIN Config")
            .setContentText("Premi per inserire il PIN di accoppiamento 6 cifre")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1001, notif1)
        notificationManager.notify(1002, notif2)
    }

    if (isFullScreenWirelessFlow) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                IconButton(
                    onClick = { isFullScreenWirelessFlow = false },
                    modifier = Modifier.align(Alignment.Start)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }

                Spacer(modifier = Modifier.height(12.dp))

                Icon(
                    imageVector = Icons.Default.SignalWifi4Bar,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(64.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Configurazione Wireless Debugging",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(12.dp))

                Card(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Quando le notifiche sono arrivate, esci da questa schermata e vai nelle impostazioni.\n\nControlla la tendina notifiche: troverai 'IP & Port config.' per l'indirizzo/porta e 'PIN Config' per il codice a 6 cifre.",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 20.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = adbPort,
                    onValueChange = {
                        adbPort = it
                        prefs.lastAdbPort = it
                    },
                    label = { Text("Indirizzo IP e Porta (IP1.IP2.IP3.IP4:PORT)") },
                    placeholder = { Text("192.168.1.50:5555") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = pairPort,
                        onValueChange = {
                            pairPort = it
                            prefs.lastPairPort = it
                        },
                        label = { Text("Porta Pairing") },
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
                        label = { Text("PIN Accoppiamento") },
                        placeholder = { Text("6 cifre") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        val mainPortInt = adbPort.substringAfterLast(":", adbPort).toIntOrNull() ?: adbPort.toIntOrNull() ?: 5555
                        val pairingPortInt = pairPort.toIntOrNull() ?: mainPortInt

                        scope.launch {
                            val resPair = if (pairCode.isNotBlank()) adbManager.pairDevice(pairingPortInt, pairCode) else adbManager.connectDevice(mainPortInt)
                            if (resPair.isSuccess) {
                                prefs.lastAdbPort = mainPortInt.toString()
                                message = "✅ Connessione Wireless salvata e attiva!"
                                isError = false
                                isFullScreenWirelessFlow = false
                            } else {
                                val resDirect = adbManager.connectDevice(mainPortInt)
                                if (resDirect.isSuccess) {
                                    prefs.lastAdbPort = mainPortInt.toString()
                                    message = "✅ Connessione ADB riuscita!"
                                    isError = false
                                    isFullScreenWirelessFlow = false
                                } else {
                                    message = resPair.exceptionOrNull()?.localizedMessage ?: "Connessione fallita"
                                    isError = true
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Verifica e Salva Connessione Wireless", fontWeight = FontWeight.Bold)
                }
            }
        }
        return
    }

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
                            is AdbConnectionStatus.Connected -> if (s.isUsb) "Connesso via PC (127.0.0.1:${s.port})" else String.format(strings.statusConnected, s.port)
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

        // Connection Method Tab Selector (Wireless hidden if Android < 11)
        if (isAndroid11OrHigher) {
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
        }

        if (isAndroid11OrHigher && selectedTabIndex == 0) {
            // --- WIRELESS DEBUGGING MODE ---
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.SignalWifi4Bar, contentDescription = "Wireless", tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Connessione Wireless Debugging",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Connetti l'app direttamente in locale senza bisogno di un computer su Android 11+.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            sendPairingNotifications()
                            isFullScreenWirelessFlow = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.PhonelinkSetup, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Inizia connessione wireless", fontWeight = FontWeight.Bold)
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
