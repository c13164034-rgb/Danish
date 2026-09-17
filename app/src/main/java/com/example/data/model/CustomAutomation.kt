package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "custom_automations")
data class CustomAutomation(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val triggerPhrase: String,
    val actionType: String, // "APP_LAUNCH", "MESSAGE", "UNLOCK_ROUTINE", "SOCIAL_MEDIA", "SYSTEM_SETTING"
    val target: String,     // package name or phone number or setting
    val payloadText: String = "", // pre-filled message or command payload
    val label: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
