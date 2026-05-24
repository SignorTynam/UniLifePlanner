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

    suspend fun getLessonByExternalId(
        provider: String,
        externalId: String
    ): LessonEntity? =
        lessonDao.getLessonByExternalId(provider, externalId)

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
                officialUrl = lesson.officialUrl?.trim()?.takeIf { it.isNotEmpty() },
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
                externalId = lesson.externalId?.trim()?.takeIf { it.isNotEmpty() },
                sourceProvider = lesson.sourceProvider?.trim()?.takeIf { it.isNotEmpty() },
                officialUrl = lesson.officialUrl?.trim()?.takeIf { it.isNotEmpty() },
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun upsertImportedLesson(
        provider: String,
        externalId: String,
        lesson: LessonEntity
    ): ImportedUpsertResult {
        val now = System.currentTimeMillis()
        val existing = lessonDao.getLessonByExternalId(provider, externalId)
        val normalizedLesson = lesson.copy(
            classroom = lesson.classroom?.trim()?.takeIf { it.isNotEmpty() },
            building = lesson.building?.trim()?.takeIf { it.isNotEmpty() },
            locationQuery = lesson.locationQuery?.trim()?.takeIf { it.isNotEmpty() },
            notes = lesson.notes?.trim()?.takeIf { it.isNotEmpty() },
            externalId = externalId.trim(),
            sourceProvider = provider,
            officialUrl = lesson.officialUrl?.trim()?.takeIf { it.isNotEmpty() },
            updatedAt = now
        )

        return if (existing == null) {
            val insertedId = lessonDao.insertLesson(
                normalizedLesson.copy(
                    id = 0,
                    createdAt = now
                )
            ).toInt()
            ImportedUpsertResult(id = insertedId, inserted = true)
        } else {
            lessonDao.updateLesson(
                normalizedLesson.copy(
                    id = existing.id,
                    reminderEnabled = existing.reminderEnabled,
                    createdAt = existing.createdAt
                )
            )
            ImportedUpsertResult(id = existing.id, inserted = false)
        }
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

    suspend fun getLessonsBySourceProvider(provider: String): List<LessonEntity> =
        lessonDao.getLessonsBySourceProvider(provider)

    suspend fun deleteLessonsBySourceProvider(provider: String) {
        lessonDao.deleteLessonsBySourceProvider(provider)
    }
}
