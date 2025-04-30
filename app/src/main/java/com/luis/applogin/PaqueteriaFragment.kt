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

class PaqueteriaFragment : Fragment(), OnPaqueteEntregadoListener {  // ✅ implementamos listener

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: EmpresaConPaquetesAdapter
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val mapaTrabajadores = mutableMapOf<String, String>()
    private val mapaEmpresas = mutableMapOf<String, Empresa>()
    private var tipoUsuario: String = "Empleado"  // ✅ nuevo campo para saber tipo

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

        db.collection("trabajador").document(uid).get().addOnSuccessListener { doc ->
            tipoUsuario = doc.getString("rol") ?: "Empleado"
            if (tipoUsuario == "Administrador") {
                cargarEmpresasYPaquetes()
            } else {
                cargarDatosFiltradosParaTrabajador(uid, doc.getString("empresaId") ?: "")
            }
        }
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
                        val estadoPaquete = doc.getString("estado") ?: ""
                        if (estadoPaquete != "pendiente") continue

                        val timestamp = doc.getTimestamp("fechaRegistro")
                        val fechaRegistro = timestamp?.toDate()?.toString() ?: ""
                        val trabajadorId = doc.getString("trabajadorAsignadoId") ?: ""

                        val paquete = Paquete(
                            nombrePaquete = doc.getString("nombrePaquete") ?: "",
                            direccion = doc.getString("Direccion") ?: "",
                            estado = estadoPaquete,
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

                    adapter = EmpresaConPaquetesAdapter(listaFinal, mapaEmpresas, mapaTrabajadores, this@PaqueteriaFragment)  // ✅ listener
                    recyclerView.adapter = adapter
                }
            }
        }
    }

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
                        val paquetes = paquetesSnapshot.mapNotNull { docPaquete ->
                            val estadoPaquete = docPaquete.getString("estado") ?: ""
                            if (estadoPaquete == "pendiente") {
                                val fecha = docPaquete.getTimestamp("fechaRegistro")?.toDate()?.toString() ?: ""
                                Paquete(
                                    nombrePaquete = docPaquete.getString("nombrePaquete") ?: "",
                                    direccion = docPaquete.getString("Direccion") ?: "",
                                    estado = estadoPaquete,
                                    trabajadorAsignadoId = uid,
                                    empresaId = empresaId,
                                    fechaRegistro = fecha
                                )
                            } else {
                                null
                            }
                        }

                        val listaFinal = listOf(EmpresaConPaquetes(empresa, paquetes))
                        adapter = EmpresaConPaquetesAdapter(listaFinal, mapaEmpresas, mapaTrabajadores, this@PaqueteriaFragment)  // ✅ listener
                        recyclerView.adapter = adapter
                    }
            }
        }
    }

    // ✅ Esta función se llama automáticamente cuando entregas un paquete
    override fun onPaqueteEntregado() {
        recargarDatos()
    }

    // ✅ Decide qué función recargar según tipo usuario
    private fun recargarDatos() {
        val uid = auth.currentUser?.uid ?: return
        if (tipoUsuario == "Administrador") {
            cargarEmpresasYPaquetes()
        } else {
            db.collection("trabajador").document(uid).get().addOnSuccessListener { doc ->
                cargarDatosFiltradosParaTrabajador(uid, doc.getString("empresaId") ?: "")
            }
        }
    }
}
