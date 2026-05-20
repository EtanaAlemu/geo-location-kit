package com.etanaalemu.geo.core.util

object FlagEmoji {
    fun fromIso2(countryIso: String): String {
        if (countryIso.length != 2) return "🏳️"
        val upper = countryIso.uppercase()
        val firstLetter = upper.codePointAt(0) - 'A'.code + 0x1F1E6
        val secondLetter = upper.codePointAt(1) - 'A'.code + 0x1F1E6
        return String(Character.toChars(firstLetter)) + String(Character.toChars(secondLetter))
    }
}
