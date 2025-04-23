package com.example.proyectoandroidkotlin.fragmentos

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.proyectoandroidkotlin.R
import com.example.proyectoandroidkotlin.activities.InicioActivity
import com.example.proyectoandroidkotlin.activities.RegistroActivity
import com.example.proyectoandroidkotlin.adaptadores.FragmentoListaUsuarioAdaptador
import com.example.proyectoandroidkotlin.adaptadores.RecyclerViewAdaptador.AdaptadorInterfaz
import com.example.proyectoandroidkotlin.adaptadores.RecyclerViewAdaptador
import com.example.proyectoandroidkotlin.databinding.RecyclerBinding
import com.example.proyectoandroidkotlin.entidades.UsuarioEntidad
import com.example.proyectoandroidkotlin.tablasBBDD.UsuarioBBDD
import com.example.proyectoandroidkotlin.viewmodel.UsuarioViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ListaUsuarioFragmento: Fragment() {

    companion object {
        private const val MOD_USER = 1000
    }

    private lateinit var binding: RecyclerBinding
    private val usuarioBBDD by lazy { UsuarioBBDD(requireContext()) }
    private var usuarioViewModel: UsuarioViewModel ?= null
    private var recyclerViewAdaptador: RecyclerViewAdaptador ?= null
    private var listaUsuarios: ArrayList<UsuarioEntidad> = arrayListOf()
    private var userType: String = ""
    private var usuario: UsuarioEntidad ?= null
    private var usuarioLogin: UsuarioEntidad ?= null
    private var bundleEnvio: Bundle = Bundle()


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
    }

    private fun updateRecyclerView() {
        recyclerViewAdaptador = RecyclerViewAdaptador(requireContext(), listaUsuarios, usuarioLogin?.rol == 1, object : AdaptadorInterfaz {
            override fun onClickListener(position: Int) {
                usuario = listaUsuarios[position]

                if(usuario?.id == usuarioLogin?.id) {
                    launcherEditar.launch(intentEditar(usuario))
                }
            }

            override fun onLongClickListener(position: Int): Boolean {
                usuario = listaUsuarios[position]
                crearDialogOpciones(usuario)

                return true
            }
        })

        binding.recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = recyclerViewAdaptador
        }
    }

    private fun intentEditar(usuario: UsuarioEntidad?): Intent {
        var intentEditar = Intent(context, RegistroActivity::class.java)
        bundleEnvio.putSerializable("usuarioEditar", usuario)
        bundleEnvio.putSerializable("usuarioEditor", usuarioLogin)
        intentEditar.putExtras(bundleEnvio)

        return intentEditar
    }

    private val launcherEditar: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { result ->
            if(result.resultCode == MOD_USER && result.data != null) {
                usuarioViewModel?.cargarListaUsuarios(userType, requireContext())
            }
    }

    private fun crearDialogOpciones(usuario: UsuarioEntidad?) {
        MaterialAlertDialogBuilder(requireContext())
            .setMessage(context?.getString(R.string.messageDialogUsuario))
            .setNeutralButton(context?.getString(R.string.dialog_cancelar), null)
            .setNegativeButton(context?.getString(R.string.dialog_eliminar)) { dialog, which ->
                usuario?.let {
                    if(usuarioBBDD.eliminarUsuarioById(usuario.id)) {
                        Toast.makeText(requireContext(), context?.getString(R.string.usuario_eliminado), Toast.LENGTH_SHORT).show()
                        usuarioViewModel?.cargarListaUsuarios(userType, requireContext())
                    }
                }
            }
            .setPositiveButton(context?.getString(R.string.dialog_editar)) { dialog, which ->
                launcherEditar.launch(intentEditar(usuario))
            }
            .show()
    }
}