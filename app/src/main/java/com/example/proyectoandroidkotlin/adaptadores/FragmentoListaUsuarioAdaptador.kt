package com.example.proyectoandroidkotlin.adaptadores

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.proyectoandroidkotlin.entidades.UsuarioEntidad
import com.example.proyectoandroidkotlin.fragmentos.ListaUsuarioFragmento

class FragmentoListaUsuarioAdaptador(activity: FragmentActivity, var usuarioLogin: UsuarioEntidad?): FragmentStateAdapter(activity) {
    private val listaUsuarioString = arrayListOf<String>("ADMIN", "NORMAL", "BAJA")

    override fun createFragment(position: Int): Fragment {

        return if(usuarioLogin?.rol == 1) {
            ListaUsuarioFragmento().newInstance(listaUsuarioString[position], usuarioLogin)
        } else {
            ListaUsuarioFragmento().newInstance(listaUsuarioString[1], usuarioLogin)
        }
    }

    override fun getItemCount(): Int {
        return if(usuarioLogin?.rol == 1) {
            listaUsuarioString.size
        } else {
            1
        }
    }
}