package com.example.unilifeplanner.data.repository

import com.example.unilifeplanner.data.local.CourseDao
import com.example.unilifeplanner.data.local.CourseEntity
import com.example.unilifeplanner.domain.model.CourseStatus
import kotlinx.coroutines.flow.Flow

class CourseRepository(
    private val courseDao: CourseDao
) {
    val allCourses: Flow<List<CourseEntity>> = courseDao.getAllCourses()
    val favoriteCoursesFlow: Flow<List<CourseEntity>> = courseDao.getFavoriteCourses()

    fun getCourseById(courseId: Int): Flow<CourseEntity?> =
        courseDao.getCourseById(courseId)

    suspend fun getCourseByExternalId(
        provider: String,
        externalId: String
    ): CourseEntity? =
        courseDao.getCourseByExternalId(provider, externalId)

    fun searchCourses(query: String): Flow<List<CourseEntity>> =
        courseDao.searchCourses(query.trim())

    fun getCoursesByStatus(status: CourseStatus): Flow<List<CourseEntity>> =
        courseDao.getCoursesByStatus(status.name)

    fun getFavoriteCourses(): Flow<List<CourseEntity>> =
        favoriteCoursesFlow

    suspend fun insertCourse(course: CourseEntity): Long {
        val now = System.currentTimeMillis()
        return courseDao.insertCourse(
            course.copy(
                id = 0,
                name = course.name.trim(),
                professor = course.professor.trim(),
                classroom = course.classroom?.trim()?.takeIf { it.isNotEmpty() },
                notes = course.notes?.trim()?.takeIf { it.isNotEmpty() },
                officialUrl = course.officialUrl?.trim()?.takeIf { it.isNotEmpty() },
                createdAt = now,
                updatedAt = now
            )
        )
    }

    suspend fun insertCourse(
        name: String,
        professor: String,
        examDate: Long?,
        credits: Int,
        status: CourseStatus,
        isFavorite: Boolean = false,
        reminderEnabled: Boolean = false,
        notes: String?
    ): Long {
        val now = System.currentTimeMillis()
        return courseDao.insertCourse(
            CourseEntity(
                name = name.trim(),
                professor = professor.trim(),
                examDate = examDate,
                credits = credits,
                status = status.name,
                isFavorite = isFavorite,
                reminderEnabled = reminderEnabled,
                classroom = null,
                notes = notes?.trim()?.takeIf { it.isNotEmpty() },
                createdAt = now,
                updatedAt = now
            )
        )
    }

    suspend fun updateCourse(
        course: CourseEntity,
        name: String,
        professor: String,
        examDate: Long?,
        credits: Int,
        status: CourseStatus,
        isFavorite: Boolean = course.isFavorite,
        reminderEnabled: Boolean = course.reminderEnabled,
        notes: String?
    ) {
        courseDao.updateCourse(
            course.copy(
                name = name.trim(),
                professor = professor.trim(),
                examDate = examDate,
                credits = credits,
                status = status.name,
                isFavorite = isFavorite,
                reminderEnabled = reminderEnabled,
                classroom = course.classroom?.trim()?.takeIf { it.isNotEmpty() },
                notes = notes?.trim()?.takeIf { it.isNotEmpty() },
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun updateCourse(course: CourseEntity) {
        courseDao.updateCourse(
            course.copy(
                name = course.name.trim(),
                professor = course.professor.trim(),
                classroom = course.classroom?.trim()?.takeIf { it.isNotEmpty() },
                notes = course.notes?.trim()?.takeIf { it.isNotEmpty() },
                externalId = course.externalId?.trim()?.takeIf { it.isNotEmpty() },
                sourceProvider = course.sourceProvider?.trim()?.takeIf { it.isNotEmpty() },
                officialUrl = course.officialUrl?.trim()?.takeIf { it.isNotEmpty() },
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun upsertImportedCourse(
        provider: String,
        externalId: String,
        course: CourseEntity
    ): ImportedUpsertResult {
        val now = System.currentTimeMillis()
        val existing = courseDao.getCourseByExternalId(provider, externalId)
        val normalizedCourse = course.copy(
            name = course.name.trim(),
            professor = course.professor.trim().ifBlank { "Docente non indicato" },
            classroom = course.classroom?.trim()?.takeIf { it.isNotEmpty() },
            notes = course.notes?.trim()?.takeIf { it.isNotEmpty() },
            externalId = externalId.trim(),
            sourceProvider = provider,
            officialUrl = course.officialUrl?.trim()?.takeIf { it.isNotEmpty() },
            updatedAt = now
        )

        return if (existing == null) {
            val insertedId = courseDao.insertCourse(
                normalizedCourse.copy(
                    id = 0,
                    createdAt = now
                )
            ).toInt()
            ImportedUpsertResult(id = insertedId, inserted = true)
        } else {
            courseDao.updateCourse(
                normalizedCourse.copy(
                    id = existing.id,
                    examDate = existing.examDate,
                    status = existing.status,
                    isFavorite = existing.isFavorite,
                    reminderEnabled = existing.reminderEnabled,
                    createdAt = existing.createdAt
                )
            )
            ImportedUpsertResult(id = existing.id, inserted = false)
        }
    }

    suspend fun deleteCourse(course: CourseEntity) {
        courseDao.deleteCourse(course)
    }

    suspend fun deleteCourseById(courseId: Int) {
        courseDao.deleteCourseById(courseId)
    }

    suspend fun updateFavorite(
        courseId: Int,
        isFavorite: Boolean
    ) {
        courseDao.updateFavorite(
            courseId = courseId,
            isFavorite = isFavorite
        )
    }

    suspend fun updateReminderEnabled(
        courseId: Int,
        enabled: Boolean
    ) {
        courseDao.updateReminderEnabled(
            courseId = courseId,
            enabled = enabled
        )
    }

    fun getCompletedCoursesCount(): Flow<Int> =
        courseDao.getCompletedCoursesCount()

    fun getTotalCoursesCount(): Flow<Int> =
        courseDao.getTotalCoursesCount()

    fun getTotalCredits(): Flow<Int?> =
        courseDao.getTotalCredits()

    fun getCompletedCredits(): Flow<Int?> =
        courseDao.getCompletedCredits()

    suspend fun deleteAllCourses() {
        courseDao.deleteAllCourses()
    }

    suspend fun getCoursesBySourceProvider(provider: String): List<CourseEntity> =
        courseDao.getCoursesBySourceProvider(provider)

    suspend fun deleteCoursesBySourceProvider(provider: String) {
        courseDao.deleteCoursesBySourceProvider(provider)
    }
}

data class ImportedUpsertResult(
    val id: Int,
    val inserted: Boolean
)
