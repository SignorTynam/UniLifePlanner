package com.example.unilifeplanner.ui.exams

import com.example.unilifeplanner.domain.exams.ExamFeedbackResult

data class ExamFeedbackUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val examAppealId: Int = 0,
    val courseId: Int = 0,
    val courseName: String = "",
    val dateTimeLabel: String = "",
    val location: String? = null,
    val selectedResult: ExamFeedbackResult = ExamFeedbackResult.PASSED,
    val grade: String = "",
    val gradeError: String? = null,
    val notes: String = "",
    val markCourseCompleted: Boolean = true,
    val errorMessage: String? = null,
    val saveSuccess: Boolean = false,
    val completionMessage: String? = null
)
