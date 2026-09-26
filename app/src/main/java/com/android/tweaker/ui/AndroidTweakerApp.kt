package com.android.tweaker.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.android.tweaker.data.AdbConnectionStatus
import com.android.tweaker.data.AdbManager
import com.android.tweaker.data.PreferencesManager
import com.android.tweaker.model.AppLanguage
import com.android.tweaker.model.AppStringsProvider
import com.android.tweaker.ui.screens.*
import kotlinx.coroutines.launch

enum class AppNavigationItem(val icon: ImageVector) {
    ADB_CONNECTION(Icons.Default.Usb),
    TWEAKS(Icons.Default.Tune),
    ADB_CONSOLE(Icons.Default.Terminal),
    LANGUAGE(Icons.Default.Language),
    INFO(Icons.Default.Info),
    LICENSES(Icons.Default.Description),
    RATE_US(Icons.Default.Star)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AndroidTweakerApp(
    adbManager: AdbManager,
    prefs: PreferencesManager
) {
    val context = LocalContext.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var currentLanguage by remember {
        mutableStateOf(AppLanguage.fromCode(prefs.selectedLanguageCode))
    }
    val strings = remember(currentLanguage) { AppStringsProvider.getStrings(currentLanguage) }

    // Elevated Privileges state defaults to false on app launch (auto-removed on re-open)
    var isElevated by remember { mutableStateOf(false) }

    val adbStatus by adbManager.connectionStatus.collectAsState()
    val isConnected = adbStatus is AdbConnectionStatus.Connected

    var currentScreen by remember { mutableStateOf(AppNavigationItem.ADB_CONNECTION) }
    var showDisclaimer by remember { mutableStateOf(!prefs.isDisclaimerAccepted) }

    // Automatic startup ADB connection attempt (USB 5555 first, then saved wireless)
    LaunchedEffect(Unit) {
        val success = adbManager.autoConnectOnStartup(prefs.lastAdbPort)
        if (success) {
            currentScreen = AppNavigationItem.TWEAKS
        } else {
            currentScreen = AppNavigationItem.ADB_CONNECTION
        }
    }

    if (showDisclaimer) {
        DisclaimerDialog(
            onAccept = {
                prefs.isDisclaimerAccepted = true
                showDisclaimer = false
            },
            strings = strings
        )
    }

    fun getScreenTitle(item: AppNavigationItem): String = when (item) {
        AppNavigationItem.ADB_CONNECTION -> strings.navAdbConnection
        AppNavigationItem.TWEAKS -> strings.navTweaks
        AppNavigationItem.ADB_CONSOLE -> strings.navAdbConsole
        AppNavigationItem.LANGUAGE -> strings.navLanguage
        AppNavigationItem.INFO -> strings.navInfo
        AppNavigationItem.LICENSES -> strings.navLicenses
        AppNavigationItem.RATE_US -> strings.navRateUs
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Android Tweaker",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(horizontal = 28.dp, vertical = 16.dp),
                    color = MaterialTheme.colorScheme.primary
                )
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))

                AppNavigationItem.values().forEach { item ->
                    val title = getScreenTitle(item)
                    NavigationDrawerItem(
                        icon = { Icon(item.icon, contentDescription = title) },
                        label = { Text(title) },
                        selected = (currentScreen == item),
                        onClick = {
                            scope.launch { drawerState.close() }
                            if (item == AppNavigationItem.RATE_US) {
                                try {
                                    val intent = Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse("market://details?id=com.android.tweaker")
                                    )
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    val webIntent = Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse("https://play.google.com/store/apps/details?id=com.android.tweaker")
                                    )
                                    context.startActivity(webIntent)
                                }
                            } else {
                                currentScreen = item
                            }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = getScreenTitle(currentScreen),
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    }
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (currentScreen) {
                    AppNavigationItem.ADB_CONNECTION -> AdbConnectionScreen(adbManager, prefs, strings)
                    AppNavigationItem.TWEAKS -> {
                        if (!isConnected) {
                            Surface(
                                color = MaterialTheme.colorScheme.background,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(24.dp),
                                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(64.dp)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "IMPOSSIBILE AVVIARE I TWEAKS",
                                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.error,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "adb non connesso",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(24.dp))
                                    Button(
                                        onClick = { currentScreen = AppNavigationItem.ADB_CONNECTION },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Default.Usb, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Connetti ad adb", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        } else {
                            TweaksScreen(
                                adbManager = adbManager,
                                strings = strings,
                                isElevated = isElevated,
                                onToggleElevated = { isElevated = it }
                            )
                        }
                    }
                    AppNavigationItem.ADB_CONSOLE -> AdbConsoleScreen(adbManager, strings)
                    AppNavigationItem.LANGUAGE -> LanguageScreen(
                        currentLanguage = currentLanguage,
                        onLanguageSelected = { lang ->
                            currentLanguage = lang
                            prefs.selectedLanguageCode = lang.code
                        },
                        strings = strings
                    )
                    AppNavigationItem.INFO -> InfoScreen(strings)
                    AppNavigationItem.LICENSES -> LicensesScreen(strings)
                    AppNavigationItem.RATE_US -> {}
                }
            }
        }
    }
}
