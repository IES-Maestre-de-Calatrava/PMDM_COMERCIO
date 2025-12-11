package es.maestre.juntos.view

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.appbar.MaterialToolbar
import es.maestre.juntos.R
import es.maestre.juntos.adapter.ComentarioAdapter
import es.maestre.juntos.databinding.ActivityConectaBinding
import es.maestre.juntos.viewModel.ComentarioViewModel
import kotlin.getValue

/**
 * Actividad que muestra la pantalla de comentarios en el recyclerView
 */
class ConectaActivity : AppCompatActivity() {

    // instancia de mi comentarioAdapter
    lateinit var myAdapter: ComentarioAdapter
    private lateinit var binding: ActivityConectaBinding

    // instancia a mi ComentarioViewModel
    val viewmodel: ComentarioViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityConectaBinding.inflate(layoutInflater)
        setContentView(binding.root)


        //configurar barra de herramientas
        val toolbar: MaterialToolbar = binding.materialToolbar
        setSupportActionBar(toolbar)

        // Configuramos el RecyclerView
        initRecyclerView(viewmodel)

        //si cambian los datos, actualizar rv
        viewmodel.data.observe(this) { data ->
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


    private fun initRecyclerView(viewmodel: ComentarioViewModel) {
        val manager = LinearLayoutManager(this)
        binding.rvComentarios.layoutManager = manager
        myAdapter = ComentarioAdapter( mutableListOf())
        binding.rvComentarios.adapter = myAdapter

        //separador de elementos
        val decoration = DividerItemDecoration(this, manager.orientation)
        binding.rvComentarios.addItemDecoration(decoration)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.menu_add -> {
                val intent = Intent(this, ComentarioActivity::class.java)
                intent.putExtra("mode","NEW")
                startActivity(intent)
                true
            }
            R.id.menu_detalle -> {
                val selectedItem = myAdapter.getSelectedItem()
                if (selectedItem != null) {
                    val intent = Intent(this, ComentarioActivity::class.java)
                    intent.putExtra("mode","DETAIL")
                    intent.putExtra("comentario_data",selectedItem)
                    startActivity(intent)
                }
                true
            }
            R.id.menu_edicion -> {
                val selectedItem = myAdapter.getSelectedItem()
                if (selectedItem != null) {
                    val intent = Intent(this, ComentarioActivity::class.java)
                    intent.putExtra("mode","EDIT")
                    intent.putExtra("comentario_data",selectedItem)
                    startActivity(intent)
                }
                true
            }
            R.id.menu_eliminar -> {
                val selectedItem = myAdapter.getSelectedItem()
                if (selectedItem != null) {
                    viewmodel.delete(selectedItem)
                    myAdapter.notifyItemRemoved(myAdapter.getSelectedPosition())
                    myAdapter.notifyDataSetChanged()
                    Toast.makeText(this,"Se eliminó el comentario", Toast.LENGTH_SHORT).show()
                }
                true
            }
            else -> false
        }
    }
}