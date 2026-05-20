package com.etanaalemu.geo.database.lite

import android.content.Context
import com.etanaalemu.geo.core.locale.GeoLanguageConfig
import com.etanaalemu.geo.database.common.GeoCountryRepositoryBase
import com.etanaalemu.geo.database.lite.dao.LocationLiteDao
import com.etanaalemu.geo.database.lite.data.LocationLiteDaoDataSource

internal class GeoCountryRepositoryLiteImpl(
    dao: LocationLiteDao,
    appContext: Context,
    languageConfig: GeoLanguageConfig,
) : GeoCountryRepositoryBase(LocationLiteDaoDataSource(dao), appContext, languageConfig)
