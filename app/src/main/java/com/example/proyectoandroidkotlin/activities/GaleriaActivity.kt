package com.example.proyectoandroidkotlin.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.proyectoandroidkotlin.databinding.GaleriaLayoutBinding

class GaleriaActivity: AppCompatActivity(){
    lateinit var binding: GaleriaLayoutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = GaleriaLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}