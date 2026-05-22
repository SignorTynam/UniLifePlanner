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

object NotificationHelper {
    const val EXTRA_COURSE_ID = "extra_course_id"

    private const val CHANNEL_ID = "exam_reminders"
    private const val CHANNEL_NAME = "Promemoria esami"
    private const val CHANNEL_DESCRIPTION = "Notifiche locali per ricordare gli esami"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = CHANNEL_DESCRIPTION
        }

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }

    fun showExamReminderNotification(
        context: Context,
        courseId: Int,
        courseName: String,
        reminderType: ExamReminderType
    ) {
        if (!hasNotificationPermission(context)) return

        createNotificationChannel(context)

        val message = when (reminderType) {
            ExamReminderType.DAY_BEFORE -> "Domani hai l'esame di $courseName"
            ExamReminderType.SAME_DAY -> "Oggi hai l'esame di $courseName"
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_COURSE_ID, courseId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId(courseId, reminderType),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Promemoria esame")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        NotificationManagerCompat.from(context).notify(
            notificationId(courseId, reminderType),
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

    private fun notificationId(
        courseId: Int,
        reminderType: ExamReminderType
    ): Int {
        val suffix = when (reminderType) {
            ExamReminderType.DAY_BEFORE -> 1
            ExamReminderType.SAME_DAY -> 2
        }

        return courseId * 10 + suffix
    }
}
