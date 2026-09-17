package com.example.ui.components

import android.app.Activity
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DialerSip
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.MessagePlatform
import com.example.engine.SocialPlatform
import com.example.service.DeviceActionController
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ShieldEmerald

@Composable
fun AppAndSocialHubView(
    activity: Activity,
    onStatusMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var recipient by remember { mutableStateOf("") }
    var messageContent by remember { mutableStateOf("") }
    var selectedPlatform by remember { mutableStateOf(MessagePlatform.WHATSAPP) }
    var customAppQuery by remember { mutableStateOf("") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section 1: Quick Direct Message Dispatcher
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Chat,
                            contentDescription = "Message",
                            tint = CyberCyan,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Direct Message Dispatcher",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Text(
                        text = "Kisi bhi vyakti ko turant WhatsApp ya SMS sandesh bheje bina kisi third-party cloud ke:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )

                    // Platform Selection Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedPlatform == MessagePlatform.WHATSAPP,
                            onClick = { selectedPlatform = MessagePlatform.WHATSAPP },
                            label = { Text("WhatsApp Direct") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ShieldEmerald.copy(alpha = 0.2f),
                                selectedLabelColor = ShieldEmerald
                            ),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )

                        FilterChip(
                            selected = selectedPlatform == MessagePlatform.SMS,
                            onClick = { selectedPlatform = MessagePlatform.SMS },
                            label = { Text("Default SMS") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyberCyan.copy(alpha = 0.2f),
                                selectedLabelColor = CyberCyan
                            ),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = recipient,
                        onValueChange = { recipient = it },
                        label = { Text("Mobile Number ya Contact Name") },
                        placeholder = { Text("+91 9876543210 ya Rahul") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("msg_recipient_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyan
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = messageContent,
                        onValueChange = { messageContent = it },
                        label = { Text("Message Text") },
                        placeholder = { Text("E.g. Main abhi thodi der me call karta hu.") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("msg_content_input"),
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyan
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            if (messageContent.isNotBlank()) {
                                val (success, message) = DeviceActionController.composeMessage(
                                    activity,
                                    recipient = recipient.ifBlank { "Contact" },
                                    messageText = messageContent,
                                    platform = selectedPlatform
                                )
                                onStatusMessage(message)
                            } else {
                                onStatusMessage("Kripya message likhe.")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("dispatch_message_btn"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyberCyan,
                            contentColor = Color.Black
                        )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (selectedPlatform == MessagePlatform.WHATSAPP) "Send via WhatsApp" else "Send via SMS",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Section 2: Social Media Hub
        item {
            Text(
                text = "Social Media Hub",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Apne social accounts ko one-tap se directly launch kare:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SocialAppCard(
                    title = "WhatsApp",
                    subtitle = "Chats & Status",
                    icon = Icons.AutoMirrored.Filled.Chat,
                    accentColor = ShieldEmerald,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        val (_, msg) = DeviceActionController.openSocialApp(activity, SocialPlatform.WHATSAPP)
                        onStatusMessage(msg)
                    }
                )

                SocialAppCard(
                    title = "Instagram",
                    subtitle = "Reels & Direct",
                    icon = Icons.Default.Share,
                    accentColor = Color(0xFFE1306C),
                    modifier = Modifier.weight(1f),
                    onClick = {
                        val (_, msg) = DeviceActionController.openSocialApp(activity, SocialPlatform.INSTAGRAM)
                        onStatusMessage(msg)
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SocialAppCard(
                    title = "YouTube",
                    subtitle = "Videos & Shorts",
                    icon = Icons.Default.PlayCircle,
                    accentColor = Color(0xFFFF0000),
                    modifier = Modifier.weight(1f),
                    onClick = {
                        val (_, msg) = DeviceActionController.openSocialApp(activity, SocialPlatform.YOUTUBE)
                        onStatusMessage(msg)
                    }
                )

                SocialAppCard(
                    title = "Telegram",
                    subtitle = "Channels & Msg",
                    icon = Icons.AutoMirrored.Filled.Send,
                    accentColor = Color(0xFF229ED9),
                    modifier = Modifier.weight(1f),
                    onClick = {
                        val (_, msg) = DeviceActionController.openSocialApp(activity, SocialPlatform.TELEGRAM)
                        onStatusMessage(msg)
                    }
                )
            }
        }

        // Section 3: Core System Apps
        item {
            Text(
                text = "Phone Utilities & Hardware Control",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                UtilityChip(
                    label = "Camera",
                    icon = Icons.Default.CameraAlt,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        val (_, msg) = DeviceActionController.launchAppByName(activity, "Camera", null)
                        onStatusMessage(msg)
                    }
                )

                UtilityChip(
                    label = "Phone Dialer",
                    icon = Icons.Default.DialerSip,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        val (_, msg) = DeviceActionController.launchAppByName(activity, "Phone", null)
                        onStatusMessage(msg)
                    }
                )

                UtilityChip(
                    label = "Gallery",
                    icon = Icons.Default.PhotoLibrary,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        val (_, msg) = DeviceActionController.launchAppByName(activity, "Gallery", null)
                        onStatusMessage(msg)
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                UtilityChip(
                    label = "Settings",
                    icon = Icons.Default.Settings,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        val (_, msg) = DeviceActionController.launchAppByName(activity, "Settings", null)
                        onStatusMessage(msg)
                    }
                )

                UtilityChip(
                    label = "Browser",
                    icon = Icons.Default.Language,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        val (_, msg) = DeviceActionController.launchAppByName(activity, "Browser", null)
                        onStatusMessage(msg)
                    }
                )
            }
        }

        // Section 4: Universal App Launcher by Name
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Any Installed App Launcher",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Phone me maujood kisi bhi application ka naam likhkar seedha start kare:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = customAppQuery,
                            onValueChange = { customAppQuery = it },
                            placeholder = { Text("App name (e.g. Spotify, Uber, Clock)...", fontSize = 12.sp) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = CyberCyan
                                )
                            }
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = {
                                if (customAppQuery.isNotBlank()) {
                                    val (_, msg) = DeviceActionController.launchAppByName(
                                        activity,
                                        customAppQuery,
                                        null
                                    )
                                    onStatusMessage(msg)
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CyberCyan,
                                contentColor = Color.Black
                            )
                        ) {
                            Text("Launch")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SocialAppCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = accentColor.copy(alpha = 0.15f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = accentColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun UtilityChip(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(48.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = CyberCyan,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
