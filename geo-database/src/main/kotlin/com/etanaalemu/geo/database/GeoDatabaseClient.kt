package com.etanaalemu.geo.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

internal object GeoDatabaseClient {
    private const val DB_NAME = "location.db"
    private const val ASSET_PATH = "databases/location.db"

    @Volatile
    private var instance: LocationDatabase? = null

    fun getInstance(context: Context): LocationDatabase {
        return instance ?: synchronized(this) {
            instance ?: buildDatabase(context.applicationContext).also { instance = it }
        }
    }

    private fun buildDatabase(context: Context): LocationDatabase {
        return Room.databaseBuilder(
            context,
            LocationDatabase::class.java,
            DB_NAME,
        )
            .createFromAsset(ASSET_PATH)
            // Mostly read-only static geo data: avoid a large growing -wal file next to location.db.
            .setJournalMode(RoomDatabase.JournalMode.TRUNCATE)
            .addMigrations(
                LocationMigrations.MIGRATION_1_2,
                LocationMigrations.MIGRATION_2_3,
                LocationMigrations.MIGRATION_3_4,
            )
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()
    }
}
