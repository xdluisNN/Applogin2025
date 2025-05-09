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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_agregar__empleado_, container, false)

        recyclerSinEmpresa = view.findViewById(R.id.recyclerSinEmpresa)
        recyclerConEmpresa = view.findViewById(R.id.recyclerConEmpresa)

        obtenerYMostrarTrabajadores()

        return view
    }

    private fun obtenerYMostrarTrabajadores() {
        db.collection("trabajador")
            .get()
            .addOnSuccessListener { result ->
                val empleadosSinEmpresa = mutableListOf<Trabajador>()
                val empleadosConEmpresa = mutableListOf<Trabajador>()

                for (document in result) {
                    val trabajador = document.toObject(Trabajador::class.java)

                    if (trabajador.empresaId.isNullOrEmpty()) {
                        empleadosSinEmpresa.add(trabajador)
                    } else {
                        empleadosConEmpresa.add(trabajador)
                    }
                }

                llenarTablaSinEmpresa(empleadosSinEmpresa)
                llenarTablaConEmpresa(empleadosConEmpresa)
            }
            .addOnFailureListener { exception ->
                Log.w("Firestore", "Error obteniendo documentos: ", exception)
            }
    }

    private fun llenarTablaSinEmpresa(lista: List<Trabajador>) {
        val adapter = TrabajadorAdapter(lista)
        recyclerSinEmpresa.layoutManager = LinearLayoutManager(context)
        recyclerSinEmpresa.adapter = adapter
    }

    private fun llenarTablaConEmpresa(lista: List<Trabajador>) {
        val adapter = TrabajadorAdapter(lista)
        recyclerConEmpresa.layoutManager = LinearLayoutManager(context)
        recyclerConEmpresa.adapter = adapter
    }
}
