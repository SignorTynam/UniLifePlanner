package com.example.unilifeplanner.university.publicimport

import org.junit.Assert.assertEquals
import org.junit.Test

class StudyYearOptionsTest {
    @Test
    fun availableStudyYearOptions_usesDurationYears() {
        assertEquals(listOf(1, 2, 3), degreeProgram("Laurea", 3).availableStudyYearOptions().map { it.year })
        assertEquals(listOf(1, 2), degreeProgram("Laurea Magistrale", 2).availableStudyYearOptions().map { it.year })
        assertEquals(
            listOf(1, 2, 3, 4, 5),
            degreeProgram("Laurea Magistrale a Ciclo Unico", 5).availableStudyYearOptions().map { it.year }
        )
        assertEquals(
            listOf(1, 2, 3, 4, 5, 6),
            degreeProgram("Laurea Magistrale a Ciclo Unico", 6).availableStudyYearOptions().map { it.year }
        )
    }

    @Test
    fun availableStudyYearOptions_fallsBackToDegreeType() {
        assertEquals(listOf(1, 2, 3), degreeProgram("Laurea", null).availableStudyYearOptions().map { it.year })
        assertEquals(listOf(1, 2), degreeProgram("Laurea Magistrale", null).availableStudyYearOptions().map { it.year })
    }

    private fun degreeProgram(
        degreeType: String?,
        durationYears: Int?
    ) = PublicDegreeProgram(
        externalId = "6673",
        name = "Ingegneria e scienze informatiche",
        campus = "Cesena",
        degreeType = degreeType,
        academicYear = "2025/2026",
        officialUrl = "https://corsi.unibo.it/laurea/IngegneriaScienzeInformatiche",
        durationYears = durationYears
    )
}
