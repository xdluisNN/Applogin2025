package com.luis.applogin

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
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

class Agregar_Empresa_Fragment : Fragment() {

    private lateinit var editTextNombre: EditText
    private lateinit var editTextDescripcion: EditText
    private lateinit var btnRegistrarEmpresa: Button
    private lateinit var btnSeleccionarImagen: Button
    private lateinit var imagePreview: ImageView
    private lateinit var editTextInicioContrato: EditText
    private lateinit var editTextFinContrato: EditText
    private lateinit var btnEliminarImagen: ImageButton

    private var imagenUri: Uri? = null
    private var fechaInicioCalendar: Calendar? = null

    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_agregar__empresa_, container, false)

        editTextNombre = view.findViewById(R.id.editTextNombre)
        editTextDescripcion = view.findViewById(R.id.editTextDescripcion)
        btnRegistrarEmpresa = view.findViewById(R.id.btnRegistrarEmpresa)
        btnSeleccionarImagen = view.findViewById(R.id.btnSeleccionarImagen)
        imagePreview = view.findViewById(R.id.imagePreview)
        editTextInicioContrato = view.findViewById(R.id.editTextInicioContrato)
        editTextFinContrato = view.findViewById(R.id.editTextFinContrato)
        btnEliminarImagen = view.findViewById(R.id.btnEliminarImagen)


        editTextInicioContrato.setOnClickListener { mostrarDatePickerInicio() }
        editTextFinContrato.setOnClickListener { mostrarDatePickerFin() }

        btnSeleccionarImagen.setOnClickListener {
            seleccionarImagenDeGaleria()
        }

        btnRegistrarEmpresa.setOnClickListener {
            if (validarCampos()) {
                mostrarDialogoConfirmacionRegistro()
            }
        }

        btnEliminarImagen.setOnClickListener {
            imagenUri = null
            imagePreview.setImageDrawable(null)
            btnEliminarImagen.visibility = View.GONE
        }

        return view
    }

    private val seleccionarImagenLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                imagenUri = it.data?.data
                imagePreview.setImageURI(imagenUri)
                btnEliminarImagen.visibility = View.VISIBLE
            }
        }

    private fun seleccionarImagenDeGaleria() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
        }
        seleccionarImagenLauncher.launch(intent)
    }


    private fun mostrarDatePickerInicio() {
        val calendario = Calendar.getInstance()
        val anio = calendario.get(Calendar.YEAR)
        val mes = calendario.get(Calendar.MONTH)
        val dia = calendario.get(Calendar.DAY_OF_MONTH)

        val datePicker = DatePickerDialog(requireContext(), { _, year, month, dayOfMonth ->
            val fechaSeleccionada = String.format("%02d-%02d-%04d", dayOfMonth, month + 1, year)
            editTextInicioContrato.setText(fechaSeleccionada)


            fechaInicioCalendar = Calendar.getInstance().apply {
                set(year, month, dayOfMonth)
            }

            editTextFinContrato.text.clear()
        }, anio, mes, dia)


        datePicker.datePicker.minDate = calendario.timeInMillis
        datePicker.show()
    }


    private fun mostrarDatePickerFin() {
        if (fechaInicioCalendar == null) {
            Toast.makeText(requireContext(), "Primero selecciona la fecha de inicio", Toast.LENGTH_SHORT).show()
            return
        }

        val calendario = Calendar.getInstance()
        val anio = calendario.get(Calendar.YEAR)
        val mes = calendario.get(Calendar.MONTH)
        val dia = calendario.get(Calendar.DAY_OF_MONTH)

        val datePicker = DatePickerDialog(requireContext(), { _, year, month, dayOfMonth ->
            val fechaSeleccionada = String.format("%02d-%02d-%04d", dayOfMonth, month + 1, year)
            editTextFinContrato.setText(fechaSeleccionada)
        }, anio, mes, dia)


        val minFinCalendar = fechaInicioCalendar!!.clone() as Calendar
        minFinCalendar.add(Calendar.DAY_OF_MONTH, 1)
        datePicker.datePicker.minDate = minFinCalendar.timeInMillis

        datePicker.show()
    }

    private fun subirImagenYRegistrarEmpresa() {
        val nombre = editTextNombre.text.toString().trim()
        val descripcion = editTextDescripcion.text.toString().trim()
        val inicioContrato = editTextInicioContrato.text.toString().trim()
        val finContrato = editTextFinContrato.text.toString().trim()

        val nombreArchivo = "empresas/${UUID.randomUUID()}.jpg"
        val referencia = storage.reference.child(nombreArchivo)

        referencia.putFile(imagenUri!!)
            .addOnSuccessListener {
                referencia.downloadUrl.addOnSuccessListener { uri ->
                    guardarEmpresaFirestore(nombre, descripcion, inicioContrato, finContrato, uri.toString())
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error al subir imagen", Toast.LENGTH_SHORT).show()
            }
    }

    private fun mostrarDialogoConfirmacionRegistro() {
        AlertDialog.Builder(requireContext())
            .setTitle("Confirmar registro")
            .setMessage("¿Estás seguro de que deseas registrar esta empresa?")
            .setPositiveButton("Confirmar") { _, _ ->
                subirImagenYRegistrarEmpresa()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun validarCampos(): Boolean {
        val nombre = editTextNombre.text.toString().trim()
        val descripcion = editTextDescripcion.text.toString().trim()
        val inicioContrato = editTextInicioContrato.text.toString().trim()
        val finContrato = editTextFinContrato.text.toString().trim()

        var valido = true

        if (nombre.isEmpty()) {
            editTextNombre.error = "Campo obligatorio"
            valido = false
        }

        if (descripcion.isEmpty()) {
            editTextDescripcion.error = "Campo obligatorio"
            valido = false
        }

        if (imagenUri == null) {
            Toast.makeText(requireContext(), "Selecciona una imagen", Toast.LENGTH_SHORT).show()
            valido = false
        }

        if (inicioContrato.isEmpty()) {
            editTextInicioContrato.error = "Selecciona la fecha de inicio"
            valido = false
        }

        if (finContrato.isEmpty()) {
            editTextFinContrato.error = "Selecciona la fecha de fin"
            valido = false
        }

        return valido
    }

    private fun guardarEmpresaFirestore(nombre: String, descripcion: String, inicioContrato: String, finContrato: String, imagenUrl: String) {
        val nuevaEmpresa = hashMapOf(
            "nombre" to nombre,
            "descripcion" to descripcion,
            "fechaRegistro" to Timestamp.now(),
            "imagenUrl" to imagenUrl,
            "inicioContrato" to inicioContrato,
            "finContrato" to finContrato
        )

        db.collection("empresas")
            .add(nuevaEmpresa)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Empresa registrada", Toast.LENGTH_SHORT).show()
                editTextNombre.text.clear()
                editTextDescripcion.text.clear()
                editTextInicioContrato.text.clear()
                editTextFinContrato.text.clear()
                imagePreview.setImageResource(0)
                imagenUri = null
                fechaInicioCalendar = null
                btnEliminarImagen.visibility = View.GONE
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error al registrar empresa", Toast.LENGTH_SHORT).show()
            }
    }
}
