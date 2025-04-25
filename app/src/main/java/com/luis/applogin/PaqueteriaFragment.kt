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
    private val mapaTrabajadores = mutableMapOf<String, String>()
    private val mapaEmpresas = mutableMapOf<String, Empresa>()

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
        db.collection("trabajador").get().addOnSuccessListener { trabajadoresSnapshot ->
            for (doc in trabajadoresSnapshot) {
                val trabajadorId = doc.id
                val nombreTrabajador = doc.getString("nombre") ?: ""
                mapaTrabajadores[trabajadorId] = nombreTrabajador
            }

            db.collection("empresas").get().addOnSuccessListener { empresasSnapshot ->
                for (doc in empresasSnapshot) {
                    val empresaId = doc.id
                    val nombre = doc.getString("nombre") ?: ""
                    val imagenUrl = doc.getString("imagenUrl") ?: ""

                    mapaEmpresas[empresaId] = Empresa(nombre, "", imagenUrl)
                }

                db.collection("paquetes").get().addOnSuccessListener { paquetesSnapshot ->
                    val paquetesPorEmpresa = mutableMapOf<String, MutableList<Paquete>>()

                    for (doc in paquetesSnapshot) {
                        val empresaId = doc.getString("empresaId") ?: continue
                        val timestamp = doc.getTimestamp("fechaRegistro")
                        val fechaRegistro = timestamp?.toDate()?.toString() ?: ""

                        val trabajadorId = doc.getString("trabajadorAsignadoId") ?: ""

                        val paquete = Paquete(
                            nombrePaquete = doc.getString("nombrePaquete") ?: "",
                            direccion = doc.getString("Direccion") ?: "",
                            estado = doc.getString("estado") ?: "",
                            trabajadorAsignadoId = trabajadorId,  // <- GUARDAMOS EL ID
                            empresaId = empresaId,
                            fechaRegistro = fechaRegistro
                        )
                        paquetesPorEmpresa.getOrPut(empresaId) { mutableListOf() }.add(paquete)
                    }

                    val listaFinal = mapaEmpresas.map { (empresaId, empresa) ->
                        val paquetes = paquetesPorEmpresa[empresaId] ?: emptyList()
                        EmpresaConPaquetes(empresa, paquetes)
                    }

                    adapter = EmpresaConPaquetesAdapter(listaFinal, mapaEmpresas, mapaTrabajadores)
                    recyclerView.adapter = adapter
                }
            }
        }
    }

}
