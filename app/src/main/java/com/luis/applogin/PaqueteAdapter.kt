package com.luis.applogin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PaqueteAdapter(private val listaPaquetes: List<Paquete>) :
    RecyclerView.Adapter<PaqueteAdapter.PaqueteViewHolder>() {

    class PaqueteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nombre: TextView = itemView.findViewById(R.id.txtNombrePaquete)
        val direccion: TextView = itemView.findViewById(R.id.txtDireccion)
        val estado: TextView = itemView.findViewById(R.id.txtEstado)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaqueteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_paquete, parent, false)
        return PaqueteViewHolder(view)
    }

    override fun onBindViewHolder(holder: PaqueteViewHolder, position: Int) {
        val paquete = listaPaquetes[position]
        holder.nombre.text = paquete.nombrePaquete
        holder.direccion.text = paquete.direccion
        holder.estado.text = paquete.estado
    }

    override fun getItemCount(): Int = listaPaquetes.size
}
