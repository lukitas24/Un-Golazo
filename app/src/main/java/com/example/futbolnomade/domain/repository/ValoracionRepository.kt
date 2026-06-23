package com.example.futbolnomade.domain.repository

import com.example.futbolnomade.domain.model.ValoracionPartido

interface ValoracionRepository {

    suspend fun guardarValoracion(
        valoracion: ValoracionPartido
    ): Boolean

    suspend fun obtenerValoracionesDeUsuario(
        emailUsuario: String
    ): List<ValoracionPartido>

    suspend fun yaValoro(
        partidoId: String,
        autorEmail: String
    ): Boolean
}