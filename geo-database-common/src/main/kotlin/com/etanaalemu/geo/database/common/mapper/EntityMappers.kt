package com.etanaalemu.geo.database.common.mapper

import com.etanaalemu.geo.core.locale.GeoLocale
import com.etanaalemu.geo.core.locale.LocalizedNameResolver
import com.etanaalemu.geo.core.model.Country
import com.etanaalemu.geo.core.model.Region
import com.etanaalemu.geo.core.model.State
import com.etanaalemu.geo.core.model.Subregion
import com.etanaalemu.geo.database.common.entity.CountryEntity
import com.etanaalemu.geo.database.common.entity.RegionEntity
import com.etanaalemu.geo.database.common.entity.StateEntity
import com.etanaalemu.geo.database.common.entity.SubregionEntity
import com.etanaalemu.geo.database.common.locale.TranslationJsonParser

fun CountryEntity.toDomain(locale: GeoLocale): Country = Country(
    id = id,
    name = LocalizedNameResolver.resolveCountryName(
        defaultName = name,
        nativeName = nativeName.takeIf { it.isNotBlank() },
        translations = TranslationJsonParser.parse(translationsJson),
        locale = locale,
    ),
    iso2 = iso2,
    iso3 = iso3,
    formattedPhoneCode = formatPhoneCode(phoneCode),
    currency = currency,
    capital = capital,
    regionId = regionId,
    subregionId = subregionId,
    currencyName = currencyName,
    currencySymbol = currencySymbol,
    emoji = emoji,
    emojiUnicode = emojiUnicode,
    nationality = nationality,
    numericCode = numericCode,
    population = population,
    gdp = gdp,
    tld = tld,
    latitude = latitude,
    longitude = longitude,
    translationsJson = translationsJson,
)

fun StateEntity.toDomain(locale: GeoLocale): State = State(
    id = id,
    name = LocalizedNameResolver.resolveStateName(
        defaultName = name,
        nativeName = nativeName.takeIf { it.isNotBlank() },
        locale = locale,
    ),
    countryId = countryId,
    stateCode = stateCode,
    timezone = timezone,
    latitude = latitude,
    longitude = longitude,
    type = type,
    iso3166_2 = iso3166_2,
)

fun RegionEntity.toDomain(): Region = Region(id = id, name = name)

fun SubregionEntity.toDomain(): Subregion = Subregion(id = id, name = name, regionId = regionId)

internal fun formatPhoneCode(phoneCode: String): String {
    val trimmed = phoneCode.trim()
    return if (trimmed.startsWith("+")) trimmed else "+$trimmed"
}

internal fun normalizePhoneCode(input: String): String {
    return input.trim().removePrefix("+").filter { it.isDigit() }
}
