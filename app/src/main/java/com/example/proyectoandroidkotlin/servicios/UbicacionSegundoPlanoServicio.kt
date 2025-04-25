package com.example.proyectoandroidkotlin.servicios

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.IBinder
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.proyectoandroidkotlin.R
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

class UbicacionSegundoPlanoServicio: Service() {

    companion object {
        private const val NOMBRE_CANAL = "UBICACION_SEGUNDO_PLANO"
        private const val DESCRIPCION_NOTIFICACION = "Ubicacion en segundo plano"
        private const val IMPORTANCIA_NOTIFICACION = NotificationManager.IMPORTANCE_HIGH
        private const val ID_CANAL = "ID_UBI"
        private const val ID_NOTIFICACION = 2
    }

    private val fusedLocationProviderClient by lazy { LocationServices.getFusedLocationProviderClient(this) }
    private var locationCallback: LocationCallback ?= null


    override fun onCreate() {
        super.onCreate()
        crearCanalNotificacion()

        locationCallback = object: LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {

                locationResult.lastLocation?.let {
                    val latitud = it.latitude.toString()
                    val longitud = it.longitude.toString()

                    crearNotificacion(latitud, longitud)
                }
            }
        }

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000).apply {
            setMinUpdateDistanceMeters(100F)
            setGranularity(Granularity.GRANULARITY_PERMISSION_LEVEL)
            setWaitForAccurateLocation(true)
        }.build()

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationCallback?.let { fusedLocationProviderClient.requestLocationUpdates(locationRequest, it, Looper.getMainLooper()) }
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        locationCallback?.let { fusedLocationProviderClient.removeLocationUpdates(it) }
    }

    private fun crearCanalNotificacion() {
        val canal = NotificationChannel(ID_CANAL, NOMBRE_CANAL, IMPORTANCIA_NOTIFICACION).apply {
            description = DESCRIPCION_NOTIFICACION
        }

        val notificationManager = ContextCompat.getSystemService(this, NotificationManager::class.java)
        notificationManager?.createNotificationChannel(canal)
    }

    private fun crearNotificacion(latitud: String, longitud: String) {
        val builder = NotificationCompat.Builder(this, ID_CANAL)
            .setSmallIcon(R.drawable.notificacion)
            .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.spiderman))
            .setContentTitle(getString(R.string.titulo_ubicacion))
            .setContentText(getString(R.string.ubicacion_segundo_plano_con_datos, latitud , longitud))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOngoing(true)

        startForeground(ID_NOTIFICACION, builder.build())
    }
}