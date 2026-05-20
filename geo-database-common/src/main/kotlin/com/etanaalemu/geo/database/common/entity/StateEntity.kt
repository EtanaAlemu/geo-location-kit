package com.etanaalemu.geo.database.common.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "states",
    foreignKeys = [
        ForeignKey(
            entity = CountryEntity::class,
            parentColumns = ["id"],
            childColumns = ["countryId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["countryId"])],
)
data class StateEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val countryId: Int,
    val stateCode: String,
    val nativeName: String = "",
    val timezone: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val type: String = "",
    val iso3166_2: String = "",
)
