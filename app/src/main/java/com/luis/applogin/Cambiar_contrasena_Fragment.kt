package com.luis.applogin

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

class Cambiar_contrasena_Fragment : Fragment() {

    private lateinit var etPasswordActual: EditText
    private lateinit var etPasswordNueva: EditText
    private lateinit var etPasswordConfirmacion: EditText
    private lateinit var btnCambiarPassword: Button

    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_cambiar_contrasena, container, false)

        etPasswordActual = view.findViewById(R.id.etPasswordActual)
        etPasswordNueva = view.findViewById(R.id.etPasswordNueva)
        etPasswordConfirmacion = view.findViewById(R.id.etPasswordConfirmacion)
        btnCambiarPassword = view.findViewById(R.id.btnCambiarPassword)

        btnCambiarPassword.setOnClickListener {
            cambiarPassword()
        }

        return view
    }

    private fun cambiarPassword() {
        val actual = etPasswordActual.text.toString()
        val nueva = etPasswordNueva.text.toString()
        val confirmacion = etPasswordConfirmacion.text.toString()

        if (actual.isEmpty() && nueva.isEmpty() && confirmacion.isEmpty()) {
            Toast.makeText(requireContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        if (nueva != confirmacion) {
            Toast.makeText(requireContext(), "Las nuevas contraseñas no coinciden", Toast.LENGTH_SHORT).show()
            return
        }

        val user = auth.currentUser
        val email = user?.email

        val credential = EmailAuthProvider.getCredential(email!!, actual)

        user.reauthenticate(credential)
            .addOnSuccessListener {
                user.updatePassword(nueva)
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Contraseña actualizada", Toast.LENGTH_SHORT).show()
                        requireActivity().supportFragmentManager.popBackStack()
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Error al actualizar contraseña", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Contraseña actual incorrecta", Toast.LENGTH_SHORT).show()
            }
    }
}
