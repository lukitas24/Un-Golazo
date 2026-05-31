package com.example.futbolnomade.data.repository

import com.example.futbolnomade.domain.model.Partido
import com.example.futbolnomade.domain.repository.PartidoRepository

class PartidoRepositoryImpl : PartidoRepository {

    private val partidos = mutableListOf(
        Partido(
            id = 1,
            titulo = "Picadito",
            horario = "18:00",
            fecha = "24/06",
            ubicacion = "Puerto Madryn, Mitre 423",
            dificultad = "Avanzado",
            participantesActuales = 1,
            participantesMaximos = 10,
            creador = "Dani Martinez",
            calificacionCreador = 4.9
        ),
        Partido(
            id = 2,
            titulo = "La revancha",
            horario = "21:00",
            fecha = "20/05",
            ubicacion = "Puerto Madryn, Muzzio y Luis Maria Campos",
            dificultad = "Fácil",
            participantesActuales = 5,
            participantesMaximos = 10,
            creador = "Teresa Rodrigo",
            calificacionCreador = 4.9
        )
    )

    override fun obtenerPartidos(): List<Partido> {
        return partidos
    }

    override fun obtenerPartido(id: Int): Partido? {
        return partidos.find { it.id == id }
    }

    override fun crearPartido(partido: Partido) {
        partidos.add(partido)
    }
}