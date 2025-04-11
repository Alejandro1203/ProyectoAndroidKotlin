package com.example.proyectoandroidkotlin.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.proyectoandroidkotlin.R
import com.example.proyectoandroidkotlin.databinding.LoginLayoutBinding
import com.example.proyectoandroidkotlin.tablasBBDD.UsuarioBBDD
import com.google.android.material.snackbar.Snackbar
import kotlin.properties.Delegates


class LoginActivity: AppCompatActivity() {
    lateinit var loginIntent: Intent
    lateinit var nombre: String
    lateinit var contrasenya: String
    var id by Delegates.notNull<Int>()
    var bundleEnvio = Bundle()
    var usuarioBBDD = UsuarioBBDD(this)
    lateinit var binding: LoginLayoutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LoginLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnInicioSesion.setOnClickListener { v ->
            if(camposRellenos()) {
                if(validarCampos()) {
                    nombre = binding.edtNombre.editText?.text.toString().trim()
                    contrasenya = binding.edtContrasenya.editText?.text.toString().trim()
                    id = usuarioBBDD.getIdByNombreAndContrasenya(nombre, contrasenya)

                    if(id != -1) {
                        if(usuarioBBDD.getBajaUsuario(id)) {
                            Snackbar.make(binding.snackbar, R.string.cuenta_baja, Snackbar.LENGTH_SHORT).show();
                        } else if(usuarioBBDD.ingresarById(id)){
                            inicioSesion(id);
                        } else {
                            Snackbar.make(binding.snackbar, R.string.error_ingreso, Snackbar.LENGTH_SHORT).show();
                        }
                    } else {
                        Snackbar.make(binding.snackbar, R.string.error_no_coincide, Snackbar.LENGTH_SHORT).show();
                    }
                }
            } else {
                Snackbar.make(binding.snackbar, R.string.error_campos_incompletos, Snackbar.LENGTH_SHORT).show();
            }
        }

        binding.txtRegistrar.setOnClickListener { v ->
            startActivity(Intent(this, RegistroActivity::class.java))
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
            Snackbar.make(binding.snackbar, R.string.error_longitud_nombre, Snackbar.LENGTH_SHORT).show();
            return false;
        }
    }

    private fun inicioSesion(id: Int) {
        loginIntent = Intent(this, InicioActivity::class.java)
        bundleEnvio.putSerializable("usuarioLogin", usuarioBBDD.getUsuarioById(id))
        bundleEnvio.putInt("idLogin", id)
        bundleEnvio.putInt("rolLogin", usuarioBBDD.getRolById(id))
        loginIntent.putExtras(bundleEnvio)
        startActivity(loginIntent)
        finish()
    }
}