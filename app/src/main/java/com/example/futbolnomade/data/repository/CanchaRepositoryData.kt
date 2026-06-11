package com.example.futbolnomade.data.repository

import com.example.futbolnomade.data.remote.CanchaRemoteDataSource
import com.example.futbolnomade.domain.model.Cancha
import com.example.futbolnomade.domain.repository.CanchaRepository
import kotlinx.coroutines.flow.Flow

class CanchaRepositoryImpl(
    private val remoteDataSource: CanchaRemoteDataSource = CanchaRemoteDataSource()
) : CanchaRepository {

    override fun getCanchas(userId: String): Flow<List<Cancha>> =
        remoteDataSource.getCanchas(userId)

    override suspend fun guardarCancha(cancha: Cancha) =
        remoteDataSource.guardarCancha(cancha)

    override suspend fun eliminarCancha(id: String) =
        remoteDataSource.eliminarCancha(id)
}
