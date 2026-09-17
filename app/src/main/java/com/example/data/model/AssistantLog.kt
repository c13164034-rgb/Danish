package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "assistant_logs")
data class AssistantLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val commandText: String,
    val intentType: String,
    val targetDetail: String,
    val executionStatus: String, // "SUCCESS", "EXECUTED", "EXPLANATION", "ERROR"
    val timestamp: Long = System.currentTimeMillis()
)
