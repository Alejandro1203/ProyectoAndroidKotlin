package com.example.proyectoandroidkotlin.adaptadores

import android.content.Context
import android.view.LayoutInflater
import android.view.View.GONE
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.RecyclerView
import com.example.proyectoandroidkotlin.adaptadores.RecyclerViewAdaptador.ViewHolderUsuario
import com.example.proyectoandroidkotlin.databinding.UsuarioBinding
import com.example.proyectoandroidkotlin.entidades.UsuarioEntidad
import com.example.proyectoandroidkotlin.tablasBBDD.GrupoUsuarioBBDD
import androidx.core.net.toUri

class RecyclerViewAdaptador(val context: Context, val listaUsuarios: ArrayList<UsuarioEntidad>, val esAdministrador: Boolean, val adaptadorInterfaz: AdaptadorInterfaz): RecyclerView.Adapter<ViewHolderUsuario>(), Filterable {
    private var listaFiltrada: ArrayList<UsuarioEntidad> = listaUsuarios

    override fun onCreateViewHolder(parent: ViewGroup,viewType: Int): ViewHolderUsuario {
        val binding = UsuarioBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolderUsuario(binding, context, esAdministrador)
    }

    override fun onBindViewHolder(holder: ViewHolderUsuario, position: Int) {
        val usuario = listaFiltrada[position]
        holder.bindUsuario(usuario, position)

        if(usuario.baja == 1) {
            holder.binding.cardView.setCardBackgroundColor("#E15E5B".toColorInt())
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
        return listaFiltrada.size
    }

    override fun getFilter(): Filter {
        return object: Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults? {
                var filtro = constraint.toString().lowercase().trim()

                filtro.let {
                    if(it.isNotEmpty()) {
                        var nuevaListaFiltrada: ArrayList<UsuarioEntidad> = arrayListOf()

                        for(usuario in listaUsuarios) {
                            if(usuario.nombre.lowercase().contains(filtro) || usuario.correo.lowercase().contains(filtro) ||
                               usuario.fechaNacimiento.contains(filtro)) {
                                nuevaListaFiltrada.add(usuario)
                            }
                        }

                        listaFiltrada = nuevaListaFiltrada
                    }
                }

                var resultadosFiltrados = FilterResults()
                resultadosFiltrados.values = listaFiltrada

                return resultadosFiltrados
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults) {
                listaFiltrada = results.values as ArrayList<UsuarioEntidad>
                notifyDataSetChanged()
            }

        }

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

            binding.check.setOnCheckedChangeListener { buttonView, isChecked ->
                adaptadorInterfaz.onCheckChangeListener(position, isChecked)
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
        fun onCheckChangeListener(position: Int, isChecked: Boolean)
    }
}