package es.maestre.juntosjc

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import es.maestre.juntosjc.model.Ayuda
import es.maestre.juntosjc.model.EventoItem
import es.maestre.juntosjc.ui.theme.JUNTOSJCTheme
import es.maestre.juntosjc.ui.theme.JuntosTheme
import es.maestre.juntosjc.viewModel.EventoViewModel
import es.maestre.juntosjc.viewModel.UserPreferencesViewModel
import java.text.SimpleDateFormat
import java.util.*

class CalendarioActivity : FragmentActivity() {

    private val viewModel: EventoViewModel by viewModels()
    private val preferencesViewModel: UserPreferencesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val isDarkTheme by preferencesViewModel.isDarkTheme.collectAsStateWithLifecycle()

            JUNTOSJCTheme(darkTheme = isDarkTheme) {
                MyAppCalendario(
                    viewModel = viewModel,
                    preferencesViewModel = preferencesViewModel
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyAppCalendario(viewModel: EventoViewModel, preferencesViewModel: UserPreferencesViewModel) {
    val context = LocalContext.current
    val fm = (context as FragmentActivity).supportFragmentManager

    val eventos = viewModel.listaEventosFiltrados
    val fechasConEventos = viewModel.fechasConEventos

    // Fecha seleccionada, por defecto coge la de hoy
    var fechaSeleccionada by remember { mutableStateOf(System.currentTimeMillis()) }

    // Texto que muestra la fecha seleccionada
    val fechaFormateada = remember(fechaSeleccionada) {
        SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date(fechaSeleccionada))
    }

    var showDialog by remember { mutableStateOf(false) }
    var nuevoTitulo by remember { mutableStateOf("") }
    var nuevaDesc by remember { mutableStateOf("") }
    var nuevosAsistentes by remember { mutableStateOf("") }
    var nuevaHora by remember { mutableStateOf("") }

    // Cargamos todas las fechas con eventos al entrar
    LaunchedEffect(Unit) {
        viewModel.cargarTodasLasFechasConEventos()
        viewModel.obtenerEventosPorFechaSupabase(fechaSeleccionada)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
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
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    val intent = Intent(context, DetalleEventoActivity::class.java).apply {
                        putExtra("ID_EVENTO", 0)
                        putExtra("FECHA_EVENTO", fechaSeleccionada)
                    }
                    context.startActivity(intent)
                },
                containerColor = colorResource(R.color.white),
                contentColor = Color.Unspecified
            ) {
                Icon(
                    painter = painterResource(R.drawable.add_to_svgrepo_com),
                    contentDescription = stringResource(R.string.addevento)
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Título
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

            // Botón que abre el MaterialDatePicker
            Button(
                onClick = {
                    val picker = MaterialDatePicker.Builder.datePicker()
                        .setTitleText("Selecciona fecha")
                        .setSelection(fechaSeleccionada)
                        .setDayViewDecorator(EventoDayDecorator(fechasConEventos.toSet()))
                        .build()

                    picker.addOnPositiveButtonClickListener { selectionMillis ->
                        fechaSeleccionada = selectionMillis
                        viewModel.obtenerEventosPorFechaSupabase(selectionMillis)
                    }

                    picker.show(fm, "MATERIAL_DATE_PICKER")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    painter = painterResource(R.drawable.calendar_svgrepo_com),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = Color.Unspecified
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = fechaFormateada)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Lista de eventos del día
            Text(
                text = stringResource(R.string.eventosDelDia),
                modifier = Modifier.padding(bottom = 8.dp),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (eventos.isEmpty()) {
                    item {
                        Text(
                            stringResource(R.string.nohayeventos),
                            color = colorResource(R.color.gris)
                        )
                    }
                } else {
                    items(eventos.sortedBy { it.Hora }) { evento ->
                        EventoItemRow(
                            evento = evento,
                            onDeleteConfirmed = {
                                evento.id_evento?.let { id ->
                                    viewModel.borrarEventoSupabase(id, fechaSeleccionada)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EventoItemRow(evento: EventoItem, onDeleteConfirmed: () -> Unit) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.preguntaEliminar)) },
            text = { Text(stringResource(R.string.confirmacionEliminar)) },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteConfirmed()
                    showDeleteDialog = false
                }) {
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
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {
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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = " ${evento.titulo_evento}  Hora: ${evento.Hora.take(5)}",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge,
                color = JuntosTheme.colors.content
            )
        }
    }
}