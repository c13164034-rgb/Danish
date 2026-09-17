package com.example.engine

import com.example.data.model.CustomAutomation
import java.util.Locale

object LocalNlpEngine {

    /**
     * Parses user command into a deterministic, on-device LocalAssistantAction.
     * Completely offline, 100% on-device local inference with zero network dependency.
     */
    fun parseCommand(
        rawInput: String,
        customAutomations: List<CustomAutomation> = emptyList()
    ): LocalAssistantAction {
        val trimmed = rawInput.trim()
        val lower = trimmed.lowercase(Locale.ROOT)

        if (lower.isEmpty()) {
            return LocalAssistantAction.Informational(
                responseText = "Kripya koi command de ya niche diye gaye options me se chune.",
                suggestionChips = listOf("Screen Unlock Karo", "WhatsApp Kholo", "Rahul ko message bhejo", "Privacy Check")
            )
        }

        // 1. Check user-defined custom automations first
        for (automation in customAutomations) {
            if (lower.contains(automation.triggerPhrase.lowercase(Locale.ROOT)) ||
                automation.triggerPhrase.lowercase(Locale.ROOT).contains(lower)
            ) {
                return LocalAssistantAction.CustomRoutine(
                    title = automation.label.ifBlank { automation.triggerPhrase },
                    actionType = automation.actionType,
                    target = automation.target,
                    payload = automation.payloadText
                )
            }
        }

        // 2. Unlock Screen intent
        if (isUnlockCommand(lower)) {
            return LocalAssistantAction.UnlockScreen(
                sourceCommand = trimmed,
                interactionType = "KEYGUARD_DISMISS"
            )
        }

        // 3. Message dispatch intent (SMS / WhatsApp)
        val messageAction = extractMessageIntent(lower, trimmed)
        if (messageAction != null) {
            return messageAction
        }

        // 4. App Launch intent
        val appAction = extractAppLaunchIntent(lower)
        if (appAction != null) {
            return appAction
        }

        // 5. Social Media intents
        val socialAction = extractSocialIntent(lower)
        if (socialAction != null) {
            return socialAction
        }

        // 6. Device Setting intents
        val settingAction = extractSettingIntent(lower)
        if (settingAction != null) {
            return settingAction
        }

        // 7. Privacy & Storage inquiry
        if (lower.contains("privacy") || lower.contains("safe") || lower.contains("server") ||
            lower.contains("storage") || lower.contains("offline") || lower.contains("security") ||
            lower.contains("data")
        ) {
            return LocalAssistantAction.PrivacyAudit
        }

        // 8. Help / Commands list
        if (lower.contains("help") || lower.contains("madad") || lower.contains("kya kar sakte ho") ||
            lower.contains("commands") || lower.contains("guide")
        ) {
            return LocalAssistantAction.Informational(
                responseText = "Main aapka 100% offline local phone assistant hu. Main ye sab kar sakta hu:\n" +
                        "• 'Screen unlock karo' - Saved interaction se screen unlock & wake karna\n" +
                        "• 'WhatsApp kholo', 'Camera kholo', 'Instagram chalao'\n" +
                        "• 'Rahul ko message bhejo: Kal milenge'\n" +
                        "• 'Wi-Fi kholo', 'Settings chalao'\n" +
                        "• Custom automations & privacy check",
                suggestionChips = listOf("Screen Unlock Karo", "Instagram Kholo", "Message Bhejo", "Device Settings")
            )
        }

        // Default fallback with helpful context
        return LocalAssistantAction.Informational(
            responseText = "Samajh nahi aaya: \"$trimmed\"\nAap 'Screen unlock karo', 'WhatsApp kholo', ya 'Rahul ko message bhejo: Hello' bol ya type kar sakte hain.",
            suggestionChips = listOf("Screen Unlock Karo", "WhatsApp Kholo", "Instagram", "Settings")
        )
    }

    private fun isUnlockCommand(lower: String): Boolean {
        return lower.contains("unlock") ||
                lower.contains("screen unlock") ||
                lower.contains("phone unlock") ||
                lower.contains("mobile unlock") ||
                lower.contains("screen kholo") ||
                lower.contains("phone kholo") ||
                lower.contains("turan phone unlock") ||
                lower.contains("unlock phone")
    }

    private fun extractMessageIntent(lower: String, original: String): LocalAssistantAction.SendMessage? {
        val isMessage = lower.contains("message") ||
                lower.contains("msg") ||
                lower.contains("sandesh") ||
                lower.contains("sms") ||
                lower.contains("whatsapp karo") ||
                lower.contains("whatsapp pe") ||
                lower.contains("bhejo")

        if (!isMessage) return null

        val platform = when {
            lower.contains("whatsapp") -> MessagePlatform.WHATSAPP
            lower.contains("sms") -> MessagePlatform.SMS
            else -> MessagePlatform.GENERAL
        }

        // Try extracting recipient and message
        // Pattern e.g. "Rahul ko message bhejo: kal milte hain"
        var recipient = ""
        var message = ""

        if (original.contains(":")) {
            val parts = original.split(":", limit = 2)
            val left = parts[0]
            message = parts.getOrElse(1) { "" }.trim()

            // Find name in left e.g. "rahul ko message bhejo"
            val leftWords = left.split(" ")
            val koIndex = leftWords.indexOfFirst { it.equals("ko", ignoreCase = true) }
            recipient = if (koIndex > 0) {
                leftWords[koIndex - 1]
            } else {
                leftWords.firstOrNull { it.matches(Regex("[0-9]{10}")) } ?: leftWords.lastOrNull() ?: "Friend"
            }
        } else {
            // General heuristics
            val words = original.split(" ")
            val koIdx = words.indexOfFirst { it.equals("ko", ignoreCase = true) }
            val toIdx = words.indexOfFirst { it.equals("to", ignoreCase = true) }

            val targetIdx = if (koIdx > 0) koIdx - 1 else if (toIdx in 0 until words.lastIndex) toIdx + 1 else -1
            recipient = if (targetIdx in words.indices) words[targetIdx] else "Contact"

            // Message might be after "bhejo" or "send"
            val bhejoIdx = words.indexOfFirst { it.equals("bhejo", ignoreCase = true) || it.equals("send", ignoreCase = true) }
            message = if (bhejoIdx in 0 until words.lastIndex) {
                words.subList(bhejoIdx + 1, words.size).joinToString(" ")
            } else {
                "Hello"
            }
        }

        return LocalAssistantAction.SendMessage(
            recipient = recipient.ifBlank { "Contact" },
            messageText = message.ifBlank { "Hello" },
            platform = platform
        )
    }

    private fun extractAppLaunchIntent(lower: String): LocalAssistantAction.OpenApp? {
        val appKeywords = mapOf(
            "whatsapp" to Pair("WhatsApp", "com.whatsapp"),
            "instagram" to Pair("Instagram", "com.instagram.android"),
            "telegram" to Pair("Telegram", "org.telegram.messenger"),
            "youtube" to Pair("YouTube", "com.google.android.youtube"),
            "twitter" to Pair("Twitter / X", "com.twitter.android"),
            "facebook" to Pair("Facebook", "com.facebook.katana"),
            "camera" to Pair("Camera", "android.media.action.IMAGE_CAPTURE"),
            "kamera" to Pair("Camera", "android.media.action.IMAGE_CAPTURE"),
            "gallery" to Pair("Gallery", null),
            "photos" to Pair("Google Photos", "com.google.android.apps.photos"),
            "settings" to Pair("Settings", "android.settings.SETTINGS"),
            "dialer" to Pair("Phone Dialer", null),
            "phone" to Pair("Phone", null),
            "calculator" to Pair("Calculator", null),
            "clock" to Pair("Clock", null),
            "alarm" to Pair("Alarm", null),
            "chrome" to Pair("Chrome Browser", "com.android.chrome"),
            "browser" to Pair("Browser", null),
            "maps" to Pair("Google Maps", "com.google.android.apps.maps")
        )

        for ((key, pair) in appKeywords) {
            if (lower.contains(key)) {
                return LocalAssistantAction.OpenApp(
                    appName = pair.first,
                    targetPackage = pair.second
                )
            }
        }

        if (lower.startsWith("open ") || lower.startsWith("kholo ") || lower.startsWith("chalao ") || lower.startsWith("launch ")) {
            val words = lower.split(" ", limit = 2)
            if (words.size > 1) {
                val appName = words[1].trim()
                return LocalAssistantAction.OpenApp(
                    appName = appName.replaceFirstChar { it.uppercase() },
                    targetPackage = null
                )
            }
        }

        return null
    }

    private fun extractSocialIntent(lower: String): LocalAssistantAction.SocialMedia? {
        return when {
            lower.contains("insta") -> LocalAssistantAction.SocialMedia(SocialPlatform.INSTAGRAM, "Instagram feed & messages")
            lower.contains("whatsapp") -> LocalAssistantAction.SocialMedia(SocialPlatform.WHATSAPP, "WhatsApp chats & status")
            lower.contains("telegram") -> LocalAssistantAction.SocialMedia(SocialPlatform.TELEGRAM, "Telegram channels & chats")
            lower.contains("tweet") || lower.contains("twitter") || lower.contains(" x ") -> LocalAssistantAction.SocialMedia(SocialPlatform.TWITTER_X, "X / Twitter timeline & posts")
            lower.contains("facebook") || lower.contains(" fb ") -> LocalAssistantAction.SocialMedia(SocialPlatform.FACEBOOK, "Facebook feed & groups")
            lower.contains("youtube") -> LocalAssistantAction.SocialMedia(SocialPlatform.YOUTUBE, "YouTube videos & subscriptions")
            else -> null
        }
    }

    private fun extractSettingIntent(lower: String): LocalAssistantAction.SystemSetting? {
        return when {
            lower.contains("wifi") || lower.contains("wi-fi") -> LocalAssistantAction.SystemSetting(SettingType.WIFI, "Wi-Fi Settings")
            lower.contains("bluetooth") -> LocalAssistantAction.SystemSetting(SettingType.BLUETOOTH, "Bluetooth Settings")
            lower.contains("brightness") || lower.contains("display") -> LocalAssistantAction.SystemSetting(SettingType.DISPLAY, "Display Settings")
            lower.contains("sound") || lower.contains("volume") || lower.contains("awaz") -> LocalAssistantAction.SystemSetting(SettingType.SOUND, "Sound & Vibration")
            lower.contains("battery") -> LocalAssistantAction.SystemSetting(SettingType.BATTERY, "Battery Usage & Optimization")
            lower.contains("setting") -> LocalAssistantAction.SystemSetting(SettingType.GENERAL_SETTINGS, "Device Settings")
            else -> null
        }
    }
}
