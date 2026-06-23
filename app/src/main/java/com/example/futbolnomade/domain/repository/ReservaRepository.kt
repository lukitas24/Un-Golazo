package com.example.futbolnomade.domain.repository

import com.example.futbolnomade.domain.model.Reserva
import kotlinx.coroutines.flow.Flow

interface ReservaRepository {
    fun obtenerReservasPorUsuario(usuarioId: String): Flow<List<Reserva>>
    fun obtenerReservasPorCancha(canchaId: String): Flow<List<Reserva>>
    suspend fun crearReserva(reserva: Reserva)
    suspend fun cancelarReserva(reservaId: String)
    suspend fun actualizarEstadoReserva(reservaId: String, nuevoEstado: String)
}
