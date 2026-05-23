package com.example.unilifeplanner.data.repository

import com.example.unilifeplanner.data.local.LessonDao
import com.example.unilifeplanner.data.local.LessonEntity
import com.example.unilifeplanner.data.local.LessonWithCourse
import kotlinx.coroutines.flow.Flow

class LessonRepository(
    private val lessonDao: LessonDao
) {
    fun getLessonsForCourse(courseId: Int): Flow<List<LessonEntity>> =
        lessonDao.getLessonsForCourse(courseId)

    fun getAllLessons(): Flow<List<LessonEntity>> =
        lessonDao.getAllLessons()

    fun getLessonsWithCourse(): Flow<List<LessonWithCourse>> =
        lessonDao.getLessonsWithCourse()

    fun getLessonsWithCourseForCourse(courseId: Int): Flow<List<LessonWithCourse>> =
        lessonDao.getLessonsWithCourseForCourse(courseId)

    fun getLessonById(lessonId: Int): Flow<LessonEntity?> =
        lessonDao.getLessonById(lessonId)

    suspend fun getLessonsWithReminderEnabled(): List<LessonEntity> =
        lessonDao.getLessonsWithReminderEnabled()

    suspend fun insertLesson(lesson: LessonEntity): Long {
        val now = System.currentTimeMillis()
        return lessonDao.insertLesson(
            lesson.copy(
                id = 0,
                classroom = lesson.classroom?.trim()?.takeIf { it.isNotEmpty() },
                building = lesson.building?.trim()?.takeIf { it.isNotEmpty() },
                locationQuery = lesson.locationQuery?.trim()?.takeIf { it.isNotEmpty() },
                notes = lesson.notes?.trim()?.takeIf { it.isNotEmpty() },
                createdAt = now,
                updatedAt = now
            )
        )
    }

    suspend fun updateLesson(lesson: LessonEntity) {
        lessonDao.updateLesson(
            lesson.copy(
                classroom = lesson.classroom?.trim()?.takeIf { it.isNotEmpty() },
                building = lesson.building?.trim()?.takeIf { it.isNotEmpty() },
                locationQuery = lesson.locationQuery?.trim()?.takeIf { it.isNotEmpty() },
                notes = lesson.notes?.trim()?.takeIf { it.isNotEmpty() },
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun deleteLesson(lesson: LessonEntity) {
        lessonDao.deleteLesson(lesson)
    }

    suspend fun deleteLessonById(lessonId: Int) {
        lessonDao.deleteLessonById(lessonId)
    }

    suspend fun updateLessonReminderEnabled(
        lessonId: Int,
        enabled: Boolean
    ) {
        lessonDao.updateLessonReminderEnabled(
            lessonId = lessonId,
            enabled = enabled
        )
    }

    suspend fun deleteLessonsForCourse(courseId: Int) {
        lessonDao.deleteLessonsForCourse(courseId)
    }
}
