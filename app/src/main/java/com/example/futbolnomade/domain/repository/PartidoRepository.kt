package com.example.futbolnomade.domain.repository

import com.example.futbolnomade.domain.model.EstadoPartido
import com.example.futbolnomade.domain.model.Partido

interface PartidoRepository {

    suspend fun obtenerPartidos(): List<Partido>

    suspend fun obtenerPartido(id: String): Partido?

    suspend fun crearPartido(partido: Partido)

    /*
     * Para futuras pantallas de edición.
     * Se diferencia de crearPartido para que la intención quede clara.
     */
    suspend fun actualizarPartido(partido: Partido)

    suspend fun actualizarEstadoPartido(
        partidoId: String,
        nuevoEstado: EstadoPartido
    )

    suspend fun eliminarPartido(id: String)

    suspend fun anotarseAPartido(
        partidoId: String,
        usuario: String,
        usuarioUid: String
    ): Boolean

    suspend fun eliminarJugador(
        partidoId: String,
        jugadorAEliminar: String,
        usuarioSolicitante: String
    ): Boolean

    suspend fun cancelarInscripcion(
        partidoId: String,
        usuario: String,
        usuarioUid: String
    ): Boolean
}