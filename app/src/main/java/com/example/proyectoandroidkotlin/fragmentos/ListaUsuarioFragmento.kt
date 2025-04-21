package com.example.proyectoandroidkotlin.fragmentos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.proyectoandroidkotlin.adaptadores.AdaptadorInterfaz
import com.example.proyectoandroidkotlin.adaptadores.AdaptadorRecyclerView
import com.example.proyectoandroidkotlin.databinding.RecyclerBinding
import com.example.proyectoandroidkotlin.entidades.EntidadUsuario

class ListaUsuarioFragmento: Fragment() {
    lateinit var binding: RecyclerBinding
    lateinit var adaptadorRecyclerView: AdaptadorRecyclerView
    lateinit var listaUsuarios: ArrayList<EntidadUsuario>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = RecyclerBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        updateRecyclerView()
    }

    private fun cargarListaUsuarios() {

    }

    private fun updateRecyclerView() {
        adaptadorRecyclerView = AdaptadorRecyclerView(requireContext(), listaUsuarios, true, object : AdaptadorInterfaz {
            override fun onClickListener(position: Int) {
                TODO("Not yet implemented")
            }

            override fun onLongClickListener(position: Int): Boolean {
                TODO("Not yet implemented")
            }
        })

        binding.recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = adaptadorRecyclerView
        }
    }
}