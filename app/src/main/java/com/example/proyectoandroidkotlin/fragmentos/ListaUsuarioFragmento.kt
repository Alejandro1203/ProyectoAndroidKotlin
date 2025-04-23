package com.example.proyectoandroidkotlin.fragmentos

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.proyectoandroidkotlin.activities.RegistroActivity
import com.example.proyectoandroidkotlin.adaptadores.RecyclerViewAdaptador.AdaptadorInterfaz
import com.example.proyectoandroidkotlin.adaptadores.RecyclerViewAdaptador
import com.example.proyectoandroidkotlin.databinding.RecyclerBinding
import com.example.proyectoandroidkotlin.entidades.UsuarioEntidad
import com.example.proyectoandroidkotlin.viewmodel.UsuarioViewModel

class ListaUsuarioFragmento: Fragment() {
    private lateinit var binding: RecyclerBinding
    private var usuarioViewModel: UsuarioViewModel ?= null
    private var recyclerViewAdaptador: RecyclerViewAdaptador ?= null
    private var listaUsuarios: ArrayList<UsuarioEntidad> = arrayListOf()
    private var userType: String = ""
    private var usuario: UsuarioEntidad ?= null
    private var usuarioLogin: UsuarioEntidad ?= null
    private var bundleEnvio: Bundle = Bundle()
    private final var MOD_USER = 1000

    fun newInstance(userType: String, usuario: UsuarioEntidad?): ListaUsuarioFragmento {
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

            usuarioLogin = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                arguments?.getSerializable("USUARIO", UsuarioEntidad::class.java)!!
            } else {
                @Suppress("DEPRECATION")
                arguments?.getSerializable("USUARIO") as UsuarioEntidad
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = RecyclerBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        usuarioViewModel = ViewModelProvider(this)[UsuarioViewModel::class.java]

        usuarioViewModel?.getListaUsuarios()?.observe(viewLifecycleOwner) { usuarios ->
            listaUsuarios = ArrayList(usuarios)
            updateRecyclerView()
        }

        usuarioViewModel?.cargarListaUsuarios(userType, requireContext())

        updateRecyclerView()
    }

    private fun updateRecyclerView() {
        recyclerViewAdaptador = RecyclerViewAdaptador(requireContext(), listaUsuarios, usuarioLogin?.rol == 1, object : AdaptadorInterfaz {
            override fun onClickListener(position: Int) {
                usuario = listaUsuarios[position]

                if(usuario?.id == usuarioLogin?.id) {
                    launcherEditar.launch(intentEditar())
                }
            }

            override fun onLongClickListener(position: Int): Boolean {
                usuario = listaUsuarios[position]
                launcherEditar.launch(intentEditar())

                return true
            }
        })

        binding.recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = recyclerViewAdaptador
        }
    }

    private fun intentEditar(): Intent {
        var intentEditar = Intent(context, RegistroActivity::class.java)
        bundleEnvio.putSerializable("usuarioEditar", usuario)
        bundleEnvio.putSerializable("usuarioEditor", usuarioLogin)
        intentEditar.putExtras(bundleEnvio)

        return intentEditar
    }

    private val launcherEditar: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { result ->
            if(result.resultCode == MOD_USER && result.data != null) {
                updateRecyclerView()
            }
        }
}