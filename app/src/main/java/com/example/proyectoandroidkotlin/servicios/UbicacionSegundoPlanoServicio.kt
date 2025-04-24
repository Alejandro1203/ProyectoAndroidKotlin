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
    }

    private val fusedLocationProviderClient by lazy { LocationServices.getFusedLocationProviderClient(this) }
    private var locationCallback: LocationCallback ?= null
    private var latitud: String = ""
    private var longitud: String = ""


    override fun onCreate() {
        super.onCreate()

        locationCallback = object:  LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {

                locationResult.lastLocation?.let {
                    latitud = it.latitude.toString()
                    longitud = it.longitude.toString()

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
        fusedLocationProviderClient.removeLocationUpdates(locationCallback!!)
    }

    private fun crearNotificacion(latitud: String, longitud: String) {
        val canal = NotificationChannel(ID_CANAL, NOMBRE_CANAL, IMPORTANCIA_NOTIFICACION).apply {
            description = DESCRIPCION_NOTIFICACION
        }

        val notificationManager: NotificationManager = this.getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(canal)

        val builder = NotificationCompat.Builder(this, ID_CANAL)
            .setSmallIcon(R.drawable.notificacion)
            .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.spiderman))
            .setContentTitle(getString(R.string.titulo_ubicacion))
            .setContentText(getString(R.string.ubicacion_segundo_plano) + " Latitud:" + latitud + "\nLongitud: " + longitud)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setOngoing(true)

        startForeground(2, builder.build())
    }
}