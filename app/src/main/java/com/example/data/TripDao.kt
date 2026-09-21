package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TripDao {

    @Query("SELECT * FROM trips WHERE status IN ('STARTING', 'ACTIVE', 'WAITING') ORDER BY tripId DESC LIMIT 1")
    suspend fun getActiveTrip(): TripEntity?

    @Query("SELECT * FROM trips WHERE tripId = :tripId LIMIT 1")
    suspend fun getTripById(tripId: Long): TripEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrip(trip: TripEntity): Long

    @Update
    suspend fun updateTrip(trip: TripEntity)

    @Query("SELECT * FROM trips WHERE status = 'COMPLETED' ORDER BY startTimestamp DESC")
    fun getAllCompletedTrips(): Flow<List<TripEntity>>

    @Query("SELECT * FROM trips WHERE status = 'COMPLETED' AND startTimestamp >= :startOfDayMs ORDER BY startTimestamp DESC")
    fun getTodayCompletedTrips(startOfDayMs: Long): Flow<List<TripEntity>>

    @Delete
    suspend fun deleteTrip(trip: TripEntity)

    @Query("DELETE FROM trips")
    suspend fun deleteAllTrips()
}
