package com.pushup.notificationreader.data

import kotlinx.coroutines.flow.Flow

class NotificationRepository(private val dao: NotificationDao) {

    val allNotifications: Flow<List<NotificationEntity>> = dao.getAllFlow()

    suspend fun insert(notification: NotificationEntity) {
        dao.insert(notification)
    }

    suspend fun deleteAll() {
        dao.deleteAll()
    }

    suspend fun getAll(): List<NotificationEntity> {
        return dao.getAll()
    }

    suspend fun count(): Int {
        return dao.count()
    }
}
