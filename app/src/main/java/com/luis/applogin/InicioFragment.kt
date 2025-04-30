package com.luis.applogin

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class InicioFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_inicio, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<LinearLayout>(R.id.btnPaqueteria).setOnClickListener {
            // Navegar a Fragmento Paquetería
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, PaqueteriaFragment())
                .commit()
        }

        view.findViewById<LinearLayout>(R.id.btnAsociados).setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, AsociadosFragment())
                .commit()
        }

        val btnSistemas = view.findViewById<LinearLayout>(R.id.btnSistemas)

        // Ocultar por defecto
        btnSistemas.visibility = View.GONE

        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.uid?.let { uid ->
            val db = FirebaseFirestore.getInstance()
            db.collection("trabajador").document(uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val rol = document.getString("rol")
                        if (rol == "Administrador") {
                            // Mostrar y activar el botón solo si es administrador
                            btnSistemas.visibility = View.VISIBLE
                            btnSistemas.setOnClickListener {
                                parentFragmentManager.beginTransaction()
                                    .replace(R.id.fragment_container, SistemasFragment())
                                    .commit()
                            }
                        }
                    }
                }
        }
    }
}