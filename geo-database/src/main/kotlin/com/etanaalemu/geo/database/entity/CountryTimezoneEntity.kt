package com.etanaalemu.geo.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.etanaalemu.geo.database.common.entity.CountryEntity

@Entity(
    tableName = "country_timezones",
    primaryKeys = ["countryId", "zoneName"],
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
data class CountryTimezoneEntity(
    val countryId: Int,
    val zoneName: String,
    val gmtOffset: Int,
    val gmtOffsetName: String,
    val abbreviation: String,
    val tzName: String,
)
