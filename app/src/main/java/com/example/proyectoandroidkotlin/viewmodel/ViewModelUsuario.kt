package com.example.proyectoandroidkotlin.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.proyectoandroidkotlin.entidades.EntidadUsuario
import com.example.proyectoandroidkotlin.tablasBBDD.UsuarioBBDD

class ViewModelUsuario: ViewModel() {
    private var listaUsuarios = MutableLiveData<List<EntidadUsuario>>()

    fun getListaUsuarios(): LiveData<List<EntidadUsuario>> {
        return listaUsuarios
    }

    fun cargarListaUsuarios(userType: String, context: Context) {
        val usuarioBBDD = UsuarioBBDD(context)
        var usuario: List<EntidadUsuario> = emptyList()

        when(userType) {
            "ADMIN" -> usuario = usuarioBBDD.getAllUsuariosByRol(1)
            "NORMAL" -> usuario = usuarioBBDD.getAllUsuariosByRol(2)
            "BAJA" -> usuario = usuarioBBDD.getAllUsuariosBaja()
        }

        listaUsuarios.value = usuario
    }
}