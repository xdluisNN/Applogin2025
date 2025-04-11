package com.luis.applogin

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

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
    }

    private fun openFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
