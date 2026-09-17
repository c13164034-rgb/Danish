package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "unlock_profiles")
data class UnlockProfile(
    @PrimaryKey
    val id: Int = 1,
    val interactionType: String = "DISMISS_KEYGUARD", // "DISMISS_KEYGUARD", "PIN_SEQUENCE", "SWIPE_GESTURE", "CUSTOM_RECIPE"
    val interactionInstructions: String = "Wake screen and prompt system keyguard dismissal with saved recipe",
    val secretCodeOrPin: String = "",
    val autoWakeScreen: Boolean = true,
    val vibrateOnUnlock: Boolean = true,
    val isEnabled: Boolean = true,
    val lastTriggeredAt: Long = 0L
)
