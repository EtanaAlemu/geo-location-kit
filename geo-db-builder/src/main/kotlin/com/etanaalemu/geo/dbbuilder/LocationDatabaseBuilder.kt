package com.etanaalemu.geo.dbbuilder

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URI
import java.nio.charset.StandardCharsets
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.util.zip.GZIPInputStream

class LocationDatabaseBuilder(
    private val gson: Gson = Gson(),
) {
    data class Config(
        val releaseTag: String = DEFAULT_RELEASE_TAG,
        val dataDir: File,
        val outputDb: File,
        val skipDownload: Boolean = false,
        val roomIdentityHash: String? = null,
        val useNestedCombinedJson: Boolean = true,
        val includeCities: Boolean = true,
    )

    data class BuildResult(
        val countryCount: Int,
        val stateCount: Int,
        val cityCount: Int,
        val postcodeCount: Int,
        val outputPath: String,
    )

    fun build(config: Config): BuildResult {
        config.dataDir.mkdirs()
        config.outputDb.parentFile?.mkdirs()

        if (!config.skipDownload) {
            downloadReleaseAssets(
                config.releaseTag,
                config.dataDir,
                config.useNestedCombinedJson,
                config.includeCities,
            )
        }

        if (config.outputDb.exists()) {
            config.outputDb.delete()
        }

        DriverManager.getConnection("jdbc:sqlite:${config.outputDb.absolutePath}").use { connection ->
            connection.autoCommit = false
            connection.createStatement().use { it.execute("PRAGMA foreign_keys=OFF") }
            createSchema(connection, config.includeCities)
            insertRegionsAndSubregions(connection)
            val (countryCount, stateCount, cityCount) = if (
                config.useNestedCombinedJson && File(config.dataDir, NESTED_JSON_FILE).exists()
            ) {
                importNestedCombinedJson(
                    connection,
                    File(config.dataDir, NESTED_JSON_FILE),
                    config.includeCities,
                )
            } else {
                importFlatJsonFiles(connection, config.dataDir, config.includeCities)
            }
            val postcodeCount = if (config.includeCities) {
                val pcFile = File(config.dataDir, POSTCODES_JSON_FILE)
                if (pcFile.exists()) {
                    importPostcodes(connection, pcFile)
                } else {
                    0
                }
            } else {
                0
            }
            insertRoomMasterTable(connection, config.roomIdentityHash)
            connection.createStatement().use { it.execute("PRAGMA foreign_keys=ON") }
            connection.commit()
            return BuildResult(
                countryCount = countryCount,
                stateCount = stateCount,
                cityCount = cityCount,
                postcodeCount = postcodeCount,
                outputPath = config.outputDb.absolutePath,
            )
        }
    }

    private fun insertRegionsAndSubregions(connection: Connection) {
        connection.prepareStatement("INSERT OR REPLACE INTO regions (id, name) VALUES (?, ?)").use { ps ->
            for ((id, name) in Dr5hnRegionsSeed.regions) {
                ps.setInt(1, id)
                ps.setString(2, name)
                ps.addBatch()
            }
            ps.executeBatch()
        }
        connection.prepareStatement(
            "INSERT OR REPLACE INTO subregions (id, name, regionId) VALUES (?, ?, ?)",
        ).use { ps ->
            for ((id, name, regionId) in Dr5hnRegionsSeed.subregions) {
                ps.setInt(1, id)
                ps.setString(2, name)
                ps.setInt(3, regionId)
                ps.addBatch()
            }
            ps.executeBatch()
        }
    }

    private fun importFlatJsonFiles(
        connection: Connection,
        dataDir: File,
        includeCities: Boolean,
    ): Triple<Int, Int, Int> {
        val countriesFile = File(dataDir, "countries.json")
        val statesFile = File(dataDir, "states.json")
        val citiesFile = File(dataDir, "cities.json")
        require(countriesFile.exists()) { "Missing ${countriesFile.absolutePath}" }
        require(statesFile.exists()) { "Missing ${statesFile.absolutePath}" }
        val countryCount = insertCountries(connection, countriesFile)
        val stateCount = insertStates(connection, statesFile)
        val cityCount = if (includeCities) {
            require(citiesFile.exists()) { "Missing ${citiesFile.absolutePath}" }
            insertCities(connection, citiesFile)
        } else {
            0
        }
        return Triple(countryCount, stateCount, cityCount)
    }

    private data class PendingCountryTimezone(
        val zoneName: String,
        val gmtOffset: Int,
        val gmtOffsetName: String,
        val abbreviation: String,
        val tzName: String,
    )

    private fun importNestedCombinedJson(
        connection: Connection,
        file: File,
        includeCities: Boolean,
    ): Triple<Int, Int, Int> {
        var countryCount = 0
        var stateCount = 0
        var cityCount = 0
        val cityBatchSize = 2_000

        val countrySql = """
            INSERT INTO countries (
                id, name, iso2, iso3, phoneCode, currency, capital, nativeName, translationsJson,
                regionId, subregionId, currencyName, currencySymbol, emoji, emojiU, nationality,
                numericCode, population, gdp, tld, latitude, longitude
            ) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
            """.trimIndent()

        val stateSql = """
            INSERT INTO states (
                id, name, countryId, stateCode, nativeName, timezone, latitude, longitude, type, iso3166_2
            ) VALUES (?,?,?,?,?,?,?,?,?,?)
            """.trimIndent()

        val citySql = """
            INSERT INTO cities (id, name, stateId, countryId, latitude, longitude, timezone)
            VALUES (?,?,?,?,?,?,?)
            """.trimIndent()

        val tzSql = """
            INSERT INTO country_timezones (
                countryId, zoneName, gmtOffset, gmtOffsetName, abbreviation, tzName
            ) VALUES (?,?,?,?,?,?)
            """.trimIndent()

        val tzPs: PreparedStatement? = if (includeCities) {
            connection.prepareStatement(tzSql)
        } else {
            null
        }
        try {
            connection.prepareStatement(countrySql).use { countryPs ->
                connection.prepareStatement(stateSql).use { statePs ->
                    val cityPs: PreparedStatement? = if (includeCities) {
                        connection.prepareStatement(citySql)
                    } else {
                        null
                    }
                    try {
                        fun flushTimezones(countryId: Int, pending: MutableList<PendingCountryTimezone>) {
                            if (countryId == 0 || pending.isEmpty()) return
                            val ps = tzPs ?: run {
                                pending.clear()
                                return
                            }
                            for (t in pending) {
                                ps.setInt(1, countryId)
                                ps.setString(2, t.zoneName)
                                ps.setInt(3, t.gmtOffset)
                                ps.setString(4, t.gmtOffsetName)
                                ps.setString(5, t.abbreviation)
                                ps.setString(6, t.tzName)
                                ps.addBatch()
                            }
                            ps.executeBatch()
                            pending.clear()
                        }

                        FileInputStream(file).use { input ->
                            JsonReader(InputStreamReader(input, StandardCharsets.UTF_8)).use { reader ->
                                reader.beginArray()
                                while (reader.hasNext()) {
                                    reader.beginObject()
                                    var countryId = 0
                                    var countryName = ""
                                    var iso2 = ""
                                    var iso3 = ""
                                    var phoneCode = ""
                                    var currency = ""
                                    var capital = ""
                                    var nativeName = ""
                                    var translationsJson = "{}"
                                    var regionId = 0
                                    var subregionId = 0
                                    var currencyName = ""
                                    var currencySymbol = ""
                                    var emoji = ""
                                    var emojiU = ""
                                    var nationality = ""
                                    var numericCode = ""
                                    var population = 0L
                                    var gdp = ""
                                    var tld = ""
                                    var countryLatitude = 0.0
                                    var countryLongitude = 0.0
                                    val pendingTz = mutableListOf<PendingCountryTimezone>()
                                    while (reader.hasNext()) {
                                        when (reader.nextName()) {
                                            "id" -> countryId = reader.nextInt()
                                            "name" -> countryName = reader.nextStringOrEmpty()
                                            "iso2" -> iso2 = reader.nextStringOrEmpty()
                                            "iso3" -> iso3 = reader.nextStringOrEmpty()
                                            "phonecode", "phone_code" -> phoneCode = reader.nextStringOrEmpty()
                                            "currency" -> currency = reader.nextStringOrEmpty()
                                            "capital" -> capital = reader.nextStringOrEmpty()
                                            "native" -> nativeName = reader.nextStringOrEmpty()
                                            "translations" -> translationsJson = readTranslationsJson(reader)
                                            "region_id" -> regionId = reader.nextIntOrZero()
                                            "subregion_id" -> subregionId = reader.nextIntOrZero()
                                            "currency_name" -> currencyName = reader.nextStringOrEmpty()
                                            "currency_symbol" -> currencySymbol = reader.nextStringOrEmpty()
                                            "emoji" -> emoji = reader.nextStringOrEmpty()
                                            "emojiU" -> emojiU = reader.nextStringOrEmpty()
                                            "nationality" -> nationality = reader.nextStringOrEmpty()
                                            "numeric_code" -> numericCode = reader.nextStringOrEmpty()
                                            "population" -> population = readPopulation(reader)
                                            "gdp" -> gdp = reader.nextStringOrEmpty()
                                            "tld" -> tld = reader.nextStringOrEmpty()
                                            "latitude" -> countryLatitude =
                                                reader.nextStringOrEmpty().toDoubleOrNull() ?: 0.0
                                            "longitude" -> countryLongitude =
                                                reader.nextStringOrEmpty().toDoubleOrNull() ?: 0.0
                                            "timezones" -> readCountryTimezonesArray(reader, pendingTz)
                                            "states" -> {
                                                bindCountryInsert(
                                                    countryPs,
                                                    countryId,
                                                    countryName,
                                                    iso2,
                                                    iso3,
                                                    phoneCode,
                                                    currency,
                                                    capital,
                                                    nativeName,
                                                    translationsJson,
                                                    regionId,
                                                    subregionId,
                                                    currencyName,
                                                    currencySymbol,
                                                    emoji,
                                                    emojiU,
                                                    nationality,
                                                    numericCode,
                                                    population,
                                                    gdp,
                                                    tld,
                                                    countryLatitude,
                                                    countryLongitude,
                                                )
                                                countryPs.addBatch()
                                                countryCount++

                                                reader.beginArray()
                                                while (reader.hasNext()) {
                                                    reader.beginObject()
                                                    var stateId = 0
                                                    var stateName = ""
                                                    var stateCode = ""
                                                    var stateNativeName = ""
                                                    var stateTimezone = ""
                                                    var stateLatitude = 0.0
                                                    var stateLongitude = 0.0
                                                    var stateType = ""
                                                    var stateIso3166 = ""
                                                    while (reader.hasNext()) {
                                                        when (reader.nextName()) {
                                                            "id" -> stateId = reader.nextInt()
                                                            "name" -> stateName = reader.nextStringOrEmpty()
                                                            "iso2", "state_code" -> stateCode = reader.nextStringOrEmpty()
                                                            "native" -> stateNativeName = reader.nextStringOrEmpty()
                                                            "timezone" -> stateTimezone = reader.nextStringOrEmpty()
                                                            "latitude" -> stateLatitude =
                                                                reader.nextStringOrEmpty().toDoubleOrNull() ?: 0.0
                                                            "longitude" -> stateLongitude =
                                                                reader.nextStringOrEmpty().toDoubleOrNull() ?: 0.0
                                                            "type" -> stateType = reader.nextStringOrEmpty()
                                                            "iso3166_2" -> stateIso3166 = reader.nextStringOrEmpty()
                                                            "cities" -> {
                                                                statePs.setInt(1, stateId)
                                                                statePs.setString(2, stateName)
                                                                statePs.setInt(3, countryId)
                                                                statePs.setString(4, stateCode)
                                                                statePs.setString(5, stateNativeName)
                                                                statePs.setString(6, stateTimezone)
                                                                statePs.setDouble(7, stateLatitude)
                                                                statePs.setDouble(8, stateLongitude)
                                                                statePs.setString(9, stateType)
                                                                statePs.setString(10, stateIso3166)
                                                                statePs.addBatch()
                                                                stateCount++

                                                                if (includeCities && cityPs != null) {
                                                                    reader.beginArray()
                                                                    while (reader.hasNext()) {
                                                                        reader.beginObject()
                                                                        var cityId = 0
                                                                        var cityName = ""
                                                                        var latitude = 0.0
                                                                        var longitude = 0.0
                                                                        var cityTimezone = ""
                                                                        while (reader.hasNext()) {
                                                                            when (reader.nextName()) {
                                                                                "id" -> cityId = reader.nextInt()
                                                                                "name" -> cityName = reader.nextStringOrEmpty()
                                                                                "latitude" -> latitude =
                                                                                    reader.nextStringOrEmpty().toDoubleOrNull()
                                                                                        ?: 0.0
                                                                                "longitude" -> longitude =
                                                                                    reader.nextStringOrEmpty().toDoubleOrNull()
                                                                                        ?: 0.0
                                                                                "timezone" -> cityTimezone =
                                                                                    reader.nextStringOrEmpty()
                                                                                else -> reader.skipValue()
                                                                            }
                                                                        }
                                                                        reader.endObject()

                                                                        cityPs.setInt(1, cityId)
                                                                        cityPs.setString(2, cityName)
                                                                        cityPs.setInt(3, stateId)
                                                                        cityPs.setInt(4, countryId)
                                                                        cityPs.setDouble(5, latitude)
                                                                        cityPs.setDouble(6, longitude)
                                                                        cityPs.setString(7, cityTimezone)
                                                                        cityPs.addBatch()
                                                                        cityCount++
                                                                        if (cityCount % cityBatchSize == 0) {
                                                                            cityPs.executeBatch()
                                                                        }
                                                                    }
                                                                    reader.endArray()
                                                                } else {
                                                                    reader.skipValue()
                                                                }
                                                            }
                                                            else -> reader.skipValue()
                                                        }
                                                    }
                                                    reader.endObject()
                                                }
                                                reader.endArray()
                                                countryPs.executeBatch()
                                                statePs.executeBatch()
                                                flushTimezones(countryId, pendingTz)
                                            }
                                            else -> reader.skipValue()
                                        }
                                    }
                                    flushTimezones(countryId, pendingTz)
                                    reader.endObject()
                                }
                                reader.endArray()
                            }
                        }
                        cityPs?.executeBatch()
                    } finally {
                        cityPs?.close()
                    }
                }
            }
        } finally {
            tzPs?.close()
        }

        return Triple(countryCount, stateCount, cityCount)
    }

    private fun bindCountryInsert(
        ps: PreparedStatement,
        countryId: Int,
        countryName: String,
        iso2: String,
        iso3: String,
        phoneCode: String,
        currency: String,
        capital: String,
        nativeName: String,
        translationsJson: String,
        regionId: Int,
        subregionId: Int,
        currencyName: String,
        currencySymbol: String,
        emoji: String,
        emojiU: String,
        nationality: String,
        numericCode: String,
        population: Long,
        gdp: String,
        tld: String,
        latitude: Double,
        longitude: Double,
    ) {
        ps.setInt(1, countryId)
        ps.setString(2, countryName)
        ps.setString(3, iso2)
        ps.setString(4, iso3)
        ps.setString(5, phoneCode)
        ps.setString(6, currency)
        ps.setString(7, capital)
        ps.setString(8, nativeName)
        ps.setString(9, translationsJson)
        ps.setInt(10, regionId)
        ps.setInt(11, subregionId)
        ps.setString(12, currencyName)
        ps.setString(13, currencySymbol)
        ps.setString(14, emoji)
        ps.setString(15, emojiU)
        ps.setString(16, nationality)
        ps.setString(17, numericCode)
        ps.setLong(18, population)
        ps.setString(19, gdp)
        ps.setString(20, tld)
        ps.setDouble(21, latitude)
        ps.setDouble(22, longitude)
    }

    private fun readCountryTimezonesArray(reader: JsonReader, out: MutableList<PendingCountryTimezone>) {
        reader.beginArray()
        while (reader.hasNext()) {
            reader.beginObject()
            var zoneName = ""
            var gmtOffset = 0
            var gmtOffsetName = ""
            var abbreviation = ""
            var tzName = ""
            while (reader.hasNext()) {
                when (reader.nextName()) {
                    "zoneName" -> zoneName = reader.nextString()
                    "gmtOffset" -> gmtOffset = reader.nextInt()
                    "gmtOffsetName" -> gmtOffsetName = reader.nextString()
                    "abbreviation" -> abbreviation = reader.nextString()
                    "tzName" -> tzName = reader.nextString()
                    else -> reader.skipValue()
                }
            }
            reader.endObject()
            if (zoneName.isNotBlank()) {
                out.add(
                    PendingCountryTimezone(
                        zoneName = zoneName,
                        gmtOffset = gmtOffset,
                        gmtOffsetName = gmtOffsetName,
                        abbreviation = abbreviation,
                        tzName = tzName,
                    ),
                )
            }
        }
        reader.endArray()
    }

    private fun JsonReader.nextIntOrZero(): Int =
        when (peek()) {
            JsonToken.NULL -> {
                nextNull()
                0
            }
            JsonToken.NUMBER -> nextInt()
            else -> {
                skipValue()
                0
            }
        }

    private fun JsonReader.nextStringOrEmpty(): String =
        when (peek()) {
            JsonToken.NULL -> {
                nextNull()
                ""
            }
            JsonToken.STRING -> nextString()
            JsonToken.NUMBER -> {
                val d = nextDouble()
                if (d % 1.0 == 0.0) d.toLong().toString() else d.toString()
            }
            else -> {
                skipValue()
                ""
            }
        }

    private fun readPopulation(reader: JsonReader): Long =
        when (reader.peek()) {
            JsonToken.NULL -> {
                reader.nextNull()
                0L
            }
            JsonToken.NUMBER -> reader.nextLong()
            JsonToken.STRING -> reader.nextString().toLongOrNull() ?: 0L
            else -> {
                reader.skipValue()
                0L
            }
        }

    private fun importPostcodes(connection: Connection, file: File): Int {
        var count = 0
        val batchSize = 5_000
        connection.prepareStatement(
            """
            INSERT INTO postcodes (
                id, code, countryId, countryCode, stateId, stateCode, cityId,
                localityName, type, latitude, longitude, source, wikiDataId
            ) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)
            """.trimIndent(),
        ).use { ps ->
            streamJsonArray(file, PostcodeJson::class.java) { pc ->
                ps.setInt(1, pc.id)
                ps.setString(2, pc.code)
                ps.setInt(3, pc.countryId)
                ps.setString(4, pc.countryCode.orEmpty())
                ps.setInt(5, pc.stateId ?: 0)
                ps.setString(6, pc.stateCode.orEmpty())
                if (pc.cityId != null) {
                    ps.setInt(7, pc.cityId)
                } else {
                    ps.setNull(7, java.sql.Types.INTEGER)
                }
                ps.setString(8, pc.localityName.orEmpty())
                ps.setString(9, pc.type.orEmpty())
                val lat = pc.latitude?.toDoubleOrNull()
                val lng = pc.longitude?.toDoubleOrNull()
                if (lat != null) ps.setDouble(10, lat) else ps.setNull(10, java.sql.Types.REAL)
                if (lng != null) ps.setDouble(11, lng) else ps.setNull(11, java.sql.Types.REAL)
                ps.setString(12, pc.source.orEmpty())
                if (pc.wikiDataId != null) {
                    ps.setString(13, pc.wikiDataId)
                } else {
                    ps.setNull(13, java.sql.Types.VARCHAR)
                }
                ps.addBatch()
                count++
                if (count % batchSize == 0) {
                    ps.executeBatch()
                }
            }
            ps.executeBatch()
        }
        return count
    }

    private fun createSchema(connection: Connection, includeCities: Boolean) {
        connection.createStatement().use { statement ->
            DatabaseSchema.ddl(includeCities).forEach { ddl ->
                statement.execute(ddl)
            }
        }
    }

    private fun insertRoomMasterTable(connection: Connection, identityHash: String?) {
        val hash = identityHash ?: DatabaseSchema.ROOM_IDENTITY_HASH_PLACEHOLDER
        connection.prepareStatement(
            "INSERT OR REPLACE INTO room_master_table (id, identity_hash) VALUES (42, ?)",
        ).use { ps ->
            ps.setString(1, hash)
            ps.executeUpdate()
        }
    }

    private fun insertCountries(connection: Connection, file: File): Int {
        val countries: List<CountryJson> = parseCountries(file)
        connection.prepareStatement(
            """
            INSERT INTO countries (
                id, name, iso2, iso3, phoneCode, currency, capital, nativeName, translationsJson,
                regionId, subregionId, currencyName, currencySymbol, emoji, emojiU, nationality,
                numericCode, population, gdp, tld, latitude, longitude
            ) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
            """.trimIndent(),
        ).use { ps ->
            countries.forEach { country ->
                ps.setInt(1, country.id)
                ps.setString(2, country.name)
                ps.setString(3, country.iso2)
                ps.setString(4, country.iso3)
                ps.setString(5, country.phoneCode)
                ps.setString(6, country.currency.orEmpty())
                ps.setString(7, country.capital.orEmpty())
                ps.setString(8, country.native.orEmpty())
                ps.setString(9, encodeTranslations(country.translations))
                ps.setInt(10, 0)
                ps.setInt(11, 0)
                ps.setString(12, "")
                ps.setString(13, "")
                ps.setString(14, "")
                ps.setString(15, "")
                ps.setString(16, "")
                ps.setString(17, "")
                ps.setLong(18, 0L)
                ps.setString(19, "")
                ps.setString(20, "")
                ps.setDouble(21, 0.0)
                ps.setDouble(22, 0.0)
                ps.addBatch()
            }
            ps.executeBatch()
        }
        return countries.size
    }

    private fun insertStates(connection: Connection, file: File): Int {
        val states: List<StateJson> = parseStates(file)
        connection.prepareStatement(
            """
            INSERT INTO states (
                id, name, countryId, stateCode, nativeName, timezone, latitude, longitude, type, iso3166_2
            ) VALUES (?,?,?,?,?,?,?,?,?,?)
            """.trimIndent(),
        ).use { ps ->
            states.forEach { state ->
                ps.setInt(1, state.id)
                ps.setString(2, state.name)
                ps.setInt(3, state.countryId)
                ps.setString(4, state.stateCode ?: state.iso2.orEmpty())
                ps.setString(5, state.native.orEmpty())
                ps.setString(6, "")
                ps.setDouble(7, 0.0)
                ps.setDouble(8, 0.0)
                ps.setString(9, "")
                ps.setString(10, "")
                ps.addBatch()
            }
            ps.executeBatch()
        }
        return states.size
    }

    private fun insertCities(connection: Connection, file: File): Int {
        var count = 0
        val batchSize = 2_000
        connection.prepareStatement(
            """
            INSERT INTO cities (id, name, stateId, countryId, latitude, longitude, timezone)
            VALUES (?,?,?,?,?,?,?)
            """.trimIndent(),
        ).use { ps ->
            streamJsonArray(file, CityJson::class.java) { city ->
                ps.setInt(1, city.id)
                ps.setString(2, city.name)
                ps.setInt(3, city.stateId)
                ps.setInt(4, city.countryId)
                ps.setDouble(5, city.latitude?.toDoubleOrNull() ?: 0.0)
                ps.setDouble(6, city.longitude?.toDoubleOrNull() ?: 0.0)
                ps.setString(7, city.timezone.orEmpty())
                ps.addBatch()
                count++
                if (count % batchSize == 0) {
                    ps.executeBatch()
                }
            }
            ps.executeBatch()
        }
        return count
    }

    private fun parseCountries(file: File): List<CountryJson> = parseJsonArray(file)

    private fun parseStates(file: File): List<StateJson> = parseJsonArray(file)

    private inline fun <reified T> parseJsonArray(file: File): List<T> {
        FileInputStream(file).use { input ->
            val type = object : TypeToken<List<T>>() {}.type
            return gson.fromJson(InputStreamReader(input, StandardCharsets.UTF_8), type)
        }
    }

    private fun <T> streamJsonArray(file: File, clazz: Class<T>, onItem: (T) -> Unit) {
        FileInputStream(file).use { input ->
            JsonReader(InputStreamReader(input, StandardCharsets.UTF_8)).use { reader ->
                reader.beginArray()
                while (reader.hasNext()) {
                    val item: T = gson.fromJson(reader, clazz)
                    onItem(item)
                }
                reader.endArray()
            }
        }
    }

    private fun downloadReleaseAssets(
        releaseTag: String,
        dataDir: File,
        useNested: Boolean,
        includeCities: Boolean,
    ) {
        val baseUrl =
            "https://github.com/dr5hn/countries-states-cities-database/releases/download/$releaseTag"
        if (useNested) {
            downloadAndDecompress(
                "$baseUrl/json-countries%2Bstates%2Bcities.json.gz",
                File(dataDir, NESTED_JSON_FILE),
            )
            if (includeCities) {
                downloadAndDecompress(
                    "$baseUrl/json-postcodes.json.gz",
                    File(dataDir, POSTCODES_JSON_FILE),
                )
            }
        } else {
            downloadAndDecompress("$baseUrl/json-countries.json.gz", File(dataDir, "countries.json"))
            downloadAndDecompress("$baseUrl/json-states.json.gz", File(dataDir, "states.json"))
            if (includeCities) {
                downloadAndDecompress("$baseUrl/json-cities.json.gz", File(dataDir, "cities.json"))
                downloadAndDecompress(
                    "$baseUrl/json-postcodes.json.gz",
                    File(dataDir, POSTCODES_JSON_FILE),
                )
            }
        }
    }

    private fun downloadAndDecompress(url: String, output: File) {
        if (output.exists() && output.length() > 0) {
            println("Skipping download, exists: ${output.name}")
            return
        }
        println("Downloading $url")
        val connection = URI(url).toURL().openConnection() as HttpURLConnection
        connection.connectTimeout = 60_000
        connection.readTimeout = 600_000
        connection.instanceFollowRedirects = true
        connection.inputStream.use { raw ->
            BufferedInputStream(GZIPInputStream(raw)).use { gzip ->
                output.outputStream().use { out ->
                    gzip.copyTo(out)
                }
            }
        }
        println("Saved ${output.name} (${output.length()} bytes)")
    }

    private fun readTranslationsJson(reader: JsonReader): String {
        val map: Map<String, String> = gson.fromJson(
            reader,
            object : TypeToken<Map<String, String>>() {}.type,
        )
        return encodeTranslations(map)
    }

    private fun encodeTranslations(translations: Map<String, String>?): String {
        if (translations.isNullOrEmpty()) return "{}"
        return gson.toJson(translations)
    }

    companion object {
        const val DEFAULT_RELEASE_TAG = "v3.2-export.2"
        const val NESTED_JSON_FILE = "countries+states+cities.json"
        const val POSTCODES_JSON_FILE = "postcodes.json"
    }
}
