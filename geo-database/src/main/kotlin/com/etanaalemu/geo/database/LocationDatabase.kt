package com.etanaalemu.geo.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.etanaalemu.geo.database.common.entity.CountryEntity
import com.etanaalemu.geo.database.common.entity.RegionEntity
import com.etanaalemu.geo.database.common.entity.StateEntity
import com.etanaalemu.geo.database.common.entity.SubregionEntity
import com.etanaalemu.geo.database.entity.CountryTimezoneEntity
import com.etanaalemu.geo.database.dao.LocationDao
import com.etanaalemu.geo.database.entity.CityEntity
import com.etanaalemu.geo.database.entity.CityFtsEntity
import com.etanaalemu.geo.database.entity.PostcodeEntity

@Database(
    entities = [
        RegionEntity::class,
        SubregionEntity::class,
        CountryEntity::class,
        StateEntity::class,
        CityEntity::class,
        CityFtsEntity::class,
        CountryTimezoneEntity::class,
        PostcodeEntity::class,
    ],
    version = 4,
    exportSchema = true,
)
abstract class LocationDatabase : RoomDatabase() {
    abstract fun locationDao(): LocationDao
}
