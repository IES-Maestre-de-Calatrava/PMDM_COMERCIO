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
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import es.maestre.juntosjc.model.Ayuda
import es.maestre.juntosjc.model.TareaItem
import es.maestre.juntosjc.viewModel.TareaViewModel
import kotlin.getValue

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.lifecycle.compose.collectAsStateWithLifecycle

import es.maestre.juntosjc.viewModel.UserPreferencesViewModel
import es.maestre.juntosjc.ui.theme.JUNTOSJCTheme
import es.maestre.juntosjc.ui.theme.JuntosTheme
import es.maestre.juntosjc.model.AppFeature

/**
 * Clase TareasActivity: en esta clase se muestra una LazyColumn con
 * las tareas. Se puede añadir tareas, visualizarlas, modificarlas y borrarlas
 */
class TareasActivity: ComponentActivity()  {

    // instancio mi viewModel para el acceso a BBDD
    private val viewModel: TareaViewModel by viewModels()

    private val preferencesViewModel: UserPreferencesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Pido los datos al abrir
        viewModel.obtenerTareasSupabase()

        setContent {
            val isDarkTheme by preferencesViewModel.isDarkTheme.collectAsStateWithLifecycle()

            JUNTOSJCTheme (darkTheme = isDarkTheme){
                MyAppTareas(
                    viewModel = viewModel,
                    preferencesViewModel = preferencesViewModel
                )
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
fun MyAppTareas(viewModel: TareaViewModel, preferencesViewModel: UserPreferencesViewModel) {
    val context = LocalContext.current

    var filtroEstado by remember { mutableIntStateOf(0) }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.obtenerTareasSupabase()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            // Cabecera con TopAppBar
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.logo_nuevo),
                            contentDescription = null,
                            modifier = Modifier.size(30.dp),
                            tint = Color.Unspecified

                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.txt_Juntos),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = JuntosTheme.colors.azulOscuroLogo
                            )
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = JuntosTheme.colors.container,
                    titleContentColor = JuntosTheme.colors.content
                ),
                actions = {
                    IconButton(onClick = {
                        val intent = Intent(context, AyudaActivity::class.java)
                        intent.putExtra("SECCION", Ayuda.TAREAS) // Filtro para Tareas
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
        },

        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    val intent = Intent(context, DetalleTareaActivity::class.java).apply {
                        putExtra("ID_TAREA", 0)
                    }
                    context.startActivity(intent)
                },
                containerColor = colorResource(R.color.white),
                contentColor = Color.Unspecified
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.add_to_svgrepo_com),
                    contentDescription = stringResource(R.string.btn_Crear)
                )
            }
        }


    ) { paddingSobrante ->
        // El contenido de la LazyColumn se ajusta debajo de la cabecera gracias a paddingSobrante
        Column(
            modifier = Modifier.padding(paddingSobrante)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(R.drawable.information_svgrepo_com),
                    contentDescription = null,
                    modifier = Modifier.size(30.dp),
                    tint = Color.Unspecified

                )
                Spacer(modifier = Modifier.width(8.dp)) // Espacio entre texto e icono
                Text(
                    stringResource(R.string.txt_tareas),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = JuntosTheme.colors.azulOscuroLogo
                    )
                )
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Botón para "Todos"
                FiltroButton("Todos", 0, filtroEstado) { filtroEstado = it }
                // Botones por estado (usando tus IDs 1, 2, 3)
                FiltroButton("Hecha", 1, filtroEstado) { filtroEstado = it }
                FiltroButton("En Proceso", 2, filtroEstado) { filtroEstado = it }
                FiltroButton("Pendiente", 3, filtroEstado) { filtroEstado = it }
            }


            ListaTareas(viewModel = viewModel, estadofiltro = filtroEstado)
        }
    }
}

/**
 * Funcion que carga los items de la BBDD en el LazyColumn
 */
@Composable
fun ListaTareas(viewModel: TareaViewModel, estadofiltro: Int) {
    val listaTareas = viewModel.listaTareasSupabase
    val context = LocalContext.current

    // 1. Calculamos la lista filtrada
    val listaFiltrada = if (estadofiltro == 0) {
        listaTareas
    } else {
        listaTareas.filter { it.estado == estadofiltro }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 2. IMPORTANTE: Usar listaFiltrada aquí, no listaTareas
        items(listaFiltrada) { tarea ->
            TareaItem(
                tarea = tarea,
                onClick = {
                    val intent = Intent(context, DetalleTareaActivity::class.java).apply {
                        putExtra("ID_TAREA", tarea.id_tarea ?: -1)
                        putExtra("TITULO_TAREA", tarea.titulo_tarea)
                        putExtra("DESCRIPCION_TAREA", tarea.descripcion_tarea)
                        putExtra("FECHA_ENTREGA", tarea.fecha_entrega)
                        putExtra("ESTADO", tarea.estado)
                        putExtra("PERSONA_ENCARGADA", tarea.persona_encargada)
                        putExtra("HORA", tarea.hora)
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
    val iconoEstado = elegirEstado(tarea.estado)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = JuntosTheme.colors.container)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = iconoEstado),
                contentDescription = stringResource(R.string.icono_estado),
                modifier = Modifier.size(28.dp),
                tint = Color.Unspecified
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = tarea.titulo_tarea, // titulo de la tarea
                    style = MaterialTheme.typography.titleMedium,
                    color=JuntosTheme.colors.content
                )
                Text(
                    text = tarea.fecha_entrega,
                    style = MaterialTheme.typography.titleSmall,
                    color = JuntosTheme.colors.content
                )
            }
        }
    }
}

fun elegirEstado(estado: Int): Int {

    return when (estado){
        1 -> R.drawable.done
        2 -> R.drawable.in_proggres
        3 -> R.drawable.to_do
        else -> R.drawable.to_do
    }
}


@Composable
fun FiltroButton(texto: String, estadoId: Int, estadoActual: Int, onClick: (Int) -> Unit) {
    val isSelected = estadoId == estadoActual

    val backgroundColor = if (isSelected) {
        // Colores después de pulsar
        when (estadoId) {
            1 -> colorResource(R.color.verde_esmeralda)
            2 -> colorResource(R.color.amarillo_electrico)
            3 -> colorResource(R.color.rojo)
            else -> colorResource(R.color.azul_contraste) // Caso 0: Todos
        }
    } else {
        // Colores pasteles
        when (estadoId) {
            1 -> colorResource(R.color.verde_pastel)
            2 -> colorResource(R.color.amarillo_pastel)
            3 -> colorResource(R.color.rojo_pastel)
            else -> colorResource(R.color.azul_pastel) // Caso 0: Todos
        }
    }

    Button(
        onClick = { onClick(estadoId) },
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = colorResource(R.color.grisOscuro)
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = if (isSelected) 6.dp else 2.dp,
            pressedElevation = 1.dp
        ),
        modifier = Modifier.padding(horizontal = 4.dp),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = texto,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
        )
    }
}