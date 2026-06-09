package com.example.unilifeplanner.university.publicimport.unibo

import com.example.unilifeplanner.domain.exams.examDateToStartOfDayMillis
import com.example.unilifeplanner.university.publicimport.PublicDegreeProgram
import com.example.unilifeplanner.university.publicimport.PublicTeaching
import java.time.LocalDate
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
        assertEquals(3, results.first().durationYears)
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
    fun parseCurriculaOrTeachingPlans_readsOfficialPlanLinks() {
        val degreeProgram = PublicDegreeProgram(
            externalId = "6673",
            name = "Ingegneria e scienze informatiche",
            campus = "Cesena",
            degreeType = "Laurea",
            academicYear = "2025/2026",
            officialUrl = "https://corsi.unibo.it/laurea/IngegneriaScienzeInformatiche"
        )
        val html = """
            <!doctype html>
            <html>
            <body>
                <a href="/laurea/IngegneriaScienzeInformatiche/insegnamenti/piano/2025/6673/000/000">Piano didattico - Informatica</a>
                <a href="/laurea/IngegneriaScienzeInformatiche/insegnamenti/piano/2025/6673/000/000#top">Duplicato</a>
                <a href="/laurea/IngegneriaScienzeInformatiche/insegnamenti/piano/2024/6673/000/000">Vecchio anno</a>
            </body>
            </html>
        """.trimIndent()

        val curricula = parser.parseCurriculaOrTeachingPlans(
            html = html,
            academicYear = "2025/2026",
            degreeProgram = degreeProgram
        )

        assertEquals(1, curricula.size)
        assertEquals("Piano didattico - Informatica", curricula.first().name)
        assertEquals("6673", curricula.first().degreeProgramExternalId)
        assertEquals("2025/2026", curricula.first().academicYear)
        assertTrue(curricula.first().externalId.isNotBlank())
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

    @Test
    fun parseDurationYears_readsItalianDurationText() {
        assertEquals(3, parser.parseDurationYears("3 anni"))
        assertEquals(2, parser.parseDurationYears("Durata 2 anni"))
        assertEquals(5, parser.parseDurationYears("5 anni"))
        assertEquals(6, parser.parseDurationYears("6 anni"))
    }

    @Test
    fun parseStudyYearFromText_readsItalianStudyYearText() {
        assertEquals(1, parser.parseStudyYearFromText("Primo Anno"))
        assertEquals(2, parser.parseStudyYearFromText("Secondo Anno"))
        assertEquals(3, parser.parseStudyYearFromText("Terzo Anno"))
        assertEquals(1, parser.parseStudyYearFromText("1° Anno"))
        assertEquals(2, parser.parseStudyYearFromText("II anno"))
        assertEquals(6, parser.parseStudyYearFromText("VI anno"))
    }

    @Test
    fun parseTeachingsFromDegreeProgramPage_filtersBySelectedStudyYear() {
        val teachings = parser.parseTeachingsFromDegreeProgramPage(
            html = """
                <!doctype html>
                <html>
                <body>
                <div class="manifestum">
                    <h2>Primo Anno</h2>
                    <table><tbody>
                        <tr>
                            <td class="code">10001</td>
                            <td class="title">PROGRAMMAZIONE</td>
                            <td class="info">12 CFU</td>
                        </tr>
                    </tbody></table>
                    <h2>Secondo Anno</h2>
                    <table><tbody>
                        <tr>
                            <td class="code">20002</td>
                            <td class="title">ALGORITMI</td>
                            <td class="info">6 CFU</td>
                        </tr>
                    </tbody></table>
                    <h2>Terzo Anno</h2>
                    <table><tbody>
                        <tr>
                            <td class="code">30003</td>
                            <td class="title">SISTEMI OPERATIVI</td>
                            <td class="info">6 CFU</td>
                        </tr>
                    </tbody></table>
                </div>
                </body>
                </html>
            """.trimIndent(),
            degreeProgram = degreeProgram(),
            selectedStudyYear = 2
        )

        assertEquals(1, teachings.size)
        assertEquals("20002", teachings.first().code)
        assertEquals("ALGORITMI", teachings.first().name)
        assertEquals(2, teachings.first().studyYear)
    }

    @Test
    fun parseExamAppealsFromDegreeProgramPage_readsPublicAppealFields() {
        val appeals = parser.parseExamAppealsFromDegreeProgramPage(
            html = fixture("unibo/exam_appeals.html"),
            degreeProgram = degreeProgram(),
            teachings = listOf(algorithmsTeaching())
        )

        val appeal = appeals.first()

        assertEquals("11929", appeal.teachingCode)
        assertEquals("11929", appeal.teachingExternalId)
        assertEquals("ALGORITMI E STRUTTURE DATI", appeal.teachingName)
        assertEquals(examDateToStartOfDayMillis(LocalDate.of(2026, 6, 25)), appeal.dateMillis)
        assertEquals(9 * 60, appeal.timeMinutes)
        assertEquals("Scritto", appeal.type)
        assertEquals("Aula 2.1", appeal.location)
        assertTrue(requireNotNull(appeal.notes).contains("Lista iscrizioni: aperta dal 10 giugno 2026 al 22 giugno 2026"))
        assertTrue(appeal.notes.contains("Note: Portare documento."))
    }

    @Test
    fun parseExamAppealsFromDegreeProgramPage_buildsDistinctExternalIdsForSameTeaching() {
        val appeals = parser.parseExamAppealsFromDegreeProgramPage(
            html = fixture("unibo/exam_appeals.html"),
            degreeProgram = degreeProgram(),
            teachings = listOf(algorithmsTeaching())
        )

        assertEquals(3, appeals.size)
        assertEquals(3, appeals.map { it.externalId }.distinct().size)
    }

    @Test
    fun parseExamAppealsFromDegreeProgramPage_linksSameCodeDifferentProfessorsToSameTeaching() {
        val appeals = parser.parseExamAppealsFromDegreeProgramPage(
            html = fixture("unibo/exam_appeals.html"),
            degreeProgram = degreeProgram(),
            teachings = listOf(algorithmsTeaching())
        )

        assertTrue(appeals.any { it.professor == "MANIEZZO VITTORIO" })
        assertTrue(appeals.any { it.professor == "MARGARA LUCIANO" })
        assertEquals(setOf("11929"), appeals.map { it.teachingExternalId }.toSet())
    }

    @Test
    fun parseExamAppealsFromDegreeProgramPage_emptyPageDoesNotCrash() {
        val appeals = parser.parseExamAppealsFromDegreeProgramPage(
            html = fixture("unibo/incomplete.html"),
            degreeProgram = degreeProgram(),
            teachings = listOf(algorithmsTeaching())
        )

        assertTrue(appeals.isEmpty())
    }

    private fun fixture(path: String): String {
        return requireNotNull(javaClass.classLoader?.getResource(path)) {
            "Missing fixture $path"
        }.readText()
    }

    private fun degreeProgram() = PublicDegreeProgram(
        externalId = "6673",
        name = "Ingegneria e scienze informatiche",
        campus = "Cesena",
        degreeType = "Laurea",
        academicYear = "2025/2026",
        officialUrl = "https://corsi.unibo.it/laurea/IngegneriaScienzeInformatiche"
    )

    private fun algorithmsTeaching() = PublicTeaching(
        externalId = "11929",
        degreeProgramExternalId = "6673",
        name = "ALGORITMI E STRUTTURE DATI",
        code = "11929",
        professor = null,
        credits = 6,
        academicYear = "2025/2026",
        officialUrl = "https://www.unibo.it/it/studiare/insegnamenti"
    )
}
