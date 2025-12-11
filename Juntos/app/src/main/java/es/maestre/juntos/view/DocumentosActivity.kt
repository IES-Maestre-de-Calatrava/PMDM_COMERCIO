package es.maestre.juntos.view

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import es.maestre.juntos.R
import es.maestre.juntos.databinding.ActivityDocumentosBinding
import es.maestre.juntos.viewModel.DocumentoViewModel
import kotlin.getValue

/**
 * Actividad que muestra la pantalla de tareas, inactiva en esta entrega a la espera
 * de la reunion con los de comercio para especificar como la quieren
 */
class DocumentosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDocumentosBinding
    private val viewModel: DocumentoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityDocumentosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /**
         * Boton para volver a la pantalla principal
         */
        binding.btnTienda.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        // 2. En tu onCreate, configura un botón para abrir la galería
        // Suponiendo que tienes un botón btnSubirFoto en tu pantalla

        binding.btnSubirDocumento.setOnClickListener {
            pickImageLauncher.launch(arrayOf(
                "image/*",
                "application/pdf",
                "application/msword", // .doc
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document" // .docx
            ))
        } // establecemos un boton para subir diferentes tipos de documentos (imagen, pdf, doc, docx)

        binding.btnDescargarDocumentos.setOnClickListener {
            val intent = Intent(this, DescargasActivity::class.java)
            startActivity(intent)
        }


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }




    val pickImageLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->

        uri?.let {
            // Obtener el tipo MIME del archivo seleccionado
            val mimeType = contentResolver.getType(it)

            // Convertir URI a ByteArray para subirlo
            val inputStream = contentResolver.openInputStream(it)
            val byteArray = inputStream?.readBytes()

            if (byteArray != null) {

                val extension = mimeType?.substringAfterLast('/', "jpg") ?: "jpg"

                // Llamar a la función de subida (puedes mostrar un ProgressBar aquí)
                viewModel.subirImagen(
                    byteArray = byteArray,
                    extension = extension, // extension del archivo
                    onSuccess = { url ->

                        Toast.makeText(this, "Archivo subido correctamente", Toast.LENGTH_SHORT).show()

                    },

                    onError = { e ->

                        Toast.makeText(this, "Error al subir: ${e.message}", Toast.LENGTH_LONG).show()

                    }

                )

            }

        }

    }

}