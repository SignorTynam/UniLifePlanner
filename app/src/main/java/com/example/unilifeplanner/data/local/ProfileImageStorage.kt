package com.example.unilifeplanner.data.local

import android.content.Context
import android.net.Uri
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ProfileImageStorage(
    private val context: Context
) {
    suspend fun saveProfileImage(
        sourceUri: Uri,
        userEmail: String
    ): String? = withContext(Dispatchers.IO) {
        val normalizedEmail = userEmail.lowercase().trim().ifBlank { "anonymous" }
        val imageDir = File(context.filesDir, PROFILE_IMAGES_DIR)
        if (!imageDir.exists() && !imageDir.mkdirs()) {
            return@withContext null
        }

        val targetFile = File(imageDir, "profile_${normalizedEmail.hashCode()}.jpg")
        var tempFile: File? = null

        try {
            val inputStream = context.contentResolver.openInputStream(sourceUri)
                ?: return@withContext null
            val workingFile = File.createTempFile("profile_${normalizedEmail.hashCode()}_", ".tmp", imageDir)
            tempFile = workingFile

            inputStream.use { input ->
                workingFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            try {
                Files.move(
                    workingFile.toPath(),
                    targetFile.toPath(),
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE
                )
            } catch (_: Exception) {
                Files.move(
                    workingFile.toPath(),
                    targetFile.toPath(),
                    StandardCopyOption.REPLACE_EXISTING
                )
            }

            Uri.fromFile(targetFile).toString()
        } catch (_: Exception) {
            tempFile?.delete()
            null
        }
    }

    private companion object {
        const val PROFILE_IMAGES_DIR = "profile_images"
    }
}
