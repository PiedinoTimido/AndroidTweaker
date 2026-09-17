package com.android.tweaker.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material.icons.filled.Dangerous
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.tweaker.model.Strings
import kotlinx.coroutines.delay

enum class DangerDialogStep {
    STEP_ONE,
    STEP_TWO,
    NONE
}

@Composable
fun DangerConfirmationDialog(
    commandTitle: String,
    commandString: String,
    dangerDescription: String,
    currentStep: DangerDialogStep,
    onDismiss: () -> Unit,
    onProceedStepOne: () -> Unit,
    onConfirmFinalExecute: () -> Unit,
    strings: Strings
) {
    if (currentStep == DangerDialogStep.STEP_ONE) {
        AlertDialog(
            onDismissRequest = onDismiss,
            icon = {
                Icon(
                    imageVector = Icons.Default.WarningAmber,
                    contentDescription = "Warning",
                    tint = Color(0xFFFF9800),
                    modifier = Modifier.size(40.dp)
                )
            },
            title = {
                Text(
                    text = strings.dangerStepOneTitle,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column {
                    Text(
                        text = "Command: $commandTitle",
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "It will: $dangerDescription",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = onProceedStepOne,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100))
                ) {
                    Text(strings.dangerProceed)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = onDismiss) {
                    Text(strings.dangerCancel)
                }
            }
        )
    } else if (currentStep == DangerDialogStep.STEP_TWO) {
        var secondsRemaining by remember { mutableIntStateOf(5) }

        LaunchedEffect(Unit) {
            secondsRemaining = 5
            while (secondsRemaining > 0) {
                delay(1000L)
                secondsRemaining--
            }
        }

        val progress by animateFloatAsState(
            targetValue = (5 - secondsRemaining) / 5f,
            label = "CountdownProgress"
        )

        AlertDialog(
            onDismissRequest = onDismiss,
            icon = {
                Icon(
                    imageVector = Icons.Default.Dangerous,
                    contentDescription = "Dangerous",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    text = strings.dangerStepTwoTitle,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp
                    ),
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Target command: $commandString",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = strings.dangerStepTwoSubtitle,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    if (secondsRemaining > 0) {
                        Box(contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                progress = { progress },
                                modifier = Modifier.size(64.dp),
                                color = MaterialTheme.colorScheme.error,
                                trackColor = MaterialTheme.colorScheme.errorContainer
                            )
                            Text(
                                text = "$secondsRemaining",
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    } else {
                        Text(
                            text = "Safety unlock ready!",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = onConfirmFinalExecute,
                    enabled = (secondsRemaining == 0),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        disabledContainerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
                    )
                ) {
                    if (secondsRemaining > 0) {
                        Text(String.format(strings.dangerProceedCountdown, secondsRemaining))
                    } else {
                        Text(strings.dangerProceedReady, fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                OutlinedButton(onClick = onDismiss) {
                    Text(strings.dangerCancel)
                }
            }
        )
    }
}
