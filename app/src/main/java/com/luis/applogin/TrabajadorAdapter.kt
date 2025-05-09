package com.luis.applogin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class TrabajadorAdapter(
    private val listaTrabajadores: List<Trabajador>,
    private val listaEmpresas: List<Pair<String, String>>,
    private val onAsignarEmpresa: (Trabajador, String) -> Unit
) : RecyclerView.Adapter<TrabajadorAdapter.TrabajadorViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrabajadorViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_trabajador, parent, false)
        return TrabajadorViewHolder(view)
    }

    override fun onBindViewHolder(holder: TrabajadorViewHolder, position: Int) {
        val trabajador = listaTrabajadores[position]
        holder.bind(trabajador)
    }

    override fun getItemCount() = listaTrabajadores.size

    inner class TrabajadorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtNombre: TextView = itemView.findViewById(R.id.txtNombre)
        private val txtEmail: TextView = itemView.findViewById(R.id.txtEmail)
        private val spinnerEmpresas: Spinner = itemView.findViewById(R.id.spinnerEmpresas)
        private val btnAsignar: Button = itemView.findViewById(R.id.btnAsignar)
        private val btnEstado: Button = itemView.findViewById(R.id.btnEstado) // Botón nuevo

        fun bind(trabajador: Trabajador) {
            txtNombre.text = trabajador.nombre
            txtEmail.text = trabajador.email

            // Llenar Spinner solo si no tiene empresa asignada
            if (trabajador.empresaId.isNullOrEmpty()) {
                spinnerEmpresas.visibility = View.VISIBLE
                btnAsignar.visibility = View.VISIBLE

                val nombresEmpresas = listaEmpresas.map { it.second }
                val adapter = ArrayAdapter(itemView.context, android.R.layout.simple_spinner_item, nombresEmpresas)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerEmpresas.adapter = adapter

                btnAsignar.setOnClickListener {
                    val seleccion = spinnerEmpresas.selectedItemPosition
                    val empresaId = listaEmpresas[seleccion].first
                    onAsignarEmpresa(trabajador, empresaId)
                }
            } else {
                spinnerEmpresas.visibility = View.GONE
                btnAsignar.visibility = View.GONE
            }

            // Botón Activar/Desactivar cuenta
            val estadoActual = trabajador.estado ?: "activo"
            btnEstado.text = if (estadoActual == "activo") "Desactivar Cuenta" else "Activar Cuenta"

            btnEstado.setOnClickListener {
                val nuevoEstado = if (estadoActual == "activo") "inactivo" else "activo"
                trabajador.uid?.let { uid ->
                    actualizarEstadoCuenta(uid, nuevoEstado) {
                        trabajador.estado = nuevoEstado  // Cambiar estado en la lista local
                        notifyItemChanged(adapterPosition)  // Refrescar visualmente el item
                    }
                }
            }
        }

        private fun actualizarEstadoCuenta(uid: String, nuevoEstado: String, onSuccess: () -> Unit) {
            val db = FirebaseFirestore.getInstance()
            db.collection("trabajador").document(uid)
                .update("estado", nuevoEstado)
                .addOnSuccessListener {
                    val mensaje = if (nuevoEstado == "activo") "Cuenta activada" else "Cuenta desactivada"
                    Toast.makeText(itemView.context, mensaje, Toast.LENGTH_SHORT).show()
                    onSuccess()
                }
                .addOnFailureListener {
                    Toast.makeText(itemView.context, "Error al cambiar estado", Toast.LENGTH_SHORT).show()
                }
        }

    }
}
