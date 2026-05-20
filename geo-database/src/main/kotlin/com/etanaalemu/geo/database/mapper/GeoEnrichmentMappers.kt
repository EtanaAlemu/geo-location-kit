package com.etanaalemu.geo.database.mapper

import com.etanaalemu.geo.core.model.CountryTimezone
import com.etanaalemu.geo.core.model.Postcode
import com.etanaalemu.geo.database.entity.CountryTimezoneEntity
import com.etanaalemu.geo.database.entity.PostcodeEntity

internal fun CountryTimezoneEntity.toCountryTimezoneDomain(): CountryTimezone = CountryTimezone(
    zoneName = zoneName,
    gmtOffset = gmtOffset,
    gmtOffsetName = gmtOffsetName,
    abbreviation = abbreviation,
    tzName = tzName,
)

internal fun PostcodeEntity.toPostcodeDomain(): Postcode = Postcode(
    id = id,
    code = code,
    countryId = countryId,
    countryCode = countryCode,
    stateId = stateId,
    stateCode = stateCode,
    cityId = cityId,
    localityName = localityName,
    type = type,
    latitude = latitude,
    longitude = longitude,
    source = source,
    wikiDataId = wikiDataId,
)
