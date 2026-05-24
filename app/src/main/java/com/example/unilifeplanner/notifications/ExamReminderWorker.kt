package com.example.unilifeplanner.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class ExamReminderWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val examAppealId = inputData.getInt(KEY_EXAM_APPEAL_ID, -1)
        val courseId = inputData.getInt(KEY_COURSE_ID, -1)
        val courseName = inputData.getString(KEY_COURSE_NAME).orEmpty()
        val examDateText = inputData.getString(KEY_EXAM_DATE_TEXT).orEmpty()
        val reminderTypeName = inputData.getString(KEY_REMINDER_TYPE).orEmpty()
        val reminderType = runCatching {
            ExamReminderType.valueOf(reminderTypeName)
        }.getOrNull()

        if (examAppealId <= 0 || courseId <= 0 || courseName.isBlank() || reminderType == null) {
            return Result.success()
        }

        NotificationHelper.showExamReminderNotification(
            context = applicationContext,
            examAppealId = examAppealId,
            courseId = courseId,
            courseName = courseName,
            examDateText = examDateText,
            reminderType = reminderType
        )

        return Result.success()
    }

    companion object {
        const val KEY_EXAM_APPEAL_ID = "exam_appeal_id"
        const val KEY_COURSE_ID = "course_id"
        const val KEY_COURSE_NAME = "course_name"
        const val KEY_EXAM_DATE_TEXT = "exam_date_text"
        const val KEY_REMINDER_TYPE = "reminder_type"
    }
}
