package com.example.proyectoandroidkotlin.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.proyectoandroidkotlin.R
import com.example.proyectoandroidkotlin.adaptadores.FragmentoListaUsuarioAdaptador
import com.example.proyectoandroidkotlin.databinding.InicioLayoutBinding
import com.example.proyectoandroidkotlin.entidades.UsuarioEntidad
import com.example.proyectoandroidkotlin.servicios.UbicacionSegundoPlanoServicio
import com.google.android.material.tabs.TabLayoutMediator

class InicioActivity: AppCompatActivity() {

    companion object {
        private const val REQUEST_CODE_PERMISO_NOTIFICACION = 500
        private const val REQUEST_CODE_BACKGROUND_LOCATION = 7000;
    }

    private val binding by lazy { InicioLayoutBinding.inflate(layoutInflater) }
    private var usuario: UsuarioEntidad? = null
    private var bundleRecogida: Bundle? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.topAppBar)

        if(tienePermisoBackGround()) {
            startForegroundService(Intent(this, UbicacionSegundoPlanoServicio::class.java))
        } else {
            pedirPermisoBackGround()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pedirPermisoNotificacion()
        }

        bundleRecogida = intent.extras

        if(bundleRecogida != null) {
            usuario = try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    requireNotNull(bundleRecogida?.getSerializable("usuarioLogin", UsuarioEntidad::class.java)) {
                        Log.e(getString(R.string.error_clase_InicioActivity), getString(R.string.no_usuario_bundle))
                    }
                } else {
                    @Suppress("DEPRECATION")
                    requireNotNull(bundleRecogida?.getSerializable("usuarioLogin") as UsuarioEntidad) {
                        Log.e(getString(R.string.error_clase_InicioActivity), getString(R.string.no_usuario_bundle))
                    }
                }
            } catch (e: Exception) {
                Log.e(getString(R.string.error_clase_InicioActivity), getString(R.string.no_usuario_bundle) + e)
                null
            }

            usuario?.let { usuario -> setViewPagerAdapter(usuario) }
        }

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

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onResume() {
        super.onResume()
        usuario?.let { usuario -> setViewPagerAdapter(usuario) }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopService(Intent(this, UbicacionSegundoPlanoServicio::class.java))
    }

    private fun setViewPagerAdapter(usuario: UsuarioEntidad) {
        binding.viewpager.adapter = FragmentoListaUsuarioAdaptador(this, usuario)
        binding.viewpager.offscreenPageLimit = 3
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun pedirPermisoNotificacion() {
        if(!tienePermisoNotificacion()) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), REQUEST_CODE_PERMISO_NOTIFICACION)
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun tienePermisoNotificacion(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    }

    private fun pedirPermisoBackGround() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION), REQUEST_CODE_BACKGROUND_LOCATION);
    }

    private fun tienePermisoBackGround(): Boolean {
        return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String?>, grantResults: IntArray, deviceId: Int) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults, deviceId)

        if (requestCode == REQUEST_CODE_BACKGROUND_LOCATION && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startForegroundService(Intent(this, UbicacionSegundoPlanoServicio::class.java))
        } else {
            pedirPermisoBackGround();
        }
    }
}