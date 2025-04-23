package com.example.proyectoandroidkotlin.adaptadores

import android.content.Context
import android.view.LayoutInflater
import android.view.View.GONE
import android.view.ViewGroup
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.RecyclerView
import com.example.proyectoandroidkotlin.adaptadores.RecyclerViewAdaptador.ViewHolderUsuario
import com.example.proyectoandroidkotlin.databinding.UsuarioBinding
import com.example.proyectoandroidkotlin.entidades.UsuarioEntidad
import com.example.proyectoandroidkotlin.tablasBBDD.GrupoUsuarioBBDD
import androidx.core.net.toUri

class RecyclerViewAdaptador(val context: Context, val listaUsuarios: ArrayList<UsuarioEntidad>, val esAdministrador: Boolean, val adaptadorInterfaz: AdaptadorInterfaz): RecyclerView.Adapter<ViewHolderUsuario>() {

    override fun onCreateViewHolder(parent: ViewGroup,viewType: Int): ViewHolderUsuario {
        val binding = UsuarioBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolderUsuario(binding, context, esAdministrador)
    }

    override fun onBindViewHolder(holder: ViewHolderUsuario, position: Int) {
        val usuario = listaUsuarios[position]
        holder.bindUsuario(usuario, position)

        if(usuario.baja == 1) {
            holder.binding.cardView.setBackgroundColor("#E15E5B".toColorInt())
        } else if(usuario.rol == 1) {
            holder.binding.cardView.setCardBackgroundColor("#5B9EE1".toColorInt())
        } else {
            holder.binding.cardView.setCardBackgroundColor("#5BE18E".toColorInt())
        }

        if(esAdministrador) {
            holder.binding.cardView.setOnLongClickListener { adaptadorInterfaz.onLongClickListener(position) }
        } else {
            holder.binding.cardView.setOnClickListener { adaptadorInterfaz.onClickListener(position) }
        }
    }

    override fun getItemCount(): Int {
        return listaUsuarios.size
    }

    inner class ViewHolderUsuario(val binding: UsuarioBinding, val context: Context, val esAdministrador: Boolean): RecyclerView.ViewHolder(binding.root) {

        fun bindUsuario(usuario: UsuarioEntidad, position: Int) {
            binding.fotoUsuario.setImageURI(usuario.fotoPerfil.toUri())
            binding.txtId.text = usuario.id.toString()
            binding.txtNombre.text = usuario.nombre
            binding.txtRol.text = getRolById(usuario.rol)
            binding.txtCorreo.text = usuario.correo
            binding.txtFechaNacimiento.text = usuario.fechaNacimiento

            if(!esAdministrador) {
                binding.check.visibility = GONE
            }
        }

        private fun getRolById(id: Int): String {
            val grupoUsuarioBBDD = GrupoUsuarioBBDD(binding.root.context)
            return grupoUsuarioBBDD.getRolById(id)
        }
    }

    interface AdaptadorInterfaz {
        fun onClickListener(position: Int)
        fun onLongClickListener(position: Int): Boolean
    }
}

