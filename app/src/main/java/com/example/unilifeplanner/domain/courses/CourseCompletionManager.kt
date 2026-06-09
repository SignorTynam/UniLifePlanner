package com.example.unilifeplanner.domain.courses

import com.example.unilifeplanner.data.repository.ExamAppealRepository
import com.example.unilifeplanner.data.repository.LessonRepository
import com.example.unilifeplanner.notifications.ExamReminderScheduler
import com.example.unilifeplanner.notifications.LessonReminderScheduler

class CourseCompletionManager(
    private val lessonRepository: LessonRepository,
    private val examAppealRepository: ExamAppealRepository,
    private val lessonReminderScheduler: LessonReminderScheduler,
    private val examReminderScheduler: ExamReminderScheduler
) {
    suspend fun disableFutureRemindersForCompletedCourse(courseId: Int) {
        lessonRepository.getLessonsForCourseOnce(courseId).forEach { lesson ->
            lessonReminderScheduler.cancelLessonReminder(lesson.id)
        }
        lessonRepository.disableLessonRemindersForCourse(courseId)

        examAppealRepository.getExamAppealsForCourseOnce(courseId).forEach { exam ->
            examReminderScheduler.cancelExamAppealReminders(exam.id)
        }
        examAppealRepository.disableExamAppealRemindersForCourse(courseId)
    }
}
