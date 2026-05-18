package com.example.unilifeplanner.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CourseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourse(course: CourseEntity): Long

    @Update
    suspend fun updateCourse(course: CourseEntity)

    @Delete
    suspend fun deleteCourse(course: CourseEntity)

    @Query("DELETE FROM courses WHERE id = :courseId")
    suspend fun deleteCourseById(courseId: Int)

    @Query("SELECT * FROM courses ORDER BY examDate IS NULL ASC, examDate ASC, name ASC")
    fun getAllCourses(): Flow<List<CourseEntity>>

    @Query("SELECT * FROM courses WHERE id = :courseId")
    fun getCourseById(courseId: Int): Flow<CourseEntity?>

    @Query(
        """
        SELECT * FROM courses
        WHERE name LIKE '%' || :query || '%'
           OR professor LIKE '%' || :query || '%'
        ORDER BY examDate IS NULL ASC, examDate ASC, name ASC
        """
    )
    fun searchCourses(query: String): Flow<List<CourseEntity>>

    @Query(
        """
        SELECT * FROM courses
        WHERE status = :status
        ORDER BY examDate IS NULL ASC, examDate ASC, name ASC
        """
    )
    fun getCoursesByStatus(status: String): Flow<List<CourseEntity>>

    @Query(
        """
        SELECT * FROM courses
        WHERE isFavorite = 1
        ORDER BY examDate IS NULL ASC, examDate ASC, name ASC
        """
    )
    fun getFavoriteCourses(): Flow<List<CourseEntity>>

    @Query(
        """
        UPDATE courses
        SET isFavorite = :isFavorite,
            updatedAt = CAST(strftime('%s', 'now') AS INTEGER) * 1000
        WHERE id = :courseId
        """
    )
    suspend fun updateFavorite(
        courseId: Int,
        isFavorite: Boolean
    )

    @Query("SELECT COUNT(*) FROM courses WHERE status = 'COMPLETED'")
    fun getCompletedCoursesCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM courses")
    fun getTotalCoursesCount(): Flow<Int>

    @Query("SELECT SUM(credits) FROM courses")
    fun getTotalCredits(): Flow<Int?>

    @Query("SELECT SUM(credits) FROM courses WHERE status = 'COMPLETED'")
    fun getCompletedCredits(): Flow<Int?>

    @Query("DELETE FROM courses")
    suspend fun deleteAllCourses()
}
