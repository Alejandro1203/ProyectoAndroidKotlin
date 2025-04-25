package com.example.proyectoandroidkotlin.activities

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.proyectoandroidkotlin.R
import com.example.proyectoandroidkotlin.adaptadores.GaleriaAdaptador
import com.example.proyectoandroidkotlin.databinding.GaleriaLayoutBinding
import com.example.proyectoandroidkotlin.entidades.UsuarioEntidad
import com.example.proyectoandroidkotlin.tablasBBDD.UsuarioBBDD

class GaleriaActivity: AppCompatActivity(){
    lateinit var binding: GaleriaLayoutBinding
    private val usuarioBBDD by lazy { UsuarioBBDD(this) }
    private var bundleRecogida: Bundle ?= null
    private var usuario: UsuarioEntidad ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = GaleriaLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bundleRecogida = intent.extras

        if(bundleRecogida != null) {
            usuario = obtenerUsuarioSerializable("usuarioEditar")

            val galeriaUsuario = usuario?.let { obtenerGaleriaUsuario(it) }
            binding.galeria.adapter =
                usuario?.nombre?.let { galeriaUsuario?.let { listaImagenes -> GaleriaAdaptador(this, it, listaImagenes) } }
        }
    }

    private fun obtenerUsuarioSerializable(key: String): UsuarioEntidad? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requireNotNull(bundleRecogida?.getSerializable(key, UsuarioEntidad::class.java)) {
                    Log.e(getString(R.string.error_clase_RegistroActivity), getString(R.string.no_usuario_bundle))
                }
            } else {
                @Suppress("DEPRECATION")
                requireNotNull(bundleRecogida?.getSerializable(key) as UsuarioEntidad) {
                    Log.e(getString(R.string.error_clase_RegistroActivity), getString(R.string.no_usuario_bundle))
                }
            }
        } catch (e: Exception) {
            Log.e(getString(R.string.error_clase_RegistroActivity), getString(R.string.no_usuario_bundle) + e)
            null
        }
    }

    private fun obtenerGaleriaUsuario(usuario: UsuarioEntidad): ArrayList<String> {
        return ArrayList(usuarioBBDD.getGaleriaById(usuario.id).split(";"))
    }
}