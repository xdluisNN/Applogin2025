package com.luis.applogin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class PaqueteriaFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: EmpresaConPaquetesAdapter
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_paqueteria, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerEmpresasConPaquetes)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        cargarEmpresasYPaquetes()
    }

    private fun cargarEmpresasYPaquetes() {
        db.collection("empresas").get().addOnSuccessListener { empresasSnapshot ->
            val empresas = empresasSnapshot.documents.map { doc ->
                val empresaId = doc.id
                val nombre = doc.getString("nombre") ?: ""
                val imagenUrl = doc.getString("imagenUrl") ?: ""

                empresaId to Empresa(nombre, "", imagenUrl)
            }.toMap()

            db.collection("paquetes").get().addOnSuccessListener { paquetesSnapshot ->
                val paquetesPorEmpresa = mutableMapOf<String, MutableList<Paquete>>()

                for (doc in paquetesSnapshot) {
                    val empresaId = doc.getString("empresaId") ?: continue
                    val paquete = Paquete(
                        nombrePaquete = doc.getString("nombrePaquete") ?: "",
                        direccion = doc.getString("direccion") ?: "",
                        estado = doc.getString("estado") ?: "",
                        trabajadorAsignadoId = doc.getString("trabajadorAsignadoId") ?: ""
                    )
                    paquetesPorEmpresa.getOrPut(empresaId) { mutableListOf() }.add(paquete)
                }

                val listaFinal = empresas.map { (empresaId, empresa) ->
                    val paquetes = paquetesPorEmpresa[empresaId] ?: emptyList()
                    EmpresaConPaquetes(empresa, paquetes)
                }

                adapter = EmpresaConPaquetesAdapter(listaFinal)
                recyclerView.adapter = adapter
            }
        }
    }
}
