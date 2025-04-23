package com.example.proyectoandroidkotlin.tablasBBDD

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.example.proyectoandroidkotlin.R
import com.example.proyectoandroidkotlin.entidades.GrupoUsuarioEntidad

class GrupoUsuarioBBDD(val context: Context): SQLiteOpenHelper(context, DATABASE_NOMBRE, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NOMBRE = "DBGrupoUsuario"
        private const val DATABASE_VERSION = 1
        private const val TABLA_NOMBRE = "Grupo_Usuario"
        private const val COLUMNA_ID = "id"
        private const val COLUMNA_ROL = "rol"
    }

    private val sqlCreate = """
        CREATE TABLE IF NOT EXISTS $TABLA_NOMBRE (
            $COLUMNA_ID INTEGER PRIMARY KEY,
            $COLUMNA_ROL TEXT
        );
    """.trimIndent()

    private val sqlInsert = """
        INSERT OR REPLACE INTO $TABLA_NOMBRE($COLUMNA_ID, $COLUMNA_ROL)
        VALUES (1, 'Administrador'), (2, 'Usuario Normal');
    """.trimIndent()

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(sqlCreate)
        db.execSQL(sqlInsert)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLA_NOMBRE")
        onCreate(db)
    }

    fun getAllGrupoUsuarios(): List<GrupoUsuarioEntidad> {
        val gruposUsuario = mutableListOf<GrupoUsuarioEntidad>()
        var nuevoGrupo: GrupoUsuarioEntidad
        val query = "SELECT * FROM $TABLA_NOMBRE"

        try {
            readableDatabase.use { db ->
                db.rawQuery(query, null).use { cursor ->
                    while (cursor.moveToNext()) {
                        nuevoGrupo = GrupoUsuarioEntidad(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMNA_ID)), cursor.getString(cursor.getColumnIndexOrThrow(COLUMNA_ROL)))
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
        val query = "SELECT $COLUMNA_ROL FROM $TABLA_NOMBRE WHERE $COLUMNA_ID= ?"

        try {
            readableDatabase.use { db ->
                db.rawQuery(query, arrayOf(id.toString())).use { cursor ->
                    if (cursor.moveToFirst()) {
                        rol = cursor.getString(cursor.getColumnIndexOrThrow(COLUMNA_ROL))
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(context.getString(R.string.error_GrupoUsuario), context.getString(R.string.metodo_getRolById) + ":", e)
        }

        return rol
    }
}