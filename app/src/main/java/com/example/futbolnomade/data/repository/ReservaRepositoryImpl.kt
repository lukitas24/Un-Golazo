package com.example.futbolnomade.data.repository

import com.example.futbolnomade.data.remote.ReservaRemoteDataSource
import com.example.futbolnomade.domain.model.Reserva
import com.example.futbolnomade.domain.repository.ReservaRepository
import kotlinx.coroutines.flow.Flow

class ReservaRepositoryImpl(
    private val remoteDataSource: ReservaRemoteDataSource = ReservaRemoteDataSource()
) : ReservaRepository {

    override fun obtenerReservasPorUsuario(usuarioId: String): Flow<List<Reserva>> {
        return remoteDataSource.obtenerReservasPorUsuario(usuarioId)
    }

    override fun obtenerReservasPorCancha(canchaId: String): Flow<List<Reserva>> {
        return remoteDataSource.obtenerReservasPorCancha(canchaId)
    }

    override suspend fun crearReserva(reserva: Reserva) {
        remoteDataSource.crearReserva(reserva)
    }

    override suspend fun cancelarReserva(reservaId: String) {
        remoteDataSource.cancelarReserva(reservaId)
    }

    override suspend fun actualizarEstadoReserva(reservaId: String, nuevoEstado: String) {
        remoteDataSource.actualizarEstadoReserva(reservaId, nuevoEstado)
    }
}
