package com.etanaalemu.geo.database.common.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "countries",
    indices = [
        Index(value = ["iso2"], unique = true),
        Index(value = ["phoneCode"]),
        Index(value = ["regionId"]),
        Index(value = ["subregionId"]),
    ],
)
data class CountryEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val iso2: String,
    val iso3: String,
    val phoneCode: String,
    val currency: String,
    val capital: String,
    val nativeName: String = "",
    val translationsJson: String = "{}",
    val regionId: Int = 0,
    val subregionId: Int = 0,
    val currencyName: String = "",
    val currencySymbol: String = "",
    val emoji: String = "",
    @ColumnInfo(name = "emojiU") val emojiUnicode: String = "",
    val nationality: String = "",
    val numericCode: String = "",
    val population: Long = 0L,
    val gdp: String = "",
    val tld: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
)
