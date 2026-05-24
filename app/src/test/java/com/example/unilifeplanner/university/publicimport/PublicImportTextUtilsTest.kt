package com.example.unilifeplanner.university.publicimport

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PublicImportTextUtilsTest {
    @Test
    fun parseCredits_returnsNumberWhenPresent() {
        assertEquals(6, parseCredits("6 CFU"))
        assertEquals(12, parseCredits("12"))
        assertNull(parseCredits(""))
    }

    @Test
    fun parseDayOfWeekItalian_mapsItalianDays() {
        assertEquals(1, parseDayOfWeekItalian("Lunedì"))
        assertEquals(2, parseDayOfWeekItalian("Martedì"))
        assertEquals(7, parseDayOfWeekItalian("Domenica"))
    }

    @Test
    fun parseTimeToMinutes_validatesClockTime() {
        assertEquals(540, parseTimeToMinutes("09:00"))
        assertEquals(540, parseTimeToMinutes("9:00"))
        assertNull(parseTimeToMinutes("25:00"))
        assertNull(parseTimeToMinutes("10:70"))
    }

    @Test
    fun buildStableExternalId_isStableAndSensitiveToInput() {
        val first = buildStableExternalId("UNIBO_PUBLIC", "2025/2026", "Programmazione")
        val second = buildStableExternalId("UNIBO_PUBLIC", "2025/2026", "Programmazione")
        val different = buildStableExternalId("UNIBO_PUBLIC", "2025/2026", "Analisi")

        assertEquals(first, second)
        assertNotEquals(first, different)
    }
}
