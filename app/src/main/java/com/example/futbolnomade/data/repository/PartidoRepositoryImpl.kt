package com.example.futbolnomade.data.repository

import com.example.futbolnomade.data.remote.PartidoRemoteDataSource
import com.example.futbolnomade.domain.model.Partido
import com.example.futbolnomade.domain.repository.PartidoRepository

class PartidoRepositoryImpl(
    private val remoteDataSource: PartidoRemoteDataSource = PartidoRemoteDataSource()
) : PartidoRepository {

    override suspend fun obtenerPartidos(): List<Partido> {
        return remoteDataSource.obtenerPartidos()
    }

    override suspend fun obtenerPartido(id: String): Partido? {
        return remoteDataSource.obtenerPartido(id)
    }

    override suspend fun crearPartido(partido: Partido) {
        remoteDataSource.crearPartido(partido)
    }

    override fun eliminarPartido(id: Int) {
        partidos.removeAll { partido ->
            partido.id == id
        }
    }

    override fun anotarseAPartido(
        partidoId: Int,
        usuario: String
    ): Boolean {
        val index = partidos.indexOfFirst { partido ->
            partido.id == partidoId
        }

        if (index == -1) return false

        val partido = partidos[index]

        if (usuario in partido.usuariosAnotados) {
            return false
        }

        if (partido.participantesActuales >= partido.participantesMaximos) {
            return false
        }

        partidos[index] = partido.copy(
            participantesActuales = partido.participantesActuales + 1,
            usuariosAnotados = partido.usuariosAnotados + usuario
        )

        return true
    }

    override fun cancelarInscripcion(
        partidoId: Int,
        usuario: String
    ): Boolean {
        val index = partidos.indexOfFirst { partido ->
            partido.id == partidoId
        }

        if (index == -1) return false

        val partido = partidos[index]

        if (usuario !in partido.usuariosAnotados) {
            return false
        }

        partidos[index] = partido.copy(
            participantesActuales = partido.participantesActuales - 1,
            usuariosAnotados = partido.usuariosAnotados - usuario
        )

        return true
    }
}
