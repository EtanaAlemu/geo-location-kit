package com.etanaalemu.geo.database.common.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "regions")
data class RegionEntity(
    @PrimaryKey val id: Int,
    val name: String,
)
