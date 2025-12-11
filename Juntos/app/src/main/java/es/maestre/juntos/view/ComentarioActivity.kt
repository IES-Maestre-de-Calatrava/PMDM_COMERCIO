package es.maestre.juntos.view

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import es.maestre.juntos.R
import es.maestre.juntos.databinding.ActivityComentarioBinding
import es.maestre.juntos.model.Comentario
import es.maestre.juntos.viewModel.ComentarioViewModel
import androidx.activity.result.contract.ActivityResultContracts



/**
 * Actividad que muestra la pantalla de editor de comentarios
 */
class ComentarioActivity: AppCompatActivity() {

    private lateinit var binding: ActivityComentarioBinding
    private val viewModel: ComentarioViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityComentarioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /**
         * Boton para volver a la pantalla principal
         */
        binding.btnVolver.setOnClickListener {
            val intent = Intent(this, ConectaActivity::class.java)
            startActivity(intent)
        }

        // Recupero los datos del Intent
        var mode = intent.getStringExtra("mode")
        var idComentario:Long = 0

        // Switch con las opciones estandar, nuevo comentario, detalles del comentario, editar comentario y eliminar comentario
        when (mode) {
            "NEW" -> {
                idComentario = 0
            }
            "DETAIL" -> {

                var currentComentario: Comentario? = null

                /**
                 * Aqui tuve que buscar en internet porque el metodo getSerializableExtra me daba error, me salio que era por culpa de la version de android API 33 "TIRAMISU"
                 * asi que con el if lo soluciona comprobando cual es
                 */
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    // Metodo seguro para API 33+
                    currentComentario = intent.getSerializableExtra("comentario_data", Comentario::class.java)
                } else {
                    // Metodo obsoleto con casting para API < 33
                    @Suppress("DEPRECATION")
                    currentComentario = intent.getSerializableExtra("comentario_data") as Comentario?
                }

                idComentario = currentComentario?.idComentario ?: 0
                binding.nombre.setText(currentComentario?.nombre)
                binding.texto.setText(currentComentario?.texto)

                //poner campos deshabilitados
                binding.nombre.setEnabled(false)
                binding.texto.setEnabled(false)


                //en detalles no se puede guardar ni editar
                binding.btnGuardar.setVisibility(View.GONE)
            }
            "EDIT" -> {
                var currentComentario: Comentario? = null

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    // Metodo seguro para API 33+
                    currentComentario = intent.getSerializableExtra("comentario_data", Comentario::class.java)
                } else {
                    // Metodo obsoleto con casting para API < 33
                    @Suppress("DEPRECATION")
                    currentComentario = intent.getSerializableExtra("comentario_data") as Comentario?
                }

                idComentario = currentComentario?.idComentario ?: 0
                binding.nombre.setText(currentComentario?.nombre)
                binding.texto.setText(currentComentario?.texto)
            }
        }

        binding.btnGuardar.setOnClickListener {
            val comentario = Comentario( idComentario,binding.nombre.text.toString(),
                binding.texto.text.toString())

            when (mode) {
                "NEW" -> {
                    //crear comentario
                    viewModel.insert(comentario)
                    Toast.makeText(this,"Comentario creado", Toast.LENGTH_LONG).show()
                    finish()
                }
                "EDIT" -> {
                    //editar comentario
                    viewModel.update(comentario)
                    Toast.makeText(this,"Comentario modificado", Toast.LENGTH_LONG).show()
                    finish()
                }

            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

    }




}