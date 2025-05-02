package com.luis.applogin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HistorialFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: EmpresaConPaquetesAdapter
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val mapaTrabajadores = mutableMapOf<String, String>()
    private val mapaEmpresas = mutableMapOf<String, Empresa>()

    private val listenerVacio = object : OnPaqueteEntregadoListener {
        override fun onPaqueteEntregado() {
            // No hacer nada
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_historial, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerHistorial)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val uid = auth.currentUser?.uid ?: return

        db.collection("trabajador").document(uid).get().addOnSuccessListener { doc ->
            val rol = doc.getString("rol") ?: "Empleado"
            if (rol == "Administrador") {
                cargarHistorialAdmin()
            } else {
                cargarHistorialTrabajador(uid, doc.getString("empresaId") ?: "")
            }
        }
    }

    private fun cargarHistorialAdmin() {
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
                        val estado = doc.getString("estado") ?: ""
                        if (estado != "entregado") continue

                        val empresaId = doc.getString("empresaId") ?: continue
                        val timestamp = doc.getTimestamp("fechaRegistro")
                        val fechaRegistro = timestamp?.toDate()?.toString() ?: ""
                        val trabajadorId = doc.getString("trabajadorAsignadoId") ?: ""

                        val paquete = Paquete(
                            uid = doc.id,
                            nombrePaquete = doc.getString("nombrePaquete") ?: "",
                            direccion = doc.getString("Direccion") ?: "",
                            estado = estado,
                            trabajadorAsignadoId = trabajadorId,
                            empresaId = empresaId,
                            fechaRegistro = fechaRegistro
                        )
                        paquetesPorEmpresa.getOrPut(empresaId) { mutableListOf() }.add(paquete)
                    }

                    val listaFinal = mapaEmpresas.map { (empresaId, empresa) ->
                        val paquetes = paquetesPorEmpresa[empresaId] ?: emptyList()
                        EmpresaConPaquetes(empresa, paquetes)
                    }

                    adapter = EmpresaConPaquetesAdapter(listaFinal, mapaEmpresas, mapaTrabajadores, listenerVacio, mostrarBotonEntregar = false)
                    recyclerView.adapter = adapter
                }
            }
        }
    }

    private fun cargarHistorialTrabajador(uid: String, empresaId: String) {
        db.collection("empresas").document(empresaId).get().addOnSuccessListener { empresaDoc ->
            val nombreEmpresa = empresaDoc.getString("nombre") ?: ""
            val imagenUrl = empresaDoc.getString("imagenUrl") ?: ""
            val empresa = Empresa(nombreEmpresa, "", imagenUrl)
            mapaEmpresas[empresaId] = empresa

            db.collection("trabajador").document(uid).get().addOnSuccessListener { trabajadorDoc ->
                val nombreTrabajador = trabajadorDoc.getString("nombre") ?: "Sin nombre"
                mapaTrabajadores[uid] = nombreTrabajador

                db.collection("paquetes")
                    .whereEqualTo("empresaId", empresaId)
                    .whereEqualTo("trabajadorAsignadoId", uid)
                    .get()
                    .addOnSuccessListener { paquetesSnapshot ->
                        val paquetes = paquetesSnapshot.mapNotNull { docPaquete ->
                            val estado = docPaquete.getString("estado") ?: ""
                            if (estado == "entregado") {
                                val fecha = docPaquete.getTimestamp("fechaRegistro")?.toDate()?.toString() ?: ""
                                Paquete(
                                    uid = docPaquete.id,
                                    nombrePaquete = docPaquete.getString("nombrePaquete") ?: "",
                                    direccion = docPaquete.getString("Direccion") ?: "",
                                    estado = estado,
                                    trabajadorAsignadoId = uid,
                                    empresaId = empresaId,
                                    fechaRegistro = fecha
                                )
                            } else null
                        }

                        val listaFinal = listOf(EmpresaConPaquetes(empresa, paquetes))
                        adapter = EmpresaConPaquetesAdapter(listaFinal, mapaEmpresas, mapaTrabajadores, listenerVacio, mostrarBotonEntregar = false)
                        recyclerView.adapter = adapter
                    }
            }
        }
    }
}
