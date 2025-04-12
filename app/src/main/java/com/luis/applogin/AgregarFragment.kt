package com.luis.applogin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class AgregarFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var editTextNombre: EditText
    private lateinit var editTextDescripcion: EditText
    private lateinit var btnRegistrarEmpresa: Button

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_agregar, container, false)

        editTextNombre = view.findViewById(R.id.editTextNombre)
        editTextDescripcion = view.findViewById(R.id.editTextDescripcion)
        btnRegistrarEmpresa = view.findViewById(R.id.btnRegistrarEmpresa)

        btnRegistrarEmpresa.setOnClickListener {
            registrarEmpresa()
        }

        return view
    }

    private fun registrarEmpresa() {
        val nombre = editTextNombre.text.toString().trim()
        val descripcion = editTextDescripcion.text.toString().trim()

        if (nombre.isEmpty()) {
            editTextNombre.error = "Campo obligatorio"
            return
        }

        val nuevaEmpresa = hashMapOf(
            "nombre" to nombre,
            "descripcion" to descripcion,
            "fechaRegistro" to Timestamp.now(),
            "imagenUrl" to "" // Esto puede cambiar si agregas imagen en el futuro
        )

        db.collection("empresas")
            .add(nuevaEmpresa)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Empresa registrada", Toast.LENGTH_SHORT).show()
                editTextNombre.text.clear()
                editTextDescripcion.text.clear()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error al registrar", Toast.LENGTH_SHORT).show()
            }
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AgregarFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
