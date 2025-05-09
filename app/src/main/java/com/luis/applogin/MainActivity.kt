package com.luis.applogin

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private lateinit var ingresarButton: Button
    private lateinit var emailText: EditText
    private lateinit var contraText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Vinculamos los elementos de la interfaz
        ingresarButton = findViewById(R.id.ingresarbutton)
        emailText = findViewById(R.id.emailText)
        contraText = findViewById(R.id.contraText)
        val btnRegistrar = findViewById<Button>(R.id.btnRegistrar)
        btnRegistrar.setOnClickListener {
            val intent = Intent(this, RegistroActivity::class.java)
            startActivity(intent)
        }
        setup()
    }

    private fun setup() {
        title = "Autenticación"

        ingresarButton.setOnClickListener {
            if (emailText.text.isNotEmpty() && contraText.text.isNotEmpty()) {
                FirebaseAuth.getInstance().signInWithEmailAndPassword(
                    emailText.text.toString(),
                    contraText.text.toString()
                ).addOnCompleteListener { authResult ->
                    if (authResult.isSuccessful) {
                        val user = authResult.result?.user
                        user?.let {
                            // Verificamos en Firestore si la cuenta está activa
                            val db = FirebaseFirestore.getInstance()
                            db.collection("trabajador").document(it.uid)
                                .get()
                                .addOnSuccessListener { document ->
                                    if (document.exists()) {
                                        val estado = document.getString("estado")
                                        if (estado == "inactivo") {
                                            FirebaseAuth.getInstance().signOut()
                                            Toast.makeText(this, "Cuenta desactivada, contacte al administrador", Toast.LENGTH_LONG).show()
                                        } else {
                                            home(it.email ?: "", ProviderType.BASIC)
                                        }
                                    } else {
                                        // Si no se encuentra documento, permitimos login
                                        home(it.email ?: "", ProviderType.BASIC)
                                    }
                                }
                                .addOnFailureListener {
                                    showAlert()
                                }
                        }
                    } else {
                        showAlert()
                    }
                }
            } else {
                Toast.makeText(this, "Por favor, complete los campos requeridos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showAlert() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Error")
        builder.setMessage("Ha ocurrido un error al iniciar sesión")
        builder.setPositiveButton("Aceptar", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun home(email: String, provider: ProviderType) {
        val homeIntent = Intent(this, HomeActivity::class.java).apply {
            putExtra("email", email)
            putExtra("provider", provider.name)
        }
        startActivity(homeIntent)
    }

    override fun onResume() {
        super.onResume()
        emailText.setText("")
        contraText.setText("")
    }
}
