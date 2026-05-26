package com.example.futbolnomade

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity

class ElementosActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_elementos)

        val lista = findViewById<ListView>(R.id.listaElementos)

        val elementos = arrayOf(
            "Ver partidos",
            "Buscar canchas",
            "Reservar cancha",
            "Unirme a un partido",
            "Crear partido",
            "Calificar jugadores"
        )

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            elementos
        )

        lista.adapter = adapter
    }
}