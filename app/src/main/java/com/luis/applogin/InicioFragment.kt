package com.luis.applogin

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout

class InicioFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_inicio, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<LinearLayout>(R.id.btnPaqueteria).setOnClickListener {
            // Navegar a Fragmento Paquetería
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, PaqueteriaFragment())
                .addToBackStack(null)
                .commit()
        }

        view.findViewById<LinearLayout>(R.id.btnAsociados).setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, AsociadosFragment())
                .addToBackStack(null)
                .commit()
        }

        view.findViewById<LinearLayout>(R.id.btnSistemas).setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, SistemasFragment())
                .addToBackStack(null)
                .commit()
        }
    }


}