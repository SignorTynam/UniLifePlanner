package com.example.unilifeplanner.university.publicimport.unibo

import com.example.unilifeplanner.university.publicimport.PublicDegreeProgram
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UniboPublicParserTest {
    private val parser = UniboPublicParser()

    @Test
    fun parseDegreeProgramSearchResults_readsPublicCards() {
        val results = parser.parseDegreeProgramSearchResults(
            html = fixture("unibo/degree_search.html"),
            academicYear = "2025/2026"
        )

        assertEquals(1, results.size)
        assertEquals("6673", results.first().externalId)
        assertEquals("Ingegneria e scienze informatiche", results.first().name)
        assertEquals("Cesena", results.first().campus)
        assertEquals("Laurea", results.first().degreeType)
    }

    @Test
    fun parseTeachingsFromDegreeProgramPage_readsPlanRows() {
        val degreeProgram = PublicDegreeProgram(
            externalId = "6673",
            name = "Ingegneria e scienze informatiche",
            campus = "Cesena",
            degreeType = "Laurea",
            academicYear = "2025/2026",
            officialUrl = "https://corsi.unibo.it/laurea/IngegneriaScienzeInformatiche"
        )

        val teachings = parser.parseTeachingsFromDegreeProgramPage(
            html = fixture("unibo/teaching_plan.html"),
            degreeProgram = degreeProgram
        )

        assertEquals(2, teachings.size)
        assertEquals("00819", teachings.first().code)
        assertEquals("PROGRAMMAZIONE", teachings.first().name)
        assertEquals(12, teachings.first().credits)
    }

    @Test
    fun parseIncompleteHtml_doesNotCrash() {
        val results = parser.parseDegreeProgramSearchResults(fixture("unibo/incomplete.html"))
        val teachings = parser.parseTeachingsFromDegreeProgramPage(
            html = fixture("unibo/incomplete.html"),
            degreeProgram = PublicDegreeProgram(
                externalId = "6673",
                name = "Ingegneria e scienze informatiche",
                campus = "Cesena",
                degreeType = "Laurea",
                academicYear = "2025/2026",
                officialUrl = "https://corsi.unibo.it/laurea/IngegneriaScienzeInformatiche"
            )
        )

        assertTrue(results.isEmpty())
        assertTrue(teachings.isEmpty())
    }

    private fun fixture(path: String): String {
        return requireNotNull(javaClass.classLoader?.getResource(path)) {
            "Missing fixture $path"
        }.readText()
    }
}
