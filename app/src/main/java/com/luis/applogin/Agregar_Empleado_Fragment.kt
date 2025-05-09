package com.luis.applogin

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class Agregar_Empleado_Fragment : Fragment() {

    private lateinit var recyclerSinEmpresa: RecyclerView
    private lateinit var recyclerConEmpresa: RecyclerView

    private val db = FirebaseFirestore.getInstance()

    private val empleadosSinEmpresa = mutableListOf<Trabajador>()
    private val empleadosConEmpresa = mutableListOf<Trabajador>()
    private val listaEmpresas = mutableListOf<Pair<String, String>>()

    private lateinit var adapterSinEmpresa: TrabajadorAdapter
    private lateinit var adapterConEmpresa: TrabajadorAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_agregar__empleado_, container, false)

        recyclerSinEmpresa = view.findViewById(R.id.recyclerSinEmpresa)
        recyclerConEmpresa = view.findViewById(R.id.recyclerConEmpresa)

        recyclerSinEmpresa.layoutManager = LinearLayoutManager(context)
        recyclerConEmpresa.layoutManager = LinearLayoutManager(context)

        obtenerYMostrarDatos()

        return view
    }

    private fun obtenerYMostrarDatos() {
        db.collection("empresas").get()
            .addOnSuccessListener { empresaDocs ->
                listaEmpresas.clear()
                for (doc in empresaDocs) {
                    val id = doc.id
                    val nombre = doc.getString("nombre") ?: "Sin nombre"
                    listaEmpresas.add(Pair(id, nombre))
                }

                // Ahora los empleados
                db.collection("trabajador")
                    .get()
                    .addOnSuccessListener { result ->
                        empleadosSinEmpresa.clear()
                        empleadosConEmpresa.clear()

                        for (document in result) {
                            val trabajador = document.toObject(Trabajador::class.java)

                            if (trabajador.empresaId.isNullOrEmpty()) {
                                empleadosSinEmpresa.add(trabajador)
                            } else {
                                empleadosConEmpresa.add(trabajador)
                            }
                        }

                        configurarAdapters()
                    }
                    .addOnFailureListener { exception ->
                        Log.w("Firestore", "Error obteniendo trabajadores: ", exception)
                    }
            }
    }

    private fun configurarAdapters() {
        adapterSinEmpresa = TrabajadorAdapter(empleadosSinEmpresa, listaEmpresas) { trabajador, empresaId ->
            asignarEmpresa(trabajador, empresaId)
        }

        adapterConEmpresa = TrabajadorAdapter(empleadosConEmpresa, emptyList()) { _, _ -> }

        recyclerSinEmpresa.adapter = adapterSinEmpresa
        recyclerConEmpresa.adapter = adapterConEmpresa
    }

    private fun asignarEmpresa(trabajador: Trabajador, empresaId: String) {
        db.collection("trabajador").document(trabajador.uid ?: "")
            .update("empresaId", empresaId)
            .addOnSuccessListener {
                // Actualiza listas visualmente
                trabajador.empresaId = empresaId
                empleadosSinEmpresa.remove(trabajador)
                empleadosConEmpresa.add(trabajador)

                adapterSinEmpresa.notifyDataSetChanged()
                adapterConEmpresa.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Log.e("Firestore", "Error al asignar empresa", it)
            }
    }
}
