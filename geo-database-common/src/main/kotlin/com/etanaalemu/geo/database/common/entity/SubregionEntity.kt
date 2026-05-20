package com.etanaalemu.geo.database.common.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "subregions",
    foreignKeys = [
        ForeignKey(
            entity = RegionEntity::class,
            parentColumns = ["id"],
            childColumns = ["regionId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["regionId"])],
)
data class SubregionEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val regionId: Int,
)
