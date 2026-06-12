package com.example.futbolnomade.domain.repository

import com.example.futbolnomade.domain.model.Jugador

interface JugadorRepository {
    suspend fun obtenerJugador(id: String): Jugador?
    suspend fun guardarJugador(jugador: Jugador)
    suspend fun obtenerTodos(): List<Jugador>
}
