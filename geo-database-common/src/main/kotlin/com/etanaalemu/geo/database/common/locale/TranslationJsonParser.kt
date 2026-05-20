package com.etanaalemu.geo.database.common.locale

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.concurrent.ConcurrentHashMap

/**
 * Parses dr5hn `translations` JSON objects. Results are **memoized by exact string contents**
 * so repeated mapping of the same country rows (Flow updates, language changes) does not
 * re-run Gson for identical JSON.
 */
internal object TranslationJsonParser {
    private val gson = Gson()
    private val mapType = object : TypeToken<Map<String, String>>() {}.type

    private val parseCache = ConcurrentHashMap<String, Map<String, String>>()

    fun parse(json: String): Map<String, String> {
        if (json.isBlank() || json == "{}") return emptyMap()
        return parseCache.computeIfAbsent(json) { key ->
            runCatching { gson.fromJson<Map<String, String>>(key, mapType) }
                .getOrDefault(emptyMap())
        }
    }

    fun encode(map: Map<String, String>): String {
        if (map.isEmpty()) return "{}"
        return gson.toJson(map)
    }
}
