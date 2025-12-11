package es.maestre.juntos.view

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.activity.viewModels
import es.maestre.juntos.R
import es.maestre.juntos.databinding.ActivityMainBinding
import es.maestre.juntos.viewModel.ComentarioViewModel
import es.maestre.juntos.viewModel.DocumentoViewModel



/**
 * Actividad principal de la aplicación que sirve de puente entre las diferentes activities
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // instancia del ViewModel
    private val comentarioViewModel: ComentarioViewModel by viewModels()
    private val documentosViewModel: DocumentoViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Llamada al metodo de insertar comentarios si no hay, que insertara comentarios iniciales en caso de no haber
        comentarioViewModel.insertarComentariosInicio()
        documentosViewModel.insertarDocumentosInicio()


        // BINDING DE LOS BOTONES PRINCIPALES (Activos: conectr y calendario)

        binding.btnCalendarioGrande.setOnClickListener {
            val intent = Intent(this, CalendarioActivity::class.java)
            startActivity(intent)
        }

        binding.btnConectaGrande.setOnClickListener {
            val intent = Intent(this, ConectaActivity::class.java)
            startActivity(intent)
        }


        binding.btnTareasGrande.setOnClickListener {
            val intent = Intent(this, TareasActivity::class.java)
            startActivity(intent)
        }

        binding.btnDocumentosGrande.setOnClickListener {
            val intent = Intent(this, DocumentosActivity::class.java)
            startActivity(intent)
        }



        // BINDING DEL BOTON DE ACERCA DE

        binding.btnAcercaDe.setOnClickListener {
            val intent = Intent(this, AcercadeActivity::class.java)
            startActivity(intent)
        }


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

}