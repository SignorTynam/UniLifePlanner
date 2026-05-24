package com.example.unilifeplanner.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ExamAppealDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExamAppeal(examAppeal: ExamAppealEntity): Long

    @Update
    suspend fun updateExamAppeal(examAppeal: ExamAppealEntity)

    @Delete
    suspend fun deleteExamAppeal(examAppeal: ExamAppealEntity)

    @Query("DELETE FROM exam_appeals WHERE id = :examAppealId")
    suspend fun deleteExamAppealById(examAppealId: Int)

    @Query("SELECT * FROM exam_appeals WHERE id = :examAppealId")
    fun getExamAppealById(examAppealId: Int): Flow<ExamAppealEntity?>

    @Query(
        """
        SELECT * FROM exam_appeals
        WHERE source = :source
          AND externalId = :externalId
        LIMIT 1
        """
    )
    suspend fun getExamAppealByExternalId(
        source: String,
        externalId: String
    ): ExamAppealEntity?

    @Query(
        """
        SELECT * FROM exam_appeals
        WHERE courseId = :courseId
        ORDER BY dateMillis ASC, timeMinutes IS NULL ASC, timeMinutes ASC
        """
    )
    fun getExamAppealsForCourse(courseId: Int): Flow<List<ExamAppealEntity>>

    @Query(
        """
        SELECT exam_appeals.*, courses.name AS courseName, courses.professor AS courseProfessor
        FROM exam_appeals
        INNER JOIN courses ON exam_appeals.courseId = courses.id
        ORDER BY exam_appeals.dateMillis ASC, exam_appeals.timeMinutes IS NULL ASC, exam_appeals.timeMinutes ASC
        """
    )
    fun getExamAppealsWithCourse(): Flow<List<ExamAppealWithCourse>>

    @Query(
        """
        SELECT exam_appeals.*, courses.name AS courseName, courses.professor AS courseProfessor
        FROM exam_appeals
        INNER JOIN courses ON exam_appeals.courseId = courses.id
        WHERE exam_appeals.courseId = :courseId
        ORDER BY exam_appeals.dateMillis ASC, exam_appeals.timeMinutes IS NULL ASC, exam_appeals.timeMinutes ASC
        """
    )
    fun getExamAppealsWithCourseForCourse(courseId: Int): Flow<List<ExamAppealWithCourse>>

    @Query(
        """
        SELECT exam_appeals.*, courses.name AS courseName, courses.professor AS courseProfessor
        FROM exam_appeals
        INNER JOIN courses ON exam_appeals.courseId = courses.id
        WHERE exam_appeals.id = :examAppealId
        LIMIT 1
        """
    )
    fun getExamAppealWithCourseById(examAppealId: Int): Flow<ExamAppealWithCourse?>

    @Query("SELECT * FROM exam_appeals WHERE reminderEnabled = 1")
    suspend fun getExamAppealsWithReminderEnabled(): List<ExamAppealEntity>

    @Query(
        """
        UPDATE exam_appeals
        SET reminderEnabled = :enabled,
            updatedAt = CAST(strftime('%s', 'now') AS INTEGER) * 1000
        WHERE id = :examAppealId
        """
    )
    suspend fun updateExamAppealReminderEnabled(
        examAppealId: Int,
        enabled: Boolean
    )

    @Query("DELETE FROM exam_appeals WHERE courseId = :courseId")
    suspend fun deleteExamAppealsForCourse(courseId: Int)

    @Transaction
    suspend fun upsertImportedExamAppeal(
        source: String,
        externalId: String,
        examAppeal: ExamAppealEntity
    ): Long {
        val existing = getExamAppealByExternalId(source, externalId)
        return if (existing == null) {
            insertExamAppeal(
                examAppeal.copy(
                    id = 0,
                    source = source,
                    externalId = externalId
                )
            )
        } else {
            updateExamAppeal(
                examAppeal.copy(
                    id = existing.id,
                    source = source,
                    externalId = externalId,
                    reminderEnabled = existing.reminderEnabled,
                    createdAt = existing.createdAt
                )
            )
            existing.id.toLong()
        }
    }
}
