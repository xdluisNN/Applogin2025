package com.luis.applogin

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class AgregarFragment : Fragment() {

    private lateinit var editTextNombre: EditText
    private lateinit var editTextDescripcion: EditText
    private lateinit var btnRegistrarEmpresa: Button
    private lateinit var btnSeleccionarImagen: Button
    private lateinit var imagePreview: ImageView
    private var imagenUri: Uri? = null

    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_agregar, container, false)

        editTextNombre = view.findViewById(R.id.editTextNombre)
        editTextDescripcion = view.findViewById(R.id.editTextDescripcion)
        btnRegistrarEmpresa = view.findViewById(R.id.btnRegistrarEmpresa)
        btnSeleccionarImagen = view.findViewById(R.id.btnSeleccionarImagen)
        imagePreview = view.findViewById(R.id.imagePreview)

        btnSeleccionarImagen.setOnClickListener {
            seleccionarImagenDeGaleria()
        }

        btnRegistrarEmpresa.setOnClickListener {
            subirImagenYRegistrarEmpresa()
        }

        return view
    }

    // Selección desde galería
    private val seleccionarImagenLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            imagenUri = it.data?.data
            imagePreview.setImageURI(imagenUri)
        }
    }

    private fun seleccionarImagenDeGaleria() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        seleccionarImagenLauncher.launch(intent)
    }

    // Subir imagen y luego registrar datos
    private fun subirImagenYRegistrarEmpresa() {
        val nombre = editTextNombre.text.toString().trim()
        val descripcion = editTextDescripcion.text.toString().trim()

        if (nombre.isEmpty()) {
            editTextNombre.error = "Campo obligatorio"
            return
        }
        if (descripcion.isEmpty()) {
            editTextDescripcion.error = "Campo obligatorio"
            return
        }
        if (imagenUri == null) {
            Toast.makeText(requireContext(), "Selecciona una imagen", Toast.LENGTH_SHORT).show()
            return
        }

        if (imagenUri != null) {
            val nombreArchivo = "empresas/${UUID.randomUUID()}.jpg"
            val referencia = storage.reference.child(nombreArchivo)

            referencia.putFile(imagenUri!!)
                .addOnSuccessListener {
                    referencia.downloadUrl.addOnSuccessListener { uri ->
                        guardarEmpresaFirestore(nombre, descripcion, uri.toString())
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Error al subir imagen", Toast.LENGTH_SHORT).show()
                }
        } else {
            guardarEmpresaFirestore(nombre, descripcion, "")
        }
    }

    // Guardar en Firestore
    private fun guardarEmpresaFirestore(nombre: String, descripcion: String, imagenUrl: String) {
        val nuevaEmpresa = hashMapOf(
            "nombre" to nombre,
            "descripcion" to descripcion,
            "fechaRegistro" to Timestamp.now(),
            "imagenUrl" to imagenUrl
        )

        db.collection("empresas")
            .add(nuevaEmpresa)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Empresa registrada", Toast.LENGTH_SHORT).show()
                editTextNombre.text.clear()
                editTextDescripcion.text.clear()
                imagePreview.setImageResource(0)
                imagenUri = null
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error al registrar empresa", Toast.LENGTH_SHORT).show()
            }
    }
}
