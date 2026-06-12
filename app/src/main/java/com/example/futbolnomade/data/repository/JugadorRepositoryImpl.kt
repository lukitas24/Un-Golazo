package com.example.futbolnomade.data.repository

import com.example.futbolnomade.data.remote.JugadorRemoteDataSource
import com.example.futbolnomade.domain.model.Jugador
import com.example.futbolnomade.domain.repository.JugadorRepository

class JugadorRepositoryImpl(
    private val remoteDataSource: JugadorRemoteDataSource = JugadorRemoteDataSource()
) : JugadorRepository {

    override suspend fun obtenerJugador(id: String): Jugador? {
        return remoteDataSource.obtenerJugador(id)
    }

    override suspend fun guardarJugador(jugador: Jugador) {
        remoteDataSource.guardarJugador(jugador)
    }

    override suspend fun obtenerTodos(): List<Jugador> {
        return remoteDataSource.obtenerTodos()
    }
}
