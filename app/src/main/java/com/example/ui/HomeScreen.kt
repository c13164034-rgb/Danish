package com.example.ui

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.AppAndSocialHubView
import com.example.ui.components.AssistantTerminalView
import com.example.ui.components.PrivacyStorageView
import com.example.ui.components.UnlockManagerView
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ShieldEmerald
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    activity: Activity,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val inputText by viewModel.inputText.collectAsStateWithLifecycle()
    val isExecuting by viewModel.isExecuting.collectAsStateWithLifecycle()
    val automations by viewModel.automations.collectAsStateWithLifecycle()
    val logs by viewModel.logs.collectAsStateWithLifecycle()
    val unlockProfile by viewModel.unlockProfile.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(CyberCyan),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SmartToy,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = "Local Assistant",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Zero Server • 100% On-Device",
                                style = MaterialTheme.typography.labelSmall,
                                color = ShieldEmerald,
                                fontSize = 10.sp
                            )
                        }
                    }
                },
                actions = {
                    Surface(
                        shape = CircleShape,
                        color = ShieldEmerald.copy(alpha = 0.2f),
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(ShieldEmerald)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "OFFLINE",
                                style = MaterialTheme.typography.labelSmall,
                                color = ShieldEmerald,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                NavigationBarItem(
                    selected = selectedTab == AssistantTab.AI_TERMINAL,
                    onClick = { viewModel.setTab(AssistantTab.AI_TERMINAL) },
                    icon = {
                        Icon(Icons.Default.SmartToy, contentDescription = "Terminal")
                    },
                    label = { Text("Terminal") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        indicatorColor = CyberCyan
                    ),
                    modifier = Modifier.testTag("nav_tab_terminal")
                )

                NavigationBarItem(
                    selected = selectedTab == AssistantTab.APPS_AND_SOCIAL,
                    onClick = { viewModel.setTab(AssistantTab.APPS_AND_SOCIAL) },
                    icon = {
                        Icon(Icons.Default.Apps, contentDescription = "Apps & Social")
                    },
                    label = { Text("Apps Hub") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        indicatorColor = CyberCyan
                    ),
                    modifier = Modifier.testTag("nav_tab_apps")
                )

                NavigationBarItem(
                    selected = selectedTab == AssistantTab.UNLOCK_MANAGER,
                    onClick = { viewModel.setTab(AssistantTab.UNLOCK_MANAGER) },
                    icon = {
                        Icon(Icons.Default.LockOpen, contentDescription = "Screen Unlock")
                    },
                    label = { Text("Unlock") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        indicatorColor = CyberCyan
                    ),
                    modifier = Modifier.testTag("nav_tab_unlock")
                )

                NavigationBarItem(
                    selected = selectedTab == AssistantTab.PRIVACY_STORAGE,
                    onClick = { viewModel.setTab(AssistantTab.PRIVACY_STORAGE) },
                    icon = {
                        BadgedBox(
                            badge = {
                                Badge(containerColor = ShieldEmerald) {
                                    Text("0", color = Color.Black, fontSize = 9.sp)
                                }
                            }
                        ) {
                            Icon(Icons.Default.Security, contentDescription = "Privacy & Storage")
                        }
                    },
                    label = { Text("Privacy") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        indicatorColor = CyberCyan
                    ),
                    modifier = Modifier.testTag("nav_tab_privacy")
                )
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                AssistantTab.AI_TERMINAL -> {
                    AssistantTerminalView(
                        activity = activity,
                        messages = chatMessages,
                        inputText = inputText,
                        isExecuting = isExecuting,
                        onInputChange = { viewModel.updateInputText(it) },
                        onSendCommand = { cmd ->
                            viewModel.executeUserCommand(activity, cmd)
                        }
                    )
                }

                AssistantTab.APPS_AND_SOCIAL -> {
                    AppAndSocialHubView(
                        activity = activity,
                        onStatusMessage = { msg ->
                            scope.launch { snackbarHostState.showSnackbar(msg) }
                        }
                    )
                }

                AssistantTab.UNLOCK_MANAGER -> {
                    UnlockManagerView(
                        activity = activity,
                        unlockProfile = unlockProfile,
                        onSaveProfile = { method, instructions, pin, autoWake, vibrate ->
                            viewModel.updateUnlockConfig(method, instructions, pin, autoWake, vibrate)
                        },
                        onStatusMessage = { msg ->
                            scope.launch { snackbarHostState.showSnackbar(msg) }
                        }
                    )
                }

                AssistantTab.PRIVACY_STORAGE -> {
                    PrivacyStorageView(
                        logs = logs,
                        automations = automations,
                        onClearLogs = { viewModel.clearLogHistory() },
                        onAddAutomation = { trigger, action, target, payload, label ->
                            viewModel.saveAutomation(trigger, action, target, payload, label)
                        },
                        onDeleteAutomation = { automation ->
                            viewModel.deleteAutomation(automation)
                        }
                    )
                }
            }
        }
    }
}
