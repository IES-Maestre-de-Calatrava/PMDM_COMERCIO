package es.maestre.juntos.view

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import es.maestre.juntos.R
import es.maestre.juntos.adapter.DocumentoAdapter
import es.maestre.juntos.databinding.ActivityDescargasBinding
import es.maestre.juntos.viewModel.DocumentoViewModel

/**
 * Actividad que muestra la pantalla de documentos disponibles para descargar
 */
class DescargasActivity: AppCompatActivity() {

    // instancia de mi DocumentoAdapter
    lateinit var myAdapter: DocumentoAdapter
    private lateinit var binding: ActivityDescargasBinding

    // instancia a mi DocumentoViewModel
    val viewModel: DocumentoViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityDescargasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initRecyclerView(viewModel)

        viewModel.data.observe(this) { data ->
            myAdapter.updateData(data)
        }

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


    private fun initRecyclerView(viewModel: DocumentoViewModel) {
        val manager = LinearLayoutManager(this)
        binding.rvDescargas.layoutManager = manager
        myAdapter = DocumentoAdapter(mutableListOf())
        binding.rvDescargas.adapter = myAdapter

        //separador de elementos
        val decoration = DividerItemDecoration(this, manager.orientation)
        binding.rvDescargas.addItemDecoration(decoration)
    }

}