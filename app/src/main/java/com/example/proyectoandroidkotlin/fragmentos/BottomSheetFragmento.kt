package com.example.proyectoandroidkotlin.fragmentos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.proyectoandroidkotlin.R
import com.example.proyectoandroidkotlin.databinding.BottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class BottomSheetFragmento(): BottomSheetDialogFragment() {
    private lateinit var binding: BottomSheetBinding
    private var interfaz: BottomSheetInterfaz ?= null

    fun setBottomSheetListener(interfaz: BottomSheetInterfaz) {
        this.interfaz = interfaz
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = BottomSheetBinding.inflate(layoutInflater)
        val menu = binding.bottomSheet.menu
        val editar = menu.findItem(R.id.opcionEditar)

        editar.isVisible = !(arguments != null && arguments?.getInt("contador")!! > 1)
        binding.bottomSheet.setNavigationItemSelectedListener { item ->
            val id = item.itemId

            when (id) {
                R.id.opcionEditar -> interfaz?.onEditarListener()
                R.id.opcionEliminar -> interfaz?.onEliminarListener()
            }
            dismiss()

            true
        }

        return binding.root
    }

    interface BottomSheetInterfaz {
        fun onEditarListener()
        fun onEliminarListener()
    }
}