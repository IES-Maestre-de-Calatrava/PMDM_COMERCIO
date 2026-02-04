package es.maestre.juntosjc

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import es.maestre.juntosjc.model.TareaItem
import es.maestre.juntosjc.ui.theme.JUNTOSJCTheme
import es.maestre.juntosjc.viewModel.TareaViewModel
import kotlin.getValue

/**
 * Clase TareasActivity: en esta clase se muestra una LazyColumn con
 * las tareas. Se puede añadir tareas, visualizarlas, modificarlas y borrarlas
 */
class TareasActivity: ComponentActivity()  {

    // instancio mi viewModel para el acceso a BBDD
    private val viewModel: TareaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Pido los datos al abrir
        viewModel.obtenerTareasSupabase()

        setContent {
            JUNTOSJCTheme {
                MyAppTareas(viewModel = viewModel)
            }
        }
    }
    override fun onResume() {
        super.onResume()
        // Cada vez que la pantalla vuelve a estar visible, pedimos los datos
        viewModel.obtenerTareasSupabase()
    }
}

/**
 * Funcion que me genera la cabecera de la Activity con su nombre correspondiente
 * y me llama a la funcion que me lista los datos
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyAppTareas(viewModel: TareaViewModel) {
    val context = LocalContext.current

    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.obtenerTareasSupabase()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            // Cabecera con TopAppBar
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.txt_tareas),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorResource(R.color.container),
                    titleContentColor = colorResource(R.color.content)
                ),
                actions = {
                    IconButton(onClick = {
                        val intent = Intent(context, AyudaActivity::class.java)
                        intent.putExtra("SECCION", "TAREAS") // Filtro para Tareas
                        context.startActivity(intent)
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.help_question_svgrepo_com),
                            contentDescription = "Ayuda",
                            tint = Color.Unspecified
                        )
                    }
                }
            )
        }
    ) { paddingSobrante ->
        // El contenido de la LazyColumn se ajusta debajo de la cabecera gracias a paddingSobrante
        Column(
            modifier = Modifier.padding(paddingSobrante),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = {
                    // Creamos el Intent para ir a DetalleTareaActivity
                    val intent = Intent(context, DetalleTareaActivity::class.java).apply {
                        // Pasamos el ID como 0, ya q al no haber, para que mi clase tarea al tener el autoGenerate = true, me genere el id que vale
                        putExtra("ID_TAREA", 0)
                    }
                    context.startActivity(intent)
                },
                modifier = Modifier.height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(R.color.verde_esmeralda),
                    contentColor = Color.Unspecified
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.add_to_svgrepo_com),
                        contentDescription = stringResource(R.string.descripcion_btnCrear_tarea),
                        modifier = Modifier.size(30.dp),
                        tint = Color.Unspecified
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = stringResource(R.string.btn_Crear),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
            ListaTareas(viewModel = viewModel)
        }
    }
}

/**
 * Funcion que carga los items de la BBDD en el LazyColumn
 */
@Composable
fun ListaTareas(viewModel: TareaViewModel) {

    // Observamos los datos del LiveData definido en el ViewModel
    val listaTareas = viewModel.listaTareasSupabase
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        items(listaTareas) { tarea ->
            TareaItem(
                tarea = tarea,
                onClick = {
                    // Creamos el Intent para ir a DetalleTareaActivity
                    val intent = Intent(context, DetalleTareaActivity::class.java).apply {
                        // Pasamos el ID de la tarea como "extra"
                        putExtra("ID_TAREA", tarea.id_tarea ?: -1)
                        putExtra("TITULO_TAREA", tarea.titulo_tarea)
                        putExtra("DESCRIPCION_TAREA", tarea.descripcion_tarea)
                        putExtra("FECHA_ENTREGA", tarea.fecha_entrega)
                        putExtra("COMPLETA", tarea.completa)
                        putExtra("PERSONA_ENCARGADA", tarea.persona_encargada)
                    }
                    context.startActivity(intent)
                }
            )
        }
    }
}

/**
 * Funcion que establece la estructura de cada uno de los items que
 * se cargan en el LazyColumn
 */
@Composable
fun TareaItem(tarea: TareaItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Description,
                contentDescription = "Icono Tarea",
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = tarea.titulo_tarea, // titulo de la tarea
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = stringResource(R.string.texto_ver_COMENTARIO),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}