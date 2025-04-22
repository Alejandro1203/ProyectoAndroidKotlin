package com.example.proyectoandroidkotlin.adaptadores

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.proyectoandroidkotlin.entidades.EntidadUsuario
import com.example.proyectoandroidkotlin.fragmentos.ListaUsuarioFragmento

class AdaptadorFragmentoListaUsuarios(activity: FragmentActivity, var usuario: EntidadUsuario?): FragmentStateAdapter(activity) {
    private val listaUsuarioString = arrayListOf<String>("ADMIN", "NORMAL", "BAJA")

    override fun createFragment(position: Int): Fragment {

        if(usuario?.rol == 1) {
            return ListaUsuarioFragmento().newInstance(listaUsuarioString[position], usuario)
        } else {
            return ListaUsuarioFragmento().newInstance(listaUsuarioString[1], usuario)
        }

    }

    override fun getItemCount(): Int {
        return if(usuario?.rol == 1) {
            listaUsuarioString.size
        } else {
            1
        }
    }
}