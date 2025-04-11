package com.example.proyectoandroidkotlin.entidades

import java.io.Serializable

class EntidadUsuario: Serializable {
    var id = 0
    var nombre = ""
    var correo = ""
    var contrasenya = ""
    var fechaNacimiento = ""
    var rol = 0
    var fotoPerfil = ""
    var baja = 0
    var galeria = ""
    var ultimaModificacion = ""
    var latitud = ""
    var longitud = ""

    constructor(nombre: String, correo: String, contrasenya: String, fechaNacimiento: String, rol: Int, fotoPerfil: String,
                baja: Int, galeria: String, ultimaModificacion: String, latitud: String, longitud: String) {
        this.nombre = nombre
        this.correo = correo
        this.contrasenya = contrasenya
        this.fechaNacimiento = fechaNacimiento
        this.rol = rol
        this.fotoPerfil = fotoPerfil
        this.baja = baja
        this.galeria = galeria
        this.ultimaModificacion = ultimaModificacion
        this.latitud = latitud
        this.longitud = longitud
    }

    constructor(id: Int, nombre: String, correo: String, contrasenya: String, fechaNacimiento: String, rol: Int, fotoPerfil: String,
                baja: Int, galeria: String, ultimaModificacion: String, latitud: String, longitud: String) {
        this.id = id
        this.nombre = nombre
        this.correo = correo
        this.contrasenya = contrasenya
        this.fechaNacimiento = fechaNacimiento
        this.rol = rol
        this.fotoPerfil = fotoPerfil
        this.baja = baja
        this.galeria = galeria
        this.ultimaModificacion = ultimaModificacion
        this.latitud = latitud
        this.longitud = longitud
    }
}