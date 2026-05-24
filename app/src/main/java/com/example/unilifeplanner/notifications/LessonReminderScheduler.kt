package com.example.unilifeplanner.notifications

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.unilifeplanner.domain.lessons.nextLessonReminderAtMillis
import java.util.concurrent.TimeUnit

class LessonReminderScheduler(
    context: Context
) {
    private val appContext = context.applicationContext
    private val workManager = WorkManager.getInstance(appContext)

    fun scheduleLessonReminder(
        lessonId: Int,
        courseId: Int,
        courseName: String,
        dateMillis: Long?,
        dayOfWeek: Int,
        startTimeMinutes: Int,
        classroom: String?
    ) {
        val triggerAtMillis = nextLessonReminderAtMillis(
            dateMillis = dateMillis,
            dayOfWeek = dayOfWeek,
            startTimeMinutes = startTimeMinutes
        )
        val delay = triggerAtMillis - System.currentTimeMillis()
        if (delay <= 0L) {
            cancelLessonReminder(lessonId)
            return
        }

        val request = OneTimeWorkRequestBuilder<LessonReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(
                workDataOf(
                    LessonReminderWorker.KEY_LESSON_ID to lessonId,
                    LessonReminderWorker.KEY_COURSE_ID to courseId,
                    LessonReminderWorker.KEY_COURSE_NAME to courseName,
                    LessonReminderWorker.KEY_DATE_MILLIS to (dateMillis ?: -1L),
                    LessonReminderWorker.KEY_DAY_OF_WEEK to dayOfWeek,
                    LessonReminderWorker.KEY_START_TIME_MINUTES to startTimeMinutes,
                    LessonReminderWorker.KEY_CLASSROOM to classroom
                )
            )
            .build()

        workManager.enqueueUniqueWork(
            workName(lessonId),
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    fun cancelLessonReminder(lessonId: Int) {
        workManager.cancelUniqueWork(workName(lessonId))
    }

    fun rescheduleLessonReminder(
        lessonId: Int,
        courseId: Int,
        courseName: String,
        dateMillis: Long?,
        dayOfWeek: Int,
        startTimeMinutes: Int,
        classroom: String?
    ) {
        cancelLessonReminder(lessonId)
        scheduleLessonReminder(
            lessonId = lessonId,
            courseId = courseId,
            courseName = courseName,
            dateMillis = dateMillis,
            dayOfWeek = dayOfWeek,
            startTimeMinutes = startTimeMinutes,
            classroom = classroom
        )
    }

    private fun workName(lessonId: Int): String = "lesson_reminder_$lessonId"
}
