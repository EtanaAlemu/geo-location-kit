package com.etanaalemu.geo.database.mapper

import com.etanaalemu.geo.core.locale.GeoLocale
import com.etanaalemu.geo.core.locale.LocalizedNameResolver
import com.etanaalemu.geo.core.model.City
import com.etanaalemu.geo.database.entity.CityEntity

internal fun CityEntity.toDomain(locale: GeoLocale): City = City(
    id = id,
    name = LocalizedNameResolver.resolveCityName(name, locale),
    stateId = stateId,
    countryId = countryId,
    latitude = latitude,
    longitude = longitude,
    timezone = timezone,
)
