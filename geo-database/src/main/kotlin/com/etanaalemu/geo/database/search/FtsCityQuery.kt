package com.etanaalemu.geo.database.search

/**
 * Builds FTS4 [MATCH](https://www.sqlite.org/fts3.html) expressions for city names.
 *
 * Uses prefix queries (`token*`) with AND between tokens. Requires the FTS4 table
 * to be created with `prefix=` (see [com.etanaalemu.geo.database.entity.CityFtsEntity]).
 */
object FtsCityQuery {
    private val tokenRegex = Regex("[\\p{L}\\p{N}]+")

    /**
     * Returns an FTS4 MATCH string, or null if there are no usable tokens (min 2 chars per token).
     */
    fun toPrefixMatchQuery(userInput: String): String? {
        val tokens = tokenRegex.findAll(userInput)
            .map { it.value.filter { ch -> ch.isLetterOrDigit() } }
            .filter { it.length >= 2 }
            .toList()
        if (tokens.isEmpty()) return null
        return tokens.joinToString(separator = " AND ") { "$it*" }
    }
}
