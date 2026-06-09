package com.example.unilifeplanner.university.publicimport.unibo

import com.example.unilifeplanner.domain.exams.examDateToStartOfDayMillis
import com.example.unilifeplanner.university.publicimport.PublicCurriculum
import com.example.unilifeplanner.university.publicimport.PublicDegreeProgram
import com.example.unilifeplanner.university.publicimport.PublicExamAppeal
import com.example.unilifeplanner.university.publicimport.PublicLesson
import com.example.unilifeplanner.university.publicimport.PublicTeaching
import com.example.unilifeplanner.university.publicimport.PublicTeachingDetails
import com.example.unilifeplanner.university.publicimport.buildStableExternalId
import com.example.unilifeplanner.university.publicimport.normalizeText
import com.example.unilifeplanner.university.publicimport.parseCredits
import com.example.unilifeplanner.university.publicimport.parseDayOfWeekItalian
import com.example.unilifeplanner.university.publicimport.parseTimeToMinutes
import com.example.unilifeplanner.university.unibo.publicdata.UniboPublicConfig
import java.time.LocalDate
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

    fun parseCurriculaOrTeachingPlans(
        html: String,
        academicYear: String,
        degreeProgram: PublicDegreeProgram
    ): List<PublicCurriculum> {
        val document = Jsoup.parse(html, UniboPublicConfig.COURSE_SITE_BASE_URL)
        val startYear = academicYear.substringBefore("/").trim()
        return document.select(TEACHING_PLAN_LINK_SELECTOR)
            .map { link -> link to link.absUrl("href").normalizeOfficialUrl() }
            .filter { (_, url) -> startYear.isBlank() || url.contains("/$startYear/") }
            .distinctBy { (_, url) -> url }
            .mapIndexed { index, (link, url) ->
                PublicCurriculum(
                    externalId = buildStableExternalId(
                        UniboPublicConfig.PROVIDER,
                        degreeProgram.externalId,
                        academicYear,
                        url
                    ),
                    name = link.curriculumName(index),
                    academicYear = academicYear,
                    degreeProgramExternalId = degreeProgram.externalId,
                    officialUrl = url
                )
            }
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
                dateMillis = null,
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

    fun parseExamAppealsFromDegreeProgramPage(
        html: String,
        degreeProgram: PublicDegreeProgram,
        teachings: List<PublicTeaching>
    ): List<PublicExamAppeal> {
        val document = Jsoup.parse(html, degreeProgram.officialUrl)
        val officialUrl = document.selectFirst("link[rel=canonical]")
            ?.absUrl("href")
            .cleanOrNull()
            ?: document.selectFirst("meta[property=og:url]")
                ?.attr("content")
                .cleanOrNull()
            ?: degreeProgram.officialUrl.trimEnd('/') + "/appelli"
        val teachingMatcher = TeachingMatcher(teachings)
        val appeals = linkedMapOf<String, PublicExamAppeal>()

        document.select(EXAM_APPEAL_TEACHING_SELECTOR).forEach { heading ->
            val context = heading.toExamTeachingContext(teachingMatcher) ?: return@forEach
            val panel = heading.nextElementSibling()
                ?.takeIf { normalizeText(it.className()).contains("items-container") }
            val tables = panel?.select(EXAM_APPEAL_TABLE_SELECTOR).orEmpty()

            tables.forEach { table ->
                table.toPublicExamAppeal(
                    context = context,
                    degreeProgram = degreeProgram,
                    officialUrl = officialUrl
                )?.let { appeal -> appeals[appeal.externalId] = appeal }
            }

            if (tables.isEmpty()) {
                parseTextExamAppeals(
                    text = panel?.text().orEmpty(),
                    context = context,
                    degreeProgram = degreeProgram,
                    officialUrl = officialUrl
                ).forEach { appeal -> appeals[appeal.externalId] = appeal }
            }
        }

        if (appeals.isNotEmpty()) return appeals.values.toList()

        return parseFallbackTextExamAppeals(
            document = document,
            degreeProgram = degreeProgram,
            teachings = teachings,
            officialUrl = officialUrl
        ).distinctBy { it.externalId }
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

    private fun String.normalizeOfficialUrl(): String =
        trim()
            .substringBefore("#")
            .trimEnd('/')

    private fun Element.curriculumName(index: Int): String {
        val ownText = text().cleanOrNull()
            ?.takeUnless { it.isGenericTeachingPlanLabel() }
        if (ownText != null) return ownText

        val parentText = parents()
            .take(3)
            .mapNotNull { parent -> parent.text().cleanOrNull() }
            .firstOrNull { text ->
                val normalized = normalizeText(text)
                normalized.length in 4..120 &&
                    !normalized.isGenericTeachingPlanLabel() &&
                    !normalized.contains("insegnamenti")
            }

        return parentText ?: "Curriculum ${index + 1}"
    }

    private fun String.isGenericTeachingPlanLabel(): Boolean {
        val normalized = normalizeText(this)
        return normalized.isBlank() ||
            normalized == "piano didattico" ||
            normalized == "insegnamenti" ||
            normalized == "vedi il piano didattico" ||
            normalized == "consulta il piano didattico"
    }

    private fun Element.toExamTeachingContext(
        matcher: TeachingMatcher
    ): ExamTeachingContext? {
        val code = selectFirst("span.code")?.text().cleanOrNull()
            ?: EXAM_HEADING_CODE_REGEX.find(text())?.groupValues?.getOrNull(1)
        val professor = selectFirst("span.docente")?.text().cleanOrNull()
        val anchor = selectFirst("a") ?: this
        val name = anchor.clone().also { clone ->
            clone.select("span.code, span.docente, i").remove()
        }.text().cleanOrNull()
            ?: text()
                .removePrefix(code.orEmpty())
                .removeSuffix(professor.orEmpty())
                .cleanOrNull()
            ?: return null
        val teaching = matcher.find(code = code, name = name) ?: return null

        return ExamTeachingContext(
            teaching = teaching,
            teachingName = teaching.name,
            code = teaching.code ?: code,
            professor = professor ?: teaching.professor
        )
    }

    private fun Element.toPublicExamAppeal(
        context: ExamTeachingContext,
        degreeProgram: PublicDegreeProgram,
        officialUrl: String
    ): PublicExamAppeal? {
        val values = select("tr").associate { row ->
            val label = row.selectFirst("th")?.text()
                ?.trim(':', ' ')
                ?.let(::normalizeText)
                .orEmpty()
            val value = row.selectFirst("td")?.text()
                ?.replace("\\s+".toRegex(), " ")
                ?.trim()
                .orEmpty()
            label to value
        }

        return buildExamAppeal(
            context = context,
            degreeProgram = degreeProgram,
            officialUrl = officialUrl,
            dateTimeText = values["data e ora"],
            registrationText = values["lista iscrizioni"],
            type = values["tipo prova"].cleanOrNull(),
            location = values["luogo"].cleanOrNull(),
            note = values["note"].cleanOrNull()
        )
    }

    private fun parseTextExamAppeals(
        text: String,
        context: ExamTeachingContext,
        degreeProgram: PublicDegreeProgram,
        officialUrl: String
    ): List<PublicExamAppeal> {
        val lines = text.readableLines()
        val result = mutableListOf<PublicExamAppeal>()
        var current = mutableMapOf<String, String>()

        fun flush() {
            if (current.isEmpty()) return
            buildExamAppeal(
                context = context,
                degreeProgram = degreeProgram,
                officialUrl = officialUrl,
                dateTimeText = current["data e ora"],
                registrationText = current["lista iscrizioni"],
                type = current["tipo prova"].cleanOrNull(),
                location = current["luogo"].cleanOrNull(),
                note = current["note"].cleanOrNull()
            )?.let(result::add)
            current = mutableMapOf()
        }

        lines.forEach { line ->
            val label = line.substringBefore(":", "").let(::normalizeText)
            val value = line.substringAfter(":", "").trim()
            if (label == "data e ora") flush()
            if (label in EXAM_FIELD_LABELS) current[label] = value
        }
        flush()
        return result
    }

    private fun parseFallbackTextExamAppeals(
        document: Document,
        degreeProgram: PublicDegreeProgram,
        teachings: List<PublicTeaching>,
        officialUrl: String
    ): List<PublicExamAppeal> {
        val matcher = TeachingMatcher(teachings)
        val lines = document.body()?.text().orEmpty().readableLines()
        val result = mutableListOf<PublicExamAppeal>()
        var context: ExamTeachingContext? = null
        var current = mutableMapOf<String, String>()

        fun flush() {
            val activeContext = context
            if (activeContext != null && current.isNotEmpty()) {
                buildExamAppeal(
                    context = activeContext,
                    degreeProgram = degreeProgram,
                    officialUrl = officialUrl,
                    dateTimeText = current["data e ora"],
                    registrationText = current["lista iscrizioni"],
                    type = current["tipo prova"].cleanOrNull(),
                    location = current["luogo"].cleanOrNull(),
                    note = current["note"].cleanOrNull()
                )?.let(result::add)
            }
            current = mutableMapOf()
        }

        lines.forEach { line ->
            val headingMatch = EXAM_TEXT_HEADING_REGEX.matchEntire(line)
            if (headingMatch != null) {
                flush()
                val code = headingMatch.groupValues[1]
                val title = headingMatch.groupValues[2].cleanOrNull() ?: line
                matcher.find(code = code, name = title)?.let { teaching ->
                    context = ExamTeachingContext(
                        teaching = teaching,
                        teachingName = teaching.name,
                        code = teaching.code ?: code,
                        professor = teaching.professor
                    )
                }
                return@forEach
            }

            val label = line.substringBefore(":", "").let(::normalizeText)
            val value = line.substringAfter(":", "").trim()
            if (label == "data e ora") flush()
            if (label in EXAM_FIELD_LABELS) current[label] = value
        }
        flush()
        return result
    }

    private fun buildExamAppeal(
        context: ExamTeachingContext,
        degreeProgram: PublicDegreeProgram,
        officialUrl: String,
        dateTimeText: String?,
        registrationText: String?,
        type: String?,
        location: String?,
        note: String?
    ): PublicExamAppeal? {
        val parsedDateTime = dateTimeText?.let(::parseItalianExamDateTime) ?: return null
        val notes = buildList {
            registrationText.cleanOrNull()?.let { add("Lista iscrizioni: $it") }
            note.cleanOrNull()?.let { add("Note: $it") }
        }.joinToString("\n").cleanOrNull()
        val externalId = buildStableExternalId(
            UniboPublicConfig.PROVIDER,
            degreeProgram.externalId,
            degreeProgram.academicYear,
            context.teaching.externalId,
            context.code,
            parsedDateTime.date.toString(),
            parsedDateTime.timeMinutes?.toString(),
            type,
            location,
            context.professor,
            context.teachingName
        )

        return PublicExamAppeal(
            externalId = externalId,
            teachingExternalId = context.teaching.externalId,
            teachingCode = context.code,
            teachingName = context.teachingName,
            professor = context.professor,
            dateMillis = examDateToStartOfDayMillis(parsedDateTime.date),
            timeMinutes = parsedDateTime.timeMinutes,
            location = location.cleanOrNull(),
            type = type.cleanOrNull(),
            notes = notes,
            officialUrl = officialUrl.cleanOrNull()
        )
    }

    private fun parseItalianExamDateTime(value: String): ParsedExamDateTime? {
        val match = ITALIAN_DATE_TIME_REGEX.find(value) ?: return null
        val day = match.groupValues[1].toIntOrNull() ?: return null
        val month = ITALIAN_MONTHS[normalizeText(match.groupValues[2])] ?: return null
        val year = match.groupValues[3].toIntOrNull() ?: return null
        val timeMinutes = match.groupValues.getOrNull(4)
            ?.takeIf { it.isNotBlank() }
            ?.let(::parseTimeToMinutes)

        return runCatching {
            ParsedExamDateTime(
                date = LocalDate.of(year, month, day),
                timeMinutes = timeMinutes
            )
        }.getOrNull()
    }

    private fun String.readableLines(): List<String> =
        replace("\\s+".toRegex(), " ")
            .replace(" Data e ora:", "\nData e ora:")
            .replace(" Lista iscrizioni:", "\nLista iscrizioni:")
            .replace(" Tipo prova:", "\nTipo prova:")
            .replace(" Luogo:", "\nLuogo:")
            .replace(" Note:", "\nNote:")
            .split("\n")
            .mapNotNull { it.cleanOrNull() }

    private data class ParsedExamDateTime(
        val date: LocalDate,
        val timeMinutes: Int?
    )

    private data class ExamTeachingContext(
        val teaching: PublicTeaching,
        val teachingName: String,
        val code: String?,
        val professor: String?
    )

    private class TeachingMatcher(teachings: List<PublicTeaching>) {
        private val byCode = teachings
            .mapNotNull { teaching -> teaching.code?.trim()?.takeIf { it.isNotEmpty() }?.let { it to teaching } }
            .toMap()
        private val byNormalizedName = teachings.associateBy { normalizeText(it.name) }

        fun find(code: String?, name: String): PublicTeaching? {
            code?.trim()?.takeIf { it.isNotEmpty() }?.let { teachingCode ->
                byCode[teachingCode]?.let { return it }
            }
            return byNormalizedName[normalizeText(name)]
        }
    }

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
        private const val EXAM_APPEAL_TEACHING_SELECTOR = ".dropdown-component h3, h3[id^=tab]"
        private const val EXAM_APPEAL_TABLE_SELECTOR = "table.single-item"
        private val DEGREE_CODE_REGEX = "\\b\\d{4}\\b".toRegex()
        private val TIME_RANGE_REGEX =
            "(\\d{1,2}:\\d{2})\\s*-\\s*(\\d{1,2}:\\d{2})".toRegex()
        private val EXAM_HEADING_CODE_REGEX = "^\\s*(\\d{4,6})\\b".toRegex()
        private val EXAM_TEXT_HEADING_REGEX = "^(\\d{4,6})\\s+(.+)$".toRegex()
        private val ITALIAN_DATE_TIME_REGEX = (
            "(\\d{1,2})\\s+" +
                "(gennaio|febbraio|marzo|aprile|maggio|giugno|luglio|agosto|settembre|ottobre|novembre|dicembre)" +
                "\\s+(\\d{4})(?:\\s+ore\\s+(\\d{1,2}:\\d{2}))?"
            ).toRegex(setOf(RegexOption.IGNORE_CASE))
        private val EXAM_FIELD_LABELS = setOf(
            "data e ora",
            "lista iscrizioni",
            "tipo prova",
            "luogo",
            "note"
        )
        private val ITALIAN_MONTHS = mapOf(
            "gennaio" to 1,
            "febbraio" to 2,
            "marzo" to 3,
            "aprile" to 4,
            "maggio" to 5,
            "giugno" to 6,
            "luglio" to 7,
            "agosto" to 8,
            "settembre" to 9,
            "ottobre" to 10,
            "novembre" to 11,
            "dicembre" to 12
        )
    }
}
