package com.luis.applogin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Agregar_Empleado_Fragment : Fragment() {

    private lateinit var editTextNombre: EditText
    private lateinit var editTextDireccion: EditText
    private lateinit var editTextTelefono: EditText
    private lateinit var editTextEmail: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var spinnerEmpresa: Spinner
    private lateinit var spinnerRol: Spinner
    private lateinit var btnRegistrar: Button

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val listaEmpresas = mutableListOf<Pair<String, String>>() // id y nombre

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_agregar__empleado_, container, false)

        editTextNombre = view.findViewById(R.id.editTextNombre)
        editTextDireccion = view.findViewById(R.id.editTextDireccion)
        editTextTelefono = view.findViewById(R.id.editTextTelefono)
        editTextEmail = view.findViewById(R.id.editTextEmail)
        editTextPassword = view.findViewById(R.id.editTextPassword)
        spinnerEmpresa = view.findViewById(R.id.spinnerEmpresa)
        spinnerRol = view.findViewById(R.id.spinnerRol)
        btnRegistrar = view.findViewById(R.id.btnRegistrarTrabajador)

        cargarEmpresas()
        cargarRoles()

        btnRegistrar.setOnClickListener {
            registrarTrabajador()
        }

        return view
    }

    private fun cargarEmpresas() {
        db.collection("empresas").get()
            .addOnSuccessListener { documentos ->
                val nombres = mutableListOf<String>()
                listaEmpresas.clear()
                for (doc in documentos) {
                    val id = doc.id
                    val nombre = doc.getString("nombre") ?: "Sin nombre"
                    listaEmpresas.add(Pair(id, nombre))
                    nombres.add(nombre)
                }

                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, nombres)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerEmpresa.adapter = adapter
            }
    }

    private fun cargarRoles() {
        val roles = listOf("Empleado", "Administrador")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, roles)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerRol.adapter = adapter
    }

    private fun registrarTrabajador() {
        val nombre = editTextNombre.text.toString().trim()
        val direccion = editTextDireccion.text.toString().trim()
        val telefono = editTextTelefono.text.toString().trim()
        val email = editTextEmail.text.toString().trim()
        val password = editTextPassword.text.toString().trim()
        val rol = spinnerRol.selectedItem.toString()
        val empresaSeleccionada = spinnerEmpresa.selectedItemPosition
        val empresaId = if (empresaSeleccionada >= 0) listaEmpresas[empresaSeleccionada].first else ""

        // Validaciones
        if (nombre.isEmpty()) {
            editTextNombre.error = "Campo obligatorio"
            return
        }
        if (direccion.isEmpty()) {
            editTextDireccion.error = "Campo obligatorio"
            return
        }
        if (telefono.isEmpty()) {
            editTextTelefono.error = "Campo obligatorio"
            return
        }
        if (email.isEmpty()) {
            editTextEmail.error = "Campo obligatorio"
            return
        }
        if (password.isEmpty()) {
            editTextPassword.error = "Campo obligatorio"
            return
        }
        if (empresaId.isEmpty()) {
            Toast.makeText(requireContext(), "Selecciona una empresa", Toast.LENGTH_SHORT).show()
            return
        }
        if (rol.isEmpty()) {
            Toast.makeText(requireContext(), "Selecciona un rol", Toast.LENGTH_SHORT).show()
            return
        }

        // Crear usuario en Auth
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val uid = authResult.user?.uid ?: return@addOnSuccessListener
                val trabajador = hashMapOf(
                    "nombre" to nombre,
                    "direccion" to direccion,
                    "telefono" to telefono,
                    "email" to email,
                    "rol" to rol,
                    "empresaId" to empresaId,
                    "uid" to uid
                )

                db.collection("trabajador").document(uid).set(trabajador)
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Trabajador registrado exitosamente", Toast.LENGTH_SHORT).show()
                        limpiarCampos()
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Error al guardar en Firestore", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error al registrar usuario: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun limpiarCampos() {
        editTextNombre.setText("")
        editTextDireccion.setText("")
        editTextTelefono.setText("")
        editTextEmail.setText("")
        editTextPassword.setText("")
        spinnerEmpresa.setSelection(0)
        spinnerRol.setSelection(0)
    }
}
