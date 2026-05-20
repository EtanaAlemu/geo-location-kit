package com.etanaalemu.geo.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.etanaalemu.geo.database.common.entity.CountryEntity
import com.etanaalemu.geo.database.common.entity.StateEntity

@Entity(
    tableName = "cities",
    foreignKeys = [
        ForeignKey(
            entity = StateEntity::class,
            parentColumns = ["id"],
            childColumns = ["stateId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = CountryEntity::class,
            parentColumns = ["id"],
            childColumns = ["countryId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["stateId"]),
        Index(value = ["countryId"]),
        Index(value = ["stateId", "name"]),
    ],
)
data class CityEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val stateId: Int,
    val countryId: Int,
    val latitude: Double,
    val longitude: Double,
    val timezone: String = "",
)
