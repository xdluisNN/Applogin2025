package com.luis.applogin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.Locale

class EmpresaConPaquetesAdapter(
    private val lista: List<EmpresaConPaquetes>,
    private val empresas: Map<String, Empresa>,
    private val trabajador: Map<String, String>
) : RecyclerView.Adapter<EmpresaConPaquetesAdapter.EmpresaViewHolder>() {

    class EmpresaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nombre: TextView = itemView.findViewById(R.id.textNombre)
        val imagen: ImageView = itemView.findViewById(R.id.imageEmpresa)
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

        if (empresa.imagenUrl.isNotEmpty()) {
            holder.imagen.visibility = View.VISIBLE
            Glide.with(holder.itemView.context).load(empresa.imagenUrl).into(holder.imagen)
        } else {
            holder.imagen.visibility = View.GONE
        }

        holder.recyclerPaquetes.layoutManager = LinearLayoutManager(holder.itemView.context)
        holder.recyclerPaquetes.adapter = PaqueteAdapter(item.paquetes) { paquete ->
            val context = holder.itemView.context
            val nombreEmpresa = empresas[paquete.empresaId]?.nombre ?: "Empresa desconocida"
            val nombreTrabajador = trabajador[paquete.trabajadorAsignadoId] ?: "No asignado"
            val fechaFormateada = formatearFecha(paquete.fechaRegistro)

            val mensaje = """
                Nombre: ${paquete.nombrePaquete}
                Dirección: ${paquete.direccion}
                Empresa: $nombreEmpresa
                Fecha de Registro: $fechaFormateada
                Trabajador: $nombreTrabajador
                Estado: ${paquete.estado}
            """.trimIndent()

            android.app.AlertDialog.Builder(context)
                .setTitle("Detalles del Paquete")
                .setMessage(mensaje)
                .setPositiveButton("OK", null)
                .show()
        }
    }

    override fun getItemCount(): Int = lista.size

    private fun formatearFecha(fechaTexto: String): String {
        return try {
            val formatoOriginal = SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH)
            val fecha = formatoOriginal.parse(fechaTexto)
            val formatoNuevo = SimpleDateFormat("dd 'de' MMMM 'de' yyyy, hh:mm a", Locale("es", "ES"))
            fecha?.let { formatoNuevo.format(it) } ?: fechaTexto
        } catch (e: Exception) {
            fechaTexto // Si falla, regresamos el texto original
        }
    }
}
