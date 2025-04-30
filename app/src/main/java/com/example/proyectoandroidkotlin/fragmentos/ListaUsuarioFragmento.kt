package com.example.proyectoandroidkotlin.fragmentos

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.NotificationCompat
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.proyectoandroidkotlin.R
import com.example.proyectoandroidkotlin.activities.LoginActivity
import com.example.proyectoandroidkotlin.activities.RegistroActivity
import com.example.proyectoandroidkotlin.adaptadores.RecyclerViewAdaptador.AdaptadorInterfaz
import com.example.proyectoandroidkotlin.adaptadores.RecyclerViewAdaptador
import com.example.proyectoandroidkotlin.databinding.RecyclerBinding
import com.example.proyectoandroidkotlin.entidades.UsuarioEntidad
import com.example.proyectoandroidkotlin.tablasBBDD.UsuarioBBDD
import com.example.proyectoandroidkotlin.viewmodel.UsuarioViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout
import com.google.android.material.textfield.TextInputLayout

class ListaUsuarioFragmento: Fragment() {

    companion object {
        private const val MOD_USER = 1000
        private const val ID_NOTIFICACION = 1
    }

    private lateinit var binding: RecyclerBinding
    private val usuarioBBDD by lazy { UsuarioBBDD(requireContext()) }
    private var usuarioViewModel: UsuarioViewModel ?= null
    private var recyclerViewAdaptador: RecyclerViewAdaptador ?= null
    private var listaUsuarios: ArrayList<UsuarioEntidad> = arrayListOf()
    private var listaUsuariosCheckeados: ArrayList<UsuarioEntidad> = arrayListOf()
    private var userType: String = ""
    private var usuario: UsuarioEntidad ?= null
    private var usuarioLogin: UsuarioEntidad ?= null
    private var bundleEnvio: Bundle = Bundle()
    private var contadorCheck: Int = 0


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
                arguments?.getSerializable("USUARIO", UsuarioEntidad::class.java)
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

            val tabLayout = requireActivity().findViewById<TabLayout>(R.id.tabLayout)

            if(usuarioLogin?.rol == 1) {
                when(userType) {
                    "ADMIN" -> {
                        val tab = tabLayout.getTabAt(0)
                        val badge = tab?.orCreateBadge
                        badge?.number = listaUsuarios.count { it.rol == 1 && it.baja == 0}
                    }
                    "NORMAL" -> {
                        val tab = tabLayout.getTabAt(1)
                        val badge = tab?.orCreateBadge
                        badge?.number = listaUsuarios.count { it.rol == 2 && it.baja == 0}
                    }
                    "BAJA" -> {
                        val tab = tabLayout.getTabAt(2)
                        val badge = tab?.orCreateBadge
                        badge?.number = listaUsuarios.count { it.baja == 1}
                    }
                }
            } else {
                val tab = tabLayout.getTabAt(0)
                val badge = tab?.orCreateBadge
                badge?.number = listaUsuarios.count { it.rol == 2 && it.baja == 0}
            }

            updateRecyclerView()
        }

        usuarioViewModel?.cargarListaUsuarios(userType, requireContext())

        if(usuarioLogin?.rol == 1) {
            requireActivity().addMenuProvider(object: MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menu.clear()
                    menuInflater.inflate(R.menu.menu, menu)
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                    if(contadorCheck > 0) {
                        if(menuItem.itemId == R.id.abrir_bottom_sheet) {
                            mostrarBottomSheet()
                            return true
                        }
                    }

                    return false
                }
            }, viewLifecycleOwner)
        }
    }

    override fun onResume() {
        super.onResume()

        requireActivity().findViewById<TextInputLayout>(R.id.buscador).editText?.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                recyclerViewAdaptador?.filter?.filter(s)
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })
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

                if(usuario?.id == usuarioLogin?.id) {
                    launcherEditar.launch(intentEditar(usuario))
                } else {
                    crearDialogOpciones(usuario)
                }

                return true
            }

            override fun onCheckChangeListener(position: Int, isChecked: Boolean) {
                usuario = listaUsuarios[position]

                usuario?.let { usuario ->
                    if(isChecked) {
                        listaUsuariosCheckeados.add(usuario)
                        contadorCheck++
                    } else {
                        listaUsuariosCheckeados.remove(usuario)
                        contadorCheck--
                    }
                }
            }
        })

        binding.recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = recyclerViewAdaptador
        }
    }

    private fun mostrarBottomSheet() {
        val bundle = Bundle().apply {
            putInt("contador", contadorCheck)
        }

        val bottomSheetFragmento = BottomSheetFragmento().apply {
            arguments = bundle

            setBottomSheetListener(object: BottomSheetFragmento.BottomSheetInterfaz {
                override fun onEditarListener() {
                    launcherEditar.launch(intentEditar(listaUsuariosCheckeados[0]))
                }

                override fun onEliminarListener() {
                    crearDialogConfirmacion(listaUsuariosCheckeados)
                }
            })
        }

        bottomSheetFragmento.show(parentFragmentManager, tag)
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
            .setPositiveButton(context?.getString(R.string.dialog_eliminar)) { dialog, which ->
                usuario?.let { usuario ->
                    crearDialogConfirmacion(usuario)
                }
            }
            .setNegativeButton(context?.getString(R.string.dialog_editar)) { dialog, which ->
                launcherEditar.launch(intentEditar(usuario))
            }
            .show()
    }

    private fun crearDialogConfirmacion(usuario: UsuarioEntidad) {
        MaterialAlertDialogBuilder(requireContext())
            .setMessage(context?.getString(R.string.dialog_confirmacion))
            .setNeutralButton(context?.getString(R.string.dialog_cancelar), null)
            .setNegativeButton(context?.getString(R.string.dialog_eliminar)) { dialog, which ->
                if(usuarioBBDD.eliminarUsuarioById(usuario.id)) {
                    Toast.makeText(requireContext(), context?.getString(R.string.usuario_eliminado), Toast.LENGTH_SHORT).show()
                    crearNotificacion(usuario, ID_NOTIFICACION)
                    usuarioViewModel?.cargarListaUsuarios(userType, requireContext())
                }
            }
            .show()
    }

    private fun crearDialogConfirmacion(usuarios: ArrayList<UsuarioEntidad>) {
        MaterialAlertDialogBuilder(requireContext())
            .setMessage(context?.getString(R.string.dialog_confirmacion))
            .setNeutralButton(context?.getString(R.string.dialog_cancelar), null)
            .setNegativeButton(context?.getString(R.string.dialog_eliminar)) { dialog, which ->

                var idNotificacion = ID_NOTIFICACION

                for(usuario in usuarios) {
                    if(usuarioBBDD.eliminarUsuarioById(usuario.id)) {
                        idNotificacion++
                        Toast.makeText(requireContext(), context?.getString(R.string.usuario_eliminado), Toast.LENGTH_SHORT).show()
                        crearNotificacion(usuario, idNotificacion)
                        usuarioViewModel?.cargarListaUsuarios(userType, requireContext())
                    }
                }
            }
            .show()
    }

    private fun crearNotificacion(usuario: UsuarioEntidad, idNotificacion: Int) {
        val canalNombre = "CANAL_NOMBRE"
        val descripcion = "Eliminado"
        val importancia = NotificationManager.IMPORTANCE_HIGH
        val canalId = "CANAL_ID"

        val canal = NotificationChannel(canalId, canalNombre, importancia).apply {
            description = descripcion
        }

        val contentIntent = Intent(context, LoginActivity::class.java)
        bundleEnvio.putSerializable("usuarioLogin", usuarioLogin)
        contentIntent.putExtras(bundleEnvio)
        val contentPendingIntent = PendingIntent.getActivity(context, 1, contentIntent, PendingIntent.FLAG_IMMUTABLE)

        val notificationManager: NotificationManager = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(canal)

        val builder = NotificationCompat.Builder(requireContext(), canalId)
            .setSmallIcon(R.drawable.notificacion)
            .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.spiderman))
            .setContentTitle(context?.getString(R.string.usuario_eliminado))
            .setContentText(context?.getString(R.string.usuario_eliminado) + " " + usuario.nombre)
            .setContentIntent(contentPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        notificationManager.notify(idNotificacion, builder.build())
    }
}