package com.example.unilifeplanner.university.publicimport.unibo

import com.example.unilifeplanner.data.repository.CourseRepository
import com.example.unilifeplanner.data.repository.LessonRepository
import com.example.unilifeplanner.notifications.LessonReminderScheduler
import com.example.unilifeplanner.university.publicimport.PublicImportPreview
import com.example.unilifeplanner.university.publicimport.PublicImportResult
import com.example.unilifeplanner.university.unibo.publicdata.UniboPublicConfig
import kotlinx.coroutines.flow.first

class UniboPublicImporter(
    private val courseRepository: CourseRepository,
    private val lessonRepository: LessonRepository,
    private val lessonReminderScheduler: LessonReminderScheduler,
    private val mapper: UniboPublicMapper = UniboPublicMapper()
) {
    suspend fun importPreview(preview: PublicImportPreview): PublicImportResult {
        var importedTeachings = 0
        var updatedTeachings = 0
        var importedLessons = 0
        var updatedLessons = 0
        val warnings = preview.warnings.toMutableList()
        val localCourseIds = mutableMapOf<String, Int>()

        preview.teachings.forEach { teaching ->
            val lessonsForTeaching = preview.lessonsByTeachingExternalId[teaching.externalId].orEmpty()
            val mappedCourse = mapper.mapTeachingToCourse(
                teaching = teaching,
                primaryClassroom = lessonsForTeaching.firstOrNull()?.classroom
            )
            val result = courseRepository.upsertImportedCourse(
                provider = UniboPublicConfig.PROVIDER,
                externalId = teaching.externalId,
                course = mappedCourse
            )
            localCourseIds[teaching.externalId] = result.id
            if (result.inserted) importedTeachings++ else updatedTeachings++
        }

        preview.lessons.forEach { lesson ->
            val courseId = localCourseIds[lesson.teachingExternalId]
            if (courseId == null) {
                warnings += "Una lezione non e stata importata perche l'insegnamento non e stato trovato."
                return@forEach
            }

            val entity = mapper.mapLessonToEntity(lesson, courseId)
            if (entity == null) {
                warnings += "Alcuni orari non sono stati riconosciuti."
                return@forEach
            }

            val result = lessonRepository.upsertImportedLesson(
                provider = UniboPublicConfig.PROVIDER,
                externalId = lesson.externalId,
                lesson = entity
            )
            if (result.inserted) importedLessons++ else updatedLessons++
            scheduleLessonReminderIfEnabled(result.id)
        }

        if (updatedTeachings > 0 || updatedLessons > 0) {
            warnings += "Alcune lezioni precedentemente importate potrebbero non essere piu presenti sul sito."
        }

        return PublicImportResult(
            importedTeachings = importedTeachings,
            updatedTeachings = updatedTeachings,
            importedLessons = importedLessons,
            updatedLessons = updatedLessons,
            warnings = warnings.distinct()
        )
    }

    private suspend fun scheduleLessonReminderIfEnabled(lessonId: Int) {
        val lesson = lessonRepository.getLessonById(lessonId).first() ?: return
        if (!lesson.reminderEnabled) {
            lessonReminderScheduler.cancelLessonReminder(lessonId)
            return
        }
        val course = courseRepository.getCourseById(lesson.courseId).first() ?: return
        lessonReminderScheduler.scheduleLessonReminder(
            lessonId = lesson.id,
            courseId = course.id,
            courseName = course.name,
            dateMillis = lesson.dateMillis,
            dayOfWeek = lesson.dayOfWeek,
            startTimeMinutes = lesson.startTimeMinutes,
            classroom = lesson.classroom
        )
    }
}
