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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import es.maestre.juntosjc.viewModel.ComentarioViewModel
import es.maestre.juntosjc.ui.theme.JUNTOSJCTheme
import kotlin.getValue
import es.maestre.juntosjc.model.ComentarioItem
import io.github.jan.supabase.SupabaseClient
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Clase DetalleComentarioActivity: esta clase es la que muestra la informacion
 * de cada uno de los items del LazyView de la RedSocialActivity, pudiendo guardarlos o eliminarlos
 */
class DetalleComentarioActivity: ComponentActivity() {

    // instancio mi viewModel para el acceso a BBDD
    private val viewModel: ComentarioViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // recupero los parámetros que hemos pasado
        val idComentario = intent.getIntExtra("ID_COMENTARIO", -1)
        val nombreBackup = intent.getStringExtra("NOMBRE_USUARIO") ?: ""
        val textoBackup = intent.getStringExtra("TEXTO") ?: ""
        val tituloBackup = intent.getStringExtra("TITULO") ?: ""
        val horaBackUp = intent.getStringExtra("HORA") ?: ""
        val emailBackUp = intent.getStringExtra("EMAIL_USUARIO") ?: ""


        enableEdgeToEdge()
        setContent {
            JUNTOSJCTheme {
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
                                email_usuario = emailBackUp)
                    } else {
                        ComentarioItem(null, "", "", "", hora=horaBackUp, email_usuario = viewModel.getEmailUsuario() ?: "") // Nuevo comentario
                    }
                }

                MyAppDetalle(viewModel = viewModel, idComentario = idComentario, comentarioRecibido = comentarioActual) // hay que pasarselo a la funcion para completar los campos
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
fun MyAppDetalle(viewModel: ComentarioViewModel, idComentario: Int, comentarioRecibido: ComentarioItem?) {

    val context = LocalContext.current // Para cerrar la pantalla tras la acción

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(R.drawable.community_comments_svgrepo_com),
                            contentDescription = null,
                            modifier = Modifier.size(30.dp),
                            tint = Color.Unspecified

                        )
                        Spacer(modifier = Modifier.width(8.dp)) // Espacio entre texto e icono
                        Text(
                            stringResource(R.string.txt_detalle),
                            fontWeight = FontWeight.Bold
                        )
                    }
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
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Solo mostramos los campos si el comentario ha cargado
            comentarioRecibido?.let { comentario ->
                CamposDetalle(comentario = comentario, viewModel = viewModel, esNuevo = idComentario <= 0, onActionDone = { (context as ComponentActivity).finish() })            }
        }
    }
}

/**
 * Funcion que arma la pantalla con los campos editables y los botones de guardado y eliminado
 */
@Composable
fun CamposDetalle(
    comentario: ComentarioItem,
    viewModel: ComentarioViewModel,
    esNuevo: Boolean,
    onActionDone: () -> Unit
) {
    // Usamos estados para que los campos sean editables
    var titulo by remember { mutableStateOf(comentario.titulo) }
    var texto by remember { mutableStateOf(comentario.texto) }
    var hora by remember { mutableStateOf(comentario.hora) }

    val mensajeError = "Modifique su nombre de usuario en el apartado perfil"
    var nombreUsuarioPerfil by remember { mutableStateOf<String>(mensajeError) }
    var iconoUsuarioActual by remember { mutableStateOf<String?>(null) }

    // recuperamos el email actual
    val emailSesion = remember { viewModel.getEmailUsuario() }


    // Si el email es nuevo, el dueño es el actual, si no los comparamos
    val esDueño = if (esNuevo) true else (emailSesion == comentario.email_usuario)

    LaunchedEffect(Unit) {
        val iconoRecuperado = viewModel.obtenerIconoDesdePerfiles()
        val nombreRecuperado = viewModel.obtenernombreDesdePerfiles()
        // Asignamos a las variables de estado para que la pantalla se actualice
        iconoUsuarioActual = iconoRecuperado
        if (!nombreRecuperado.isNullOrBlank()) {
            nombreUsuarioPerfil = nombreRecuperado
        }
    }


    val esError = nombreUsuarioPerfil == mensajeError

    // Campos editables segun los atributos del comentario
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
            color = if (esError && esNuevo) colorResource(R.color.rojo) else colorResource(R.color.black)        )
    }

    Text(text = stringResource(R.string.lbTitulo), fontWeight = FontWeight.Bold)
    OutlinedTextField(
        value = titulo,
        onValueChange = { if (esDueño) titulo = it },
        readOnly = !esDueño,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(stringResource(R.string.lbTitulo)) }
    )

    Text(text = stringResource(R.string.lb_comentario), fontWeight = FontWeight.Bold)
    OutlinedTextField(
        value = texto,
        onValueChange = { if (esDueño) texto = it },
        readOnly = !esDueño,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(stringResource(R.string.lbComentario)) },
        minLines = 3
    )

    if (esDueño) {
        Button(
            onClick = {
                val horaNueva = horaActualComentario()

                val nuevoItem = ComentarioItem(
                    id_comentario = if (esNuevo) null else comentario.id_comentario,
                    nombre_usuario = nombreUsuarioPerfil,
                    texto = texto,
                    titulo = titulo,
                    icono_usuario = iconoUsuarioActual ?: comentario.icono_usuario,
                    hora = horaNueva,
                    email_usuario = emailSesion
                )
                if (esNuevo) {
                    viewModel.insertarComentarioSupabase(nuevoItem) { onActionDone() }
                } else {
                    viewModel.actualizarComentarioSupabase(nuevoItem) { onActionDone() }
                }
            },
            enabled = !(esNuevo && esError),
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
                    contentDescription = stringResource(R.string.descripcion_btnGuardar_detalle),
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
                    comentario.id_comentario?.let { idSeguro ->
                        viewModel.borrarComentarioSupabase(idSeguro) {
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
                        contentDescription = stringResource(R.string.descripcion_btnEliminar_detalle),
                        modifier = Modifier.size(24.dp),
                        tint = Color.Unspecified
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

}


fun horaActualComentario(): String {
    val ahora = LocalTime.now()
    val formatter = DateTimeFormatter.ofPattern("HH:mm")
    return ahora.format(formatter)
}



