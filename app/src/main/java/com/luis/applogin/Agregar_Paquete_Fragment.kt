package com.luis.applogin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue

class Agregar_Paquete_Fragment : Fragment() {

    private lateinit var etNombrePaquete: EditText
    private lateinit var etDireccionPaquete: EditText
    private lateinit var spinnerEmpresa: Spinner
    private lateinit var spinnerTrabajador: Spinner
    private lateinit var btnGuardarPaquete: Button

    private val db = FirebaseFirestore.getInstance()

    private val listaEmpresas = mutableListOf<Pair<String, String>>() // ID, Nombre
    private val listaTrabajadores = mutableListOf<Pair<String, String>>() // ID, Nombre

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_agregar__paquete_, container, false)

        etNombrePaquete = view.findViewById(R.id.etNombrePaquete)
        etDireccionPaquete = view.findViewById(R.id.etDireccionPaquete)
        spinnerEmpresa = view.findViewById(R.id.spinnerEmpresa)
        spinnerTrabajador = view.findViewById(R.id.spinnerTrabajador)
        btnGuardarPaquete = view.findViewById(R.id.btnGuardarPaquete)

        cargarEmpresas()

        spinnerEmpresa.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position > 0) {
                    val empresaId = listaEmpresas[position - 1].first
                    cargarTrabajadoresPorEmpresa(empresaId)
                } else {
                    limpiarSpinnerTrabajadores()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        btnGuardarPaquete.setOnClickListener {
            guardarPaquete()
        }

        return view
    }

    private fun cargarEmpresas() {
        db.collection("empresas").get()
            .addOnSuccessListener { documentos ->
                val nombres = mutableListOf("Selecciona una empresa")
                listaEmpresas.clear()
                for (doc in documentos) {
                    val nombre = doc.getString("nombre") ?: continue
                    listaEmpresas.add(doc.id to nombre)
                    nombres.add(nombre)
                }
                spinnerEmpresa.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, nombres)
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error al cargar empresas", Toast.LENGTH_SHORT).show()
            }
    }

    private fun cargarTrabajadoresPorEmpresa(empresaId: String) {
        db.collection("trabajador")
            .whereEqualTo("empresaId", empresaId)
            .whereEqualTo("rol", "Empleado")
            .get()
            .addOnSuccessListener { documentos ->
                val nombres = mutableListOf("Selecciona un trabajador")
                listaTrabajadores.clear()

                if (documentos.isEmpty) {
                    Toast.makeText(requireContext(), "No hay trabajadores para esta empresa", Toast.LENGTH_SHORT).show()
                }

                for (doc in documentos) {
                    val nombre = doc.getString("nombre") ?: continue
                    listaTrabajadores.add(doc.id to nombre)
                    nombres.add(nombre)
                }
                spinnerTrabajador.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, nombres)
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error al cargar trabajadores", Toast.LENGTH_SHORT).show()
            }
    }

    private fun limpiarSpinnerTrabajadores() {
        listaTrabajadores.clear()
        val nombres = mutableListOf("Selecciona un trabajador")
        spinnerTrabajador.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, nombres)
    }

    private fun guardarPaquete() {
        val nombrePaquete = etNombrePaquete.text.toString().trim()
        val direccionPaquete = etDireccionPaquete.text.toString().trim()
        val empresaIndex = spinnerEmpresa.selectedItemPosition
        val trabajadorIndex = spinnerTrabajador.selectedItemPosition

        if (nombrePaquete.isEmpty()) {
            etNombrePaquete.error = "Campo obligatorio"
            etNombrePaquete.requestFocus()
            return
        }

        if (direccionPaquete.isEmpty()) {
            etDireccionPaquete.error = "Campo obligatorio"
            etDireccionPaquete.requestFocus()
            return
        }

        if (empresaIndex == 0) {
            Toast.makeText(requireContext(), "Selecciona una empresa", Toast.LENGTH_SHORT).show()
            return
        }

        if (trabajadorIndex == 0) {
            Toast.makeText(requireContext(), "Selecciona un trabajador", Toast.LENGTH_SHORT).show()
            return
        }

        val empresaId = listaEmpresas[empresaIndex - 1].first
        val trabajadorId = listaTrabajadores[trabajadorIndex - 1].first

        val paquete = hashMapOf(
            "nombrePaquete" to nombrePaquete,
            "Direccion" to direccionPaquete,
            "empresaId" to empresaId,
            "trabajadorAsignadoId" to trabajadorId,
            "estado" to "pendiente",
            "fechaRegistro" to FieldValue.serverTimestamp()
        )

        db.collection("paquetes").add(paquete)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Paquete guardado", Toast.LENGTH_SHORT).show()
                etNombrePaquete.setText("")
                etDireccionPaquete.setText("")
                spinnerEmpresa.setSelection(0)
                spinnerTrabajador.setSelection(0)
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error al guardar", Toast.LENGTH_SHORT).show()
            }
    }
}
