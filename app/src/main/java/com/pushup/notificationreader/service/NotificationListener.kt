package com.pushup.notificationreader.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.pushup.notificationreader.data.AppDatabase
import com.pushup.notificationreader.data.NotificationEntity
import com.pushup.notificationreader.data.NotificationRepository
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class NotificationListener : NotificationListenerService() {

    companion object {
        private const val TAG = "NotificationListener"
    }

    private val job = SupervisorJob()
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e(TAG, "Erro ao salvar notificacao", throwable)
    }
    private val scope = CoroutineScope(Dispatchers.IO + job + exceptionHandler)

    private lateinit var repository: NotificationRepository

    override fun onCreate() {
        super.onCreate()
        val db = AppDatabase.getInstance(applicationContext)
        repository = NotificationRepository(db.notificationDao())
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val notification = sbn.notification ?: return
        val extras = notification.extras ?: return

        val packageName = sbn.packageName ?: "unknown"
        val title = extras.getCharSequence("android.title")?.toString() ?: ""
        val text = extras.getCharSequence("android.text")?.toString() ?: ""
        val subText = extras.getCharSequence("android.subText")?.toString() ?: ""
        val bigText = extras.getCharSequence("android.bigText")?.toString() ?: ""

        val content = bigText.ifEmpty { text }

        if (title.isEmpty() && content.isEmpty()) return

        val entity = NotificationEntity(
            packageName = packageName,
            title = title,
            text = content,
            subText = subText,
            timestamp = System.currentTimeMillis()
        )

        scope.launch {
            repository.insert(entity)
            Log.d(TAG, "Notificacao salva: $packageName - $title")
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        Log.d(TAG, "Notificacao removida: ${sbn.packageName}")
    }
}
