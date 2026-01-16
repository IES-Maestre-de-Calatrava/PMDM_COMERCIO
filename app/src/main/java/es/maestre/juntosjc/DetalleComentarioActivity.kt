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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
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
import es.maestre.juntos.model.Tarea
import es.maestre.juntos.viewModel.ComentarioViewModel
import es.maestre.juntosjc.model.Comentario
import es.maestre.juntosjc.ui.theme.JUNTOSJCTheme
import kotlin.getValue

class DetalleComentarioActivity: ComponentActivity() {
    private val viewModel: ComentarioViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // recupero el id del comentario que hayamos seleccionado
        val idComentario = intent.getIntExtra("ID_COMENTARIO", -1)
        enableEdgeToEdge()
        setContent {
            JUNTOSJCTheme {
                MyAppDetalle(viewModel = viewModel, idComentario = idComentario) // hay que pasarselo a la funcion para completar los campos
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyAppDetalle(viewModel: ComentarioViewModel, idComentario: Int) {

    val context = LocalContext.current // Para cerrar la pantalla tras la acción

    // Si idComentario > 0, buscamos el comentario real. Si es 0, creamos uno vacío.
    val comentarioActual by if (idComentario > 0) {
        viewModel.getComentarioById(idComentario).observeAsState()
    } else {
        // Objeto temporal vacío para el modo creación
        remember { mutableStateOf(Comentario(idComentario = 0, nombre = "", texto = "")) }
    }

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
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Solo mostramos los campos si el comentario ha cargado
            comentarioActual?.let { comentario ->
                CamposDetalle(comentario = comentario, viewModel = viewModel, esNuevo = idComentario == 0, onActionDone = { (context as ComponentActivity).finish() })            }
        }
    }
}

@Composable
fun CamposDetalle(
    comentario: Comentario,
    viewModel: ComentarioViewModel,
    esNuevo: Boolean,
    onActionDone: () -> Unit
) {
    // Usamos estados para que los campos sean editables
    // Nota: Para editar realmente, luego usaremos estos valores en el botón Guardar
    var nombre by remember { mutableStateOf(comentario.nombre) }
    var texto by remember { mutableStateOf(comentario.texto) }

    Text(text = stringResource(R.string.lb_nombreUsuario), fontWeight = FontWeight.Bold)
    OutlinedTextField(
        value = nombre,
        onValueChange = { nombre = it },
        modifier = Modifier.fillMaxWidth(),
        label = { Text(stringResource(R.string.lbNombre)) }
    )

    Text(text = stringResource(R.string.lb_comentario), fontWeight = FontWeight.Bold)
    OutlinedTextField(
        value = texto,
        onValueChange = { texto = it },
        modifier = Modifier.fillMaxWidth(),
        label = { Text(stringResource(R.string.lbComentario)) },
        minLines = 3
    )

    Button(
        onClick = {
            if (esNuevo) {
                // INSERTAR: se autogenera el ID porque mandamos idComentario = 0
                viewModel.insert(Comentario(nombre = nombre, texto = texto))
            } else {
                // Mantenemos el actualizar normal con el id normal para sobreescribir
                val comentarioEditado = comentario.copy(nombre = nombre, texto = texto)
                viewModel.update(comentarioEditado)
            }
            onActionDone()
        },
        modifier = Modifier.fillMaxWidth().height(50.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = colorResource(R.color.azul_corporativo),
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
                viewModel.delete(comentario)
                onActionDone() // Volvemos a la lista tras eliminar
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorResource(R.color.rojo_material),
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