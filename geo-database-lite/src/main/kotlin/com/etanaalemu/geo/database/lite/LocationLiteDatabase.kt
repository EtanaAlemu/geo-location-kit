package com.etanaalemu.geo.database.lite

import androidx.room.Database
import androidx.room.RoomDatabase
import com.etanaalemu.geo.database.common.entity.CountryEntity
import com.etanaalemu.geo.database.common.entity.RegionEntity
import com.etanaalemu.geo.database.common.entity.StateEntity
import com.etanaalemu.geo.database.common.entity.SubregionEntity
import com.etanaalemu.geo.database.lite.dao.LocationLiteDao

@Database(
    entities = [
        RegionEntity::class,
        SubregionEntity::class,
        CountryEntity::class,
        StateEntity::class,
    ],
    version = 3,
    exportSchema = true,
)
abstract class LocationLiteDatabase : RoomDatabase() {
    abstract fun locationLiteDao(): LocationLiteDao
}
