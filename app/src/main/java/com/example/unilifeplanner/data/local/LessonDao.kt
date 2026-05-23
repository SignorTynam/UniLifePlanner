package com.example.unilifeplanner.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface LessonDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLesson(lesson: LessonEntity): Long

    @Update
    suspend fun updateLesson(lesson: LessonEntity)

    @Delete
    suspend fun deleteLesson(lesson: LessonEntity)

    @Query("DELETE FROM lessons WHERE id = :lessonId")
    suspend fun deleteLessonById(lessonId: Int)

    @Query(
        """
        SELECT * FROM lessons
        WHERE courseId = :courseId
        ORDER BY dayOfWeek ASC, startTimeMinutes ASC
        """
    )
    fun getLessonsForCourse(courseId: Int): Flow<List<LessonEntity>>

    @Query("SELECT * FROM lessons WHERE id = :lessonId")
    fun getLessonById(lessonId: Int): Flow<LessonEntity?>

    @Query("SELECT * FROM lessons ORDER BY dayOfWeek ASC, startTimeMinutes ASC")
    fun getAllLessons(): Flow<List<LessonEntity>>

    @Query(
        """
        SELECT lessons.*, courses.name AS courseName, courses.professor AS courseProfessor
        FROM lessons
        INNER JOIN courses ON lessons.courseId = courses.id
        ORDER BY lessons.dayOfWeek ASC, lessons.startTimeMinutes ASC
        """
    )
    fun getLessonsWithCourse(): Flow<List<LessonWithCourse>>

    @Query(
        """
        SELECT lessons.*, courses.name AS courseName, courses.professor AS courseProfessor
        FROM lessons
        INNER JOIN courses ON lessons.courseId = courses.id
        WHERE lessons.courseId = :courseId
        ORDER BY lessons.dayOfWeek ASC, lessons.startTimeMinutes ASC
        """
    )
    fun getLessonsWithCourseForCourse(courseId: Int): Flow<List<LessonWithCourse>>

    @Query("SELECT * FROM lessons WHERE reminderEnabled = 1")
    suspend fun getLessonsWithReminderEnabled(): List<LessonEntity>

    @Query(
        """
        UPDATE lessons
        SET reminderEnabled = :enabled,
            updatedAt = CAST(strftime('%s', 'now') AS INTEGER) * 1000
        WHERE id = :lessonId
        """
    )
    suspend fun updateLessonReminderEnabled(
        lessonId: Int,
        enabled: Boolean
    )

    @Query("DELETE FROM lessons WHERE courseId = :courseId")
    suspend fun deleteLessonsForCourse(courseId: Int)
}
