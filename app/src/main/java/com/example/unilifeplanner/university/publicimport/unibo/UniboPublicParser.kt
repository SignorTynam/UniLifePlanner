package com.example.unilifeplanner.university.publicimport.unibo

import com.example.unilifeplanner.university.publicimport.PublicDegreeProgram
import com.example.unilifeplanner.university.publicimport.PublicLesson
import com.example.unilifeplanner.university.publicimport.PublicTeaching
import com.example.unilifeplanner.university.publicimport.PublicTeachingDetails
import com.example.unilifeplanner.university.publicimport.buildStableExternalId
import com.example.unilifeplanner.university.publicimport.normalizeText
import com.example.unilifeplanner.university.publicimport.parseCredits
import com.example.unilifeplanner.university.publicimport.parseDayOfWeekItalian
import com.example.unilifeplanner.university.publicimport.parseTimeToMinutes
import com.example.unilifeplanner.university.unibo.publicdata.UniboPublicConfig
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

class UniboPublicParser {
    fun parseDegreeProgramSearchResults(html: String): List<PublicDegreeProgram> {
        return parseDegreeProgramSearchResults(html, academicYear = "")
    }

    fun parseDegreeProgramSearchResults(
        html: String,
        academicYear: String
    ): List<PublicDegreeProgram> {
        val document = Jsoup.parse(html, UniboPublicConfig.BASE_URL)
        return document.select(DEGREE_RESULT_SELECTOR)
            .mapNotNull { item -> item.toDegreeProgram(academicYear) }
            .distinctBy { it.externalId }
    }

    fun parseDegreeProgramSiteUrl(html: String): String? {
        val document = Jsoup.parse(html, UniboPublicConfig.BASE_URL)
        return document.select("a[href]")
            .firstOrNull { link ->
                val href = link.absUrl("href")
                href.startsWith(UniboPublicConfig.COURSE_SITE_BASE_URL) &&
                    normalizeText(link.text()).contains("sito web del corso")
            }
            ?.absUrl("href")
            ?: document.select("a[href]")
                .firstOrNull { it.absUrl("href").startsWith(UniboPublicConfig.COURSE_SITE_BASE_URL) }
                ?.absUrl("href")
    }

    fun parseTeachingPlanLinks(
        html: String,
        academicYear: String
    ): List<String> {
        val document = Jsoup.parse(html, UniboPublicConfig.COURSE_SITE_BASE_URL)
        val startYear = academicYear.substringBefore("/").trim()
        return document.select(TEACHING_PLAN_LINK_SELECTOR)
            .map { it.absUrl("href") }
            .filter { link -> startYear.isBlank() || link.contains("/$startYear/") }
            .distinct()
    }

    fun parseTeachingsFromDegreeProgramPage(
        html: String,
        degreeProgram: PublicDegreeProgram
    ): List<PublicTeaching> {
        val document = Jsoup.parse(html, UniboPublicConfig.COURSE_SITE_BASE_URL)
        val result = linkedMapOf<String, PublicTeaching>()

        document.select(TEACHING_ROW_SELECTOR).forEach { row ->
            val code = row.selectFirst(CODE_CELL_SELECTOR)?.text().cleanOrNull()
            val name = row.selectFirst(TITLE_CELL_SELECTOR)
                ?.text()
                ?.replace("\\s+".toRegex(), " ")
                ?.trim()
                .cleanOrNull()
                ?: return@forEach

            if (normalizeText(name).contains("non attivo per l'anno")) return@forEach

            val credits = row.select(INFO_CELL_SELECTOR)
                .lastOrNull()
                ?.text()
                ?.let(::parseCredits)
            val externalId = code ?: buildStableExternalId(
                UniboPublicConfig.PROVIDER,
                degreeProgram.academicYear,
                degreeProgram.name,
                name
            )

            result.putIfAbsent(
                externalId,
                PublicTeaching(
                    externalId = externalId,
                    degreeProgramExternalId = degreeProgram.externalId,
                    name = name,
                    code = code,
                    professor = null,
                    credits = credits,
                    academicYear = degreeProgram.academicYear,
                    officialUrl = null
                )
            )
        }

        return result.values.toList()
    }

    fun parseTeachingDetails(
        html: String,
        teaching: PublicTeaching
    ): PublicTeachingDetails {
        val document = Jsoup.parse(html, UniboPublicConfig.BASE_URL)
        val professor = document.select("li")
            .firstOrNull { normalizeText(it.text()).startsWith("docente:") }
            ?.text()
            ?.substringAfter(":", "")
            .cleanOrNull()
        val credits = document.select("li")
            .firstOrNull { normalizeText(it.text()).startsWith("crediti formativi:") }
            ?.text()
            ?.let(::parseCredits)
        val officialUrl = document.selectFirst("link[rel=canonical]")
            ?.attr("href")
            .cleanOrNull()
            ?: teaching.officialUrl
        val scheduleUrls = document.select(SCHEDULE_LINK_SELECTOR)
            .map { it.absUrl("href") }
            .distinct()
        val updatedTeaching = teaching.copy(
            professor = professor ?: teaching.professor,
            credits = credits ?: teaching.credits,
            officialUrl = officialUrl
        )
        return PublicTeachingDetails(
            teaching = updatedTeaching,
            scheduleUrls = scheduleUrls,
            warnings = buildList {
                if (updatedTeaching.professor.isNullOrBlank()) {
                    add("${teaching.name}: docente non indicato nelle pagine pubbliche.")
                }
            }
        )
    }

    fun parseTeachingSearchDetails(
        html: String,
        teaching: PublicTeaching,
        degreeProgram: PublicDegreeProgram
    ): PublicTeachingDetails {
        val document = Jsoup.parse(html, UniboPublicConfig.BASE_URL)
        val candidates = document.select(TEACHING_SEARCH_RESULT_SELECTOR)
        val selected = candidates.firstOrNull { item ->
            val text = normalizeText(item.text())
            val codeMatch = teaching.code?.let { normalizeText(it) in text } ?: true
            val degreeMatch = text.contains("cod. ${normalizeText(degreeProgram.externalId)}") ||
                text.contains(normalizeText(degreeProgram.name))
            codeMatch && degreeMatch
        } ?: candidates.firstOrNull { item ->
            teaching.code?.let { normalizeText(item.text()).contains(normalizeText(it)) } ?: false
        }

        if (selected == null) {
            return PublicTeachingDetails(
                teaching = teaching,
                warnings = listOf(
                    "${teaching.name}: dettagli pubblici non trovati nella ricerca UniBo."
                )
            )
        }

        val professors = selected.select(TEACHER_SELECTOR)
            .eachText()
            .mapNotNull { it.cleanOrNull() }
            .distinct()
        val credits = selected.selectFirst(CREDITS_SELECTOR)?.text()?.let(::parseCredits)
        val officialUrl = selected.select("span.teachingname a[href]")
            .firstOrNull()
            ?.absUrl("href")
            ?: teaching.officialUrl
        val scheduleUrls = selected.select(SCHEDULE_LINK_SELECTOR)
            .map { it.absUrl("href") }
            .distinct()
        val updatedTeaching = teaching.copy(
            professor = professors.joinToString(", ").cleanOrNull() ?: teaching.professor,
            credits = credits ?: teaching.credits,
            officialUrl = officialUrl
        )

        return PublicTeachingDetails(
            teaching = updatedTeaching,
            scheduleUrls = scheduleUrls,
            warnings = buildList {
                if (updatedTeaching.professor.isNullOrBlank()) {
                    add("${teaching.name}: docente non indicato nelle pagine pubbliche.")
                }
                if (scheduleUrls.isEmpty()) {
                    add("${teaching.name}: lezioni non disponibili pubblicamente.")
                }
            }
        )
    }

    fun parseLessonsFromTeachingPage(
        html: String,
        teaching: PublicTeaching
    ): List<PublicLesson> {
        val document = Jsoup.parse(html, UniboPublicConfig.BASE_URL)
        val officialUrl = document.selectFirst("link[rel=canonical]")
            ?.attr("href")
            .cleanOrNull()
            ?: teaching.officialUrl
        val lessons = linkedMapOf<String, PublicLesson>()

        document.select(LESSON_ROW_SELECTOR).forEach { row ->
            val cells = row.select("> td")
            if (cells.size < 3) return@forEach

            val day = cells[0].text()
                .substringBefore(",")
                .let(::parseDayOfWeekItalian)
                ?: return@forEach
            val timeMatch = TIME_RANGE_REGEX.find(cells[1].text()) ?: return@forEach
            val start = parseTimeToMinutes(timeMatch.groupValues[1]) ?: return@forEach
            val end = parseTimeToMinutes(timeMatch.groupValues[2]) ?: return@forEach
            if (end <= start) return@forEach

            val locationParts = cells[2].select("span")
                .eachText()
                .mapNotNull { it.cleanOrNull() }
            val classroom = locationParts.firstOrNull()
            val building = locationParts
                .firstOrNull { normalizeText(it).startsWith("edificio") }
                ?: locationParts.drop(1).joinToString(" - ").cleanOrNull()
            val notes = row.attr("data-title").cleanOrNull()
            val externalId = buildStableExternalId(
                UniboPublicConfig.PROVIDER,
                teaching.externalId,
                day.toString(),
                start.toString(),
                end.toString(),
                classroom,
                building
            )

            lessons[externalId] = PublicLesson(
                externalId = externalId,
                teachingExternalId = teaching.externalId,
                dayOfWeek = day,
                startTimeMinutes = start,
                endTimeMinutes = end,
                classroom = classroom,
                building = building,
                notes = notes,
                officialUrl = officialUrl
            )
        }

        return lessons.values.toList()
    }

    private fun Element.toDegreeProgram(academicYear: String): PublicDegreeProgram? {
        val titleElement = selectFirst(DEGREE_TITLE_SELECTOR) ?: return null
        val name = titleElement.text().cleanOrNull() ?: return null
        val officialUrl = selectFirst(DEGREE_LINK_SELECTOR)?.absUrl("href").cleanOrNull()
            ?: return null
        val externalId = titleElement.id().cleanOrNull()
            ?: selectFirst("p.tag")?.text()?.let { DEGREE_CODE_REGEX.find(it)?.value }
            ?: return null
        val campus = findLabeledValue("Sede didattica")
        val duration = findLabeledValue("Durata")
        val normalizedUrl = officialUrl.lowercase()
        val degreeType = when {
            normalizedUrl.contains("/lauree-magistrali/") -> "Laurea Magistrale"
            duration?.contains("5") == true -> "Laurea Magistrale a Ciclo Unico"
            else -> "Laurea"
        }

        return PublicDegreeProgram(
            externalId = externalId,
            name = name,
            campus = campus,
            degreeType = degreeType,
            academicYear = academicYear,
            officialUrl = officialUrl
        )
    }

    private fun Element.findLabeledValue(label: String): String? {
        val normalizedLabel = normalizeText(label)
        val paragraph = select("p").firstOrNull { element ->
            normalizeText(element.selectFirst("span")?.text().orEmpty())
                .contains(normalizedLabel)
        } ?: return null
        val prefix = paragraph.selectFirst("span")?.text().orEmpty()
        return paragraph.text()
            .removePrefix(prefix)
            .trim(':', ' ')
            .cleanOrNull()
    }

    private fun String?.cleanOrNull(): String? =
        this?.trim()?.takeIf { it.isNotEmpty() }

    companion object {
        private const val DEGREE_RESULT_SELECTOR = "div.item"
        private const val DEGREE_TITLE_SELECTOR = ".title h3"
        private const val DEGREE_LINK_SELECTOR = ".card-actions a[href]"
        private const val TEACHING_PLAN_LINK_SELECTOR = "a[href*=insegnamenti/piano/]"
        private const val TEACHING_ROW_SELECTOR = "div.manifestum table tbody tr"
        private const val CODE_CELL_SELECTOR = "td.code"
        private const val TITLE_CELL_SELECTOR = "td.title"
        private const val INFO_CELL_SELECTOR = "td.info"
        private const val TEACHING_SEARCH_RESULT_SELECTOR = "li.mainteaching"
        private const val TEACHER_SELECTOR = "span.teacher"
        private const val CREDITS_SELECTOR = "span.cfu"
        private const val SCHEDULE_LINK_SELECTOR = "a[href*=orariolezioni]"
        private const val LESSON_ROW_SELECTOR = "div.box-schedule table tbody tr"
        private val DEGREE_CODE_REGEX = "\\b\\d{4}\\b".toRegex()
        private val TIME_RANGE_REGEX =
            "(\\d{1,2}:\\d{2})\\s*-\\s*(\\d{1,2}:\\d{2})".toRegex()
    }
}
