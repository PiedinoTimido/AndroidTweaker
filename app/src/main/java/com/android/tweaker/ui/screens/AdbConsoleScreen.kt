package com.android.tweaker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.tweaker.data.AdbManager
import com.android.tweaker.model.Strings
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdbConsoleScreen(
    adbManager: AdbManager,
    strings: Strings
) {
    val scope = rememberCoroutineScope()
    var isShellMode by remember { mutableStateOf(false) }
    var commandInput by remember { mutableStateOf("") }
    var consoleLogs by remember { mutableStateOf("Android Tweaker Local ADB Console\nType your command below and press Run.\n\n") }
    val scrollState = rememberScrollState()

    fun runCmd(cmdStr: String) {
        if (cmdStr.isBlank()) return
        val commandToExecute = cmdStr.trim()
        commandInput = ""
        val prefix = if (isShellMode) "$ adb shell " else "$ adb "
        consoleLogs += "$prefix$commandToExecute\n"

        scope.launch {
            val result = adbManager.executeShellCommand(commandToExecute)
            consoleLogs += result + "\n\n"
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Top Console Mode Selector
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            SegmentedButton(
                selected = !isShellMode,
                onClick = { isShellMode = false },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
            ) {
                Text("Console ADB", fontWeight = FontWeight.Bold)
            }
            SegmentedButton(
                selected = isShellMode,
                onClick = { isShellMode = true },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
            ) {
                Text("Console ADB SHELL", fontWeight = FontWeight.Bold)
            }
        }

        // Developer Warning Banner
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Warning",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "DEVELOPER ADB CONSOLE",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = "WARNING: The ADB console is intended for developers. Executing improper shell commands can break your device. The app author assumes no responsibility.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Monospace Terminal Output Box (Scrollable)
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                SelectionContainer {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = consoleLogs,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(
                    onClick = { consoleLogs = "Terminal cleared.\n\n" },
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear Terminal")
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Input & Run Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = commandInput,
                onValueChange = { commandInput = it },
                placeholder = { Text(if (isShellMode) "e.g. pm list packages" else "e.g. shell pm list packages") },
                label = { Text(if (isShellMode) "ADB Shell Command" else "ADB Command") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                keyboardActions = KeyboardActions(onGo = { runCmd(commandInput) }),
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = { runCmd(commandInput) },
                modifier = Modifier.height(56.dp)
            ) {
                Icon(Icons.Default.Send, contentDescription = "Execute")
            }
        }
    }
}
