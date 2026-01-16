package es.maestre.juntosjc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import es.maestre.juntosjc.model.Evento
import es.maestre.juntosjc.ui.theme.JUNTOSJCTheme
import es.maestre.juntosjc.viewModel.EventoViewModel
import java.util.Calendar
import kotlin.getValue
import androidx.compose.material3.AlertDialog
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource

/**
 * Clase CalendarioActivity: en esta clase se podrán añadir eventos
 * a días en un calendario y eliminarlos, los eventos aparecerán al
 * seleccionar el día, en una LazyColumn
 */
class CalendarioActivity: ComponentActivity()  {

    // instancio mi viewModel para el acceso a BBDD
    private val viewModel: EventoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            JUNTOSJCTheme {
                MyAppCalendario(viewModel = viewModel)
            }
        }
    }
}


/**
 * Inserta los principales componentes de la activity como el datePicker, el
 * boton de añadir y filtrar los items de la LazyColumn. Tambien llama al resto de funciones
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyAppCalendario(viewModel: EventoViewModel) {
    val eventos by viewModel.data.observeAsState(initial = emptyList())

    var showDialog by remember { mutableStateOf(false) }
    var nuevoTitulo by remember { mutableStateOf("") }
    var nuevaDesc by remember { mutableStateOf("") }

    // Estado del DatePicker
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis() // Actual por defecto
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.txt_calendario),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorResource(R.color.container),
                    titleContentColor = colorResource(R.color.content)
                )

            )
        },

        // al pulsar el boton de añadir, nos muestra un cuadro de diálogo para añadir un evento
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = true },
                containerColor = colorResource(R.color.verde_esmeralda),
                contentColor = Color.Unspecified // esto hace que el contenido no coja ningun color
            ) {
                Icon(painter = painterResource(R.drawable.add_to_svgrepo_com), contentDescription = stringResource(R.string.addevento))
            }
        }


    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // El calendario (DatePicker)
            DatePicker(
                state = datePickerState,
                showModeToggle = false, // Para que no cambie a modo escribir
                title = null,
                headline = null,
                modifier = Modifier.weight(1.2f) // Ocupa la parte de arriba
            )

            HorizontalDivider()

            // La LazyColumn
            Text(
                text = stringResource(R.string.eventosDelDia),
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            val fechaSeleccionada = datePickerState.selectedDateMillis

            // Filtramos los eventos que coincidan con el día seleccionado
            val eventosDelDia = eventos.filter { ev ->
                esMismoDia(ev.fechaEvento, fechaSeleccionada)
            }

            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (eventosDelDia.isEmpty()) {
                    item { Text(
                        stringResource(R.string.nohayeventos),
                        color = colorResource(R.color.gris))
                    }
                } else {
                    items(eventosDelDia) { evento ->
                        CardEvento(
                            evento = evento,
                            onDeleteConfirmed = { viewModel.delete(evento) }
                        )
                    }
                }
            }
        }
    }

    // cuando clicamos a añadir evento, showDialog cambia a true y lo muestra
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(stringResource(R.string.nuevoevento)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = nuevoTitulo,
                        onValueChange = { nuevoTitulo = it },
                        label = { Text(stringResource(R.string.titulo)) }
                    )
                    OutlinedTextField(
                        value = nuevaDesc,
                        onValueChange = { nuevaDesc = it },
                        label = { Text(stringResource(R.string.descripcion)) }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (nuevoTitulo.isNotBlank()) {
                            // Usamos la fecha que está marcada en el DatePicker
                            val fechaParaEvento = datePickerState.selectedDateMillis ?: System.currentTimeMillis()

                            viewModel.insert(Evento(
                                tituloEvento = nuevoTitulo,
                                descripcionEvento = nuevaDesc,
                                fechaEvento = fechaParaEvento
                            ))

                            // Limpiar y cerrar
                            nuevoTitulo = ""
                            nuevaDesc = ""
                            showDialog = false
                        }
                    }
                ) {
                    Text(stringResource(R.string.btn_Guardar))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

/**
 * Esta funcion me comprueba que dos fechas coincidan para el filtrado
 * de los eventos del LazyColumn
 */
fun esMismoDia(millis1: Long, millis2: Long?): Boolean {
    if (millis2 == null) return false
    val cal1 = Calendar.getInstance().apply { timeInMillis = millis1 }
    val cal2 = Calendar.getInstance().apply { timeInMillis = millis2 }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

/**
 * Funcion que establece la estructura de cada uno de los items que
 * se cargan en el LazyColumn
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CardEvento(evento: Evento, onDeleteConfirmed: () -> Unit) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Diálogo de confirmación de borrado, solo aparece si mantenemos pulsado el evento
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.preguntaEliminar)) },
            text = { Text(stringResource(R.string.confirmacionEliminar)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteConfirmed()
                        showDeleteDialog = false
                    }
                ) {
                    Text(stringResource(R.string.btn_Eliminar), color = colorResource(R.color.rojo_material))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.btn_Cancelar))
                }
            }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth()
            .combinedClickable(
                onClick = {},
                onLongClick = { showDeleteDialog = true }
            ),
        colors = CardDefaults.cardColors(containerColor = colorResource(R.color.container))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = evento.tituloEvento, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = evento.descripcionEvento, style = MaterialTheme.typography.bodyMedium)
        }
    }
}