package com.example.proyectoandroidkotlin.tablasBBDD

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.example.proyectoandroidkotlin.R
import com.example.proyectoandroidkotlin.entidades.EntidadGrupoUsuario

class GrupoUsuarioBBDD(context: Context): SQLiteOpenHelper(context, "DBGrupoUsuario", null, 1) {
    private val BBDD_NOMBRE = "DBGrupoUsuario"
    private val BBDD_VERSION = 1
    private val TABLA_NOMBRE = "Grupo_Usuario"
    private val CAMPO_ID = "id"
    private val CAMPO_ROL = "rol"
    private lateinit var query: String
    private lateinit var context: Context

    private val sqlCreate = "CREATE TABLE IF NOT EXISTS " + TABLA_NOMBRE + "(" +
                             CAMPO_ID + " INTEGER PRIMARY KEY," +
                             CAMPO_ROL + " TEXT" +
                             ");"

    private val sqlInsert = "INSERT OR REPLACE INTO " + TABLA_NOMBRE +
                            "(" + CAMPO_ID + "," + CAMPO_ROL + ") " +
                            "VALUES (1, 'Administrador'), (2, 'Usuario Normal');"

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(sqlCreate)
        db.execSQL(sqlInsert)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $BBDD_NOMBRE")
        db.execSQL(sqlCreate)
        db.execSQL(sqlInsert)
    }

    fun getAllGrupoUsuarios(): List<EntidadGrupoUsuario> {
        var gruposUsuario = arrayListOf<EntidadGrupoUsuario>()
        var nuevoGrupo: EntidadGrupoUsuario
        query = "SELECT * " +
                "FROM $TABLA_NOMBRE"

        try {
            this.readableDatabase.use { db ->
                db.rawQuery(query, null).use { cursor ->
                    while (cursor.moveToNext()) {
                        nuevoGrupo = EntidadGrupoUsuario(cursor.getInt(0), cursor.getString(1))
                        gruposUsuario.add(nuevoGrupo)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(context.getString(R.string.error_GrupoUsuario), context.getString(R.string.metodo_getAllGrupoUsuarios) + ":", e)
        }

        return gruposUsuario
    }

    fun getRolById(id: Int): String {
        var rol = ""
        query = "SELECT " + CAMPO_ROL +
                " FROM " + TABLA_NOMBRE +
                " WHERE " + CAMPO_ID + "= ?"

        try {
            this.readableDatabase.use { db ->
                db.rawQuery(query, null).use { cursor ->
                    if (cursor.moveToNext()) {
                        rol = cursor.getString(0)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(context.getString(R.string.error_GrupoUsuario), context.getString(R.string.metodo_getRolById) + ":", e)
        }

        return rol
    }
}