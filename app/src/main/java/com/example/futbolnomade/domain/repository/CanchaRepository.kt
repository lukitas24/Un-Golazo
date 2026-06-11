package com.example.futbolnomade.domain.repository

import com.example.futbolnomade.domain.model.Cancha
import kotlinx.coroutines.flow.Flow

interface CanchaRepository {
    fun getCanchas(userId: String): Flow<List<Cancha>>
    suspend fun guardarCancha(cancha: Cancha)
    suspend fun eliminarCancha(id: String)
}
