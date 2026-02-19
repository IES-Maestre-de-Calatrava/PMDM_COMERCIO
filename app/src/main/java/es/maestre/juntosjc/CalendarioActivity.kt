package es.maestre.juntosjc

import android.content.Intent
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import es.maestre.juntosjc.viewModel.EventoViewModel
import kotlin.getValue
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.IconButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import es.maestre.juntosjc.model.Ayuda
import es.maestre.juntosjc.model.EventoItem
import androidx.compose.material3.SelectableDates
import androidx.lifecycle.compose.collectAsStateWithLifecycle

import es.maestre.juntosjc.viewModel.UserPreferencesViewModel
import es.maestre.juntosjc.ui.theme.JUNTOSJCTheme
import es.maestre.juntosjc.ui.theme.JuntosTheme
import es.maestre.juntosjc.model.AppFeature



/**
 * Clase CalendarioActivity: en esta clase se podrán añadir eventos
 * a días en un calendario y eliminarlos, los eventos aparecerán al
 * seleccionar el día, en una LazyColumn
 */
class CalendarioActivity: ComponentActivity()  {

    // instancio mi viewModel para el acceso a BBDD
    private val viewModel: EventoViewModel by viewModels()

    private val preferencesViewModel: UserPreferencesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val isDarkTheme by preferencesViewModel.isDarkTheme.collectAsStateWithLifecycle()

            JUNTOSJCTheme (darkTheme = isDarkTheme){
                MyAppCalendario(
                                viewModel = viewModel,
                                preferencesViewModel = preferencesViewModel
                )
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
fun MyAppCalendario(viewModel: EventoViewModel, preferencesViewModel: UserPreferencesViewModel) {
    val context = LocalContext.current
    val eventos = viewModel.listaEventosFiltrados
    val fechasConEventos = viewModel.fechasConEventos

    var showDialog by remember { mutableStateOf(false) }
    var nuevoTitulo by remember { mutableStateOf("") }
    var nuevaDesc by remember { mutableStateOf("") }
    var nuevosAsistentes by remember{mutableStateOf("")}
    var nuevaHora by remember{mutableStateOf("") }

    // Efecto para cargar eventos cuando cambie la fecha seleccionada
    LaunchedEffect(Unit) {
        viewModel.cargarTodasLasFechasConEventos()
    }

    val datePickerState = rememberDatePickerState(
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return true
            }

            override fun isSelectableYear(year: Int): Boolean {
                return true
            }
        }
    )

    LaunchedEffect(datePickerState.selectedDateMillis) {
        datePickerState.selectedDateMillis?.let { milis ->
            viewModel.obtenerEventosPorFechaSupabase(milis)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.icono_tiendacampa_a),
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
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),

                actions = {
                    IconButton(onClick = {
                        val intent = Intent(context, AyudaActivity::class.java)
                        intent.putExtra("SECCION", Ayuda.CALENDARIO)
                        context.startActivity(intent)
                    }) {
                        Icon(
                            painter = painterResource(R.drawable.help_question_svgrepo_com),
                            contentDescription = "Ayuda",
                            tint = Color.Unspecified
                        )
                    }
                }

            )
        },

        // al pulsar el boton de añadir, nos muestra un cuadro de diálogo para añadir un evento
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = true },
                containerColor = colorResource(R.color.white),
                        contentColor = Color.Unspecified
            ) {
                Icon(painter = painterResource(R.drawable.add_to_svgrepo_com),
                    contentDescription = stringResource(R.string.addevento))
            }
        }


    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(R.drawable.calendar_svgrepo_com),
                    contentDescription = null,
                    modifier = Modifier.size(30.dp),
                    tint = Color.Unspecified
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.txt_calendario),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = JuntosTheme.colors.azulOscuroLogo
                    )
                )

            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
            // El calendario (DatePicker)
            DatePicker(
                state = datePickerState,
                showModeToggle = false, // Para que no cambie a modo escribir
                title = null,
                headline = null,
                modifier = Modifier.fillMaxWidth(),
                colors = DatePickerDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    selectedDayContainerColor = MaterialTheme.colorScheme.primary,
                    selectedDayContentColor = colorResource(R.color.azul_contraste),
                    todayContentColor = colorResource(R.color.container),
                    todayDateBorderColor = colorResource(R.color.container),
                    dayContentColor = colorResource(R.color.black)
                )
            )

            if (fechasConEventos.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Text(
                        text = "Días con eventos:",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = colorResource(R.color.gris),
                        fontWeight = FontWeight.Bold
                    )

                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp), // Forzamos una altura para que no sea 0
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Ordenamos las fechas para que aparezcan en orden cronológico
                        items(fechasConEventos.sorted()) { fechaMilis ->
                            val fechaFormateada = java.text.SimpleDateFormat("dd MMM", java.util.Locale.getDefault())
                                .format(java.util.Date(fechaMilis))

                            androidx.compose.material3.FilterChip(
                                selected = datePickerState.selectedDateMillis == fechaMilis,
                                onClick = {
                                    datePickerState.selectedDateMillis = fechaMilis
                                },
                                label = { Text(fechaFormateada) },
                                leadingIcon = {
                                    Icon(
                                        painter = painterResource(R.drawable.calendar_svgrepo_com),
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = colorResource(R.color.azul_cielo)
                                    )
                                }
                            )
                        }
                    }
                }
            }

            // La LazyColumn
            Text(
                text = stringResource(R.string.eventosDelDia),
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (eventos.isEmpty()) {
                    item { Text(
                        stringResource(R.string.nohayeventos),
                        color = colorResource(R.color.gris))
                    }
                } else {
                    items(eventos.sortedBy {it.Hora} ) { evento ->
                        EventoItemRow(
                            evento = evento,
                            onDeleteConfirmed = {
                                evento.id_evento?.let { id ->
                                    viewModel.borrarEventoSupabase(id, datePickerState.selectedDateMillis ?: 0L)
                                }
                            }
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
                    OutlinedTextField(
                        value = nuevosAsistentes,
                        onValueChange = { nuevosAsistentes = it },
                        label = { Text(stringResource(R.string.asistentes)) }
                    )
                    OutlinedTextField(
                        value = nuevaHora,
                        onValueChange = { nuevaHora = it },
                        label = { Text(stringResource(R.string.hora)) }
                    )

                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (nuevoTitulo.isNotBlank()) {
                            // Usamos la fecha que está marcada en el DatePicker
                            val fechaParaEvento = datePickerState.selectedDateMillis ?: System.currentTimeMillis()

                            val nuevoEvento = EventoItem(
                                titulo_evento = nuevoTitulo,
                                descripcion_evento = nuevaDesc,
                                fecha_evento = fechaParaEvento,
                                asistentes = nuevosAsistentes,
                                Hora = nuevaHora
                            )

                            viewModel.insertarEventoSupabase(nuevoEvento){
                                viewModel.cargarTodasLasFechasConEventos()
                                // Limpiar y cerrar
                                nuevoTitulo = ""
                                nuevaDesc = ""
                                nuevosAsistentes = ""
                                nuevaHora = ""
                                showDialog = false
                            }
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
 * Funcion que establece la estructura de cada uno de los items que
 * se cargan en el LazyColumn
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EventoItemRow(evento: EventoItem, onDeleteConfirmed: () -> Unit) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

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
                    Text(stringResource(R.string.btn_Eliminar), color = colorResource(R.color.rojo_pastel))
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
                onClick = {
                    // Acción al hacer clic: Ir a Detalle
                    val intent = Intent(context, DetalleEventoActivity::class.java).apply {
                        putExtra("ID_EVENTO", evento.id_evento ?: -1)
                        putExtra("TITULO_EVENTO", evento.titulo_evento)
                        putExtra("DESCRIPCION_EVENTO", evento.descripcion_evento)
                        putExtra("FECHA_EVENTO", evento.fecha_evento)
                        putExtra("ASISTENTES", evento.asistentes)
                        putExtra("HORA", evento.Hora)
                    }
                    context.startActivity(intent)
                },
                onLongClick = { showDeleteDialog = true }
            ),
        colors = CardDefaults.cardColors(containerColor = JuntosTheme.colors.container)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.Start)
        {
            Text(
                text = " ${evento.titulo_evento}  Hora: ${evento.Hora.take(5)}",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge,
                color = JuntosTheme.colors.content
            )


        }
    }
}


