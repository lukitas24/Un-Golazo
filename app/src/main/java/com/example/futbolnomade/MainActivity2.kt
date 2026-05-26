package com.example.futbolnomade

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity2 : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_main2)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val mensaje = findViewById<TextView>(R.id.mensaje)
        val btnAcerca = findViewById<Button>(R.id.btnAcerca)
        val btnLista = findViewById<Button>(R.id.btnLista)

        val nombre = intent.getStringExtra("nombre_usuario")
        mensaje.setText("Bienvenido $nombre")

        btnAcerca.setOnClickListener {
            val intent = Intent(this, AcercaDeActivity::class.java)
            startActivity(intent)
        }
        btnLista.setOnClickListener {
            val intent = Intent(this, ElementosActivity::class.java)
            startActivity(intent)
        }
    }
}