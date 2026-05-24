package com.example.unilifeplanner.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.unilifeplanner.data.local.AppDatabase
import kotlinx.coroutines.flow.first

class LessonReminderWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val lessonId = inputData.getInt(KEY_LESSON_ID, -1)
        val courseId = inputData.getInt(KEY_COURSE_ID, -1)
        val courseName = inputData.getString(KEY_COURSE_NAME).orEmpty()
        val dayOfWeek = inputData.getInt(KEY_DAY_OF_WEEK, -1)
        val startTimeMinutes = inputData.getInt(KEY_START_TIME_MINUTES, -1)
        val classroom = inputData.getString(KEY_CLASSROOM)

        if (
            lessonId <= 0 ||
            courseId <= 0 ||
            courseName.isBlank() ||
            dayOfWeek !in 1..7 ||
            startTimeMinutes !in 0 until 24 * 60
        ) {
            return Result.success()
        }

        NotificationHelper.showLessonReminderNotification(
            context = applicationContext,
            courseId = courseId,
            lessonId = lessonId,
            courseName = courseName,
            dayOfWeek = dayOfWeek,
            startTimeMinutes = startTimeMinutes,
            classroom = classroom
        )

        val database = AppDatabase.getDatabase(applicationContext)
        val lesson = database.lessonDao().getLessonById(lessonId).first()
        val course = database.courseDao().getCourseById(courseId).first()

        if (lesson != null && course != null && lesson.reminderEnabled) {
            LessonReminderScheduler(applicationContext).scheduleLessonReminder(
                lessonId = lesson.id,
                courseId = lesson.courseId,
                courseName = course.name,
                dateMillis = lesson.dateMillis,
                dayOfWeek = lesson.dayOfWeek,
                startTimeMinutes = lesson.startTimeMinutes,
                classroom = lesson.classroom
            )
        }

        return Result.success()
    }

    companion object {
        const val KEY_LESSON_ID = "lesson_id"
        const val KEY_COURSE_ID = "course_id"
        const val KEY_COURSE_NAME = "course_name"
        const val KEY_DATE_MILLIS = "date_millis"
        const val KEY_DAY_OF_WEEK = "day_of_week"
        const val KEY_START_TIME_MINUTES = "start_time_minutes"
        const val KEY_CLASSROOM = "classroom"
    }
}
