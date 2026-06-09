package com.example.unilifeplanner.university.publicimport.unibo

import com.example.unilifeplanner.university.publicimport.PublicCurriculum
import com.example.unilifeplanner.university.publicimport.PublicDegreeProgram
import com.example.unilifeplanner.university.publicimport.PublicExamAppeal
import com.example.unilifeplanner.university.publicimport.PublicImportPreview
import com.example.unilifeplanner.university.publicimport.PublicImportResult
import com.example.unilifeplanner.university.publicimport.PublicLesson
import com.example.unilifeplanner.university.publicimport.PublicTeaching
import com.example.unilifeplanner.university.publicimport.PublicTeachingDetails
import com.example.unilifeplanner.university.publicimport.PublicUniversityProvider
import com.example.unilifeplanner.university.publicimport.normalizeText
import com.example.unilifeplanner.university.unibo.publicdata.UniboPublicConfig

class UniboPublicImportRepository(
    private val client: UniboPublicClient = UniboPublicClient(),
    private val parser: UniboPublicParser = UniboPublicParser(),
    private val importer: UniboPublicImporter,
    private val cache: UniboPublicImportCache = UniboPublicImportCache()
) : PublicUniversityProvider {
    override val provider: String = UniboPublicConfig.PROVIDER

    override suspend fun loadDegreePrograms(
        academicYear: String,
        campus: String?,
        degreeType: String?
    ): List<PublicDegreeProgram> {
        val normalizedCampus = campus.toCampusParam()
        val normalizedDegreeType = degreeType?.takeUnless { normalizeText(it) == "tutte" }
        val key = listOf(
            academicYear,
            normalizedCampus.orEmpty(),
            normalizedDegreeType.orEmpty()
        ).joinToString("|")

        cache.getSearch(key)?.let { return it }

        val results = client.loadDegreeProgramsPages(
            campus = normalizedCampus,
            degreeType = normalizedDegreeType
        )
            .flatMap { html -> parser.parseDegreeProgramSearchResults(html, academicYear) }
            .map { it.copy(academicYear = academicYear) }
            .distinctBy { it.externalId }

        cache.putSearch(key, results)
        return results
    }

    override suspend fun loadCurricula(degreeProgram: PublicDegreeProgram): List<PublicCurriculum> {
        val cacheKey = "${degreeProgram.externalId}|${degreeProgram.academicYear}"
        cache.getCurricula(cacheKey)?.let { return it }

        val detailHtml = client.getDegreeProgramPage(degreeProgram.officialUrl)
        val siteUrl = parser.parseDegreeProgramSiteUrl(detailHtml) ?: return emptyList()
        val baseDegreeProgram = degreeProgram.copy(officialUrl = siteUrl)
        val indexHtml = client.getTeachingPlanIndexPage(
            degreeProgramSiteUrl = siteUrl,
            academicYear = degreeProgram.academicYear,
            degreeProgramCode = degreeProgram.externalId
        )
        val curricula = parser.parseCurriculaOrTeachingPlans(
            html = indexHtml,
            academicYear = degreeProgram.academicYear,
            degreeProgram = baseDegreeProgram
        )

        cache.putCurricula(cacheKey, curricula)
        return curricula
    }

    override suspend fun loadPreview(
        degreeProgram: PublicDegreeProgram,
        curriculum: PublicCurriculum?,
        selectedStudyYear: Int?,
        forceRefresh: Boolean
    ): PublicImportPreview {
        val cacheKey = listOf(
            degreeProgram.externalId,
            degreeProgram.academicYear,
            curriculum?.externalId.orEmpty(),
            selectedStudyYear?.toString().orEmpty()
        ).joinToString("|")
        if (!forceRefresh) {
            cache.getPreview(cacheKey)?.let { return it }
        }

        val warnings = mutableListOf<String>()
        val detailHtml = client.getDegreeProgramPage(degreeProgram.officialUrl)
        val siteUrl = parser.parseDegreeProgramSiteUrl(detailHtml)
        if (siteUrl == null) {
            val preview = PublicImportPreview(
                degreeProgram = degreeProgram,
                teachings = emptyList(),
                lessons = emptyList(),
                examAppeals = emptyList(),
                selectedStudyYear = selectedStudyYear,
                warnings = listOf("Pagina pubblica del corso di laurea non trovata."),
                curriculum = curriculum
            )
            cache.putPreview(cacheKey, preview)
            return preview
        }

        val baseDegreeProgram = degreeProgram.copy(officialUrl = siteUrl)
        val planLinks = if (curriculum != null) {
            listOf(curriculum.officialUrl)
        } else {
            val indexHtml = client.getTeachingPlanIndexPage(
                degreeProgramSiteUrl = siteUrl,
                academicYear = degreeProgram.academicYear,
                degreeProgramCode = degreeProgram.externalId
            )
            parser.parseCurriculaOrTeachingPlans(
                html = indexHtml,
                academicYear = degreeProgram.academicYear,
                degreeProgram = baseDegreeProgram
            ).map { it.officialUrl }
        }
        if (planLinks.isEmpty()) {
            warnings += "Nessun piano didattico pubblico trovato per ${degreeProgram.academicYear}."
        }

        val planHtmlPages = planLinks.map { planUrl -> client.getDegreeProgramPage(planUrl) }
        val allPlanTeachings = planHtmlPages
            .flatMap { planHtml ->
                parser.parseTeachingsFromDegreeProgramPage(
                    html = planHtml,
                    degreeProgram = baseDegreeProgram
                )
            }
            .distinctBy { it.externalId }
        val cannotDistinguishStudyYear = selectedStudyYear != null &&
            allPlanTeachings.isNotEmpty() &&
            allPlanTeachings.none { it.studyYear != null }
        val planTeachings = when {
            cannotDistinguishStudyYear -> emptyList()
            selectedStudyYear == null -> allPlanTeachings
            else -> planHtmlPages
                .flatMap { planHtml ->
                    parser.parseTeachingsFromDegreeProgramPage(
                        html = planHtml,
                        degreeProgram = baseDegreeProgram,
                        selectedStudyYear = selectedStudyYear
                    )
                }
                .distinctBy { it.externalId }
        }

        if (cannotDistinguishStudyYear) {
            warnings += "Non e stato possibile distinguere gli insegnamenti per anno di corso nella pagina pubblica UniBo."
        }

        if (planTeachings.isEmpty()) {
            warnings += if (selectedStudyYear != null) {
                "Nessun insegnamento pubblico trovato per il ${selectedStudyYear}° anno."
            } else {
                "Nessun insegnamento pubblico trovato per il corso selezionato."
            }
        }

        val enrichedTeachings = mutableListOf<PublicTeaching>()
        val lessons = mutableListOf<PublicLesson>()

        planTeachings.forEach { teaching ->
            val details = loadTeachingDetailsSafely(
                teaching = teaching,
                degreeProgram = degreeProgram,
                warnings = warnings
            )
            enrichedTeachings += details.teaching
            warnings += details.warnings

            details.scheduleUrls.forEach { scheduleUrl ->
                val lessonsFromSchedule = loadLessonsSafely(
                    scheduleUrl = scheduleUrl,
                    teaching = details.teaching,
                    warnings = warnings
                )
                lessons += lessonsFromSchedule
            }

            if (details.scheduleUrls.isNotEmpty() &&
                lessons.none { it.teachingExternalId == details.teaching.externalId }
            ) {
                warnings += "${details.teaching.name}: orari pubblici presenti ma non riconosciuti."
            }
        }

        val examAppeals = loadExamAppealsSafely(
            siteUrl = siteUrl,
            degreeProgram = baseDegreeProgram,
            teachings = enrichedTeachings,
            warnings = warnings
        )

        val preview = PublicImportPreview(
            degreeProgram = baseDegreeProgram,
            teachings = enrichedTeachings,
            lessons = lessons.distinctBy { it.externalId },
            examAppeals = examAppeals.distinctBy { it.externalId },
            selectedStudyYear = selectedStudyYear,
            warnings = warnings.distinct(),
            curriculum = curriculum
        )
        cache.putPreview(cacheKey, preview)
        return preview
    }

    override suspend fun importPreview(preview: PublicImportPreview): PublicImportResult {
        return importer.importPreview(preview)
    }

    private suspend fun loadTeachingDetailsSafely(
        teaching: PublicTeaching,
        degreeProgram: PublicDegreeProgram,
        warnings: MutableList<String>
    ): PublicTeachingDetails {
        val code = teaching.code
        if (code.isNullOrBlank()) {
            return PublicTeachingDetails(
                teaching = teaching,
                warnings = listOf("${teaching.name}: codice insegnamento non disponibile.")
            )
        }

        return try {
            val html = client.searchTeachingByCodePage(
                code = code,
                academicYear = teaching.academicYear
            )
            parser.parseTeachingSearchDetails(
                html = html,
                teaching = teaching,
                degreeProgram = degreeProgram
            )
        } catch (exception: UniboPublicImportException) {
            warnings += "${teaching.name}: ${exception.message}"
            PublicTeachingDetails(teaching = teaching)
        }
    }

    private suspend fun loadLessonsSafely(
        scheduleUrl: String,
        teaching: PublicTeaching,
        warnings: MutableList<String>
    ): List<PublicLesson> {
        return try {
            val html = client.getTeachingPage(scheduleUrl)
            parser.parseLessonsFromTeachingPage(html, teaching)
        } catch (exception: UniboPublicImportException) {
            warnings += "${teaching.name}: ${exception.message}"
            emptyList()
        }
    }

    private suspend fun loadExamAppealsSafely(
        siteUrl: String,
        degreeProgram: PublicDegreeProgram,
        teachings: List<PublicTeaching>,
        warnings: MutableList<String>
    ): List<PublicExamAppeal> {
        return try {
            val html = client.getExamAppealsPage(siteUrl)
            val appeals = parser.parseExamAppealsFromDegreeProgramPage(
                html = html,
                degreeProgram = degreeProgram,
                teachings = teachings
            )
            val publicRowsCount = PUBLIC_EXAM_DATE_LABEL_REGEX.findAll(html).count()
            if (publicRowsCount > appeals.size) {
                warnings += "Alcuni appelli pubblici non sono stati importati perche non collegabili agli insegnamenti selezionati."
            }
            appeals
        } catch (exception: UniboPublicImportException) {
            warnings += "Appelli pubblici non disponibili per il corso selezionato."
            emptyList()
        } catch (exception: Exception) {
            warnings += "Appelli pubblici non disponibili per il corso selezionato."
            emptyList()
        }
    }

    private fun String?.toCampusParam(): String? {
        val normalized = normalizeText(this.orEmpty())
        return when (normalized) {
            "", "tutti" -> null
            "bologna", "campus di bologna" -> "bologna"
            "cesena", "campus di cesena" -> "cesena"
            "forli", "campus di forli" -> "forli"
            "ravenna", "campus di ravenna" -> "ravenna"
            "rimini", "campus di rimini" -> "rimini"
            else -> normalized
        }
    }

    private companion object {
        val PUBLIC_EXAM_DATE_LABEL_REGEX = "Data\\s+e\\s+ora\\s*:".toRegex(RegexOption.IGNORE_CASE)
    }
}
