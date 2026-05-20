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
                createdAt = now,
                updatedAt = now
            )
        )
    }

    suspend fun insertCourse(
        name: String,
        professor: String,
        examDate: Long?,
        classroom: String?,
        credits: Int,
        status: CourseStatus,
        isFavorite: Boolean = false,
        notes: String?
    ): Long {
        val now = System.currentTimeMillis()
        return courseDao.insertCourse(
            CourseEntity(
                name = name.trim(),
                professor = professor.trim(),
                examDate = examDate,
                classroom = classroom?.trim()?.takeIf { it.isNotEmpty() },
                credits = credits,
                status = status.name,
                isFavorite = isFavorite,
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
        classroom: String?,
        credits: Int,
        status: CourseStatus,
        isFavorite: Boolean = course.isFavorite,
        notes: String?
    ) {
        courseDao.updateCourse(
            course.copy(
                name = name.trim(),
                professor = professor.trim(),
                examDate = examDate,
                classroom = classroom?.trim()?.takeIf { it.isNotEmpty() },
                credits = credits,
                status = status.name,
                isFavorite = isFavorite,
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
                updatedAt = System.currentTimeMillis()
            )
        )
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
}
