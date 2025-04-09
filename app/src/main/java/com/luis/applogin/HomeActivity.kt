package com.luis.applogin

import android.app.AlertDialog
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.auth.FirebaseAuth

enum class ProviderType{
    BASIC
}

class HomeActivity : AppCompatActivity() {

    private lateinit var emailText2: TextView
    private lateinit var proveedorText: TextView
    private lateinit var salirButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Conectar vistas con el layout
        emailText2 = findViewById(R.id.emailText2)
        proveedorText = findViewById(R.id.proveedorText)
        salirButton = findViewById(R.id.salirButton)

        val bundle: Bundle? = intent.extras
        val email: String? = bundle?.getString("email")
        val provider: String? = bundle?.getString("provider")

        setup(email ?: "", provider ?: "")
    }

    private fun setup(email: String, provider: String) {
        title = "Inicio"
        emailText2.text = email
        proveedorText.text = provider

        salirButton.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Confirmar salida")
            builder.setMessage("¿Estás seguro de que deseas cerrar sesión?")
            builder.setPositiveButton("Sí") { _, _ ->
                FirebaseAuth.getInstance().signOut()
                onBackPressed() // O puedes usar finish() si prefieres cerrar esta actividad
            }
            builder.setNegativeButton("No", null)

            val dialog: AlertDialog = builder.create()
            dialog.show()
        }
    }
}
