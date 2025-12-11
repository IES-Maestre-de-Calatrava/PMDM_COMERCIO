package es.maestre.juntos.view

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import es.maestre.juntos.R
import es.maestre.juntos.databinding.ActivityEventocalendarioBinding
import es.maestre.juntos.util.Calendario
import java.util.concurrent.TimeUnit

/**
 * Actividad que muestra la pantalla de añadir evento al calendario
 */
class EventoCalendarioActivity: AppCompatActivity() {

    private lateinit var binding: ActivityEventocalendarioBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityEventocalendarioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Listener del botón para añadir el evento
        binding.btnGuardarEvento.setOnClickListener {
            addEvento()
        }

        binding.btnVolverEventoCalendario.setOnClickListener {
            val intent = Intent(this, CalendarioActivity::class.java)
            startActivity(intent)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    /**
     * Funcion que añade un evento al calendario y llama a addEventoToCalendar del calendario
     */
    private fun addEvento(){
        val title = binding.etTituloEvento.text.toString().trim()
        val location = binding.etUbicacionEvento.text.toString().trim()
        val description = binding.etDetallesEvento.text.toString().trim()


        // Compruebo que el título no esté vacío
        if (title.isBlank()) {
            Toast.makeText(this, "Por favor, introduce un título para el evento.", Toast.LENGTH_LONG).show()
        }
        else{
            // La hora de inicio la cojo con el TimeMillis()
            val startTimeMillis = System.currentTimeMillis()

            // La hora de fin de momento la defino automaticamente por ejemplo 1 hora después
            val endTimeMillis = startTimeMillis + TimeUnit.HOURS.toMillis(1)

            // Llamo a la funcion del calendario
            Calendario.addEventToCalendar(
                this,
                title,
                location,
                description,
                startTimeMillis,
                endTimeMillis
            )

            finish()
        }
    }

}