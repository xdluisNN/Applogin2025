package com.luis.applogin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class AsociadosFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: EmpresaAdapter
    private val empresas = mutableListOf<Empresa>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            val param1 = it.getString(ARG_PARAM1)
            val param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_asociados, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewEmpresas)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = EmpresaAdapter(empresas)
        recyclerView.adapter = adapter

        cargarEmpresas()

        return view
    }

    private fun cargarEmpresas() {
        FirebaseFirestore.getInstance().collection("empresas")
            .get()
            .addOnSuccessListener { snapshot ->
                empresas.clear()
                for (document in snapshot) {
                    val empresa = Empresa(
                        nombre = document.getString("nombre") ?: "",
                        descripcion = document.getString("descripcion") ?: "",
                        imagenUrl = document.getString("imagenUrl") ?: ""
                    )
                    empresas.add(empresa)
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error al cargar empresas", Toast.LENGTH_SHORT).show()
            }
    }

    companion object {
        private const val ARG_PARAM1 = "param1"
        private const val ARG_PARAM2 = "param2"

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AsociadosFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
