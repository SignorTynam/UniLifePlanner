package com.example.unilifeplanner.notifications

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.util.concurrent.TimeUnit

class ExamReminderScheduler(
    context: Context
) {
    private val appContext = context.applicationContext
    private val workManager = WorkManager.getInstance(appContext)

    fun scheduleExamReminders(
        courseId: Int,
        courseName: String,
        examDate: Long
    ) {
        cancelExamReminders(courseId)
        scheduleReminder(
            courseId = courseId,
            courseName = courseName,
            triggerAtMillis = examDate - ONE_DAY_MILLIS,
            reminderType = ExamReminderType.DAY_BEFORE
        )
        scheduleReminder(
            courseId = courseId,
            courseName = courseName,
            triggerAtMillis = examDate,
            reminderType = ExamReminderType.SAME_DAY
        )
    }

    fun cancelExamReminders(courseId: Int) {
        workManager.cancelUniqueWork(workName(courseId, ExamReminderType.DAY_BEFORE))
        workManager.cancelUniqueWork(workName(courseId, ExamReminderType.SAME_DAY))
    }

    fun rescheduleExamReminders(
        courseId: Int,
        courseName: String,
        newExamDate: Long
    ) {
        cancelExamReminders(courseId)
        scheduleExamReminders(
            courseId = courseId,
            courseName = courseName,
            examDate = newExamDate
        )
    }

    private fun scheduleReminder(
        courseId: Int,
        courseName: String,
        triggerAtMillis: Long,
        reminderType: ExamReminderType
    ) {
        val delay = triggerAtMillis - System.currentTimeMillis()
        if (delay <= 0L) {
            workManager.cancelUniqueWork(workName(courseId, reminderType))
            return
        }

        val request = OneTimeWorkRequestBuilder<ExamReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(
                workDataOf(
                    ExamReminderWorker.KEY_COURSE_ID to courseId,
                    ExamReminderWorker.KEY_COURSE_NAME to courseName,
                    ExamReminderWorker.KEY_REMINDER_TYPE to reminderType.name
                )
            )
            .build()

        workManager.enqueueUniqueWork(
            workName(courseId, reminderType),
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    private fun workName(
        courseId: Int,
        reminderType: ExamReminderType
    ): String {
        val suffix = when (reminderType) {
            ExamReminderType.DAY_BEFORE -> "day_before"
            ExamReminderType.SAME_DAY -> "same_day"
        }

        return "exam_reminder_${courseId}_$suffix"
    }

    private companion object {
        const val ONE_DAY_MILLIS = 24 * 60 * 60 * 1000L
    }
}
