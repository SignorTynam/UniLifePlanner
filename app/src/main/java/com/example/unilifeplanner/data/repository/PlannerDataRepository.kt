package com.example.unilifeplanner.data.repository

import androidx.room.withTransaction
import com.example.unilifeplanner.data.local.AppDatabase
import com.example.unilifeplanner.notifications.ExamReminderScheduler
import com.example.unilifeplanner.notifications.LessonReminderScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PlannerDataRepository(
    private val database: AppDatabase,
    private val lessonReminderScheduler: LessonReminderScheduler,
    private val examReminderScheduler: ExamReminderScheduler
) {
    suspend fun clearPlannerData() = withContext(Dispatchers.IO) {
        val lessonDao = database.lessonDao()
        val examAppealDao = database.examAppealDao()
        val courseDao = database.courseDao()

        lessonDao.getLessonsWithReminderEnabled().forEach { lesson ->
            lessonReminderScheduler.cancelLessonReminder(lesson.id)
        }
        examAppealDao.getExamAppealsWithReminderEnabled().forEach { examAppeal ->
            examReminderScheduler.cancelExamAppealReminders(examAppeal.id)
        }
        courseDao.getAllCoursesOnce()
            .filter { it.examDate != null }
            .forEach { course ->
                examReminderScheduler.cancelLegacyCourseExamReminders(course.id)
            }

        database.withTransaction {
            examAppealDao.deleteAllExamAppeals()
            lessonDao.deleteAllLessons()
            courseDao.deleteAllCourses()
        }
    }
}
