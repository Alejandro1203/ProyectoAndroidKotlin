package com.example.proyectoandroidkotlin.activities

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.proyectoandroidkotlin.databinding.InicioLayoutBinding
import com.example.proyectoandroidkotlin.entidades.EntidadUsuario

class InicioActivity: AppCompatActivity() {
    lateinit var binding: InicioLayoutBinding
    lateinit var usuario: EntidadUsuario
    lateinit var bundleRecogida: Bundle

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = InicioLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        bundleRecogida = intent.extras!!

        usuario = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            bundleRecogida.getSerializable("usuarioLogin", EntidadUsuario::class.java)!!
        } else {
            @Suppress("DEPRECATION")
            bundleRecogida.getSerializable("usuarioLogin") as EntidadUsuario
        }

        binding.txt.text = "Id: ${usuario.id}"
    }
}