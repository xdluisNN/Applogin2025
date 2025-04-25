package com.luis.applogin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class EmpresaConPaquetesAdapter(private val lista: List<EmpresaConPaquetes>) :
    RecyclerView.Adapter<EmpresaConPaquetesAdapter.EmpresaViewHolder>() {

    class EmpresaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nombre: TextView = itemView.findViewById(R.id.textNombre)
        val descripcion: TextView = itemView.findViewById(R.id.textDescripcion)
        val imagen: ImageView = itemView.findViewById(R.id.imageEmpresa)
        val textoNoImagen: TextView = itemView.findViewById(R.id.textNoImage)
        val recyclerPaquetes: RecyclerView = itemView.findViewById(R.id.recyclerPaquetes)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmpresaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_empresa_con_paquetes, parent, false)
        return EmpresaViewHolder(view)
    }

    override fun onBindViewHolder(holder: EmpresaViewHolder, position: Int) {
        val item = lista[position]
        val empresa = item.empresa

        holder.nombre.text = empresa.nombre
        holder.descripcion.text = empresa.descripcion

        if (empresa.imagenUrl.isNotEmpty()) {
            holder.imagen.visibility = View.VISIBLE
            holder.textoNoImagen.visibility = View.GONE
            Glide.with(holder.itemView.context).load(empresa.imagenUrl).into(holder.imagen)
        } else {
            holder.imagen.visibility = View.GONE
            holder.textoNoImagen.visibility = View.VISIBLE
        }

        holder.recyclerPaquetes.layoutManager = LinearLayoutManager(holder.itemView.context)
        holder.recyclerPaquetes.adapter = PaqueteAdapter(item.paquetes)
    }

    override fun getItemCount(): Int = lista.size
}
