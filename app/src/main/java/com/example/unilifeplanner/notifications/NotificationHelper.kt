package com.example.unilifeplanner.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.unilifeplanner.MainActivity
import com.example.unilifeplanner.R
import com.example.unilifeplanner.domain.lessons.formatMinutesToTime

object NotificationHelper {
    const val EXTRA_COURSE_ID = "extra_course_id"
    const val EXTRA_LESSON_ID = "extra_lesson_id"
    const val EXTRA_EXAM_APPEAL_ID = "extra_exam_appeal_id"
    const val CHANNEL_ID_LESSON_REMINDERS = "lesson_reminders"
    const val CHANNEL_NAME_LESSON_REMINDERS = "Promemoria lezioni"
    const val CHANNEL_DESCRIPTION_LESSON_REMINDERS =
        "Notifiche locali per ricordare le lezioni"

    private const val CHANNEL_ID = "exam_reminders"
    private const val CHANNEL_NAME = "Promemoria esami"
    private const val CHANNEL_DESCRIPTION = "Notifiche locali per ricordare gli esami"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val examChannel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = CHANNEL_DESCRIPTION
        }
        val lessonChannel = NotificationChannel(
            CHANNEL_ID_LESSON_REMINDERS,
            CHANNEL_NAME_LESSON_REMINDERS,
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = CHANNEL_DESCRIPTION_LESSON_REMINDERS
        }

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(examChannel)
        notificationManager.createNotificationChannel(lessonChannel)
    }

    fun showExamReminderNotification(
        context: Context,
        examAppealId: Int,
        courseId: Int,
        courseName: String,
        examDateText: String,
        reminderType: ExamReminderType
    ) {
        if (!hasNotificationPermission(context)) return

        createNotificationChannel(context)

        val message = when (reminderType) {
            ExamReminderType.DAY_BEFORE -> "Domani hai l'appello di $courseName"
            ExamReminderType.SAME_DAY -> "Oggi hai l'appello di $courseName"
            ExamReminderType.CUSTOM -> "Promemoria appello per $courseName"
        }
        val expandedMessage = listOf(message, examDateText.takeIf { it.isNotBlank() })
            .filterNotNull()
            .joinToString("\n")

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_COURSE_ID, courseId)
            putExtra(EXTRA_EXAM_APPEAL_ID, examAppealId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            examNotificationId(examAppealId, reminderType),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Promemoria appello")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(expandedMessage))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        NotificationManagerCompat.from(context).notify(
            examNotificationId(examAppealId, reminderType),
            notification
        )
    }

    fun showLessonReminderNotification(
        context: Context,
        courseId: Int,
        lessonId: Int,
        courseName: String,
        dayOfWeek: Int,
        startTimeMinutes: Int,
        classroom: String?
    ) {
        if (!hasNotificationPermission(context)) return

        createNotificationChannel(context)

        val lessonTime = formatMinutesToTime(startTimeMinutes)
        val classroomSuffix = classroom
            ?.takeIf { it.isNotBlank() }
            ?.let { " in $it" }
            .orEmpty()
        val message = "Domani hai lezione di $courseName alle $lessonTime$classroomSuffix"

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_COURSE_ID, courseId)
            putExtra(EXTRA_LESSON_ID, lessonId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            lessonNotificationId(lessonId),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_LESSON_REMINDERS)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Promemoria lezione")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        NotificationManagerCompat.from(context).notify(
            lessonNotificationId(lessonId),
            notification
        )
    }

    fun hasNotificationPermission(context: Context): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
    }

    private fun examNotificationId(
        examAppealId: Int,
        reminderType: ExamReminderType
    ): Int {
        val suffix = when (reminderType) {
            ExamReminderType.DAY_BEFORE -> 1
            ExamReminderType.SAME_DAY -> 2
            ExamReminderType.CUSTOM -> 3
        }

        return 400_000 + examAppealId * 10 + suffix
    }

    private fun lessonNotificationId(lessonId: Int): Int = 300_000 + lessonId
}
