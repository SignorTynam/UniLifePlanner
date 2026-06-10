package com.example.unilifeplanner.ui.exams.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.unilifeplanner.domain.exams.ExamFeedbackResult

fun ExamFeedbackResult.feedbackLabel(): String {
    return when (this) {
        ExamFeedbackResult.PASSED -> "Superato"
        ExamFeedbackResult.FAILED -> "Non superato"
        ExamFeedbackResult.WAITING_RESULT -> "In attesa del risultato"
        ExamFeedbackResult.NOT_ATTENDED -> "Non ho partecipato"
    }
}

fun ExamFeedbackResult.feedbackDescription(): String {
    return when (this) {
        ExamFeedbackResult.PASSED -> "Ho passato l’esame e posso registrare il voto."
        ExamFeedbackResult.FAILED -> "L’esame non è stato superato."
        ExamFeedbackResult.WAITING_RESULT -> "Il risultato non è ancora disponibile."
        ExamFeedbackResult.NOT_ATTENDED -> "Non ho sostenuto questo appello."
    }
}

fun ExamFeedbackResult.feedbackIcon(): ImageVector {
    return when (this) {
        ExamFeedbackResult.PASSED -> Icons.Filled.CheckCircle
        ExamFeedbackResult.FAILED -> Icons.Filled.Cancel
        ExamFeedbackResult.WAITING_RESULT -> Icons.Filled.Schedule
        ExamFeedbackResult.NOT_ATTENDED -> Icons.Filled.Block
    }
}
