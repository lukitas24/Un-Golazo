package com.example.futbolnomade.domain.usecase

import com.example.futbolnomade.domain.model.Partido
import com.example.futbolnomade.domain.repository.PartidoRepository

class ObtenerPartidosUseCase(
    private val repository: PartidoRepository
) {

    suspend operator fun invoke(): List<Partido> {
        return repository.obtenerPartidos()
    }
}