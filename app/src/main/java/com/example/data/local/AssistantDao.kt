package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AssistantLog
import com.example.data.model.CustomAutomation
import com.example.data.model.UnlockProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface AssistantDao {

    // Logs
    @Query("SELECT * FROM assistant_logs ORDER BY timestamp DESC LIMIT 100")
    fun getAllLogs(): Flow<List<AssistantLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: AssistantLog): Long

    @Query("DELETE FROM assistant_logs")
    suspend fun clearAllLogs()

    @Query("SELECT COUNT(*) FROM assistant_logs")
    suspend fun getLogCount(): Int

    // Automations
    @Query("SELECT * FROM custom_automations ORDER BY createdAt DESC")
    fun getAllAutomations(): Flow<List<CustomAutomation>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAutomation(automation: CustomAutomation): Long

    @Delete
    suspend fun deleteAutomation(automation: CustomAutomation)

    // Unlock profile
    @Query("SELECT * FROM unlock_profiles WHERE id = 1 LIMIT 1")
    fun getUnlockProfile(): Flow<UnlockProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUnlockProfile(profile: UnlockProfile)
}
