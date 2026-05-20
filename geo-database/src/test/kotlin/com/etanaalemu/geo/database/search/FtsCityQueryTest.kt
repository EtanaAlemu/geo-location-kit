package com.etanaalemu.geo.database.search

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FtsCityQueryTest {
    @Test
    fun toPrefixMatchQuery_joinsTokensWithAnd() {
        assertEquals("spring* AND field*", FtsCityQuery.toPrefixMatchQuery("spring field"))
    }

    @Test
    fun toPrefixMatchQuery_stripsNonWordChars() {
        assertEquals("san* AND jose*", FtsCityQuery.toPrefixMatchQuery("san-jose"))
    }

    @Test
    fun toPrefixMatchQuery_returnsNullForTooShortTokens() {
        assertNull(FtsCityQuery.toPrefixMatchQuery("a"))
        assertNull(FtsCityQuery.toPrefixMatchQuery("!@#"))
    }
}
