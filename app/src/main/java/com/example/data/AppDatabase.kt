package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [TripEntity::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun tripDao(): TripDao

    companion object {
        private val MIGRATION_1_2 = object : androidx.room.migration.Migration(1, 2) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE trips ADD COLUMN tariffDistanceChargeEnabled INTEGER NOT NULL DEFAULT 1"
                )
            }
        }

        private val MIGRATION_2_3 = object : androidx.room.migration.Migration(2, 3) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE trips_new (
                        tripId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        status TEXT NOT NULL,
                        startTimestamp INTEGER NOT NULL,
                        currentTimestamp INTEGER NOT NULL,
                        endTimestamp INTEGER,
                        startLatitude REAL,
                        startLongitude REAL,
                        currentLatitude REAL,
                        currentLongitude REAL,
                        totalDistanceMeters REAL NOT NULL,
                        movingDistanceMeters REAL NOT NULL,
                        waitingDurationSeconds INTEGER NOT NULL,
                        tripDurationSeconds INTEGER NOT NULL,
                        currentFare REAL NOT NULL,
                        baseFare REAL NOT NULL,
                        distanceFare REAL NOT NULL,
                        waitingFare REAL NOT NULL,
                        extraChargesTotal REAL NOT NULL,
                        extraChargesJson TEXT NOT NULL,
                        tariffId TEXT NOT NULL,
                        tariffName TEXT NOT NULL,
                        tariffBaseFare REAL NOT NULL,
                        tariffMinFare REAL NOT NULL,
                        tariffDistanceRate REAL NOT NULL,
                        tariffDistanceChargeEnabled INTEGER NOT NULL,
                        tariffWaitingRate REAL NOT NULL,
                        tariffFreeDistanceKm REAL NOT NULL,
                        tariffFreeWaitingMin REAL NOT NULL,
                        lastGpsTimestamp INTEGER NOT NULL,
                        isRecovered INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                database.execSQL(
                    """
                    INSERT INTO trips_new (
                        tripId, status, startTimestamp, currentTimestamp, endTimestamp,
                        startLatitude, startLongitude, currentLatitude, currentLongitude,
                        totalDistanceMeters, movingDistanceMeters, waitingDurationSeconds,
                        tripDurationSeconds, currentFare, baseFare, distanceFare, waitingFare,
                        extraChargesTotal, extraChargesJson, tariffId, tariffName, tariffBaseFare,
                        tariffMinFare, tariffDistanceRate, tariffDistanceChargeEnabled,
                        tariffWaitingRate, tariffFreeDistanceKm, tariffFreeWaitingMin,
                        lastGpsTimestamp, isRecovered
                    )
                    SELECT
                        tripId, status, startTimestamp, currentTimestamp, endTimestamp,
                        startLatitude, startLongitude, currentLatitude, currentLongitude,
                        totalDistanceMeters, movingDistanceMeters, waitingDurationSeconds,
                        tripDurationSeconds, currentFare, baseFare, distanceFare, waitingFare,
                        extraChargesTotal, extraChargesJson, tariffId, tariffName, tariffBaseFare,
                        tariffMinFare, tariffDistanceRate, tariffDistanceChargeEnabled,
                        tariffWaitingRate, tariffFreeDistanceKm, tariffFreeWaitingMin,
                        lastGpsTimestamp, isRecovered
                    FROM trips
                    """.trimIndent()
                )
                database.execSQL("DROP TABLE trips")
                database.execSQL("ALTER TABLE trips_new RENAME TO trips")
            }
        }

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "get_taxi_meter.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
