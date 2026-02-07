package es.maestre.juntosjc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import es.maestre.juntosjc.model.TareaItem
import es.maestre.juntosjc.ui.theme.JUNTOSJCTheme
import es.maestre.juntosjc.viewModel.TareaViewModel
import kotlin.getValue

/**
 * Clase DetalleActivity: esta clase es la que muestra la informacion
 * de cada uno de los items del LazyView de la TareasActivity, pudiendo guardarlos o eliminarlos
 */
class DetalleTareaActivity: ComponentActivity() {

    // instancio mi viewModel para el acceso a BBDD
    private val viewModel: TareaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // recupero los parámetros que hemos pasado
        val idTarea = intent.getIntExtra("ID_TAREA", -1)
        val tituloTarea = intent.getStringExtra("TITULO_TAREA") ?: ""
        val descripcionTarea = intent.getStringExtra("DESCRIPCION_TAREA") ?: ""
        val fechaEntrega = intent.getStringExtra("FECHA_ENTREGA") ?: ""
        val completa = intent.getBooleanExtra("COMPLETA", false)
        val personaEncargada = intent.getStringExtra("PERSONA_ENCARGADA") ?: ""


        enableEdgeToEdge()
        setContent {
            JUNTOSJCTheme {

                val tareaActual = remember(idTarea) {
                    if (idTarea > 0) {
                        // Buscamos en la lista descargada de Supabase
                        viewModel.listaTareasSupabase.find { it.id_tarea == idTarea }
                            ?: TareaItem(idTarea, tituloTarea, descripcionTarea, fechaEntrega, completa, personaEncargada) // Si no la encuentra, usa el backup
                    } else {
                        TareaItem(null, "", "", "", false, "") // Nueva tarea
                    }
                }


                MyAppDetalleTarea(viewModel = viewModel, idTarea = idTarea, tareaRecibida = tareaActual) // hay que pasárselo a la funcion para completar los campos
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
fun MyAppDetalleTarea(viewModel: TareaViewModel, idTarea: Int, tareaRecibida: TareaItem?) {

    val context = LocalContext.current // Para cerrar la pantalla tras la acción

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.txt_detalle),
                        fontWeight = FontWeight.Bold
                    )
                        },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorResource(R.color.container),
                    titleContentColor = colorResource(R.color.content)
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
    var completada by remember { mutableStateOf(tarea.completa) }
    var persona by remember { mutableStateOf(tarea.persona_encargada) }

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

    Text(text = "Fecha de entrega de la tarea:", fontWeight = FontWeight.Bold)
    OutlinedTextField(
        value = fecha,
        onValueChange = { fecha = it },
        modifier = Modifier.fillMaxWidth(),
        label = { Text("Fecha Entrega") },
        minLines = 1
    )

    /* Para el dato booleano, voy a usar un switch */
    Text(text = "Estado de la tarea:", fontWeight = FontWeight.Bold)
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = if (completada) "Tarea completada" else "Tarea en curso")
        Switch(
            checked = completada,
            onCheckedChange = { completada = it }
        )
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
                completa = completada,
                persona_encargada = persona
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
            Spacer(modifier = Modifier.width(10.dp))
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
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = stringResource(R.string.btn_Eliminar),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }

}