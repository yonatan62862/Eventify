package com.eventify.app.data.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface EventDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: EventEntity)

    @Query("SELECT * FROM events WHERE ownerId = :userEmail ORDER BY startDate DESC")
    fun getUserEvents(userEmail: String): List<EventEntity>

    @Query("SELECT * FROM events WHERE ownerId = :userId ORDER BY startDate DESC")
    fun getUserEventsLiveData(userId: String): LiveData<List<EventEntity>>

    @Query("DELETE FROM events WHERE id = :eventId")
    suspend fun deleteEvent(eventId: String)

    @Query("DELETE FROM events")
    suspend fun clearAllEvents()
}