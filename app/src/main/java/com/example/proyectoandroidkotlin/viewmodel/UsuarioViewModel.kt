package com.example.proyectoandroidkotlin.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.proyectoandroidkotlin.entidades.UsuarioEntidad
import com.example.proyectoandroidkotlin.tablasBBDD.UsuarioBBDD

class UsuarioViewModel: ViewModel() {
    private var listaUsuarios = MutableLiveData<List<UsuarioEntidad>>()

    fun getListaUsuarios(): LiveData<List<UsuarioEntidad>> {
        return listaUsuarios
    }

    fun cargarListaUsuarios(userType: String, context: Context) {
        val usuarioBBDD = UsuarioBBDD(context)
        var usuario: List<UsuarioEntidad> = emptyList()

        when(userType) {
            "ADMIN" -> usuario = usuarioBBDD.getAllUsuariosByRol(1)
            "NORMAL" -> usuario = usuarioBBDD.getAllUsuariosByRol(2)
            "BAJA" -> usuario = usuarioBBDD.getAllUsuariosBaja()
        }

        listaUsuarios.value = usuario
    }
}