package com.example.futbolnomade.domain.repository

import com.example.futbolnomade.domain.model.Partido

interface PartidoRepository {

    suspend fun obtenerPartidos(): List<Partido>

    suspend fun obtenerPartido(id: String): Partido?

    suspend fun crearPartido(partido: Partido)

    suspend fun eliminarPartido(id: String)

    suspend fun anotarseAPartido(
        partidoId: String,
        usuario: String
    ): Boolean

    suspend fun eliminarJugador(
        partidoId: String,
        jugadorAEliminar: String,
        usuarioSolicitante: String
    ): Boolean

    suspend fun cancelarInscripcion(
        partidoId: String,
        usuario: String
    ): Boolean
}