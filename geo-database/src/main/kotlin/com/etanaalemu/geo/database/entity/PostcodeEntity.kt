package com.etanaalemu.geo.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.etanaalemu.geo.database.common.entity.CountryEntity

@Entity(
    tableName = "postcodes",
    foreignKeys = [
        ForeignKey(
            entity = CountryEntity::class,
            parentColumns = ["id"],
            childColumns = ["countryId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["countryId"]),
        Index(value = ["countryId", "code"]),
        Index(value = ["stateId"]),
        Index(value = ["cityId"]),
    ],
)
data class PostcodeEntity(
    @PrimaryKey val id: Int,
    val code: String,
    val countryId: Int,
    val countryCode: String,
    val stateId: Int,
    val stateCode: String,
    val cityId: Int?,
    val localityName: String,
    val type: String,
    val latitude: Double?,
    val longitude: Double?,
    val source: String,
    val wikiDataId: String?,
)
