package com.example.proyectoandroidkotlin.tablasBBDD

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.example.proyectoandroidkotlin.R
import com.example.proyectoandroidkotlin.entidades.UsuarioEntidad

class UsuarioBBDD(val context: Context): SQLiteOpenHelper(context, DATABASE_NOMBRE, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NOMBRE = "DBUsuario"
        private const val DATABASE_VERSION = 2
        private const val TABLA_FORANEA_NOMBRE = "Grupo_Usuario"
        private const val COLUMNA_FORANEA_ID = "id"
        private const val TABLA_NOMBRE = "Usuario"
        private const val COLUMNA_ID = "id"
        private const val COLUMNA_NOMBRE = "nombre"
        private const val COLUMNA_CORREO = "correo"
        private const val COLUMNA_CONTRASENYA = "contrasenya"
        private const val COLUMNA_FECHA_NACIMIENTO = "fechaNacimiento"
        private const val COLUMNA_ROL = "rol"
        private const val COLUMNA_FOTO_PERFIL = "fotoPerfil"
        private const val COLUMNA_BAJA = "baja"
        private const val COLUMNA_GALERIA = "galeria"
        private const val COLUMNA_ULTIMA_MODIFICACION = "ultimaModificacion"
        private const val COLUMNA_LATITUD = "latitud"
        private const val COLUMNA_LONGITUD = "longitud"
    }

    private var valores: ContentValues ?= null
    private var whereClause: String ?= null
    private var whereArgs: Array<String> = emptyArray()
    private var listaUsuarios: ArrayList<UsuarioEntidad> = arrayListOf()
    private var usuario: UsuarioEntidad ?= null

    private val sqlInsert = """
        CREATE TABLE $TABLA_NOMBRE (
            $COLUMNA_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $COLUMNA_NOMBRE TEXT,
            $COLUMNA_CORREO TEXT,
            $COLUMNA_CONTRASENYA TEXT,
            $COLUMNA_FECHA_NACIMIENTO TEXT,
            $COLUMNA_ROL INTEGER,
            $COLUMNA_FOTO_PERFIL TEXT,
            $COLUMNA_BAJA INTEGER, 
            $COLUMNA_GALERIA TEXT,
            $COLUMNA_ULTIMA_MODIFICACION TEXT,
            $COLUMNA_LATITUD TEXT,
            $COLUMNA_LONGITUD TEXT,
            FOREIGN KEY($COLUMNA_ROL) REFERENCES $TABLA_FORANEA_NOMBRE($COLUMNA_FORANEA_ID)
        );
    """.trimIndent()  // 0 == alta && 1 == baja

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(sqlInsert)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLA_NOMBRE")
        onCreate(db)
    }

    private fun crearUsuarioByCursor(cursor: Cursor): UsuarioEntidad {
        return UsuarioEntidad(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMNA_ID)), cursor.getString(cursor.getColumnIndexOrThrow(COLUMNA_NOMBRE)),
                              cursor.getString(cursor.getColumnIndexOrThrow(COLUMNA_CORREO)), cursor.getString(cursor.getColumnIndexOrThrow(COLUMNA_CONTRASENYA)),
                              cursor.getString(cursor.getColumnIndexOrThrow(COLUMNA_FECHA_NACIMIENTO)), cursor.getInt(cursor.getColumnIndexOrThrow(COLUMNA_ROL)),
                              cursor.getString(cursor.getColumnIndexOrThrow(COLUMNA_FOTO_PERFIL)), cursor.getInt(cursor.getColumnIndexOrThrow(COLUMNA_BAJA)),
                              cursor.getString(cursor.getColumnIndexOrThrow(COLUMNA_GALERIA)), cursor.getString(cursor.getColumnIndexOrThrow(COLUMNA_ULTIMA_MODIFICACION)),
                              cursor.getString(cursor.getColumnIndexOrThrow(COLUMNA_LATITUD)), cursor.getString(cursor.getColumnIndexOrThrow(COLUMNA_LONGITUD)))
    }

    private fun setContentValueByUsuario(usuario: UsuarioEntidad) {
        valores = ContentValues().apply {
            put(COLUMNA_NOMBRE, usuario.nombre)
            put(COLUMNA_CORREO, usuario.correo)
            put(COLUMNA_CONTRASENYA, usuario.contrasenya)
            put(COLUMNA_FECHA_NACIMIENTO, usuario.fechaNacimiento)
            put(COLUMNA_ROL, usuario.rol)
            put(COLUMNA_FOTO_PERFIL, usuario.fotoPerfil)
            put(COLUMNA_BAJA, usuario.baja)
            put(COLUMNA_GALERIA, usuario.galeria)
            put(COLUMNA_ULTIMA_MODIFICACION, usuario.ultimaModificacion);
            put(COLUMNA_LATITUD, usuario.latitud)
            put(COLUMNA_LONGITUD, usuario.longitud)
        }
    }

    fun getAllUsuarios(): ArrayList<UsuarioEntidad> {
        val query = "SELECT * FROM $TABLA_NOMBRE"

        try {
            readableDatabase.use { db ->
                db.rawQuery(query, null).use { cursor ->
                    while (cursor.moveToNext()) {
                        listaUsuarios.add(crearUsuarioByCursor(cursor))
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(context.getString(R.string.error_Usuario), context.getString(R.string.metodo_getAllUsuarios) + ": ", e)
        }

        return listaUsuarios
    }

    fun getAllUsuariosBaja(): ArrayList<UsuarioEntidad> {
        val query = "SELECT * FROM $TABLA_NOMBRE WHERE $COLUMNA_BAJA=1"

        try {
            readableDatabase.use { db ->
                db.rawQuery(query, null).use { cursor ->
                    while(cursor.moveToNext()) {
                        listaUsuarios.add(crearUsuarioByCursor(cursor))
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(context.getString(R.string.error_Usuario), context.getString(R.string.metodo_getAllUsuariosBaja) + ": ", e)
        }

        return listaUsuarios
    }

    fun getAllUsuariosByRol(rol: Int): ArrayList<UsuarioEntidad> {
        val query = "SELECT * FROM $TABLA_NOMBRE WHERE $COLUMNA_ROL= ? AND $COLUMNA_BAJA = 0"

        try {
            readableDatabase.use { db ->
                db.rawQuery(query, arrayOf(rol.toString())).use { cursor ->
                    while(cursor.moveToNext()) {
                        listaUsuarios.add(crearUsuarioByCursor(cursor))
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(context.getString(R.string.error_Usuario), context.getString(R.string.metodo_getAllUsuariosByRol) + ": ", e)
        }

        return listaUsuarios
    }

    fun getUsuarioById(id: Int): UsuarioEntidad? {
        val query = "SELECT * FROM $TABLA_NOMBRE WHERE $COLUMNA_ID= ?"

        try {
            this.readableDatabase.use { db ->
                db.rawQuery(query, arrayOf(id.toString())).use { cursor ->
                    if(cursor.moveToNext()) {
                        usuario = crearUsuarioByCursor(cursor)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(context.getString(R.string.error_Usuario), context.getString(R.string.metodo_getUsuarioById) + ": ", e)
        }

        return usuario
    }

    fun getIdByNombreAndContrasenya(nombre: String, contrasenya: String): Int {
        val query = "SELECT $COLUMNA_ID FROM $TABLA_NOMBRE WHERE $COLUMNA_NOMBRE=? AND $COLUMNA_CONTRASENYA=?"

        try {
            readableDatabase.use { db ->
                db.rawQuery(query, arrayOf(nombre, contrasenya)).use { cursor ->
                    return if(cursor.moveToNext()) {
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMNA_ID))
                    } else {
                        -1
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(context.getString(R.string.error_Usuario), context.getString(R.string.metodo_getIdByNombreAndContrasenya) + ": ", e)
            return -1
        }
    }

    fun getGaleriaById(id: Int): String {
        var galeria = ""
        val query = "SELECT $COLUMNA_GALERIA FROM $TABLA_NOMBRE WHERE $COLUMNA_ID=?"

        try {
            readableDatabase.use { db ->
                db.rawQuery(query, arrayOf(id.toString())).use { cursor ->
                    if(cursor.moveToNext()) {
                        galeria = cursor.getString(cursor.getColumnIndexOrThrow(COLUMNA_GALERIA))
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(context.getString(R.string.error_Usuario), context.getString(R.string.metodo_getGaleriaById) + ": ", e)
        }

        return galeria
    }

    fun getRolById(id: Int): Int = getUsuarioById(id)?.rol ?: -1

    fun getBajaUsuario(id: Int): Boolean {
        val query = "SELECT $COLUMNA_BAJA FROM $TABLA_NOMBRE WHERE $COLUMNA_ID=?"

        try {
            readableDatabase.use { db ->
                db.rawQuery(query, arrayOf(id.toString())).use { cursor ->
                    return if(cursor.moveToNext()) {
                        cursor.getInt(0) != 0;
                    } else {
                        false
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(context.getString(R.string.error_Usuario), context.getString(R.string.metodo_getBajaUsuario) + ": ", e)
            return false
        }
    }

    fun insertarUsuario(usuario: UsuarioEntidad): Boolean {

        try {
            writableDatabase.use { db ->
                setContentValueByUsuario(usuario)

                return db.insert(TABLA_NOMBRE, null, valores) != -1L
            }
        } catch (e: Exception) {
            Log.e(context.getString(R.string.error_Usuario), context.getString(R.string.metodo_insertarUsuario) + ": ", e)
            return false
        }
    }

    fun actualizarUsuario(usuario: UsuarioEntidad): Boolean {

        try {
            writableDatabase.use { db ->
                setContentValueByUsuario(usuario)
                whereClause = "$COLUMNA_ID=?"
                whereArgs = arrayOf(usuario.id.toString())

                return db.update(TABLA_NOMBRE, valores, whereClause, whereArgs) > 0
            }
        } catch (e: Exception) {
            Log.e(context.getString(R.string.error_Usuario), context.getString(R.string.metodo_actualizarUsuario) + ": ", e)
            return false
        }
    }

    fun eliminarUsuarioById(id: Int): Boolean {

        try {
            writableDatabase.use { db ->
                whereClause = "$COLUMNA_ID=?"
                whereArgs = arrayOf(id.toString())

                return db.delete(TABLA_NOMBRE, whereClause, whereArgs) > 0
            }
        } catch (e: Exception) {
            Log.e(context.getString(R.string.error_Usuario), context.getString(R.string.metodo_actualizarUsuario) + ": ", e)
            return false
        }
    }

    fun insertarImagenesGaleria(id: Int, imagen: String): Boolean {
        var galeria = if(getGaleriaById(id) == "") {
            imagen
        } else {
            getGaleriaById(id) + ";" + imagen
        }

        try {
            writableDatabase.use { db ->
                val valores = ContentValues().apply {
                    put(COLUMNA_GALERIA, galeria)
                }


                whereClause = "$COLUMNA_ID=?"
                whereArgs = arrayOf(id.toString())

                return db.update(TABLA_NOMBRE, valores, whereClause, whereArgs) > 0
            }
        } catch (e: Exception) {
            Log.e(context.getString(R.string.error_Usuario), context.getString(R.string.metodo_insertarImagenesGaleria) + ": ", e)
            return false
        }
    }

    fun correoExiste(correo: String): Boolean {
        val query = "SELECT 1 FROM $TABLA_NOMBRE WHERE $COLUMNA_CORREO=?"

        try {
            readableDatabase.use { db ->
                db.rawQuery(query, arrayOf(correo)).use { cursor ->
                    return cursor.moveToNext()
                }
            }
        } catch (e: Exception) {
            Log.e(context.getString(R.string.error_Usuario), context.getString(R.string.metodo_correoExiste) + ": ", e)
            return false
        }
    }

    fun ingresarById(id: Int): Boolean = getUsuarioById(id) != null
}