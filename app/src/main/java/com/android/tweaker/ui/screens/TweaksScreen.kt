package com.android.tweaker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.tweaker.data.AdbManager
import com.android.tweaker.data.BatteryInfoManager
import com.android.tweaker.model.*
import com.android.tweaker.ui.components.DangerConfirmationDialog
import com.android.tweaker.ui.components.DangerDialogStep
import com.android.tweaker.ui.components.ElevatedPrivilegesDialog
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TweaksScreen(
    adbManager: AdbManager,
    strings: Strings,
    isElevated: Boolean,
    onToggleElevated: (Boolean) -> Unit
) {
    val scope = rememberCoroutineScope()
    val allTweaks = remember { TweakRepository.getTweaks() }

    var selectedCategory by remember { mutableStateOf(TweakCategoryType.INFO) }

    // Execution Output State
    var outputTitle by remember { mutableStateOf("") }
    var outputText by remember { mutableStateOf("") }
    var showOutputDialog by remember { mutableStateOf(false) }

    // Danger Confirmation State
    var activeDangerTweak by remember { mutableStateOf<TweakItem?>(null) }
    var activeDangerFormattedCmd by remember { mutableStateOf("") }
    var dangerStep by remember { mutableStateOf(DangerDialogStep.NONE) }

    // Elevated Privileges Dialog State
    var showElevatedDialog by remember { mutableStateOf(false) }

    val filteredTweaks = remember(selectedCategory) {
        allTweaks.filter { it.category == selectedCategory }
    }

    fun getCategoryTitle(cat: TweakCategoryType): String = when (cat) {
        TweakCategoryType.INFO -> strings.catInfoTitle
        TweakCategoryType.APP_CONTROL -> strings.catAppControlTitle
        TweakCategoryType.SETTINGS -> strings.catSettingsTitle
        TweakCategoryType.DANGER -> strings.catDangerTitle
    }

    fun getCategoryDesc(cat: TweakCategoryType): String = when (cat) {
        TweakCategoryType.INFO -> strings.catInfoDesc
        TweakCategoryType.APP_CONTROL -> strings.catAppControlDesc
        TweakCategoryType.SETTINGS -> strings.catSettingsDesc
        TweakCategoryType.DANGER -> strings.catDangerDesc
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Elevated Privileges Control Banner
        Surface(
            color = if (isElevated) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isElevated) Icons.Default.LockOpen else Icons.Default.Lock,
                        contentDescription = "Lock State",
                        tint = if (isElevated) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isElevated) "Elevated Privileges Active" else "Standard Privileges",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (isElevated) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
                    )
                }

                Button(
                    onClick = {
                        if (isElevated) {
                            scope.launch {
                                adbManager.executeShellCommand("pm revoke com.android.tweaker android.permission.WRITE_SECURE_SETTINGS")
                                adbManager.executeShellCommand("pm revoke com.android.tweaker android.permission.DUMP")
                                onToggleElevated(false)
                            }
                        } else {
                            showElevatedDialog = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isElevated) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(
                        text = if (isElevated) "Remove elevated privileges" else "Give elevated privileges",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Scrollable Tab Row for Categories
        ScrollableTabRow(
            selectedTabIndex = TweakCategoryType.values().indexOf(selectedCategory),
            edgePadding = 16.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            TweakCategoryType.values().forEach { category ->
                Tab(
                    selected = (selectedCategory == category),
                    onClick = { selectedCategory = category },
                    text = {
                        Text(
                            text = getCategoryTitle(category),
                            color = if (category == TweakCategoryType.DANGER) MaterialTheme.colorScheme.error else Color.Unspecified,
                            fontWeight = if (selectedCategory == category) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = getCategoryDesc(selectedCategory),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            items(filteredTweaks, key = { it.id }) { tweak ->
                TweakCard(
                    tweak = tweak,
                    strings = strings,
                    isElevated = isElevated,
                    onExecute = { formattedCmd ->
                        if (tweak.isDanger) {
                            activeDangerTweak = tweak
                            activeDangerFormattedCmd = formattedCmd
                            dangerStep = DangerDialogStep.STEP_ONE
                        } else {
                            outputTitle = tweak.title
                            outputText = "Executing command: $formattedCmd..."
                            showOutputDialog = true
                            scope.launch {
                                val result = adbManager.executeShellCommand(formattedCmd)
                                outputText = result
                            }
                        }
                    }
                )
            }
        }
    }

    // Scrollable Output Result Dialog
    if (showOutputDialog) {
        AlertDialog(
            onDismissRequest = { showOutputDialog = false },
            title = {
                Text(
                    text = outputTitle,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 350.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(12.dp)
                    ) {
                        SelectionContainer {
                            Text(
                                text = outputText,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showOutputDialog = false }) {
                    Text(strings.close)
                }
            }
        )
    }

    // Elevated Privileges Confirmation Dialog
    if (showElevatedDialog) {
        ElevatedPrivilegesDialog(
            onDismiss = { showElevatedDialog = false },
            onConfirmGrantElevated = {
                showElevatedDialog = false
                scope.launch {
                    adbManager.executeShellCommand("pm grant com.android.tweaker android.permission.WRITE_SECURE_SETTINGS")
                    adbManager.executeShellCommand("pm grant com.android.tweaker android.permission.DUMP")
                    onToggleElevated(true)
                }
            },
            strings = strings
        )
    }

    // Danger Confirmation Dialog
    activeDangerTweak?.let { tweak ->
        DangerConfirmationDialog(
            commandTitle = tweak.title,
            commandString = activeDangerFormattedCmd,
            dangerDescription = tweak.dangerDescription,
            currentStep = dangerStep,
            strings = strings,
            onDismiss = {
                dangerStep = DangerDialogStep.NONE
                activeDangerTweak = null
            },
            onProceedStepOne = {
                dangerStep = DangerDialogStep.STEP_TWO
            },
            onConfirmFinalExecute = {
                val cmdToRun = activeDangerFormattedCmd
                dangerStep = DangerDialogStep.NONE
                activeDangerTweak = null

                outputTitle = "DANGER TWEAK EXECUTED: ${tweak.title}"
                outputText = "Executing high-risk command: $cmdToRun..."
                showOutputDialog = true
                scope.launch {
                    val result = adbManager.executeShellCommand(cmdToRun)
                    outputText = result
                }
            }
        )
    }
}

@Composable
fun TweakCard(
    tweak: TweakItem,
    strings: Strings,
    isElevated: Boolean,
    onExecute: (String) -> Unit
) {
    var param1 by remember { mutableStateOf(if (tweak.inputType is InputType.SingleText) tweak.inputType.defaultValue else "") }
    var param2 by remember { mutableStateOf("") }
    var selectedOptionIndex by remember { mutableStateOf(if (tweak.inputType is InputType.Options) tweak.inputType.defaultIndex else 0) }

    val isBlockedByElevation = tweak.requiresElevation && !isElevated

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (tweak.isDanger) {
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            }
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    if (tweak.isDanger) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Danger",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(
                        text = tweak.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (tweak.isDanger) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                    )
                }

                if (isBlockedByElevation) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Requires Elevated",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Requires elevated privileges",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = tweak.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Command Template Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Code,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "adb shell ${tweak.commandTemplate}",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Input Fields
            when (val input = tweak.inputType) {
                is InputType.SingleText -> {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = param1,
                        onValueChange = { param1 = it },
                        label = { Text(input.label) },
                        placeholder = { Text(input.placeholder) },
                        singleLine = true,
                        enabled = !isBlockedByElevation,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                is InputType.TwoText -> {
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = param1,
                            onValueChange = { param1 = it },
                            label = { Text(input.label1) },
                            placeholder = { Text(input.placeholder1) },
                            singleLine = true,
                            enabled = !isBlockedByElevation,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = param2,
                            onValueChange = { param2 = it },
                            label = { Text(input.label2) },
                            placeholder = { Text(input.placeholder2) },
                            singleLine = true,
                            enabled = !isBlockedByElevation,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                is InputType.Options -> {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = input.label, style = MaterialTheme.typography.labelSmall)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        input.options.forEachIndexed { idx, opt ->
                            FilterChip(
                                selected = (selectedOptionIndex == idx),
                                onClick = { if (!isBlockedByElevation) selectedOptionIndex = idx },
                                enabled = !isBlockedByElevation,
                                label = { Text(opt, fontSize = 11.sp) }
                            )
                        }
                    }
                }
                InputType.None -> {}
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Execute Button
            Button(
                onClick = {
                    val formattedCmd = when (val input = tweak.inputType) {
                        is InputType.SingleText -> tweak.commandTemplate.replace("{package_name}", param1)
                            .replace("{permission}", param1)
                            .replace("{density_value}", param1)
                            .replace("{icon_list}", param1)
                            .replace("{timeout_ms}", param1)
                            .replace("{dimensions}", param1)
                            .replace("{path_to_apk}", param1)
                            .replace("{parameters}", param1)
                        is InputType.TwoText -> tweak.commandTemplate
                            .replace("{package_name}", param1)
                            .replace("{permission}", param2)
                        is InputType.Options -> {
                            val optValue = input.options.getOrElse(selectedOptionIndex) { "" }.split(" ").first()
                            tweak.commandTemplate.replace("{option}", optValue)
                                .replace("{mode}", optValue)
                        }
                        InputType.None -> tweak.commandTemplate
                    }
                    onExecute(formattedCmd)
                },
                enabled = !isBlockedByElevation,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (tweak.isDanger) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                ),
                modifier = Modifier.align(Alignment.End)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (tweak.isDanger) strings.dangerExecute else strings.runCommand)
            }
        }
    }
}
