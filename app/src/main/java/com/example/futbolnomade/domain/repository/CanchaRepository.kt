package com.example.futbolnomade.domain.repository

import com.example.futbolnomade.domain.model.Cancha
import com.example.futbolnomade.domain.model.HorarioDisponible

interface CanchaRepository {
    fun obtenerCanchas(): List<Cancha>
    fun obtenerCancha(id: Int): Cancha?
    fun crearCancha(cancha: Cancha)
    fun actualizarDisponibilidad(canchaId: Int, disponible: Boolean): Boolean
    fun obtenerCanchasPorPropietario(email: String): List<Cancha>

    // AGREGAR ESTOS
    fun actualizarCancha(cancha: Cancha): Boolean
    fun eliminarCancha(canchaId: Int): Boolean
    fun agregarHorario(canchaId: Int, horario: HorarioDisponible): Boolean
    fun eliminarHorario(canchaId: Int, horario: HorarioDisponible): Boolean
}
