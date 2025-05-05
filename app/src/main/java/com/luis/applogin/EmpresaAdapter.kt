package com.luis.applogin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class EmpresaAdapter(private val listaEmpresas: List<Empresa>) :
    RecyclerView.Adapter<EmpresaAdapter.EmpresaViewHolder>() {

    class EmpresaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nombre: TextView = itemView.findViewById(R.id.textNombre)
        val descripcion: TextView = itemView.findViewById(R.id.textDescripcion)
        val imagen: ImageView = itemView.findViewById(R.id.imageEmpresa)
        val textoNoImagen: TextView = itemView.findViewById(R.id.textNoImage)
        val inicioContrato: TextView = itemView.findViewById(R.id.textInicioContrato)
        val finContrato: TextView = itemView.findViewById(R.id.textFinContrato)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmpresaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.empresa_item, parent, false)
        return EmpresaViewHolder(view)
    }

    override fun onBindViewHolder(holder: EmpresaViewHolder, position: Int) {
        val empresa = listaEmpresas[position]
        holder.nombre.text = empresa.nombre
        holder.descripcion.text = empresa.descripcion
        holder.inicioContrato.text = "Inicio: ${empresa.inicioContrato}"
        holder.finContrato.text = "Fin: ${empresa.finContrato}"

        if (empresa.imagenUrl.isNotEmpty()) {
            holder.textoNoImagen.visibility = View.GONE
            holder.imagen.visibility = View.VISIBLE
            Glide.with(holder.itemView.context)
                .load(empresa.imagenUrl)
                .into(holder.imagen)
        } else {
            holder.imagen.visibility = View.GONE
            holder.textoNoImagen.visibility = View.VISIBLE
        }
    }

    override fun getItemCount(): Int = listaEmpresas.size
}
