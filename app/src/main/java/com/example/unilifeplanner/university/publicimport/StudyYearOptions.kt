package com.example.unilifeplanner.university.publicimport

data class StudyYearOption(
    val year: Int,
    val label: String
)

fun PublicDegreeProgram.availableStudyYearOptions(): List<StudyYearOption> {
    val duration = durationYears
        ?.takeIf { it in 1..6 }
        ?: fallbackDurationYears(degreeType)
    return (1..duration).map { year ->
        StudyYearOption(
            year = year,
            label = formatStudyYearLabel(year)
        )
    }
}

fun formatStudyYearLabel(year: Int): String = "$year\u00B0 anno"

private fun fallbackDurationYears(degreeType: String?): Int {
    return when (normalizeText(degreeType.orEmpty())) {
        "laurea magistrale" -> 2
        "laurea magistrale a ciclo unico" -> 5
        "laurea" -> 3
        else -> 3
    }
}
