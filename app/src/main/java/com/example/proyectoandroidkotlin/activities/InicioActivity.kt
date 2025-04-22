package com.example.proyectoandroidkotlin.activities

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.proyectoandroidkotlin.R
import com.example.proyectoandroidkotlin.adaptadores.AdaptadorFragmentoListaUsuarios
import com.example.proyectoandroidkotlin.databinding.InicioLayoutBinding
import com.example.proyectoandroidkotlin.entidades.EntidadUsuario
import com.google.android.material.tabs.TabLayoutMediator

class InicioActivity: AppCompatActivity() {
    private val binding by lazy { InicioLayoutBinding.inflate(layoutInflater) }
    private var usuario: EntidadUsuario? = null
    private var bundleRecogida: Bundle? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        bundleRecogida = intent.extras

        usuario = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requireNotNull(bundleRecogida?.getSerializable("usuarioLogin", EntidadUsuario::class.java)) {
                Log.e(getString(R.string.error_clase_InicioActivity), getString(R.string.no_usuario_bundle))
            }
        } else {
            @Suppress("DEPRECATION")
            requireNotNull(bundleRecogida?.getSerializable("usuarioLogin") as EntidadUsuario) {
                Log.e(getString(R.string.error_clase_InicioActivity), getString(R.string.no_usuario_bundle))
            }
        }

        binding.viewpager.adapter = AdaptadorFragmentoListaUsuarios(this, usuario)
        binding.viewpager.offscreenPageLimit = 3

        TabLayoutMediator(binding.tabLayout, binding.viewpager) { tab, position ->

            if(usuario?.rol == 1) {
                when (position) {
                    0 -> tab.text = getString(R.string.administradores)
                    1 -> tab.text = getString(R.string.usuarios_normales)
                    2 -> tab.text = getString(R.string.bajas)
                    else -> tab.text = getString(R.string.administradores)
                }
            } else {
                tab.text = getString(R.string.usuarios_normales)
            }
        }.attach()
    }
}