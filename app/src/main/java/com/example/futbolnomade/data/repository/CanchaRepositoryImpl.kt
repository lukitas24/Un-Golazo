package com.example.futbolnomade.data.repository

import com.example.futbolnomade.domain.model.Cancha
import com.example.futbolnomade.domain.model.HorarioDisponible
import com.example.futbolnomade.domain.repository.CanchaRepository

class CanchaRepositoryImpl : CanchaRepository {

    private val canchas = mutableListOf(
        Cancha(
            id = 1,
            nombre = "Cancha Maracaná",
            ubicacion = "-42.76920, -65.03850",
            descripcion = "Césped sintético premium, techada y con vestuarios climatizados.",
            precio = 12000.0,
            telefono = "2804001234",
            horarioApertura = "08:00",
            horarioCierre = "23:00",
            calificacion = 4.9,
            propietario = "propietario1@test.com",
            disponible = true,
            horarios = listOf(
                HorarioDisponible("Lunes", "08:00", "23:00"),
                HorarioDisponible("Martes", "08:00", "23:00"),
                HorarioDisponible("Miércoles", "08:00", "23:00"),
                HorarioDisponible("Jueves", "08:00", "23:00"),
                HorarioDisponible("Viernes", "08:00", "23:00"),
                HorarioDisponible("Sábado", "09:00", "20:00"),
                HorarioDisponible("Domingo", "09:00", "14:00")
            )
        ),
        Cancha(
            id = 2,
            nombre = "Complejo El Diego",
            ubicacion = "-42.77510, -65.04210",
            descripcion = "Cancha de fútbol 5 de alfombra clásica. Estacionamiento privado.",
            precio = 10000.0,
            telefono = "2804559876",
            horarioApertura = "14:00",
            horarioCierre = "00:00",
            calificacion = 4.7,
            propietario = "propietario2@test.com",
            disponible = true,
            horarios = listOf(
                HorarioDisponible("Lunes", "14:00", "00:00"),
                HorarioDisponible("Martes", "14:00", "00:00"),
                HorarioDisponible("Miércoles", "14:00", "00:00"),
                HorarioDisponible("Jueves", "14:00", "00:00"),
                HorarioDisponible("Viernes", "14:00", "01:00"),
                HorarioDisponible("Sábado", "12:00", "02:00")
            ) // Domingo cerrado (no se incluye en la lista)
        )
    )

    override fun obtenerCanchas(): List<Cancha> {
        return canchas.toList()
    }

    override fun obtenerCancha(id: Int): Cancha? {
        return canchas.find { it.id == id }
    }

    override fun crearCancha(cancha: Cancha) {
        // Generamos un ID autoincremental sumándole 1 al ID más alto actual
        val nuevoId = if (canchas.isEmpty()) 1 else canchas.maxOf { it.id } + 1
        canchas.add(cancha.copy(id = nuevoId))
    }

    override fun actualizarDisponibilidad(canchaId: Int, disponible: Boolean): Boolean {
        val index = canchas.indexOfFirst { it.id == canchaId }

        if (index == -1) return false

        val cancha = canchas[index]
        canchas[index] = cancha.copy(disponible = disponible)
        return true
    }

    override fun obtenerCanchasPorPropietario(email: String): List<Cancha> {
        return canchas.filter { it.propietario == email }
    }

    override fun actualizarCancha(cancha: Cancha): Boolean {
        val index = canchas.indexOfFirst { it.id == cancha.id }

        if (index == -1) return false

        canchas[index] = cancha
        return true
    }

    override fun eliminarCancha(canchaId: Int): Boolean {
        return canchas.removeIf { it.id == canchaId }
    }

    override fun agregarHorario(canchaId: Int, horario: HorarioDisponible): Boolean {
        val index = canchas.indexOfFirst { it.id == canchaId }

        if (index == -1) return false

        val canchaActual = canchas[index]

        val horariosActualizados = canchaActual.horarios
            .filterNot { it.dia.lowercase() == horario.dia.lowercase() } + horario

        canchas[index] = canchaActual.copy(
            horarios = horariosActualizados,
            horarioApertura = horariosActualizados.firstOrNull()?.horaApertura ?: canchaActual.horarioApertura,
            horarioCierre = horariosActualizados.firstOrNull()?.horaCierre ?: canchaActual.horarioCierre
        )

        return true
    }

    override fun eliminarHorario(canchaId: Int, horario: HorarioDisponible): Boolean {
        val index = canchas.indexOfFirst { it.id == canchaId }

        if (index == -1) return false

        val canchaActual = canchas[index]

        val horariosActualizados = canchaActual.horarios.filterNot {
            it.dia.lowercase() == horario.dia.lowercase()
        }

        canchas[index] = canchaActual.copy(
            horarios = horariosActualizados,
            horarioApertura = horariosActualizados.firstOrNull()?.horaApertura ?: "00:00",
            horarioCierre = horariosActualizados.firstOrNull()?.horaCierre ?: "00:00"
        )

        return true
    }
}