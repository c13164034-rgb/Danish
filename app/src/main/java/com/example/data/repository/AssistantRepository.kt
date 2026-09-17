package com.example.data.repository

import com.example.data.local.AssistantDao
import com.example.data.model.AssistantLog
import com.example.data.model.CustomAutomation
import com.example.data.model.UnlockProfile
import kotlinx.coroutines.flow.Flow

class AssistantRepository(private val dao: AssistantDao) {

    val logs: Flow<List<AssistantLog>> = dao.getAllLogs()
    val automations: Flow<List<CustomAutomation>> = dao.getAllAutomations()
    val unlockProfile: Flow<UnlockProfile?> = dao.getUnlockProfile()

    suspend fun recordLog(
        commandText: String,
        intentType: String,
        targetDetail: String,
        executionStatus: String
    ): Long {
        return dao.insertLog(
            AssistantLog(
                commandText = commandText,
                intentType = intentType,
                targetDetail = targetDetail,
                executionStatus = executionStatus
            )
        )
    }

    suspend fun clearLogs() {
        dao.clearAllLogs()
    }

    suspend fun getLogCount(): Int {
        return dao.getLogCount()
    }

    suspend fun addAutomation(automation: CustomAutomation): Long {
        return dao.insertAutomation(automation)
    }

    suspend fun removeAutomation(automation: CustomAutomation) {
        dao.deleteAutomation(automation)
    }

    suspend fun updateUnlockProfile(profile: UnlockProfile) {
        dao.saveUnlockProfile(profile)
    }
}
