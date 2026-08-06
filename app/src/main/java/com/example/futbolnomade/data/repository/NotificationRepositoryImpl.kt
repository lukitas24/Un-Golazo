package com.example.futbolnomade.data.repository

import com.example.futbolnomade.data.remote.notification.NotificationTokenRemoteDataSource
import com.example.futbolnomade.domain.repository.NotificationRepository

class NotificationRepositoryImpl(
    private val remoteDataSource:
    NotificationTokenRemoteDataSource =
        NotificationTokenRemoteDataSource()
) : NotificationRepository {

    override suspend fun registrarDispositivo(
        uid: String
    ) {
        remoteDataSource.registrarDispositivo(
            uid
        )
    }

    override fun actualizarToken(
        uid: String,
        token: String
    ) {
        remoteDataSource.actualizarToken(
            uid = uid,
            token = token
        )
    }

    override suspend fun eliminarDispositivo(
        uid: String
    ) {
        remoteDataSource.eliminarDispositivo(
            uid
        )
    }
}