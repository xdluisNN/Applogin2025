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

class PaqueteriaFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: EmpresaConPaquetesAdapter
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

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

        val uid = auth.currentUser?.uid ?: return

        // 🔍 Consultamos el rol del usuario logueado
        db.collection("trabajador").document(uid).get().addOnSuccessListener { doc ->
            val rol = doc.getString("rol") ?: "Empleado" // Asume "Empleado" si no hay rol
            if (rol == "Administrador") {
                cargarEmpresasYPaquetes()
            } else {
                cargarDatosFiltradosParaTrabajador(uid, doc.getString("empresaId") ?: "")
            }
        }
    }

    // 🔄 Carga todos los paquetes de todas las empresas
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

                    adapter = EmpresaConPaquetesAdapter(listaFinal, mapaEmpresas, mapaTrabajadores)
                    recyclerView.adapter = adapter
                }
            }
        }
    }

    // 🔄 Carga solo los paquetes del trabajador logueado y su empresa
    private fun cargarDatosFiltradosParaTrabajador(uid: String, empresaId: String) {
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
                        val paquetes = paquetesSnapshot.map { docPaquete ->
                            val fecha = docPaquete.getTimestamp("fechaRegistro")?.toDate()?.toString() ?: ""
                            Paquete(
                                nombrePaquete = docPaquete.getString("nombrePaquete") ?: "",
                                direccion = docPaquete.getString("Direccion") ?: "",
                                estado = docPaquete.getString("estado") ?: "",
                                trabajadorAsignadoId = uid,
                                empresaId = empresaId,
                                fechaRegistro = fecha
                            )
                        }

                        val listaFinal = listOf(EmpresaConPaquetes(empresa, paquetes))
                        adapter = EmpresaConPaquetesAdapter(listaFinal, mapaEmpresas, mapaTrabajadores)
                        recyclerView.adapter = adapter
                    }
            }
        }
    }
}
