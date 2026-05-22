package com.example.unilifeplanner.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class ExamReminderWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val courseId = inputData.getInt(KEY_COURSE_ID, -1)
        val courseName = inputData.getString(KEY_COURSE_NAME).orEmpty()
        val reminderTypeName = inputData.getString(KEY_REMINDER_TYPE).orEmpty()
        val reminderType = runCatching {
            ExamReminderType.valueOf(reminderTypeName)
        }.getOrNull()

        if (courseId <= 0 || courseName.isBlank() || reminderType == null) {
            return Result.success()
        }

        NotificationHelper.showExamReminderNotification(
            context = applicationContext,
            courseId = courseId,
            courseName = courseName,
            reminderType = reminderType
        )

        return Result.success()
    }

    companion object {
        const val KEY_COURSE_ID = "course_id"
        const val KEY_COURSE_NAME = "course_name"
        const val KEY_REMINDER_TYPE = "reminder_type"
    }
}
