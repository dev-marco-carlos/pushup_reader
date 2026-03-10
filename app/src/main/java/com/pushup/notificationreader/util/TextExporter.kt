package com.pushup.notificationreader.util

import android.content.Context
import com.pushup.notificationreader.data.NotificationEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class TextExporter(private val context: Context) {

    companion object {
        private const val FILE_NAME = "notifications_export.txt"
        private const val SEPARATOR_LENGTH = 50
    }

    suspend fun export(notifications: List<NotificationEntity>): String {
        return withContext(Dispatchers.IO) {
            val file = File(context.filesDir, FILE_NAME)

            file.bufferedWriter().use { writer ->
                if (notifications.isEmpty()) {
                    writer.write("Nenhuma notificacao salva.")
                } else {
                    for (notification in notifications) {
                        writer.write("=".repeat(SEPARATOR_LENGTH))
                        writer.newLine()
                        writer.write("Data/Hora: ${notification.formattedDateTime()}")
                        writer.newLine()
                        writer.write("App: ${notification.packageName}")
                        writer.newLine()
                        if (notification.title.isNotEmpty()) {
                            writer.write("Titulo: ${notification.title}")
                            writer.newLine()
                        }
                        if (notification.subText.isNotEmpty()) {
                            writer.write("SubTexto: ${notification.subText}")
                            writer.newLine()
                        }
                        if (notification.text.isNotEmpty()) {
                            writer.write("Conteudo: ${notification.text}")
                            writer.newLine()
                        }
                        writer.write("=".repeat(SEPARATOR_LENGTH))
                        writer.newLine()
                        writer.newLine()
                    }
                }
            }

            file.absolutePath
        }
    }
}
