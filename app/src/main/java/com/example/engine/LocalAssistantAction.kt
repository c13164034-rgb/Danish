package com.example.engine

sealed class LocalAssistantAction {
    data class UnlockScreen(
        val sourceCommand: String,
        val interactionType: String = "KEYGUARD_DISMISS"
    ) : LocalAssistantAction()

    data class SendMessage(
        val recipient: String,
        val messageText: String,
        val platform: MessagePlatform // SMS, WHATSAPP, GENERAL
    ) : LocalAssistantAction()

    data class OpenApp(
        val appName: String,
        val targetPackage: String? = null
    ) : LocalAssistantAction()

    data class SocialMedia(
        val platform: SocialPlatform,
        val actionDescription: String
    ) : LocalAssistantAction()

    data class SystemSetting(
        val settingType: SettingType,
        val title: String
    ) : LocalAssistantAction()

    object PrivacyAudit : LocalAssistantAction()

    data class CustomRoutine(
        val title: String,
        val actionType: String,
        val target: String,
        val payload: String
    ) : LocalAssistantAction()

    data class Informational(
        val responseText: String,
        val suggestionChips: List<String> = emptyList()
    ) : LocalAssistantAction()
}

enum class MessagePlatform {
    SMS,
    WHATSAPP,
    GENERAL
}

enum class SocialPlatform {
    WHATSAPP,
    INSTAGRAM,
    TWITTER_X,
    TELEGRAM,
    FACEBOOK,
    YOUTUBE
}

enum class SettingType {
    WIFI,
    BLUETOOTH,
    DISPLAY,
    SOUND,
    BATTERY,
    GENERAL_SETTINGS
}
