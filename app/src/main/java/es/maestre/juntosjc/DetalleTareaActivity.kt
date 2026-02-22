package es.maestre.juntosjc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import es.maestre.juntosjc.model.TareaItem
import es.maestre.juntosjc.viewModel.TareaViewModel
import kotlin.getValue
import java.time.LocalTime
import java.time.format.DateTimeFormatter

import es.maestre.juntosjc.viewModel.UserPreferencesViewModel
import es.maestre.juntosjc.ui.theme.JUNTOSJCTheme
import es.maestre.juntosjc.ui.theme.JuntosTheme
import es.maestre.juntosjc.model.AppFeature

/**
 * Clase DetalleActivity: esta clase es la que muestra la informacion
 * de cada uno de los items del LazyView de la TareasActivity, pudiendo guardarlos o eliminarlos
 */
class DetalleTareaActivity: ComponentActivity() {

    // instancio mi viewModel para el acceso a BBDD
    private val viewModel: TareaViewModel by viewModels()

    private val preferencesViewModel: UserPreferencesViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // recupero los parámetros que hemos pasado
        val idTarea = intent.getIntExtra("ID_TAREA", -1)
        val tituloTarea = intent.getStringExtra("TITULO_TAREA") ?: ""
        val descripcionTarea = intent.getStringExtra("DESCRIPCION_TAREA") ?: ""
        val fechaEntrega = intent.getStringExtra("FECHA_ENTREGA") ?: ""
        val estado = intent.getIntExtra("ESTADO", -1)
        val personaEncargada = intent.getStringExtra("PERSONA_ENCARGADA") ?: ""
        val hora = intent.getStringExtra("HORA") ?: ""


        enableEdgeToEdge()
        setContent {
            val isDarkTheme by preferencesViewModel.isDarkTheme.collectAsStateWithLifecycle()

            JUNTOSJCTheme (darkTheme = isDarkTheme) {

                val tareaActual = remember(idTarea) {
                    if (idTarea > 0) {
                        // Buscamos en la lista descargada de Supabase
                        viewModel.listaTareasSupabase.find { it.id_tarea == idTarea }
                            ?: TareaItem(idTarea, tituloTarea, descripcionTarea, fechaEntrega, estado, personaEncargada, hora) // Si no la encuentra, usa el backup
                    } else {
                        TareaItem(null, "", "", "", 3, "", "") // Nueva tarea
                    }
                }


                MyAppDetalleTarea(viewModel = viewModel, idTarea = idTarea, tareaRecibida = tareaActual, preferencesViewModel = preferencesViewModel) // hay que pasárselo a la funcion para completar los campos
            }
        }
    }
}


/**
 * Funcion que me arma la cabecera, me comprueba el id de la tarea
 * para saber si vamos a crear una nuevo o a modificar otra y me llama a
 * los campos de edicion de la tarea
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyAppDetalleTarea(viewModel: TareaViewModel, idTarea: Int, tareaRecibida: TareaItem?,  preferencesViewModel: UserPreferencesViewModel) {

    val context = LocalContext.current // Para cerrar la pantalla tras la acción

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
                    painter = painterResource(R.drawable.information_svgrepo_com),
                    contentDescription = null,
                    modifier = Modifier.size(30.dp),
                    tint = Color.Unspecified

                )
                Spacer(modifier = Modifier.width(8.dp)) // Espacio entre texto e icono
                val tituloPantalla = if (idTarea <= 0){
                    stringResource(R.string.txt_alta)
                } else {
                    stringResource(R.string.txt_edición)
                }
                Text(
                    tituloPantalla,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = JuntosTheme.colors.azulOscuroLogo
                    )
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            // Solo mostramos los campos si la tarea se ha cargado
            tareaRecibida?.let { tarea ->
                CamposDetalleTarea(tarea = tarea, viewModel = viewModel, esNuevo = idTarea <= 0, onActionDone = { (context as ComponentActivity).finish() })
            }
        }
    }
}

/**
 * Funcion que arma la pantalla con los campos editables y los botones de guardado y eliminado
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CamposDetalleTarea(
    tarea: TareaItem,
    viewModel: TareaViewModel,
    esNuevo: Boolean,
    onActionDone: () -> Unit
) {
    // Usamos estados para que los campos sean editables
    // Para editar realmente, luego usaremos estos valores en el botón Guardar
    var titulo by remember { mutableStateOf(tarea.titulo_tarea) }
    var descripcion by remember { mutableStateOf(tarea.descripcion_tarea) }
    var fecha by remember { mutableStateOf(tarea.fecha_entrega) }
    var estado by remember { mutableStateOf(tarea.estado) }
    var persona by remember { mutableStateOf(tarea.persona_encargada) }

    var showCalendar by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var hora by remember { mutableStateOf(tarea.hora) }

    val opcionesEstado = listOf(
        "Hecha" to 1,
        "En proceso" to 2,
        "Por hacer" to 3
    )
    var expanded by remember { mutableStateOf(false) }
    val textoEstadoSeleccionado = opcionesEstado.find { it.second == estado }?.first ?: "Seleccionar"

    // Campos editables segun los atributos de la tarea
    Text(text = "Titulo de la tarea:", fontWeight = FontWeight.Bold)
    OutlinedTextField(
        value = titulo,
        onValueChange = { titulo = it },
        modifier = Modifier.fillMaxWidth(),
        label = { Text("Titulo") }
    )

    Text(text = "Descripción de la tarea:", fontWeight = FontWeight.Bold)
    OutlinedTextField(
        value = descripcion,
        onValueChange = { descripcion = it },
        modifier = Modifier.fillMaxWidth(),
        label = { Text("Descripción") },
        minLines = 3
    )

    // FECHA
    Text(text = "Fecha de entrega:", fontWeight = FontWeight.Bold)
    OutlinedTextField(
        value = fecha,
        onValueChange = { },
        readOnly = true,
        modifier = Modifier.fillMaxWidth().clickable { showCalendar = true },
        label = { Text("Seleccionar Día") },
        trailingIcon = {
            IconButton(onClick = { showCalendar = true }) {
                Icon(painter = painterResource(id = R.drawable.calendar_svgrepo_com),
                    contentDescription = null,
                    tint = Color.Unspecified)
            }
        },
        enabled = false,
        colors = OutlinedTextFieldDefaults.colors(
            disabledTextColor = MaterialTheme.colorScheme.onSurface,
            disabledBorderColor = MaterialTheme.colorScheme.outline,
            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )

    if (showCalendar) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { showCalendar = false }) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                colors = CardDefaults.cardColors(JuntosTheme.colors.container)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    androidx.compose.ui.viewinterop.AndroidView(
                        factory = { context ->
                            android.widget.CalendarView(context).apply {
                                setOnDateChangeListener { _, year, month, dayOfMonth ->
                                    fecha = String.format("%02d/%02d/%d", dayOfMonth, month + 1, year)
                                    showCalendar = false
                                }
                            }
                        },
                        modifier = Modifier.wrapContentSize()
                    )
                }
            }
        }
    }

    // HORA
    Text(text = "Hora de entrega:", fontWeight = FontWeight.Bold)
    OutlinedTextField(
        value = hora,
        onValueChange = { },
        readOnly = true,
        modifier = Modifier.fillMaxWidth().clickable { showTimePicker = true },
        label = { Text("Seleccionar Hora") },
        trailingIcon = {
            IconButton(onClick = { showTimePicker = true }) {
                Icon(painter = painterResource(id = R.drawable.clock_svgrepo_com),
                    contentDescription = null,
                    tint = Color.Unspecified)
            }
        },
        enabled = false,
        colors = OutlinedTextFieldDefaults.colors(
            disabledTextColor = MaterialTheme.colorScheme.onSurface,
            disabledBorderColor = MaterialTheme.colorScheme.outline,
            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = if(hora.contains(":")) hora.split(":")[0].toInt() else 12,
            initialMinute = if(hora.contains(":")) hora.split(":")[1].toInt() else 0,
            is24Hour = true
        )

        androidx.compose.ui.window.Dialog(onDismissRequest = { showTimePicker = false }) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TimePicker(state = timePickerState)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showTimePicker = false }) { Text("Cancelar") }
                        TextButton(onClick = {
                            hora = String.format("%02d:%02d", timePickerState.hour, timePickerState.minute)
                            showTimePicker = false
                        }) { Text("Aceptar") }
                    }
                }
            }
        }
    }

    /* ESTADO DE LA TAREA */
    Text(text = "Estado de la tarea:", fontWeight = FontWeight.Bold)
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = textoEstadoSeleccionado,
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            opcionesEstado.forEach { (nombre, id) ->
                DropdownMenuItem(
                    text = { Text(text = nombre) },
                    onClick = {
                        estado = id // Actualizamos el estado con el Int (1, 2 o 3)
                        expanded = false
                    }
                )
            }
        }
    }

    Text(text = "Persona encargada de la tarea:", fontWeight = FontWeight.Bold)
    OutlinedTextField(
        value = persona,
        onValueChange = { persona = it },
        modifier = Modifier.fillMaxWidth(),
        label = { Text("Persona encargada") },
        minLines = 1
    )


    Button(
        onClick = {

            val nuevoItem = TareaItem(
                id_tarea = if (esNuevo) null else tarea.id_tarea,
                titulo_tarea = titulo,
                descripcion_tarea = descripcion,
                fecha_entrega = fecha,
                estado = estado,
                persona_encargada = persona,
                hora = hora
            )


            if (esNuevo) {
                viewModel.insertarTareaSupabase(nuevoItem) { onActionDone() }
            } else {
                viewModel.actualizarTareaSupabase(nuevoItem) { onActionDone() }

            }
        },
        modifier = Modifier.fillMaxWidth().height(50.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = colorResource(R.color.azul_pastel),
            contentColor = colorResource(R.color.white)
        )

    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.save_svgrepo_com),
                contentDescription = stringResource(R.string.descripcion_btnGuardar_tarea),
                modifier = Modifier.size(24.dp),
                tint = Color.Unspecified

            )
            Text(
                text = stringResource(R.string.btn_Guardar),
                style = MaterialTheme.typography.titleMedium
            )
        }
    }

    if (!esNuevo) {
        Button(
            onClick = {
                tarea.id_tarea?.let { idSeguro ->
                    viewModel.borrarTareaSupabase(idSeguro) {
                        onActionDone()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorResource(R.color.rojo_pastel),
                contentColor = colorResource(R.color.white)
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.trash_svgrepo_com),
                    contentDescription = stringResource(R.string.descripcion_btnEliminar_tarea),
                    modifier = Modifier.size(24.dp),
                    tint=Color.Unspecified
                )
                Text(
                    text = stringResource(R.string.btn_Eliminar),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }

}