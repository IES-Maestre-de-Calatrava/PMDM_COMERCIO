package es.maestre.juntos.view

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import es.maestre.juntos.R
import es.maestre.juntos.databinding.ActivityInvitarBinding

/**
 * Actividad que muestra la pantalla de invitar a un amigo, inactiva en esta entrega
 */
class InvitarActivity : AppCompatActivity() {

   private lateinit var binding: ActivityInvitarBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityInvitarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /**
         * Boton para volver a la pantalla principal
         */
        binding.btnTienda.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}