package com.example.proyectoandroidkotlin.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.proyectoandroidkotlin.R
import com.example.proyectoandroidkotlin.databinding.LoginLayoutBinding
import com.example.proyectoandroidkotlin.entidades.UsuarioEntidad
import com.example.proyectoandroidkotlin.tablasBBDD.UsuarioBBDD
import com.google.android.material.snackbar.Snackbar
import kotlin.properties.Delegates

class LoginActivity: AppCompatActivity() {
    private val binding by lazy { LoginLayoutBinding.inflate(layoutInflater) }
    private var loginIntent: Intent ?= null
    private var nombre: String = ""
    private var contrasenya: String = ""
    private var id by Delegates.notNull<Int>()
    private var usuario: UsuarioEntidad? = null
    private var bundleEnvio = Bundle()
    private var bundleRecogida: Bundle? = null
    private var usuarioBBDD = UsuarioBBDD(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        bundleRecogida = intent.extras

        if(bundleRecogida != null) {
            usuario = try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    requireNotNull(bundleRecogida?.getSerializable("usuarioLogin", UsuarioEntidad::class.java)) {
                        Log.e(getString(R.string.error_clase_LoginActivity), getString(R.string.no_usuario_bundle))
                    }
                } else {
                    @Suppress("DEPRECATION")
                    requireNotNull(bundleRecogida?.getSerializable("usuarioLogin") as UsuarioEntidad) {
                        Log.e(getString(R.string.error_clase_LoginActivity), getString(R.string.no_usuario_bundle))
                    }
                }
            } catch (e: Exception) {
                Log.e(getString(R.string.error_clase_LoginActivity), getString(R.string.no_usuario_bundle) + e)
                null
            }

            usuario?.id?.let { usuario -> inicioSesion(usuario) }
        }

        binding.btnInicioSesion.setOnClickListener { v ->
            if(camposRellenos()) {
                if(validarCampos()) {
                    nombre = binding.edtNombre.editText?.text.toString().trim()
                    contrasenya = binding.edtContrasenya.editText?.text.toString().trim()
                    id = usuarioBBDD.getIdByNombreAndContrasenya(nombre, contrasenya)

                    if(id != -1) {
                        if(usuarioBBDD.getBajaUsuario(id)) {
                            Snackbar.make(binding.snackbar, R.string.cuenta_baja, Snackbar.LENGTH_SHORT).show()
                        } else if(usuarioBBDD.ingresarById(id)){
                            inicioSesion(id)
                        } else {
                            Snackbar.make(binding.snackbar, R.string.error_ingreso, Snackbar.LENGTH_SHORT).show()
                        }
                    } else {
                        Snackbar.make(binding.snackbar, R.string.error_no_coincide, Snackbar.LENGTH_SHORT).show()
                    }
                }
            } else {
                Snackbar.make(binding.snackbar, R.string.error_campos_incompletos, Snackbar.LENGTH_SHORT).show()
            }
        }

        binding.txtRegistrar.setOnClickListener { v ->
            startActivity(Intent(this, RegistroActivity::class.java))
            finish()
        }
    }

    private fun camposRellenos(): Boolean {
        return !binding.edtNombre.editText?.text.toString().trim().isEmpty() &&
               !binding.edtContrasenya.editText?.text.toString().trim().isEmpty()
    }

    private fun validarCampos(): Boolean {
        return validarNombre()
    }

    private fun validarNombre(): Boolean {
        if(binding.edtNombre.editText?.text.toString().length <= 20) {
            return true
        } else {
            Snackbar.make(binding.snackbar, R.string.error_longitud_nombre, Snackbar.LENGTH_SHORT).show()
            return false
        }
    }

    private fun inicioSesion(id: Int) {
        loginIntent = Intent(this, InicioActivity::class.java)
        bundleEnvio.putSerializable("usuarioLogin", usuarioBBDD.getUsuarioById(id))
        bundleEnvio.putInt("idLogin", id)
        bundleEnvio.putInt("rolLogin", usuarioBBDD.getRolById(id))
        loginIntent?.putExtras(bundleEnvio)
        startActivity(loginIntent)
        finish()
    }
}