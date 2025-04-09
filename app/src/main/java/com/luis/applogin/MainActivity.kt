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

        setup()
    }

    private fun setup() {
        title = "Autenticación"

        ingresarButton.setOnClickListener {
            if (emailText.text.isNotEmpty() && contraText.text.isNotEmpty()) {
                FirebaseAuth.getInstance().signInWithEmailAndPassword(
                    emailText.text.toString(),
                    contraText.text.toString()
                ).addOnCompleteListener {
                    if (it.isSuccessful) {
                        home(it.result?.user?.email ?: "", ProviderType.BASIC)
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
