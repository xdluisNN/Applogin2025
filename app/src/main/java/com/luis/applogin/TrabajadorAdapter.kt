package com.luis.applogin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView

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

        fun bind(trabajador: Trabajador) {
            txtNombre.text = trabajador.nombre
            txtEmail.text = trabajador.email

            if (listaEmpresas.isNotEmpty()) {
                spinnerEmpresas.visibility = View.VISIBLE
                btnAsignar.visibility = View.VISIBLE

                // Llenamos spinner con empresas
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
                // Este es trabajador ya con empresa
                spinnerEmpresas.visibility = View.GONE
                btnAsignar.visibility = View.GONE
            }
        }
    }
}
