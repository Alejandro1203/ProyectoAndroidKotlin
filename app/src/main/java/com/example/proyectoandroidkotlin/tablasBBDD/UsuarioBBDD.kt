package com.example.proyectoandroidkotlin.tablasBBDD

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.example.proyectoandroidkotlin.R
import com.example.proyectoandroidkotlin.entidades.EntidadUsuario
import kotlin.properties.Delegates

class UsuarioBBDD(context: Context): SQLiteOpenHelper(context, "DBUsuario", null, 1) {
    private val BBDD_NOMBRE = "DBUsuario"
    private val BBDD_VERSION = 2
    private val TABLA_FORANEA_NOMBRE = "Grupo_Usuario"
    private val CAMPO_FORANEO_ID = "id"
    private val TABLA_NOMBRE = "Usuario"
    private val CAMPO_ID = "id"
    private val CAMPO_NOMBRE = "nombre"
    private val CAMPO_CORREO = "correo"
    private val CAMPO_CONTRASENYA = "contrasenya"
    private val CAMPO_FECHA_NACIMIENTO = "fechaNacimiento"
    private val CAMPO_ROL = "rol"
    private val CAMPO_FOTO_PERFIL = "fotoPerfil"
    private val CAMPO_BAJA = "baja"
    private val CAMPO_GALERIA = "galeria"
    private val CAMPO_ULTIMA_MODIFICACION = "ultimaModificacion"
    private val CAMPO_LATITUD = "latitud"
    private val CAMPO_LONGITUD = "longitud"
    private lateinit var context: Context
    private lateinit var query: String
    private lateinit var valores: ContentValues
    private lateinit var whereClause: String
    private lateinit var whereArgs: Array<String>
    private var filasAfectadas by Delegates.notNull<Int>()
    private lateinit var listaUsuarios: ArrayList<EntidadUsuario>
    private lateinit var usuario: EntidadUsuario
    private lateinit var galeria: String

    private val sqlInsert = "CREATE TABLE " + TABLA_NOMBRE + "(" +
            CAMPO_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            CAMPO_NOMBRE + " TEXT," +
            CAMPO_CORREO + " TEXT," +
            CAMPO_CONTRASENYA + " TEXT," +
            CAMPO_FECHA_NACIMIENTO + " TEXT," +
            CAMPO_ROL + " INTEGER," +
            CAMPO_FOTO_PERFIL + " TEXT," +
            CAMPO_BAJA + " INTEGER," + // 0 == alta && 1 == baja
            CAMPO_GALERIA + " TEXT," +
            CAMPO_ULTIMA_MODIFICACION + " TEXT," +
            CAMPO_LATITUD + " TEXT," +
            CAMPO_LONGITUD + " TEXT," +
            "FOREIGN KEY("+ CAMPO_ROL + ") REFERENCES " + TABLA_FORANEA_NOMBRE + "(" + CAMPO_FORANEO_ID +"));"

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(sqlInsert)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLA_NOMBRE")
        db.execSQL(sqlInsert)
    }

    private fun crearUsuarioByCursor(cursor: Cursor): EntidadUsuario {
        return EntidadUsuario(cursor.getInt(0), cursor.getString(1), cursor.getString(2),
                              cursor.getString(3), cursor.getString(4), cursor.getInt(5),
                              cursor.getString(6), cursor.getInt(7), cursor.getString(8),
                              cursor.getString(9), cursor.getString(10), cursor.getString(11))
    }

    private fun setContentValueByUsuario(usuario: EntidadUsuario) {
        valores = ContentValues()
        valores.put(CAMPO_NOMBRE, usuario.nombre)
        valores.put(CAMPO_CORREO, usuario.correo)
        valores.put(CAMPO_CONTRASENYA, usuario.contrasenya)
        valores.put(CAMPO_FECHA_NACIMIENTO, usuario.fechaNacimiento)
        valores.put(CAMPO_ROL, usuario.rol)
        valores.put(CAMPO_FOTO_PERFIL, usuario.fotoPerfil)
        valores.put(CAMPO_BAJA, usuario.baja)
        valores.put(CAMPO_GALERIA, usuario.galeria)
        valores.put(CAMPO_ULTIMA_MODIFICACION, usuario.ultimaModificacion);
        valores.put(CAMPO_LATITUD, usuario.latitud)
        valores.put(CAMPO_LONGITUD, usuario.longitud)
    }

    fun getAllUsuarios(): ArrayList<EntidadUsuario> {
        listaUsuarios = arrayListOf()
        query = "SELECT * " +
                "FROM " + TABLA_NOMBRE

        try {
            this.readableDatabase.use { db ->
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

    fun insertarUsuario(usuario: EntidadUsuario): Boolean {

        try {
            this.writableDatabase.use { db ->
                setContentValueByUsuario(usuario)

                return db.insert(TABLA_NOMBRE, null, valores).toInt() != -1
            }
        } catch (e: Exception) {
            Log.e(context.getString(R.string.error_Usuario), context.getString(R.string.metodo_insertarUsuario) + ": ", e)
            return false
        }
    }

    fun actualizarUsuario(usuario: EntidadUsuario): Boolean {

        try {
            this.writableDatabase.use { db ->
                setContentValueByUsuario(usuario)
                whereClause = "$CAMPO_ID=?"
                whereArgs = arrayOf(usuario.id.toString())

                filasAfectadas = db.update(TABLA_NOMBRE, valores, whereClause, whereArgs)

                return filasAfectadas > 0
            }
        } catch (e: Exception) {
            Log.e(context.getString(R.string.error_Usuario), context.getString(R.string.metodo_actualizarUsuario) + ": ", e)
            return false
        }
    }

    fun eliminarUsuarioById(id: Int): Boolean {
        try {
            this.writableDatabase.use { db ->
                whereClause = "$CAMPO_ID=?"
                whereArgs = arrayOf(id.toString())

                filasAfectadas = db.delete(TABLA_NOMBRE, whereClause, whereArgs)

                return filasAfectadas > 0
            }
        } catch (e: Exception) {
            Log.e(context.getString(R.string.error_Usuario), context.getString(R.string.metodo_actualizarUsuario) + ": ", e)
            return false
        }
    }

    fun ingresarById(id: Int): Boolean {
        query = "SELECT *" +
                " FROM " + TABLA_NOMBRE +
                " WHERE " + CAMPO_ID + "= ?"
        try {
            this.readableDatabase.use { db ->
                db.rawQuery(query, arrayOf(id.toString())).use { cursor ->
                    return cursor.moveToNext()
                }
            }
        } catch (e: Exception) {
            Log.e(context.getString(R.string.error_Usuario), context.getString(R.string.metodo_ingresarById) + ": ", e)
            return false
        }
    }

    fun getUsuarioById(id: Int): EntidadUsuario {
        query = "SELECT * " +
                "FROM " + TABLA_NOMBRE +
                " WHERE " + CAMPO_ID + "= ?"

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

    fun getAllUsuariosBaja(): ArrayList<EntidadUsuario> {
        listaUsuarios = arrayListOf()
        query = "SELECT *" +
                " FROM " + TABLA_NOMBRE +
                " WHERE " + CAMPO_BAJA + "=1"

        try {
            this.readableDatabase.use { db ->
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

    fun getAllUsuariosByRol(rol: Int): ArrayList<EntidadUsuario> {
        listaUsuarios = arrayListOf()
        query = "SELECT * FROM " + TABLA_NOMBRE +
                " WHERE " + CAMPO_ROL + "= ?" +
                " AND " + CAMPO_BAJA + "=0"

        try {
            this.readableDatabase.use { db ->
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

    fun getRolById(id: Int): Int {
        var rol = -1
        query = "SELECT " + CAMPO_ROL +
                " FROM " + TABLA_NOMBRE +
                " WHERE " + CAMPO_ID +"= ?"

        try {
            this.readableDatabase.use { db ->
                db.rawQuery(query, arrayOf(id.toString())).use { cursor ->
                    if(cursor.moveToNext()) {
                        rol = cursor.getInt(0)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(context.getString(R.string.error_Usuario), context.getString(R.string.metodo_getRolById) + ": ", e)
        }

        return rol
    }

    fun getIdByNombreAndContrasenya(nombre: String, contrasenya: String): Int {
        var id = -1
        query = "SELECT " + CAMPO_ID +
                " FROM " + TABLA_NOMBRE +
                " WHERE " + CAMPO_NOMBRE + "=?"  +
                " AND " + CAMPO_CONTRASENYA + "=?"

        try {
            this.readableDatabase.use { db ->
                db.rawQuery(query, arrayOf(nombre, contrasenya)).use { cursor ->
                    if(cursor.moveToNext()) {
                        id = cursor.getInt(0)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(context.getString(R.string.error_Usuario), context.getString(R.string.metodo_getIdByNombreAndContrasenya) + ": ", e)
        }

        return id
    }

    fun getGaleriaById(id: Int): String {
        galeria = ""
        query = "SELECT " + CAMPO_GALERIA +
                " FROM " + TABLA_NOMBRE +
                " WHERE " + CAMPO_ID + "=?"

        try {
            this.readableDatabase.use { db ->
                db.rawQuery(query, arrayOf(id.toString())).use { cursor ->
                    if(cursor.moveToNext()) {
                        galeria = cursor.getString(0)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(context.getString(R.string.error_Usuario), context.getString(R.string.metodo_getGaleriaById) + ": ", e)
        }

        return galeria
    }

    fun insertarImagenesGaleria(id: Int, imagen: String): Boolean {
        galeria = getGaleriaById(id) + imagen + ";"

        try {
            this.writableDatabase.use { db ->
                valores = ContentValues()
                valores.put(CAMPO_GALERIA, galeria)

                whereClause = "$CAMPO_ID=?"
                whereArgs = arrayOf(id.toString())

                filasAfectadas = db.update(TABLA_NOMBRE, valores, whereClause, whereArgs)

                return filasAfectadas > 0
            }
        } catch (e: Exception) {
            Log.e(context.getString(R.string.error_Usuario), context.getString(R.string.metodo_insertarImagenesGaleria) + ": ", e)
            return false
        }
    }

    fun correoExiste(correo: String): Boolean {
        query = "SELECT 1" +
                " FROM " + TABLA_NOMBRE +
                " WHERE " + CAMPO_CORREO +"=?"

        try {
            this.readableDatabase.use { db ->
                db.rawQuery(query, arrayOf(correo)).use { cursor ->
                    return cursor.moveToNext()
                }
            }
        } catch (e: Exception) {
            Log.e(context.getString(R.string.error_Usuario), context.getString(R.string.metodo_correoExiste) + ": ", e)
            return false
        }
    }

    fun getBajaUsuario(id: Int): Boolean {
        var estaBaja = false
        query = "SELECT " + CAMPO_BAJA +
                " FROM " + TABLA_NOMBRE +
                " WHERE " + CAMPO_ID +"=?"

        try {
            this.readableDatabase.use { db ->
                db.rawQuery(query, arrayOf(id.toString())).use { cursor ->
                    if(cursor.moveToNext()) {
                        estaBaja =  cursor.getInt(0) != 0;
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(context.getString(R.string.error_Usuario), context.getString(R.string.metodo_getBajaUsuario) + ": ", e)
            return false
        }

        return estaBaja
    }


}