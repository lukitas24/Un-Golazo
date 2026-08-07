package com.example.futbolnomade.data.repository

import com.example.futbolnomade.data.remote.PartidoRemoteDataSource
import com.example.futbolnomade.domain.model.EstadoPartido
import com.example.futbolnomade.domain.model.Partido
import com.example.futbolnomade.domain.repository.PartidoRepository

class PartidoRepositoryImpl(
    private val remoteDataSource:
    PartidoRemoteDataSource =
        PartidoRemoteDataSource()
) : PartidoRepository {

    override suspend fun obtenerPartidos(): List<Partido> {
        return remoteDataSource.obtenerPartidos()
    }

    override suspend fun obtenerPartido(
        id: String
    ): Partido? {
        return remoteDataSource.obtenerPartido(
            id
        )
    }

    override suspend fun crearPartido(
        partido: Partido
    ) {
        remoteDataSource.crearPartido(
            partido
        )
    }

    override suspend fun actualizarPartido(
        partido: Partido
    ) {
        remoteDataSource.actualizarPartido(
            partido
        )
    }

    override suspend fun actualizarEstadoPartido(
        partidoId: String,
        nuevoEstado: EstadoPartido
    ) {
        remoteDataSource.actualizarEstadoPartido(
            partidoId = partidoId,
            nuevoEstado = nuevoEstado
        )
    }

    override suspend fun eliminarPartido(
        id: String
    ) {
        remoteDataSource.eliminarPartido(
            id
        )
    }

    override suspend fun anotarseAPartido(
        partidoId: String,
        usuario: String,
        usuarioUid: String
    ): Boolean {
        return remoteDataSource.anotarseAPartido(
            partidoId = partidoId,
            usuario = usuario,
            usuarioUid = usuarioUid
        )
    }

    override suspend fun cancelarInscripcion(
        partidoId: String,
        usuario: String,
        usuarioUid: String
    ): Boolean {
        return remoteDataSource.cancelarInscripcion(
            partidoId = partidoId,
            usuario = usuario,
            usuarioUid = usuarioUid
        )
    }

    override suspend fun eliminarJugador(
        partidoId: String,
        jugadorAEliminar: String,
        usuarioSolicitante: String
    ): Boolean {
        return remoteDataSource.eliminarJugador(
            partidoId = partidoId,
            jugadorAEliminar = jugadorAEliminar,
            usuarioSolicitante = usuarioSolicitante
        )
    }
}