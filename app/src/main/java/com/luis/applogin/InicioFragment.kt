import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.luis.applogin.R

class InicioFragment : Fragment(R.layout.fragment_inicio) {

    private lateinit var emailText2: TextView
    private lateinit var proveedorText: TextView
    private lateinit var salirButton: Button

    fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        emailText2 = view.findViewById(R.id.emailText2)
        proveedorText = view.findViewById(R.id.proveedorText)
        salirButton = view.findViewById(R.id.salirButton)

        // Obtener datos del Intent desde la actividad
        val email = activity?.intent?.getStringExtra("email") ?: ""
        val provider = activity?.intent?.getStringExtra("provider") ?: ""

        emailText2.text = email
        proveedorText.text = provider

        salirButton.setOnClickListener {
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Confirmar salida")
            builder.setMessage("¿Estás seguro de que deseas cerrar sesión?")
            builder.setPositiveButton("Sí") { _, _ ->
                FirebaseAuth.getInstance().signOut()
                activity?.finish() // o usar nav al login si quieres
            }
            builder.setNegativeButton("No", null)
            builder.show()
        }
    }
}
