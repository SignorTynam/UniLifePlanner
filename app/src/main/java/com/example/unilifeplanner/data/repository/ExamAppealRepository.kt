package com.example.unilifeplanner.data.repository

import com.example.unilifeplanner.data.local.ExamAppealDao
import com.example.unilifeplanner.data.local.ExamAppealEntity
import com.example.unilifeplanner.data.local.ExamAppealWithCourse
import com.example.unilifeplanner.domain.exams.ExamFeedbackResult
import com.example.unilifeplanner.domain.exams.ExamFeedbackStatus
import kotlinx.coroutines.flow.Flow

class ExamAppealRepository(
    private val examAppealDao: ExamAppealDao
) {
    fun getExamAppealsWithCourse(): Flow<List<ExamAppealWithCourse>> =
        examAppealDao.getExamAppealsWithCourse()

    fun getExamAppealsWithCourseForCourse(courseId: Int): Flow<List<ExamAppealWithCourse>> =
        examAppealDao.getExamAppealsWithCourseForCourse(courseId)

    fun getExamAppealsForCourse(courseId: Int): Flow<List<ExamAppealEntity>> =
        examAppealDao.getExamAppealsForCourse(courseId)

    fun getExamAppealById(examAppealId: Int): Flow<ExamAppealEntity?> =
        examAppealDao.getExamAppealById(examAppealId)

    fun getExamAppealWithCourseById(examAppealId: Int): Flow<ExamAppealWithCourse?> =
        examAppealDao.getExamAppealWithCourseById(examAppealId)

    suspend fun insertExamAppeal(examAppeal: ExamAppealEntity): Long {
        val now = System.currentTimeMillis()
        return examAppealDao.insertExamAppeal(
            examAppeal.copy(
                id = 0,
                location = examAppeal.location?.trim()?.takeIf { it.isNotEmpty() },
                notes = examAppeal.notes?.trim()?.takeIf { it.isNotEmpty() },
                type = examAppeal.type?.trim()?.takeIf { it.isNotEmpty() },
                externalId = examAppeal.externalId?.trim()?.takeIf { it.isNotEmpty() },
                officialUrl = examAppeal.officialUrl?.trim()?.takeIf { it.isNotEmpty() },
                createdAt = now,
                updatedAt = now
            )
        )
    }

    suspend fun updateExamAppeal(examAppeal: ExamAppealEntity) {
        examAppealDao.updateExamAppeal(
            examAppeal.copy(
                location = examAppeal.location?.trim()?.takeIf { it.isNotEmpty() },
                notes = examAppeal.notes?.trim()?.takeIf { it.isNotEmpty() },
                type = examAppeal.type?.trim()?.takeIf { it.isNotEmpty() },
                externalId = examAppeal.externalId?.trim()?.takeIf { it.isNotEmpty() },
                officialUrl = examAppeal.officialUrl?.trim()?.takeIf { it.isNotEmpty() },
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun deleteExamAppeal(examAppeal: ExamAppealEntity) {
        examAppealDao.deleteExamAppeal(examAppeal)
    }

    suspend fun deleteExamAppealById(examAppealId: Int) {
        examAppealDao.deleteExamAppealById(examAppealId)
    }

    suspend fun updateExamAppealReminderEnabled(
        examAppealId: Int,
        enabled: Boolean
    ) {
        examAppealDao.updateExamAppealReminderEnabled(
            examAppealId = examAppealId,
            enabled = enabled
        )
    }

    suspend fun getExamAppealsWithReminderEnabled(): List<ExamAppealEntity> =
        examAppealDao.getExamAppealsWithReminderEnabled()

    suspend fun getExamAppealsForCourseOnce(courseId: Int): List<ExamAppealEntity> =
        examAppealDao.getExamAppealsForCourseOnce(courseId)

    suspend fun disableExamAppealRemindersForCourse(courseId: Int) {
        examAppealDao.disableExamAppealRemindersForCourse(courseId)
    }

    suspend fun deleteExamAppealsForCourse(courseId: Int) {
        examAppealDao.deleteExamAppealsForCourse(courseId)
    }

    suspend fun getImportedExamAppealsForCourseIds(
        source: String,
        courseIds: List<Int>
    ): List<ExamAppealEntity> {
        if (courseIds.isEmpty()) return emptyList()
        return examAppealDao.getImportedExamAppealsForCourseIds(source, courseIds)
    }

    suspend fun deleteExamAppealsByIds(ids: List<Int>) {
        if (ids.isEmpty()) return
        examAppealDao.deleteExamAppealsByIds(ids)
    }

    suspend fun upsertImportedExamAppeal(
        source: String,
        externalId: String,
        examAppeal: ExamAppealEntity
    ): ImportedUpsertResult {
        val existing = examAppealDao.getExamAppealByExternalId(source, externalId)
        val now = System.currentTimeMillis()
        val normalized = examAppeal.copy(
            source = source,
            externalId = externalId.trim(),
            location = examAppeal.location?.trim()?.takeIf { it.isNotEmpty() },
            notes = examAppeal.notes?.trim()?.takeIf { it.isNotEmpty() },
            type = examAppeal.type?.trim()?.takeIf { it.isNotEmpty() },
            officialUrl = examAppeal.officialUrl?.trim()?.takeIf { it.isNotEmpty() },
            updatedAt = now
        )

        return if (existing == null) {
            val insertedId = examAppealDao.insertExamAppeal(
                normalized.copy(
                    id = 0,
                    createdAt = now
                )
            ).toInt()
            ImportedUpsertResult(id = insertedId, inserted = true)
        } else {
            examAppealDao.updateExamAppeal(
                normalized.copy(
                    id = existing.id,
                    reminderEnabled = existing.reminderEnabled,
                    reminderDateTimeMillis = existing.reminderDateTimeMillis,
                    feedbackStatus = existing.feedbackStatus,
                    feedbackResult = existing.feedbackResult,
                    feedbackGrade = existing.feedbackGrade,
                    feedbackNotes = existing.feedbackNotes,
                    feedbackAskedAtMillis = existing.feedbackAskedAtMillis,
                    feedbackAnsweredAtMillis = existing.feedbackAnsweredAtMillis,
                    createdAt = existing.createdAt
                )
            )
            ImportedUpsertResult(id = existing.id, inserted = false)
        }
    }

    suspend fun markFeedbackScheduled(examAppealId: Int) {
        examAppealDao.updateFeedbackScheduled(
            examAppealId = examAppealId,
            status = ExamFeedbackStatus.SCHEDULED.name
        )
    }

    suspend fun markFeedbackPending(examAppealId: Int) {
        examAppealDao.markFeedbackPending(
            examAppealId = examAppealId,
            status = ExamFeedbackStatus.PENDING.name,
            askedAtMillis = System.currentTimeMillis()
        )
    }

    suspend fun saveExamFeedback(
        examAppealId: Int,
        result: ExamFeedbackResult,
        grade: String?,
        notes: String?
    ) {
        examAppealDao.saveFeedback(
            examAppealId = examAppealId,
            status = ExamFeedbackStatus.ANSWERED.name,
            result = result.name,
            grade = grade?.trim()?.takeIf { result == ExamFeedbackResult.PASSED && it.isNotEmpty() },
            notes = notes?.trim()?.takeIf { it.isNotEmpty() },
            answeredAtMillis = System.currentTimeMillis()
        )
    }

    suspend fun dismissExamFeedback(examAppealId: Int) {
        examAppealDao.dismissFeedback(
            examAppealId = examAppealId,
            status = ExamFeedbackStatus.DISMISSED.name,
            dismissedAtMillis = System.currentTimeMillis()
        )
    }

}
