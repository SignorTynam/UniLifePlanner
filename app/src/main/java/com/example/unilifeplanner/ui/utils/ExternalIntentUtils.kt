package com.example.unilifeplanner.ui.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.CalendarContract
import com.example.unilifeplanner.data.local.CourseEntity
import com.example.unilifeplanner.ui.courses.components.formatCourseStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExternalIntentUtils {
    private const val EXAM_DURATION_MILLIS = 2 * 60 * 60 * 1000L

    fun addCourseToCalendar(
        context: Context,
        course: CourseEntity
    ): ExternalIntentResult {
        val beginTime = course.examDate
            ?: return ExternalIntentResult.MissingData("Impossibile aggiungere al calendario: data esame mancante")

        val intent = Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.Events.TITLE, "Esame: ${course.name}")
            putExtra(CalendarContract.Events.DESCRIPTION, buildCourseDescription(course))
            putExtra(CalendarContract.Events.EVENT_LOCATION, course.classroom.orEmpty())
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, beginTime)
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, beginTime + EXAM_DURATION_MILLIS)
        }

        return startActivitySafely(
            context = context,
            intent = intent,
            failureMessage = "Nessuna app calendario disponibile"
        )
    }

    fun openCourseLocationInMaps(
        context: Context,
        classroom: String?
    ): ExternalIntentResult {
        val query = classroom?.trim()?.takeIf { it.isNotEmpty() }
            ?: return ExternalIntentResult.MissingData("Aula non disponibile")
        val encodedQuery = Uri.encode("$query universita")
        val uri = Uri.parse("geo:0,0?q=$encodedQuery")

        val googleMapsIntent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.google.android.apps.maps")
        }
        val fallbackIntent = Intent(Intent.ACTION_VIEW, uri)

        return when {
            googleMapsIntent.resolveActivity(context.packageManager) != null ->
                startActivitySafely(context, googleMapsIntent, "Impossibile aprire Google Maps")

            fallbackIntent.resolveActivity(context.packageManager) != null ->
                startActivitySafely(context, fallbackIntent, "Nessuna app mappe disponibile")

            else -> ExternalIntentResult.NoCompatibleApp("Nessuna app mappe disponibile")
        }
    }

    fun shareCourse(
        context: Context,
        course: CourseEntity
    ): ExternalIntentResult {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, buildShareText(course))
        }
        val chooser = Intent.createChooser(sendIntent, "Condividi corso")

        return startActivitySafely(
            context = context,
            intent = chooser,
            failureMessage = "Nessuna app disponibile per la condivisione"
        )
    }

    fun sendEmailToProfessor(
        context: Context,
        email: String?,
        courseName: String
    ): ExternalIntentResult {
        val professorEmail = email?.trim()?.takeIf { it.isNotEmpty() }
            ?: return ExternalIntentResult.MissingData("Email docente non disponibile")

        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(professorEmail))
            putExtra(Intent.EXTRA_SUBJECT, "Informazioni su $courseName")
            putExtra(
                Intent.EXTRA_TEXT,
                "Gentile docente,\n\nvorrei chiedere alcune informazioni relative al corso $courseName.\n\nCordiali saluti"
            )
        }

        return startActivitySafely(
            context = context,
            intent = intent,
            failureMessage = "Nessuna app email disponibile"
        )
    }

    fun openDialer(
        context: Context,
        phoneNumber: String?
    ): ExternalIntentResult {
        val number = phoneNumber?.trim()?.takeIf { it.isNotEmpty() }
            ?: return ExternalIntentResult.MissingData("Numero di telefono non disponibile")
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$number")
        }

        return startActivitySafely(
            context = context,
            intent = intent,
            failureMessage = "Nessuna app telefono disponibile"
        )
    }

    private fun startActivitySafely(
        context: Context,
        intent: Intent,
        failureMessage: String
    ): ExternalIntentResult {
        return try {
            if (intent.resolveActivity(context.packageManager) == null) {
                ExternalIntentResult.NoCompatibleApp(failureMessage)
            } else {
                context.startActivity(intent)
                ExternalIntentResult.Success
            }
        } catch (_: ActivityNotFoundException) {
            ExternalIntentResult.NoCompatibleApp(failureMessage)
        } catch (_: Exception) {
            ExternalIntentResult.Error(failureMessage)
        }
    }

    private fun buildCourseDescription(course: CourseEntity): String {
        return buildString {
            appendLine("Docente: ${course.professor}")
            appendLine("Aula: ${course.classroom?.takeIf { it.isNotBlank() } ?: "Non impostata"}")
            appendLine("CFU: ${course.credits}")
            appendLine("Stato: ${formatCourseStatus(course.status)}")
            course.notes?.takeIf { it.isNotBlank() }?.let { notes ->
                appendLine()
                append("Note: $notes")
            }
        }
    }

    private fun buildShareText(course: CourseEntity): String {
        return buildString {
            appendLine("Corso: ${course.name}")
            appendLine("Docente: ${course.professor}")
            appendLine("Data esame: ${formatShareDate(course.examDate)}")
            appendLine("Aula: ${course.classroom?.takeIf { it.isNotBlank() } ?: "Non impostata"}")
            appendLine("CFU: ${course.credits}")
            appendLine("Stato: ${formatCourseStatus(course.status)}")
            append("Note: ${course.notes?.takeIf { it.isNotBlank() } ?: "Nessuna nota"}")
        }
    }

    private fun formatShareDate(timestamp: Long?): String {
        return timestamp?.let {
            SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.ITALY).format(Date(it))
        } ?: "Non impostata"
    }
}

sealed class ExternalIntentResult {
    data object Success : ExternalIntentResult()
    data class MissingData(val message: String) : ExternalIntentResult()
    data class NoCompatibleApp(val message: String) : ExternalIntentResult()
    data class Error(val message: String) : ExternalIntentResult()
}
