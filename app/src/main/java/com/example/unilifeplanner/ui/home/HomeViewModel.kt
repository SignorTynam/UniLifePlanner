package com.example.unilifeplanner.ui.home

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class HomeSummaryUiState(
    val studentName: String = "studente",
    val totalCourses: Int = 0,
    val completedCourses: Int = 0,
    val inProgressCourses: Int = 0,
    val toStudyCourses: Int = 0,
    val nextExam: NextExamUi? = null,
    val favoriteCourses: List<FavoriteCourseUi> = emptyList()
)

data class NextExamUi(
    val courseName: String,
    val examDate: String,
    val classroom: String,
    val status: String
)

data class FavoriteCourseUi(
    val id: Int,
    val name: String,
    val professor: String,
    val examDate: String?
)

class HomeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(
        HomeSummaryUiState(
            totalCourses = 6,
            completedCourses = 2,
            inProgressCourses = 3,
            toStudyCourses = 1,
            nextExam = NextExamUi(
                courseName = "Algoritmi e strutture dati",
                examDate = "24 giugno 2026",
                classroom = "Aula B2",
                status = "Da preparare"
            ),
            favoriteCourses = listOf(
                FavoriteCourseUi(
                    id = 1,
                    name = "Analisi matematica",
                    professor = "Prof. Rossi",
                    examDate = "12 luglio 2026"
                ),
                FavoriteCourseUi(
                    id = 2,
                    name = "Basi di dati",
                    professor = "Prof.ssa Verdi",
                    examDate = "Da definire"
                )
            )
        )
    )
    val uiState: StateFlow<HomeSummaryUiState> = _uiState.asStateFlow()
}
