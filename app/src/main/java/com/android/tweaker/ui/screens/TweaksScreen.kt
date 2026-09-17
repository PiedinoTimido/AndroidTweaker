package com.android.tweaker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.tweaker.data.AdbManager
import com.android.tweaker.model.*
import com.android.tweaker.ui.components.DangerConfirmationDialog
import com.android.tweaker.ui.components.DangerDialogStep
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TweaksScreen(
    adbManager: AdbManager,
    strings: Strings
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

    // Output Result Dialog
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
                    SelectionContainer {
                        Text(
                            text = outputText,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(12.dp)
                        )
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
    onExecute: (String) -> Unit
) {
    var param1 by remember { mutableStateOf(if (tweak.inputType is InputType.SingleText) tweak.inputType.defaultValue else "") }
    var param2 by remember { mutableStateOf("") }
    var selectedOptionIndex by remember { mutableStateOf(if (tweak.inputType is InputType.Options) tweak.inputType.defaultIndex else 0) }

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
            Row(verticalAlignment = Alignment.CenterVertically) {
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
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = param2,
                            onValueChange = { param2 = it },
                            label = { Text(input.label2) },
                            placeholder = { Text(input.placeholder2) },
                            singleLine = true,
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
                                onClick = { selectedOptionIndex = idx },
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
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (tweak.isDanger) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
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
