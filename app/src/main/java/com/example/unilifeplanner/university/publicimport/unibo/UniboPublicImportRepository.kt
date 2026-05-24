package com.example.unilifeplanner.university.publicimport.unibo

import com.example.unilifeplanner.university.publicimport.PublicDegreeProgram
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

    override suspend fun searchDegreePrograms(
        query: String,
        academicYear: String,
        campus: String?,
        degreeType: String?
    ): List<PublicDegreeProgram> {
        val normalizedCampus = campus.toCampusParam()
        val normalizedDegreeType = degreeType?.takeUnless { normalizeText(it) == "tutte" }
        val key = listOf(
            normalizeText(query),
            academicYear,
            normalizedCampus.orEmpty(),
            normalizedDegreeType.orEmpty()
        ).joinToString("|")

        cache.getSearch(key)?.let { return it }

        val results = client.searchDegreeProgramsPages(
            query = query,
            campus = normalizedCampus,
            degreeType = normalizedDegreeType
        )
            .flatMap { html -> parser.parseDegreeProgramSearchResults(html, academicYear) }
            .map { it.copy(academicYear = academicYear) }
            .distinctBy { it.externalId }

        cache.putSearch(key, results)
        return results
    }

    override suspend fun loadPreview(degreeProgram: PublicDegreeProgram): PublicImportPreview {
        val cacheKey = "${degreeProgram.externalId}|${degreeProgram.academicYear}"
        cache.getPreview(cacheKey)?.let { return it }

        val warnings = mutableListOf<String>()
        val detailHtml = client.getDegreeProgramPage(degreeProgram.officialUrl)
        val siteUrl = parser.parseDegreeProgramSiteUrl(detailHtml)
        if (siteUrl == null) {
            val preview = PublicImportPreview(
                degreeProgram = degreeProgram,
                teachings = emptyList(),
                lessons = emptyList(),
                warnings = listOf("Pagina pubblica del corso di laurea non trovata.")
            )
            cache.putPreview(cacheKey, preview)
            return preview
        }

        val indexHtml = client.getTeachingPlanIndexPage(
            degreeProgramSiteUrl = siteUrl,
            academicYear = degreeProgram.academicYear,
            degreeProgramCode = degreeProgram.externalId
        )
        val planLinks = parser.parseTeachingPlanLinks(indexHtml, degreeProgram.academicYear)
        if (planLinks.isEmpty()) {
            warnings += "Nessun piano didattico pubblico trovato per ${degreeProgram.academicYear}."
        }

        val baseDegreeProgram = degreeProgram.copy(officialUrl = siteUrl)
        val planTeachings = planLinks
            .flatMap { planUrl ->
                val planHtml = client.getDegreeProgramPage(planUrl)
                parser.parseTeachingsFromDegreeProgramPage(planHtml, baseDegreeProgram)
            }
            .distinctBy { it.externalId }

        if (planTeachings.isEmpty()) {
            warnings += "Nessun insegnamento pubblico trovato per il corso selezionato."
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

        val preview = PublicImportPreview(
            degreeProgram = baseDegreeProgram,
            teachings = enrichedTeachings,
            lessons = lessons.distinctBy { it.externalId },
            warnings = warnings.distinct()
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
}
