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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import es.maestre.juntosjc.viewModel.ComentarioViewModel
import kotlin.getValue
import es.maestre.juntosjc.model.ComentarioItem
import io.github.jan.supabase.SupabaseClient
import java.time.LocalTime
import java.time.format.DateTimeFormatter

import es.maestre.juntosjc.viewModel.UserPreferencesViewModel
import es.maestre.juntosjc.ui.theme.JUNTOSJCTheme
import es.maestre.juntosjc.ui.theme.JuntosTheme
import es.maestre.juntosjc.model.AppFeature

/**
 * Clase DetalleComentarioActivity: esta clase es la que muestra la informacion
 * de cada uno de los items del LazyView de la RedSocialActivity, pudiendo guardarlos o eliminarlos
 */
class DetalleComentarioActivity: ComponentActivity() {

    // instancio mi viewModel para el acceso a BBDD
    private val viewModel: ComentarioViewModel by viewModels()

    private val preferencesViewModel: UserPreferencesViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // recupero los parámetros que hemos pasado
        val idComentario = intent.getIntExtra("ID_COMENTARIO", -1)
        val nombreBackup = intent.getStringExtra("NOMBRE_USUARIO") ?: ""
        val textoBackup = intent.getStringExtra("TEXTO") ?: ""
        val tituloBackup = intent.getStringExtra("TITULO") ?: ""
        val horaBackUp = intent.getStringExtra("HORA") ?: ""
        val emailBackUp = intent.getStringExtra("EMAIL_USUARIO") ?: ""
        val fechaBackUp = intent.getStringExtra("FECHA_PUBLICACION") ?: ""


        enableEdgeToEdge()
        setContent {
            val isDarkTheme by preferencesViewModel.isDarkTheme.collectAsStateWithLifecycle()

            JUNTOSJCTheme (darkTheme = isDarkTheme) {
                val comentarioActual = remember(idComentario) {
                    if (idComentario > 0) {
                        // Buscamos en la lista descargada de Supabase
                        viewModel.listaComentariosSupabase.find { it.id_comentario == idComentario }
                            ?: ComentarioItem(
                                idComentario,
                                nombreBackup,
                                textoBackup,
                                tituloBackup,
                                hora=horaBackUp,
                                email_usuario = emailBackUp,
                                fecha_publicacion = fechaBackUp)
                    } else {
                        ComentarioItem(null, "", "", "", hora=horaBackUp, email_usuario = viewModel.getEmailUsuario() ?: "", fecha_publicacion = "") // Nuevo comentario
                    }
                }

                MyAppDetalle(viewModel = viewModel, idComentario = idComentario, comentarioRecibido = comentarioActual,  preferencesViewModel = preferencesViewModel) // hay que pasarselo a la funcion para completar los campos
            }
        }
    }
}

/**
 * Funcion que me arma la cabecera, me comprueba el id del comentario
 * para saber si vamos a crear uno nuevo o a modificar otro y me llama a
 * los campos de edicion del comentario
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyAppDetalle(viewModel: ComentarioViewModel, idComentario: Int, comentarioRecibido: ComentarioItem?,  preferencesViewModel: UserPreferencesViewModel) {

    val context = LocalContext.current // Para cerrar la pantalla tras la acción
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
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
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(R.drawable.community_comments_svgrepo_com),
                    contentDescription = null,
                    modifier = Modifier.size(30.dp),
                    tint = Color.Unspecified

                )
                Spacer(modifier = Modifier.width(8.dp)) // Espacio entre texto e icono
                val tituloPantalla = if (idComentario <= 0){
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

            // Solo mostramos los campos si el comentario ha cargado
            comentarioRecibido?.let { comentario ->
                CamposDetalle(comentario = comentario, viewModel = viewModel, esNuevo = idComentario <= 0, onActionDone = { (context as ComponentActivity).finish() })            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

/**
 * Funcion que arma la pantalla con los campos editables y los botones de guardado y eliminado
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CamposDetalle(
    comentario: ComentarioItem,
    viewModel: ComentarioViewModel,
    esNuevo: Boolean,
    onActionDone: () -> Unit
) {
    var titulo by remember { mutableStateOf(comentario.titulo) }
    var texto by remember { mutableStateOf(comentario.texto) }

    val mensajeError = "Modifique su nombre de usuario en el apartado perfil"
    var nombreUsuarioPerfil by remember { mutableStateOf<String>(mensajeError) }
    var iconoUsuarioActual by remember { mutableStateOf<String?>(null) }

    // Si es nuevo y la fecha está vacía, podemos poner la de hoy por defecto para evitar nulos en BD
    var fecha by remember { mutableStateOf(if (comentario.fecha_publicacion.isNullOrBlank()) "" else comentario.fecha_publicacion) }
    var hora by remember { mutableStateOf(if (comentario.hora.isNullOrBlank()) "" else comentario.hora) }

    val emailSesion = remember { viewModel.getEmailUsuario() }
    var showCalendar by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val esDueño = if (esNuevo) true else (emailSesion == comentario.email_usuario)

    LaunchedEffect(Unit) {
        val iconoRecuperado = viewModel.obtenerIconoDesdePerfiles()
        val nombreRecuperado = viewModel.obtenernombreDesdePerfiles()
        iconoUsuarioActual = iconoRecuperado
        if (!nombreRecuperado.isNullOrBlank()) {
            nombreUsuarioPerfil = nombreRecuperado
        }
    }

    val esError = nombreUsuarioPerfil == mensajeError

    // NOMBRE USUARIO
    Text(text = stringResource(R.string.lb_nombreUsuario), fontWeight = FontWeight.Bold)
    Surface(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        color = Color.LightGray.copy(alpha = 0.2f),
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = if (esNuevo) nombreUsuarioPerfil else comentario.nombre_usuario,
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyLarge,
            color = if (esError && esNuevo) colorResource(R.color.rojo) else colorResource(R.color.black)
        )
    }

    // TITULO
    Text(text = stringResource(R.string.lbTitulo), fontWeight = FontWeight.Bold)
    OutlinedTextField(
        value = titulo,
        onValueChange = { if (esDueño) titulo = it },
        readOnly = !esDueño,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(stringResource(R.string.lbTitulo)) },
        enabled = esDueño // Bloquea visualmente si no es dueño
    )

    // COMENTARIO
    Text(text = stringResource(R.string.lb_comentario), fontWeight = FontWeight.Bold)
    OutlinedTextField(
        value = texto,
        onValueChange = { if (esDueño) texto = it },
        readOnly = !esDueño,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(stringResource(R.string.lbComentario)) },
        minLines = 3,
        enabled = esDueño
    )

    // FECHA
    Text(text = "Fecha de publicación:", fontWeight = FontWeight.Bold)
    OutlinedTextField(
        value = fecha,
        onValueChange = { },
        readOnly = true,

        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = esDueño) { showCalendar = true },
        label = { Text("Seleccionar Día") },
        trailingIcon = {
            IconButton(onClick = { showCalendar = true }, enabled = esDueño) {
                Icon(
                    painter = painterResource(id = R.drawable.calendar_svgrepo_com),
                    contentDescription = null,
                    tint = if (esDueño) Color.Unspecified else Color.Gray
                )
            }
        },
        enabled = esDueño,
        colors = OutlinedTextFieldDefaults.colors(
            disabledTextColor = MaterialTheme.colorScheme.onSurface,
            disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )

    // HORA
    Text(text = "Hora:", fontWeight = FontWeight.Bold)
    OutlinedTextField(
        value = hora,
        onValueChange = { },
        readOnly = true,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = esDueño) { showTimePicker = true },
        label = { Text("Seleccionar Hora") },
        trailingIcon = {
            IconButton(onClick = { showTimePicker = true }, enabled = esDueño) {
                Icon(
                    painter = painterResource(id = R.drawable.clock_svgrepo_com),
                    contentDescription = null,
                    tint = if (esDueño) Color.Unspecified else Color.Gray
                )
            }
        },
        enabled = esDueño,
        colors = OutlinedTextFieldDefaults.colors(
            disabledTextColor = MaterialTheme.colorScheme.onSurface,
            disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )

    if (showCalendar && esDueño) {
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

    if (showTimePicker && esDueño) {
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


    if (esDueño) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // BOTÓN GUARDAR
            Button(
                onClick = {
                    val nuevoItem = ComentarioItem(
                        id_comentario = if (esNuevo) null else comentario.id_comentario,
                        nombre_usuario = nombreUsuarioPerfil,
                        texto = texto,
                        titulo = titulo,
                        icono_usuario = iconoUsuarioActual ?: comentario.icono_usuario,
                        hora = hora,
                        email_usuario = emailSesion,
                        fecha_publicacion = fecha
                    )
                    if (esNuevo) {
                        viewModel.insertarComentarioSupabase(nuevoItem) { onActionDone() }
                    } else {
                        viewModel.actualizarComentarioSupabase(nuevoItem) { onActionDone() }
                    }
                },
                enabled = !(esNuevo && esError),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(R.color.azul_pastel),
                    contentColor = colorResource(R.color.white)
                )
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.save_svgrepo_com),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = Color.Unspecified
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = stringResource(R.string.btn_Guardar),
                        style = MaterialTheme.typography.titleSmall)
                }
            }

            // BOTÓN ELIMINAR
            if (!esNuevo) {
                Button(
                    onClick = {
                        comentario.id_comentario?.let { idSeguro ->
                            viewModel.borrarComentarioSupabase(idSeguro) { onActionDone() }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(R.color.rojo_pastel),
                        contentColor = colorResource(R.color.white)
                    )
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.trash_svgrepo_com),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = Color.Unspecified
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = stringResource(R.string.btn_Eliminar),
                            style = MaterialTheme.typography.titleSmall)
                    }
                }
            }
        }
    }

}


