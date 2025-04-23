package com.luis.applogin

import android.app.AlertDialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.activity.OnBackPressedCallback


enum class ProviderType{
    BASIC
}

class HomeActivity : AppCompatActivity() {

    private lateinit var email: String
    private lateinit var provider: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        email = intent.getStringExtra("email") ?: ""
        provider = intent.getStringExtra("provider") ?: ""

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        // Cargar fragmento por defecto
        openFragment(InicioFragment())

        bottomNav.setOnItemSelectedListener { menuItem ->
            val fragment: Fragment = when (menuItem.itemId) {
                R.id.nav_inicio -> InicioFragment()
                R.id.nav_agregar -> AgregarFragment()
                R.id.nav_historial -> HistorialFragment()
                R.id.nav_perfil -> {
                    val perfilFragment = PerfilFragment()
                    perfilFragment.arguments = Bundle().apply {
                        putString("email", email)
                        putString("provider", provider)
                    }
                    perfilFragment
                }
                else -> InicioFragment()
            }
            openFragment(fragment)
            true
        }

        // Manejo del botón atrás del sistema
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showExitConfirmation()
            }
        })

    }

    private fun showExitConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Salir de la app")
            .setMessage("¿Estás seguro de que deseas salir?")
            .setPositiveButton("Sí") { _, _ ->
                finishAffinity() // Cierra completamente la app
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun openFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
