package com.luis.applogin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment

class AgregarFragment : Fragment() {

    private lateinit var btnEmpresa: LinearLayout
    private lateinit var btnEmpleado: LinearLayout
    private lateinit var btnPaquete: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_agregar, container, false)

        btnEmpresa = view.findViewById(R.id.btnAgregarEmpresa)
        btnEmpleado = view.findViewById(R.id.btnAgregarEmpleado)
        btnPaquete = view.findViewById(R.id.btnAgregarPaquete)

        btnEmpresa.setOnClickListener {
            cargarFragment(Agregar_Empresa_Fragment())
        }

        btnEmpleado.setOnClickListener {
            cargarFragment(Agregar_Empleado_Fragment())
        }

        btnPaquete.setOnClickListener {
            cargarFragment(Agregar_Paquete_Fragment())
        }

        return view
    }

    private fun cargarFragment(fragment: Fragment) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}
