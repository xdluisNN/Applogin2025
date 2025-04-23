package com.luis.applogin

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.luis.applogin.Cambiar_contrasena_Fragment


class PerfilFragment : Fragment() {

    private lateinit var editTextNombre: EditText
    private lateinit var editTextDireccion: EditText
    private lateinit var editTextTelefono: EditText
    private lateinit var editTextEmail: EditText
    private lateinit var btnGuardar: Button
    private lateinit var btnEditar: Button
    private lateinit var logoutButton: Button
    private lateinit var btnCambiarContrasena: Button
    private lateinit var btnMasInformacion: Button


    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var uidActual: String? = null
    private var editando = false


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_perfil, container, false)

        editTextNombre = view.findViewById(R.id.editTextNombre)
        editTextDireccion = view.findViewById(R.id.editTextDireccion)
        editTextTelefono = view.findViewById(R.id.editTextTelefono)
        editTextEmail = view.findViewById(R.id.editTextEmail)
        btnEditar = view.findViewById(R.id.btnEditar)
        btnGuardar = view.findViewById(R.id.btnGuardar)
        logoutButton = view.findViewById(R.id.logoutButton)
        btnCambiarContrasena = view.findViewById(R.id.Cambiarcontrasena)
        btnMasInformacion = view.findViewById(R.id.Masinfo)


        val btnCambiarContrasena = view.findViewById<Button>(R.id.Cambiarcontrasena)
        btnCambiarContrasena.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, Cambiar_contrasena_Fragment())
                .commit()
        }


        // Deshabilitar campos al inicio
        cambiarModoEdicion(false)

        uidActual = auth.currentUser?.uid
        uidActual?.let { cargarDatosTrabajador(it) }

        btnEditar.setOnClickListener {
            if(!editando){
                cambiarModoEdicion(true)
                btnEditar.text = "Salir"
                btnEditar.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.rojo))
                logoutButton.visibility = View.GONE
            }else{
                cambiarModoEdicion(false)
                btnEditar.text = "Editar informacion"
                logoutButton.visibility = View.VISIBLE
                btnEditar.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.celeste))
                uidActual?.let { cargarDatosTrabajador(it) }
            }
        }

        btnGuardar.setOnClickListener {
            mostrarAlertaConfirmacion()
        }
        logoutButton.setOnClickListener {
            mostrarDialogoConfirmacion()
        }

        return view
    }

    private fun cambiarModoEdicion(habilitar: Boolean) {
        editando = habilitar
        editTextNombre.isEnabled = habilitar
        editTextDireccion.isEnabled = habilitar
        editTextTelefono.isEnabled = habilitar
        editTextEmail.isEnabled = habilitar
        btnGuardar.visibility = if (habilitar) View.VISIBLE else View.GONE
        btnCambiarContrasena.visibility = if (habilitar) View.GONE else View.VISIBLE
        btnMasInformacion.visibility = if (habilitar) View.GONE else View.VISIBLE
    }

    private fun cargarDatosTrabajador(uid: String) {
        db.collection("trabajador").document(uid).get()
            .addOnSuccessListener { documento ->
                if (documento.exists()) {
                    editTextNombre.setText(documento.getString("nombre"))
                    editTextDireccion.setText(documento.getString("direccion"))
                    editTextTelefono.setText(documento.getString("telefono"))
                    editTextEmail.setText(documento.getString("email"))
                }
            }
    }

    private fun mostrarAlertaConfirmacion() {
        AlertDialog.Builder(requireContext())
            .setTitle("Confirmar cambios")
            .setMessage("¿Deseas guardar los cambios realizados?")
            .setPositiveButton("Sí") { _, _ ->
                guardarCambios()
            }
            .setNegativeButton("No", null)
            .create()
            .show()
    }

    private fun guardarCambios() {
        val nombre = editTextNombre.text.toString().trim()
        val direccion = editTextDireccion.text.toString().trim()
        val telefono = editTextTelefono.text.toString().trim()
        val email = editTextEmail.text.toString().trim()

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

        val datosActualizados = mapOf(
            "nombre" to nombre,
            "direccion" to direccion,
            "telefono" to telefono,
            "email" to email
        )

        uidActual?.let { uid ->
            db.collection("trabajador").document(uid).update(datosActualizados)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Datos actualizados correctamente", Toast.LENGTH_SHORT).show()
                    cambiarModoEdicion(false)
                    btnEditar.text = "Editar informacion"
                    btnEditar.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.celeste))
                    logoutButton.visibility = View.VISIBLE
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Error al actualizar datos", Toast.LENGTH_SHORT).show()
                }
        }
    }
    private fun mostrarDialogoConfirmacion() {
        AlertDialog.Builder(requireContext())
            .setTitle("Cerrar sesión")
            .setMessage("¿Estás seguro de que deseas cerrar sesión?")
            .setPositiveButton("Sí") { _, _ ->
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(requireContext(), MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                requireActivity().finish()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }


}
