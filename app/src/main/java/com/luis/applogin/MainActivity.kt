package com.luis.applogin

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.InputType
import android.view.MotionEvent
import android.widget.*
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

        val eyeClosed = resizeDrawable(R.drawable.ojo_cerrado, 30, 30)
        contraText.setCompoundDrawablesWithIntrinsicBounds(null, null, eyeClosed, null)


        // Mostrar/Ocultar contraseña al tocar ícono dentro del EditText
        var passwordVisible = false
        contraText.setOnTouchListener { _, event ->
            val drawableRight = contraText.compoundDrawables[2]
            if (drawableRight != null && event.action == MotionEvent.ACTION_UP &&
                event.rawX >= (contraText.right - drawableRight.bounds.width() - contraText.paddingEnd)
            ) {
                passwordVisible = !passwordVisible
                if (passwordVisible) {
                    contraText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    val eyeOpen = resizeDrawable(R.drawable.ojo_abierto, 30, 30)
                    contraText.setCompoundDrawablesWithIntrinsicBounds(null, null, eyeOpen, null)
                } else {
                    contraText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                    val eyeClosed = resizeDrawable(R.drawable.ojo_cerrado, 30, 30)
                    contraText.setCompoundDrawablesWithIntrinsicBounds(null, null, eyeClosed, null)
                }
                contraText.setSelection(contraText.text.length)
                true
            } else {
                false
            }
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
        builder.create().show()
    }

    private fun home(email: String, provider: ProviderType) {
        val homeIntent = Intent(this, HomeActivity::class.java).apply {
            putExtra("email", email)
            putExtra("provider", provider.name)
        }
        startActivity(homeIntent)
    }

    private fun resizeDrawable(drawableId: Int, width: Int, height: Int): Drawable {
        val drawable = resources.getDrawable(drawableId, theme)
        val bitmap = (drawable as BitmapDrawable).bitmap
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true)
        return BitmapDrawable(resources, resizedBitmap)
    }


    override fun onResume() {
        super.onResume()
        emailText.setText("")
        contraText.setText("")
    }
}
