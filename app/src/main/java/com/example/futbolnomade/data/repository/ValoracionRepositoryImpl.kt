package com.example.futbolnomade.data.repository

import com.example.futbolnomade.data.remote.ValoracionRemoteDataSource
import com.example.futbolnomade.domain.model.ValoracionPartido
import com.example.futbolnomade.domain.repository.ValoracionRepository

class ValoracionRepositoryImpl(
    private val remoteDataSource:
    ValoracionRemoteDataSource =
        ValoracionRemoteDataSource()
) : ValoracionRepository {

    override suspend fun guardarValoracion(
        valoracion: ValoracionPartido
    ): Boolean {
        return remoteDataSource.guardarValoracion(
            valoracion
        )
    }

    override suspend fun obtenerValoracionesDeUsuario(
        emailUsuario: String
    ): List<ValoracionPartido> {
        return remoteDataSource
            .obtenerValoracionesDeUsuario(
                emailUsuario
            )
    }

    override suspend fun obtenerValoracionesSobreUsuario(
        emailUsuario: String
    ): List<ValoracionPartido> {
        return remoteDataSource
            .obtenerValoracionesSobreUsuario(
                emailUsuario
            )
    }

    override suspend fun yaValoro(
        partidoId: String,
        autorEmail: String
    ): Boolean {
        return remoteDataSource.yaValoro(
            partidoId,
            autorEmail
        )
    }
}