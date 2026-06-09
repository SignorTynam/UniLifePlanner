package com.example.unilifeplanner.notifications

import com.example.unilifeplanner.domain.exams.examDateToStartOfDayMillis
import com.example.unilifeplanner.domain.exams.examStartMillis
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Test

class ExamReminderSchedulerTest {
    private val zoneId = ZoneId.systemDefault()

    @Test
    fun postExamFeedbackTriggerMillis_withTime_schedulesFiveHoursAfterExamStart() {
        val dateMillis = examDateToStartOfDayMillis(LocalDate.of(2026, 6, 25), zoneId)
        val timeMinutes = 9 * 60

        val trigger = postExamFeedbackTriggerMillis(dateMillis, timeMinutes)

        assertEquals(
            examStartMillis(dateMillis, timeMinutes, zoneId) + 5 * 60 * 60 * 1000L,
            trigger
        )
    }

    @Test
    fun postExamFeedbackTriggerMillis_withoutTime_schedulesAtSixPm() {
        val dateMillis = examDateToStartOfDayMillis(LocalDate.of(2026, 6, 25), zoneId)

        val trigger = postExamFeedbackTriggerMillis(dateMillis, null)

        assertEquals(dateMillis + 18 * 60 * 60 * 1000L, trigger)
    }
}
