package com.etanaalemu.geo.database.entity

import androidx.room.Entity
import androidx.room.Fts4

/**
 * FTS4 index over [CityEntity.name], backed by `cities` (external content).
 * Used for token/prefix search within a state; see [com.etanaalemu.geo.database.search.FtsCityQuery].
 */
@Fts4(
    contentEntity = CityEntity::class,
    prefix = intArrayOf(2, 3, 4, 5, 6, 7, 8, 9, 10),
)
@Entity(tableName = "cities_fts")
data class CityFtsEntity(
    val name: String,
)
