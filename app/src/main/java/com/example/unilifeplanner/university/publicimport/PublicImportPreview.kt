package com.example.unilifeplanner.university.publicimport

data class PublicImportPreview(
    val degreeProgram: PublicDegreeProgram,
    val teachings: List<PublicTeaching>,
    val lessons: List<PublicLesson>,
    val examAppeals: List<PublicExamAppeal> = emptyList(),
    val selectedStudyYear: Int? = null,
    val warnings: List<String> = emptyList(),
    val curriculum: PublicCurriculum? = null
) {
    val lessonsByTeachingExternalId: Map<String, List<PublicLesson>> =
        lessons.groupBy { it.teachingExternalId }

    val examAppealsByTeachingExternalId: Map<String, List<PublicExamAppeal>> =
        examAppeals.groupBy { it.teachingExternalId }
}
