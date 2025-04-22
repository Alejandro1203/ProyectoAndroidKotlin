package com.example.proyectoandroidkotlin.fragmentos

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.proyectoandroidkotlin.adaptadores.AdaptadorRecyclerView.AdaptadorInterfaz
import com.example.proyectoandroidkotlin.adaptadores.AdaptadorRecyclerView
import com.example.proyectoandroidkotlin.databinding.RecyclerBinding
import com.example.proyectoandroidkotlin.entidades.EntidadUsuario
import com.example.proyectoandroidkotlin.viewmodel.ViewModelUsuario

class ListaUsuarioFragmento: Fragment() {
    private lateinit var binding: RecyclerBinding
    private var viewModelUsuario: ViewModelUsuario ?= null
    private var adaptadorRecyclerView: AdaptadorRecyclerView ?= null
    private var listaUsuarios: ArrayList<EntidadUsuario> = arrayListOf()
    private var userType: String = ""
    private var usuario: EntidadUsuario ?= null

    fun newInstance(userType: String, usuario: EntidadUsuario?): ListaUsuarioFragmento {
        var fragmento = ListaUsuarioFragmento()
        val args = Bundle()
        args.putString("USER_TYPE", userType)
        args.putSerializable("USUARIO", usuario)
        fragmento.arguments = args

        return fragmento
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if(arguments != null) {
            userType = requireArguments().getString("USER_TYPE", "")

            usuario = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                arguments?.getSerializable("USUARIO", EntidadUsuario::class.java)!!
            } else {
                @Suppress("DEPRECATION")
                arguments?.getSerializable("USUARIO") as EntidadUsuario
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = RecyclerBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModelUsuario = ViewModelProvider(this)[ViewModelUsuario::class.java]

        viewModelUsuario?.getListaUsuarios()?.observe(viewLifecycleOwner) { usuarios ->
            listaUsuarios = ArrayList(usuarios)
            updateRecyclerView()
        }

        viewModelUsuario?.cargarListaUsuarios(userType, requireContext())

        updateRecyclerView()
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