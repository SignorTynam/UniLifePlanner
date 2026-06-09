package com.example.unilifeplanner.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.unilifeplanner.data.local.AppDatabase
import com.example.unilifeplanner.data.repository.ExamAppealRepository
import com.example.unilifeplanner.domain.exams.ExamFeedbackStatus
import com.example.unilifeplanner.domain.exams.examStartMillis
import kotlinx.coroutines.flow.first

class PostExamFeedbackWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val examAppealId = inputData.getInt(KEY_EXAM_APPEAL_ID, -1)
        val courseId = inputData.getInt(KEY_COURSE_ID, -1)
        val courseName = inputData.getString(KEY_COURSE_NAME).orEmpty()
        if (examAppealId <= 0 || courseId <= 0 || courseName.isBlank()) {
            return Result.success()
        }

        val repository = ExamAppealRepository(
            AppDatabase.getDatabase(applicationContext).examAppealDao()
        )
        val exam = repository.getExamAppealById(examAppealId).first()
            ?: return Result.success()
        val status = runCatching {
            ExamFeedbackStatus.valueOf(exam.feedbackStatus)
        }.getOrDefault(ExamFeedbackStatus.NOT_REQUESTED)
        if (!exam.reminderEnabled ||
            status == ExamFeedbackStatus.ANSWERED ||
            status == ExamFeedbackStatus.DISMISSED
        ) {
            return Result.success()
        }
        val startMillis = examStartMillis(
            dateMillis = exam.dateMillis,
            timeMinutes = exam.timeMinutes
        )
        if (startMillis > System.currentTimeMillis()) return Result.success()

        repository.markFeedbackPending(examAppealId)
        NotificationHelper.showPostExamFeedbackNotification(
            context = applicationContext,
            examAppealId = examAppealId,
            courseId = courseId,
            courseName = courseName
        )
        return Result.success()
    }

    companion object {
        const val KEY_EXAM_APPEAL_ID = "exam_appeal_id"
        const val KEY_COURSE_ID = "course_id"
        const val KEY_COURSE_NAME = "course_name"
    }
}
