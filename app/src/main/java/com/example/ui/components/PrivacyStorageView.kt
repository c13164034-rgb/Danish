package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.VpnLock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AssistantLog
import com.example.data.model.CustomAutomation
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ErrorCoral
import com.example.ui.theme.ShieldEmerald

@Composable
fun PrivacyStorageView(
    logs: List<AssistantLog>,
    automations: List<CustomAutomation>,
    onClearLogs: () -> Unit,
    onAddAutomation: (trigger: String, actionType: String, target: String, payload: String, label: String) -> Unit,
    onDeleteAutomation: (CustomAutomation) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Privacy Shield Card
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(ShieldEmerald.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudOff,
                                contentDescription = "Offline Privacy",
                                tint = ShieldEmerald,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "100% Offline & On-Device Security",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Zero 3rd-Party Server Connection",
                                style = MaterialTheme.typography.labelMedium,
                                color = ShieldEmerald,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Aapka poora data phone ke internal SQLite (Room) local storage me surakshit rehta hai. Is application me INTERNET permission nahi hai, jisse operating system level par kisi bhi server se data bahar bhejna asambhav hai.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Privacy Audit Metrics
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AuditMetricBox(
                            title = "Server Calls",
                            value = "0 (Blocked)",
                            icon = Icons.Default.VpnLock,
                            tint = ShieldEmerald,
                            modifier = Modifier.weight(1f)
                        )
                        AuditMetricBox(
                            title = "Cloud SDKs",
                            value = "None",
                            icon = Icons.Default.Security,
                            tint = CyberCyan,
                            modifier = Modifier.weight(1f)
                        )
                        AuditMetricBox(
                            title = "Storage",
                            value = "Room SQLite",
                            icon = Icons.Default.Storage,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Section: Custom Automations (Trigger Rules)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Custom Task Automations",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Aapke sikhaye gaye custom voice triggers & shortcuts:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = { showAddDialog = true },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyberCyan,
                        contentColor = Color.Black
                    ),
                    modifier = Modifier.testTag("add_automation_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Rule", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        if (automations.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Koi custom automation abhi add nahi hai. Niche '+ New Rule' par click karke custom trigger banaye jaise 'Ghar pahunch gaya' -> SMS to Mummy.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(automations, key = { it.id }) { automation ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "\"${automation.triggerPhrase}\"",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = CyberCyan
                            )
                            Text(
                                text = "Action: ${automation.actionType} • Target: ${automation.target}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (automation.payloadText.isNotBlank()) {
                                Text(
                                    text = "Payload: \"${automation.payloadText}\"",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ShieldEmerald
                                )
                            }
                        }

                        IconButton(onClick = { onDeleteAutomation(automation) }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Rule",
                                tint = ErrorCoral
                            )
                        }
                    }
                }
            }
        }

        // Section: Local Database Management
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Local Database Management",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Database file: local_assistant.db\nRecorded command logs: ${logs.size} entries",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = onClearLogs,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = ErrorCoral
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("clear_logs_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Clear All Local Logs History")
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddAutomationDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { trigger, action, target, payload, label ->
                onAddAutomation(trigger, action, target, payload, label)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun AuditMetricBox(
    title: String,
    value: String,
    icon: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = tint,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = tint
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
fun AddAutomationDialog(
    onDismiss: () -> Unit,
    onConfirm: (trigger: String, action: String, target: String, payload: String, label: String) -> Unit
) {
    var triggerPhrase by remember { mutableStateOf("") }
    var selectedAction by remember { mutableStateOf("APP_LAUNCH") }
    var targetText by remember { mutableStateOf("") }
    var payloadText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Custom Automation Rule") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = triggerPhrase,
                    onValueChange = { triggerPhrase = it },
                    label = { Text("Trigger Voice / Text Phrase") },
                    placeholder = { Text("E.g. Ghar pahunch gaya") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Action Type:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(
                        onClick = { selectedAction = "APP_LAUNCH" },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = if (selectedAction == "APP_LAUNCH") CyberCyan else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Text("Open App")
                    }

                    TextButton(
                        onClick = { selectedAction = "MESSAGE" },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = if (selectedAction == "MESSAGE") CyberCyan else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Text("Send Msg")
                    }

                    TextButton(
                        onClick = { selectedAction = "UNLOCK_ROUTINE" },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = if (selectedAction == "UNLOCK_ROUTINE") CyberCyan else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Text("Unlock")
                    }
                }

                OutlinedTextField(
                    value = targetText,
                    onValueChange = { targetText = it },
                    label = { Text("Target (App Name or Contact)") },
                    placeholder = { Text("E.g. WhatsApp ya 9876543210") },
                    modifier = Modifier.fillMaxWidth()
                )

                if (selectedAction == "MESSAGE") {
                    OutlinedTextField(
                        value = payloadText,
                        onValueChange = { payloadText = it },
                        label = { Text("Message Body") },
                        placeholder = { Text("E.g. I reached home safely!") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (triggerPhrase.isNotBlank()) {
                        onConfirm(
                            triggerPhrase,
                            selectedAction,
                            targetText,
                            payloadText,
                            triggerPhrase
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = CyberCyan,
                    contentColor = Color.Black
                )
            ) {
                Text("Save Rule")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
