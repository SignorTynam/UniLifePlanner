package com.example.unilifeplanner.university.publicimport.unibo

import com.example.unilifeplanner.data.local.CourseEntity
import com.example.unilifeplanner.data.local.LessonEntity
import com.example.unilifeplanner.domain.lessons.nextLessonDateMillis
import com.example.unilifeplanner.domain.model.CourseStatus
import com.example.unilifeplanner.university.publicimport.PublicLesson
import com.example.unilifeplanner.university.publicimport.PublicTeaching
import com.example.unilifeplanner.university.unibo.publicdata.UniboPublicConfig

class UniboPublicMapper {
    fun mapTeachingToCourse(
        teaching: PublicTeaching,
        primaryClassroom: String?,
        nowMillis: Long = System.currentTimeMillis()
    ): CourseEntity {
        return CourseEntity(
            name = teaching.name,
            professor = teaching.professor ?: "Docente non indicato",
            examDate = null,
            credits = teaching.credits ?: 0,
            status = CourseStatus.TO_STUDY.name,
            isFavorite = false,
            reminderEnabled = false,
            classroom = primaryClassroom,
            notes = teaching.officialUrl?.let { "Importato da UniBo: $it" },
            externalId = teaching.externalId,
            sourceProvider = UniboPublicConfig.PROVIDER,
            officialUrl = teaching.officialUrl,
            createdAt = nowMillis,
            updatedAt = nowMillis
        )
    }

    fun mapLessonToEntity(
        lesson: PublicLesson,
        courseId: Int,
        nowMillis: Long = System.currentTimeMillis()
    ): LessonEntity? {
        val dayOfWeek = lesson.dayOfWeek ?: return null
        val startTimeMinutes = lesson.startTimeMinutes ?: return null
        val endTimeMinutes = lesson.endTimeMinutes ?: return null

        return LessonEntity(
            courseId = courseId,
            dateMillis = lesson.dateMillis ?: nextLessonDateMillis(
                dayOfWeek = dayOfWeek,
                startTimeMinutes = startTimeMinutes,
                nowMillis = nowMillis
            ),
            dayOfWeek = dayOfWeek,
            startTimeMinutes = startTimeMinutes,
            endTimeMinutes = endTimeMinutes,
            classroom = lesson.classroom,
            building = lesson.building,
            locationQuery = listOfNotNull(lesson.classroom, lesson.building)
                .joinToString(", ")
                .trim()
                .takeIf { it.isNotEmpty() },
            notes = lesson.notes,
            reminderEnabled = true,
            externalId = lesson.externalId,
            sourceProvider = UniboPublicConfig.PROVIDER,
            officialUrl = lesson.officialUrl,
            createdAt = nowMillis,
            updatedAt = nowMillis
        )
    }
}
