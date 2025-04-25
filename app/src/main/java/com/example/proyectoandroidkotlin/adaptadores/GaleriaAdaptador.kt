package com.example.proyectoandroidkotlin.adaptadores

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.example.proyectoandroidkotlin.adaptadores.GaleriaAdaptador.ViewHolderImagen
import com.example.proyectoandroidkotlin.databinding.ImagenGaleriaBinding

class GaleriaAdaptador(val nombreUsuario: String, val listaImagenes: ArrayList<String>): RecyclerView.Adapter<ViewHolderImagen>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderImagen {
        val binding = ImagenGaleriaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolderImagen(binding)
    }

    override fun onBindViewHolder(holder: ViewHolderImagen, position: Int) {
        val imagen = listaImagenes[position]
        holder.bindImagen(imagen)
    }

    override fun getItemCount() = listaImagenes.size

    inner class ViewHolderImagen(val binding: ImagenGaleriaBinding): RecyclerView.ViewHolder(binding.root) {

        fun bindImagen(imagen: String) {
            binding.nombreUsuario.text = nombreUsuario
            binding.imagenGaleria.setImageURI(imagen.toUri())
        }
    }
}