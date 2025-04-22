package com.example.proyectoandroidkotlin.entidades

import java.io.Serializable

data class EntidadUsuario(var id: Int = 0, var nombre: String, var correo: String, var contrasenya: String, var fechaNacimiento: String, var rol: Int,
                          var fotoPerfil: String, var baja: Int, var galeria: String, var ultimaModificacion: String, var latitud: String, var longitud: String) : Serializable {

    constructor(nombre: String, correo: String, contrasenya: String, fechaNacimiento: String, rol: Int, fotoPerfil: String, baja: Int, galeria: String,
                ultimaModificacion: String, latitud: String, longitud: String) :
    this(id = 0, nombre = nombre, correo = correo, contrasenya = contrasenya, fechaNacimiento = fechaNacimiento, rol = rol, fotoPerfil = fotoPerfil,
         baja = baja, galeria = galeria, ultimaModificacion = ultimaModificacion, latitud = latitud, longitud = longitud)
}