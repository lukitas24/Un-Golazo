package com.example.futbolnomade.domain.repository

import com.example.futbolnomade.domain.model.Partido

interface PartidoRepository {

    fun obtenerPartidos(): List<Partido>

    fun obtenerPartido(id: Int): Partido?

    fun crearPartido(partido: Partido)

    fun eliminarPartido(id: Int)

    fun anotarseAPartido(
        partidoId: Int,
        usuario: String
    ): Boolean

    fun cancelarInscripcion(
        partidoId: Int,
        usuario: String
    ): Boolean
}