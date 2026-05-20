package com.etanaalemu.geo.database.lite

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

internal object GeoDatabaseLiteClient {
    private const val DB_NAME = "location_lite.db"
    private const val ASSET_PATH = "databases/location-lite.db"

    @Volatile
    private var instance: LocationLiteDatabase? = null

    fun getInstance(context: Context): LocationLiteDatabase {
        return instance ?: synchronized(this) {
            instance ?: buildDatabase(context.applicationContext).also { instance = it }
        }
    }

    private fun buildDatabase(context: Context): LocationLiteDatabase {
        return Room.databaseBuilder(
            context,
            LocationLiteDatabase::class.java,
            DB_NAME,
        )
            .createFromAsset(ASSET_PATH)
            .setJournalMode(RoomDatabase.JournalMode.TRUNCATE)
            .addMigrations(
                LocationLiteMigrations.MIGRATION_1_2,
                LocationLiteMigrations.MIGRATION_2_3,
            )
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()
    }
}
