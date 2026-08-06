package com.example.futbolnomade.domain.repository

interface NotificationRepository {

    suspend fun registrarDispositivo(
        uid: String
    )

    fun actualizarToken(
        uid: String,
        token: String
    )

    suspend fun eliminarDispositivo(
        uid: String
    )
}