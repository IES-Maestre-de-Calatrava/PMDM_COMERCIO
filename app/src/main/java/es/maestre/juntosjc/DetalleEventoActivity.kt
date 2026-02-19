package es.maestre.juntosjc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import es.maestre.juntosjc.model.EventoItem
import es.maestre.juntosjc.viewModel.EventoViewModel

import es.maestre.juntosjc.viewModel.UserPreferencesViewModel
import es.maestre.juntosjc.ui.theme.JUNTOSJCTheme
import es.maestre.juntosjc.ui.theme.JuntosTheme
import es.maestre.juntosjc.model.AppFeature

class DetalleEventoActivity : ComponentActivity() {

    private val viewModel: EventoViewModel by viewModels()

    private val preferencesViewModel: UserPreferencesViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Recuperamos los parámetros del Intent
        val idEvento = intent.getIntExtra("ID_EVENTO", -1)
        val titulo = intent.getStringExtra("TITULO_EVENTO") ?: ""
        val descripcion = intent.getStringExtra("DESCRIPCION_EVENTO") ?: ""
        val fecha = intent.getLongExtra("FECHA_EVENTO", 0L)
        val asistentes = intent.getStringExtra("ASISTENTES") ?: ""
        val hora = intent.getStringExtra("HORA") ?: ""

        enableEdgeToEdge()
        setContent {
            val isDarkTheme by preferencesViewModel.isDarkTheme.collectAsStateWithLifecycle()

            JUNTOSJCTheme (darkTheme = isDarkTheme) {
                val eventoActual = remember(idEvento) {
                    if (idEvento > 0) {
                        viewModel.listaEventosFiltrados.find { it.id_evento == idEvento }
                            ?: EventoItem(idEvento, titulo, descripcion, fecha, asistentes, hora)
                    } else {
                        // Esto sería para crear uno nuevo si lo llamaras sin ID
                        EventoItem(null, "", "", System.currentTimeMillis(), "", "")
                    }
                }

                MyAppDetalleEvento(viewModel = viewModel, idEvento = idEvento, eventoRecibido = eventoActual,  preferencesViewModel = preferencesViewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyAppDetalleEvento(viewModel: EventoViewModel, idEvento: Int, eventoRecibido: EventoItem?,  preferencesViewModel: UserPreferencesViewModel) {
    val context = LocalContext.current

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
                    containerColor = JuntosTheme.colors.container,
                    titleContentColor = JuntosTheme.colors.content
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(R.drawable.calendar_svgrepo_com),
                    contentDescription = null,
                    modifier = Modifier.size(30.dp),
                    tint = Color.Unspecified

                )
                Spacer(modifier = Modifier.width(8.dp)) // Espacio entre texto e icono
                Text(
                    stringResource(R.string.txt_calendario),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = JuntosTheme.colors.azulOscuroLogo
                    )
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))


            eventoRecibido?.let { evento ->
                CamposDetalleEvento(
                    evento = evento,
                    viewModel = viewModel,
                    esNuevo = idEvento <= 0,
                    onActionDone = { (context as ComponentActivity).finish() }
                )
            }
        }
    }
}

@Composable
fun CamposDetalleEvento(evento: EventoItem, viewModel: EventoViewModel, esNuevo: Boolean, onActionDone: () -> Unit) {
    var titulo by remember { mutableStateOf(evento.titulo_evento) }
    var descripcion by remember { mutableStateOf(evento.descripcion_evento) }
    var asistentes by remember { mutableStateOf(evento.asistentes) }
    var hora by remember { mutableStateOf(evento.Hora) }

    Text(text = "Título del evento:", fontWeight = FontWeight.Bold)
    OutlinedTextField(value = titulo, onValueChange = { titulo = it }, modifier = Modifier.fillMaxWidth())

    Text(text = "Descripción:", fontWeight = FontWeight.Bold)
    OutlinedTextField(value = descripcion, onValueChange = { descripcion = it }, modifier = Modifier.fillMaxWidth(), minLines = 3)

    Text(text = "Asistentes:", fontWeight = FontWeight.Bold)
    OutlinedTextField(value = asistentes, onValueChange = { asistentes = it }, modifier = Modifier.fillMaxWidth())

    Text(text = "Hora:", fontWeight = FontWeight.Bold)
    OutlinedTextField(value = hora, onValueChange = { hora = it }, modifier = Modifier.fillMaxWidth())

    Spacer(modifier = Modifier.height(8.dp))

    // Botón Guardar
    Button(
        onClick = {
            val nuevoItem = EventoItem(
                id_evento = if (esNuevo) null else evento.id_evento,
                titulo_evento = titulo,
                descripcion_evento = descripcion,
                fecha_evento = evento.fecha_evento,
                asistentes = asistentes,
                Hora = hora
            )

            if (esNuevo) {
                viewModel.insertarEventoSupabase(nuevoItem) { onActionDone() }
            } else {
                viewModel.actualizarEventoSupabase(nuevoItem) { onActionDone() }
            }
        },
        modifier = Modifier.fillMaxWidth().height(50.dp),
        colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.azul_pastel))
    ) {
        Icon(painter = painterResource(id = R.drawable.save_svgrepo_com), contentDescription = null, modifier = Modifier.size(24.dp), tint = Color.Unspecified)
        Spacer(modifier = Modifier.width(10.dp))
        Text(text = stringResource(R.string.btn_Guardar))
    }

    // Botón Eliminar
    if (!esNuevo) {
        Button(
            onClick = {
                evento.id_evento?.let { id ->
                    viewModel.borrarEventoSupabase(id, evento.fecha_evento)
                    onActionDone()
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.rojo_pastel))
        ) {
            Icon(painter = painterResource(id = R.drawable.trash_svgrepo_com), contentDescription = null, modifier = Modifier.size(24.dp), tint = Color.Unspecified)
            Spacer(modifier = Modifier.width(10.dp))
            Text(text = stringResource(R.string.btn_Eliminar))
        }
    }
}