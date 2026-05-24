package com.example.unilifeplanner.notifications

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.unilifeplanner.domain.exams.examStartMillis
import com.example.unilifeplanner.domain.exams.formatExamDateTime
import java.util.concurrent.TimeUnit

class ExamReminderScheduler(
    context: Context
) {
    private val appContext = context.applicationContext
    private val workManager = WorkManager.getInstance(appContext)

    fun scheduleExamAppealReminders(
        examAppealId: Int,
        courseId: Int,
        courseName: String,
        examDateMillis: Long,
        timeMinutes: Int?,
        reminderDateTimeMillis: Long? = null
    ) {
        cancelExamAppealReminders(examAppealId)

        if (reminderDateTimeMillis != null) {
            scheduleReminder(
                examAppealId = examAppealId,
                courseId = courseId,
                courseName = courseName,
                examDateText = formatExamDateTime(examDateMillis, timeMinutes),
                triggerAtMillis = reminderDateTimeMillis,
                reminderType = ExamReminderType.CUSTOM
            )
            return
        }

        val examStartMillis = examStartMillis(
            dateMillis = examDateMillis,
            timeMinutes = timeMinutes
        )
        val sameDayTriggerAtMillis = if (timeMinutes == null) {
            examDateMillis + DEFAULT_SAME_DAY_HOUR_MILLIS
        } else {
            examStartMillis
        }

        scheduleReminder(
            examAppealId = examAppealId,
            courseId = courseId,
            courseName = courseName,
            examDateText = formatExamDateTime(examDateMillis, timeMinutes),
            triggerAtMillis = sameDayTriggerAtMillis - ONE_DAY_MILLIS,
            reminderType = ExamReminderType.DAY_BEFORE
        )
        scheduleReminder(
            examAppealId = examAppealId,
            courseId = courseId,
            courseName = courseName,
            examDateText = formatExamDateTime(examDateMillis, timeMinutes),
            triggerAtMillis = sameDayTriggerAtMillis,
            reminderType = ExamReminderType.SAME_DAY
        )
    }

    fun cancelExamAppealReminders(examAppealId: Int) {
        workManager.cancelUniqueWork(workName(examAppealId, ExamReminderType.DAY_BEFORE))
        workManager.cancelUniqueWork(workName(examAppealId, ExamReminderType.SAME_DAY))
        workManager.cancelUniqueWork(workName(examAppealId, ExamReminderType.CUSTOM))
    }

    fun rescheduleExamAppealReminders(
        examAppealId: Int,
        courseId: Int,
        courseName: String,
        examDateMillis: Long,
        timeMinutes: Int?,
        reminderDateTimeMillis: Long? = null
    ) {
        cancelExamAppealReminders(examAppealId)
        scheduleExamAppealReminders(
            examAppealId = examAppealId,
            courseId = courseId,
            courseName = courseName,
            examDateMillis = examDateMillis,
            timeMinutes = timeMinutes,
            reminderDateTimeMillis = reminderDateTimeMillis
        )
    }

    fun cancelLegacyCourseExamReminders(courseId: Int) {
        workManager.cancelUniqueWork(legacyWorkName(courseId, ExamReminderType.DAY_BEFORE))
        workManager.cancelUniqueWork(legacyWorkName(courseId, ExamReminderType.SAME_DAY))
    }

    private fun scheduleReminder(
        examAppealId: Int,
        courseId: Int,
        courseName: String,
        examDateText: String,
        triggerAtMillis: Long,
        reminderType: ExamReminderType
    ) {
        val delay = triggerAtMillis - System.currentTimeMillis()
        if (delay <= 0L) {
            workManager.cancelUniqueWork(workName(examAppealId, reminderType))
            return
        }

        val request = OneTimeWorkRequestBuilder<ExamReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(
                workDataOf(
                    ExamReminderWorker.KEY_EXAM_APPEAL_ID to examAppealId,
                    ExamReminderWorker.KEY_COURSE_ID to courseId,
                    ExamReminderWorker.KEY_COURSE_NAME to courseName,
                    ExamReminderWorker.KEY_EXAM_DATE_TEXT to examDateText,
                    ExamReminderWorker.KEY_REMINDER_TYPE to reminderType.name
                )
            )
            .build()

        workManager.enqueueUniqueWork(
            workName(examAppealId, reminderType),
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    private fun workName(
        examAppealId: Int,
        reminderType: ExamReminderType
    ): String {
        val suffix = when (reminderType) {
            ExamReminderType.DAY_BEFORE -> "day_before"
            ExamReminderType.SAME_DAY -> "same_day"
            ExamReminderType.CUSTOM -> "custom"
        }

        return "exam_appeal_reminder_${examAppealId}_$suffix"
    }

    private fun legacyWorkName(
        courseId: Int,
        reminderType: ExamReminderType
    ): String {
        val suffix = when (reminderType) {
            ExamReminderType.DAY_BEFORE -> "day_before"
            ExamReminderType.SAME_DAY -> "same_day"
            ExamReminderType.CUSTOM -> "custom"
        }

        return "exam_reminder_${courseId}_$suffix"
    }

    private companion object {
        const val ONE_DAY_MILLIS = 24 * 60 * 60 * 1000L
        const val DEFAULT_SAME_DAY_HOUR_MILLIS = 8 * 60 * 60 * 1000L
    }
}
