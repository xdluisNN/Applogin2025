package com.luis.applogin

import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegistroActivity : AppCompatActivity() {

    private lateinit var editTextNombre: EditText
    private lateinit var editTextDireccion: EditText
    private lateinit var editTextTelefono: EditText
    private lateinit var editTextEmail: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var editTextRepetirPassword: EditText
    private lateinit var btnRegistrar: Button
    private lateinit var btnCerrar: Button

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_registro)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        editTextNombre = findViewById(R.id.editTextNombre)
        editTextDireccion = findViewById(R.id.editTextDireccion)
        editTextTelefono = findViewById(R.id.editTextTelefono)
        editTextEmail = findViewById(R.id.editTextEmail)
        editTextPassword = findViewById(R.id.editTextPassword)
        editTextRepetirPassword = findViewById(R.id.editTextRepetirPassword)
        btnRegistrar = findViewById(R.id.btnRegistrar)
        btnCerrar = findViewById(R.id.btnCerrar)

        btnCerrar.setOnClickListener {
            finish() // Cierra esta actividad y vuelve al Login
        }

        btnRegistrar.setOnClickListener {
            registrarUsuario()
        }
    }

    private fun registrarUsuario() {
        val nombre = editTextNombre.text.toString().trim()
        val direccion = editTextDireccion.text.toString().trim()
        val telefono = editTextTelefono.text.toString().trim()
        val email = editTextEmail.text.toString().trim()
        val password = editTextPassword.text.toString().trim()
        val repetirPassword = editTextRepetirPassword.text.toString().trim()

        // Validaciones
        if (nombre.isEmpty() || direccion.isEmpty() || telefono.isEmpty() || email.isEmpty() || password.isEmpty() || repetirPassword.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        if (password != repetirPassword) {
            Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
            return
        }

        // Registrar en Authentication
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val uid = authResult.user?.uid ?: return@addOnSuccessListener

                val trabajador = hashMapOf(
                    "nombre" to nombre,
                    "direccion" to direccion,
                    "telefono" to telefono,
                    "email" to email,
                    "rol" to "Empleado", // Rol fijo
                    "empresaId" to "",   // Empresa vacía
                    "uid" to uid
                )

                // Guardar en Firestore
                db.collection("trabajador").document(uid).set(trabajador)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Usuario registrado exitosamente", Toast.LENGTH_SHORT).show()
                        finish() // 🔥 Regresa al LoginActivity
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error al guardar en Firestore", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al registrar usuario: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
