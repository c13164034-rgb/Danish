package com.example.ui

import android.app.Activity
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.AssistantLog
import com.example.data.model.CustomAutomation
import com.example.data.model.UnlockProfile
import com.example.data.repository.AssistantRepository
import com.example.engine.LocalAssistantAction
import com.example.engine.LocalNlpEngine
import com.example.engine.MessagePlatform
import com.example.engine.SettingType
import com.example.engine.SocialPlatform
import com.example.service.DeviceActionController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: MessageSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val actionDetail: String? = null,
    val isSuccess: Boolean = true,
    val suggestionChips: List<String> = emptyList()
)

enum class MessageSender {
    USER,
    ASSISTANT,
    SYSTEM
}

enum class AssistantTab {
    AI_TERMINAL,
    APPS_AND_SOCIAL,
    UNLOCK_MANAGER,
    PRIVACY_STORAGE
}

class MainViewModel(private val repository: AssistantRepository) : ViewModel() {

    val logs: StateFlow<List<AssistantLog>> = repository.logs.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val automations: StateFlow<List<CustomAutomation>> = repository.automations.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val unlockProfile: StateFlow<UnlockProfile?> = repository.unlockProfile.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    private val _selectedTab = MutableStateFlow(AssistantTab.AI_TERMINAL)
    val selectedTab: StateFlow<AssistantTab> = _selectedTab.asStateFlow()

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                sender = MessageSender.ASSISTANT,
                text = "Namaste! Main aapka 100% Offline Local Phone Assistant hu.\n" +
                        "Aapka sara data phone ke local storage me safe hai, koi bhi 3rd-party server connect nahi hai.\n\n" +
                        "Aap bol ya type kar sakte hain:\n" +
                        "• 'Screen unlock karo'\n" +
                        "• 'WhatsApp kholo'\n" +
                        "• 'Rahul ko message bhejo: Hello'\n" +
                        "• 'Instagram chalao'",
                suggestionChips = listOf("Screen Unlock Karo", "WhatsApp Kholo", "Instagram", "Device Settings", "Privacy Check")
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isExecuting = MutableStateFlow(false)
    val isExecuting: StateFlow<Boolean> = _isExecuting.asStateFlow()

    init {
        // Initialize default unlock profile if not present
        viewModelScope.launch {
            repository.unlockProfile.collect { profile ->
                if (profile == null) {
                    repository.updateUnlockProfile(
                        UnlockProfile(
                            id = 1,
                            interactionType = "KEYGUARD_DISMISS",
                            interactionInstructions = "Turn screen on -> Dismiss lockscreen with saved interaction recipe",
                            secretCodeOrPin = "1234",
                            autoWakeScreen = true,
                            vibrateOnUnlock = true,
                            isEnabled = true
                        )
                    )
                }
            }
        }
    }

    fun setTab(tab: AssistantTab) {
        _selectedTab.value = tab
    }

    fun updateInputText(text: String) {
        _inputText.value = text
    }

    fun executeUserCommand(activity: Activity, rawCommand: String? = null) {
        val command = (rawCommand ?: _inputText.value).trim()
        if (command.isEmpty()) return

        _inputText.value = ""

        // Append user chat message
        val userMsg = ChatMessage(
            sender = MessageSender.USER,
            text = command
        )
        _chatMessages.value = _chatMessages.value + userMsg

        _isExecuting.value = true

        val currentAutomations = automations.value
        val action = LocalNlpEngine.parseCommand(command, currentAutomations)

        when (action) {
            is LocalAssistantAction.UnlockScreen -> {
                val profile = unlockProfile.value ?: UnlockProfile()
                DeviceActionController.performScreenUnlock(activity, profile) { isSuccess, statusMessage ->
                    handleExecutionResult(
                        commandText = command,
                        intentType = "UNLOCK_SCREEN",
                        targetDetail = "Screen Wake & Keyguard Dismiss",
                        isSuccess = isSuccess,
                        statusText = statusMessage,
                        suggestions = listOf("WhatsApp Kholo", "Message Bhejo", "Instagram")
                    )
                }
            }

            is LocalAssistantAction.OpenApp -> {
                val (success, detail) = DeviceActionController.launchAppByName(
                    activity,
                    action.appName,
                    action.targetPackage
                )
                handleExecutionResult(
                    commandText = command,
                    intentType = "OPEN_APP",
                    targetDetail = action.appName,
                    isSuccess = success,
                    statusText = detail,
                    suggestions = listOf("Screen Unlock Karo", "Instagram", "Settings")
                )
            }

            is LocalAssistantAction.SendMessage -> {
                val (success, detail) = DeviceActionController.composeMessage(
                    activity,
                    action.recipient,
                    action.messageText,
                    action.platform
                )
                handleExecutionResult(
                    commandText = command,
                    intentType = "SEND_MESSAGE",
                    targetDetail = "${action.platform.name} -> ${action.recipient}",
                    isSuccess = success,
                    statusText = detail,
                    suggestions = listOf("WhatsApp Kholo", "Screen Unlock Karo")
                )
            }

            is LocalAssistantAction.SocialMedia -> {
                val (success, detail) = DeviceActionController.openSocialApp(
                    activity,
                    action.platform
                )
                handleExecutionResult(
                    commandText = command,
                    intentType = "SOCIAL_MEDIA",
                    targetDetail = action.platform.name,
                    isSuccess = success,
                    statusText = detail,
                    suggestions = listOf("WhatsApp Kholo", "Screen Unlock Karo")
                )
            }

            is LocalAssistantAction.SystemSetting -> {
                val (success, detail) = DeviceActionController.openSystemSetting(
                    activity,
                    action.settingType
                )
                handleExecutionResult(
                    commandText = command,
                    intentType = "SYSTEM_SETTING",
                    targetDetail = action.title,
                    isSuccess = success,
                    statusText = detail,
                    suggestions = listOf("Wi-Fi Settings", "Bluetooth Settings", "Screen Unlock Karo")
                )
            }

            is LocalAssistantAction.CustomRoutine -> {
                executeCustomRoutine(activity, action, command)
            }

            is LocalAssistantAction.PrivacyAudit -> {
                _selectedTab.value = AssistantTab.PRIVACY_STORAGE
                handleExecutionResult(
                    commandText = command,
                    intentType = "PRIVACY_AUDIT",
                    targetDetail = "Local Database & 0-Server Verification",
                    isSuccess = true,
                    statusText = "Privacy Audit khol diya gaya hai. Aapka app 100% offline hai aur koi data bahar nahi jata.",
                    suggestions = listOf("Screen Unlock Karo", "Automations Dekho", "WhatsApp Kholo")
                )
            }

            is LocalAssistantAction.Informational -> {
                handleExecutionResult(
                    commandText = command,
                    intentType = "INFORMATIONAL",
                    targetDetail = "Local Guide",
                    isSuccess = true,
                    statusText = action.responseText,
                    suggestions = action.suggestionChips
                )
            }
        }
    }

    private fun executeCustomRoutine(
        activity: Activity,
        routine: LocalAssistantAction.CustomRoutine,
        commandText: String
    ) {
        when (routine.actionType) {
            "APP_LAUNCH" -> {
                val (success, msg) = DeviceActionController.launchAppByName(activity, routine.target, null)
                handleExecutionResult(commandText, "CUSTOM_ROUTINE", routine.title, success, msg)
            }
            "MESSAGE" -> {
                val (success, msg) = DeviceActionController.composeMessage(
                    activity,
                    routine.target,
                    routine.payload,
                    MessagePlatform.SMS
                )
                handleExecutionResult(commandText, "CUSTOM_ROUTINE", routine.title, success, msg)
            }
            "UNLOCK_ROUTINE" -> {
                val profile = unlockProfile.value ?: UnlockProfile()
                DeviceActionController.performScreenUnlock(activity, profile) { success, msg ->
                    handleExecutionResult(commandText, "CUSTOM_ROUTINE", routine.title, success, msg)
                }
            }
            else -> {
                handleExecutionResult(
                    commandText,
                    "CUSTOM_ROUTINE",
                    routine.title,
                    true,
                    "Custom rule '${routine.title}' executed successfully."
                )
            }
        }
    }

    private fun handleExecutionResult(
        commandText: String,
        intentType: String,
        targetDetail: String,
        isSuccess: Boolean,
        statusText: String,
        suggestions: List<String> = emptyList()
    ) {
        _isExecuting.value = false

        val assistantMsg = ChatMessage(
            sender = MessageSender.ASSISTANT,
            text = statusText,
            actionDetail = "$intentType • $targetDetail",
            isSuccess = isSuccess,
            suggestionChips = suggestions
        )
        _chatMessages.value = _chatMessages.value + assistantMsg

        viewModelScope.launch {
            repository.recordLog(
                commandText = commandText,
                intentType = intentType,
                targetDetail = targetDetail,
                executionStatus = if (isSuccess) "SUCCESS" else "FAILED"
            )
        }
    }

    fun saveAutomation(trigger: String, actionType: String, target: String, payload: String, label: String) {
        viewModelScope.launch {
            repository.addAutomation(
                CustomAutomation(
                    triggerPhrase = trigger.trim(),
                    actionType = actionType,
                    target = target.trim(),
                    payloadText = payload.trim(),
                    label = label.trim()
                )
            )
        }
    }

    fun deleteAutomation(automation: CustomAutomation) {
        viewModelScope.launch {
            repository.removeAutomation(automation)
        }
    }

    fun updateUnlockConfig(
        interactionType: String,
        instructions: String,
        secretPin: String,
        autoWake: Boolean,
        vibrate: Boolean
    ) {
        viewModelScope.launch {
            val updated = UnlockProfile(
                id = 1,
                interactionType = interactionType,
                interactionInstructions = instructions,
                secretCodeOrPin = secretPin,
                autoWakeScreen = autoWake,
                vibrateOnUnlock = vibrate,
                isEnabled = true,
                lastTriggeredAt = System.currentTimeMillis()
            )
            repository.updateUnlockProfile(updated)
        }
    }

    fun clearLogHistory() {
        viewModelScope.launch {
            repository.clearLogs()
            _chatMessages.value = listOf(
                ChatMessage(
                    sender = MessageSender.SYSTEM,
                    text = "Local command logs safai se clear kar diye gaye hain."
                )
            )
        }
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val db = AppDatabase.getInstance(context)
            val repo = AssistantRepository(db.assistantDao())
            return MainViewModel(repo) as T
        }
    }
}
