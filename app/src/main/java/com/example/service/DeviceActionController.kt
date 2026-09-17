package com.example.service

import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import com.example.data.model.UnlockProfile
import com.example.engine.MessagePlatform
import com.example.engine.SettingType
import com.example.engine.SocialPlatform
import java.net.URLEncoder

object DeviceActionController {

    /**
     * Executes the Screen Unlock interaction recipe.
     * Android OS architecture requires KeyguardManager to safely dismiss or request
     * user unlock prompt without exposing raw OS credentials to third-party code.
     */
    fun performScreenUnlock(
        activity: Activity,
        profile: UnlockProfile,
        onStatusUpdate: (isSuccess: Boolean, message: String) -> Unit
    ) {
        // Haptic feedback
        vibratePhone(activity, 80)

        // Wake screen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            activity.setShowWhenLocked(true)
            activity.setTurnScreenOn(true)
        }

        val keyguardManager = activity.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager

        if (keyguardManager == null) {
            onStatusUpdate(false, "Keyguard service unavailable.")
            return
        }

        val isLocked = keyguardManager.isKeyguardLocked
        val isSecure = keyguardManager.isKeyguardSecure

        if (!isLocked) {
            onStatusUpdate(true, "Screen pehle se unlocked hai. Saved interaction active.")
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            keyguardManager.requestDismissKeyguard(activity, object : KeyguardManager.KeyguardDismissCallback() {
                override fun onDismissSucceeded() {
                    vibratePhone(activity, 150)
                    onStatusUpdate(true, "Screen Safalta se Unlock ho gaya! Saved interaction recipe executed.")
                }

                override fun onDismissCancelled() {
                    onStatusUpdate(false, "Unlock cancel kiya gaya ya interaction interrupt hui.")
                }

                override fun onDismissError() {
                    onStatusUpdate(false, "Screen unlock karte waqt error aaya. Kripya system PIN check kare.")
                }
            })
        } else {
            onStatusUpdate(true, "Unlock command trigger kiya gaya (Legacy Keyguard mode).")
        }
    }

    fun launchAppByName(context: Context, appName: String, targetPackage: String?): Pair<Boolean, String> {
        val pm = context.packageManager

        // 1. Try target package directly if specified
        if (!targetPackage.isNullOrBlank()) {
            val intent = pm.getLaunchIntentForPackage(targetPackage)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                vibratePhone(context, 40)
                return Pair(true, "$appName khol diya gaya.")
            }
        }

        // 2. Scan installed launcher activities
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfos = pm.queryIntentActivities(mainIntent, 0)
        for (info in resolveInfos) {
            val label = info.loadLabel(pm).toString()
            if (label.contains(appName, ignoreCase = true) || appName.contains(label, ignoreCase = true)) {
                val launchIntent = pm.getLaunchIntentForPackage(info.activityInfo.packageName)
                if (launchIntent != null) {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(launchIntent)
                    vibratePhone(context, 40)
                    return Pair(true, "$label app khol diya gaya.")
                }
            }
        }

        // 3. Fallback for common generic utilities
        when (appName.lowercase()) {
            "camera" -> {
                val camIntent = Intent("android.media.action.IMAGE_CAPTURE").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                if (camIntent.resolveActivity(pm) != null) {
                    context.startActivity(camIntent)
                    return Pair(true, "Camera open kiya gaya.")
                }
            }
            "settings" -> {
                val setIntent = Intent(Settings.ACTION_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(setIntent)
                return Pair(true, "Settings open ki gayi.")
            }
            "dialer", "phone" -> {
                val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(dialIntent)
                return Pair(true, "Phone dialer khol diya gaya.")
            }
        }

        return Pair(false, "App '$appName' phone me install nahi mila ya package launch support nahi karta.")
    }

    fun composeMessage(
        context: Context,
        recipient: String,
        messageText: String,
        platform: MessagePlatform
    ): Pair<Boolean, String> {
        return try {
            when (platform) {
                MessagePlatform.WHATSAPP -> {
                    val cleanPhone = recipient.replace(Regex("[^0-9+]"), "")
                    val url = if (cleanPhone.isNotBlank()) {
                        "https://api.whatsapp.com/send?phone=$cleanPhone&text=${URLEncoder.encode(messageText, "UTF-8")}"
                    } else {
                        "https://api.whatsapp.com/send?text=${URLEncoder.encode(messageText, "UTF-8")}"
                    }
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                        setPackage("com.whatsapp")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }

                    if (intent.resolveActivity(context.packageManager) != null) {
                        context.startActivity(intent)
                        vibratePhone(context, 40)
                        Pair(true, "WhatsApp chat kholi gayi $recipient ke liye.")
                    } else {
                        // Fallback generic send
                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, messageText)
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Share via").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                        Pair(true, "Message share dialogue khol diya gaya.")
                    }
                }
                MessagePlatform.SMS -> {
                    val cleanPhone = recipient.replace(Regex("[^0-9+]"), "")
                    val smsUri = if (cleanPhone.isNotBlank()) Uri.parse("smsto:$cleanPhone") else Uri.parse("smsto:")
                    val smsIntent = Intent(Intent.ACTION_SENDTO, smsUri).apply {
                        putExtra("sms_body", messageText)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    if (smsIntent.resolveActivity(context.packageManager) != null) {
                        context.startActivity(smsIntent)
                        vibratePhone(context, 40)
                        Pair(true, "SMS composer khol diya gaya ($recipient: '$messageText').")
                    } else {
                        Pair(false, "SMS application uplabdh nahi hai.")
                    }
                }
                MessagePlatform.GENERAL -> {
                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, messageText)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(Intent.createChooser(sendIntent, "Send message to $recipient").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                    Pair(true, "Message dispatcher khol diya gaya.")
                }
            }
        } catch (e: Exception) {
            Pair(false, "Message bhejte samay samasya aayi: ${e.localizedMessage}")
        }
    }

    fun openSocialApp(context: Context, platform: SocialPlatform): Pair<Boolean, String> {
        val (pkg, name) = when (platform) {
            SocialPlatform.WHATSAPP -> Pair("com.whatsapp", "WhatsApp")
            SocialPlatform.INSTAGRAM -> Pair("com.instagram.android", "Instagram")
            SocialPlatform.TELEGRAM -> Pair("org.telegram.messenger", "Telegram")
            SocialPlatform.TWITTER_X -> Pair("com.twitter.android", "Twitter / X")
            SocialPlatform.FACEBOOK -> Pair("com.facebook.katana", "Facebook")
            SocialPlatform.YOUTUBE -> Pair("com.google.android.youtube", "YouTube")
        }

        val intent = context.packageManager.getLaunchIntentForPackage(pkg)
        return if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            vibratePhone(context, 40)
            Pair(true, "$name khol diya gaya.")
        } else {
            // Open fallback web
            val webUrl = when (platform) {
                SocialPlatform.WHATSAPP -> "https://web.whatsapp.com"
                SocialPlatform.INSTAGRAM -> "https://instagram.com"
                SocialPlatform.TELEGRAM -> "https://web.telegram.org"
                SocialPlatform.TWITTER_X -> "https://x.com"
                SocialPlatform.FACEBOOK -> "https://facebook.com"
                SocialPlatform.YOUTUBE -> "https://youtube.com"
            }
            try {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(webUrl)).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(browserIntent)
                Pair(true, "$name app nahi mila, browser me open kiya.")
            } catch (e: Exception) {
                Pair(false, "$name open nahi kiya ja saka.")
            }
        }
    }

    fun openSystemSetting(context: Context, settingType: SettingType): Pair<Boolean, String> {
        val action = when (settingType) {
            SettingType.WIFI -> Settings.ACTION_WIFI_SETTINGS
            SettingType.BLUETOOTH -> Settings.ACTION_BLUETOOTH_SETTINGS
            SettingType.DISPLAY -> Settings.ACTION_DISPLAY_SETTINGS
            SettingType.SOUND -> Settings.ACTION_SOUND_SETTINGS
            SettingType.BATTERY -> Settings.ACTION_BATTERY_SAVER_SETTINGS
            SettingType.GENERAL_SETTINGS -> Settings.ACTION_SETTINGS
        }

        return try {
            val intent = Intent(action).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            vibratePhone(context, 40)
            Pair(true, "${settingType.name} settings khol di gayi.")
        } catch (e: Exception) {
            Pair(false, "Settings open nahi ho saki: ${e.localizedMessage}")
        }
    }

    private fun vibratePhone(context: Context, millis: Long) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createOneShot(millis, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                @Suppress("DEPRECATION")
                vibrator?.vibrate(millis)
            }
        } catch (_: Exception) {
            // Ignore if vibration unavailable
        }
    }
}
