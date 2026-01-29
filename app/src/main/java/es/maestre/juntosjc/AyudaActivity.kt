package es.maestre.juntosjc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import es.maestre.juntosjc.ui.theme.JUNTOSJCTheme

class AyudaActivity : ComponentActivity() {
    val modifier: Modifier = Modifier.fillMaxWidth()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JUNTOSJCTheme {
                AyudaPrincipal()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AyudaPrincipal(){
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.txt_ayuda),
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorResource(R.color.container),
                    titleContentColor = colorResource(R.color.content)
                )
            )
        }
    ){
        Column(
            modifier = Modifier.padding(it)
        ) {
            AyudaCalendario()
            AyudaTareas()
            AyudaDocumentos()
            AyudaRedSocial()
        }

    }
}

@Preview
@Composable
fun AyudaPrincipalPreview(){
    AyudaPrincipal()
}

@Composable
fun AyudaCalendario(){
    BotonAyuda(stringResource(R.string.txt_btn_calendario)){
        BotonTexto(
            stringResource(R.string.txt_eventos),
            stringResource(R.string.txt_eventos_content))
        BotonTexto(
            stringResource(R.string.txt_add),
            stringResource(R.string.txt_add_evento_content))
        BotonTexto(
            stringResource(R.string.txt_eliminar),
            stringResource(R.string.txt_eliminar_evento))
    }
}

@Composable
fun AyudaTareas(){
    BotonAyuda(stringResource(R.string.txt_btn_tareas)){
        BotonTexto(
            stringResource(R.string.txt_add),
            stringResource(R.string.txt_add_tarea_content))
        BotonTexto(
            stringResource(R.string.txt_editar),
            stringResource(R.string.txt_editar_tarea_content))
        BotonTexto(
            stringResource(R.string.txt_eliminar),
            stringResource(R.string.txt_eliminar_tarea_content))
    }
}

@Composable
fun AyudaDocumentos() {
    BotonAyuda(stringResource(R.string.txt_btn_documentos)) {
        BotonTexto(
            stringResource(R.string.txt_subir),
            stringResource(R.string.txt_subir_documento)
        )
        BotonTexto(
            stringResource(R.string.txt_descargar),
            stringResource(R.string.txt_descargar_documento)
        )
    }
}

@Composable
fun AyudaRedSocial() {
    BotonAyuda(stringResource(R.string.txt_btn_red_social)) {
        BotonTexto(
            stringResource(R.string.txt_crear),
            stringResource(R.string.txt_crear_comentario)
        )
        BotonTexto(
            stringResource(R.string.txt_editar),
            stringResource(R.string.txt_editar_comentario)
        )
        BotonTexto(
            stringResource(R.string.txt_borrar),
            stringResource(R.string.btn_Crear)
        )
    }
}

/**
 *
 */
@Composable
fun BotonAyuda(contenido: String, function: @Composable () -> Unit) {
    var expandido by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = androidx.compose.ui.Alignment.Start 
    ) {
        androidx.compose.material3.ElevatedButton(
            onClick = { expandido = !expandido },
            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
            colors = androidx.compose.material3.ButtonDefaults.elevatedButtonColors(
                containerColor = colorResource(R.color.container),
                contentColor = colorResource(R.color.content)
            )
        ) {
            Text(
                text = if (expandido) stringResource(R.string.txt_ocultar_detalles) else contenido,
                style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        AnimatedVisibility(
            visible = expandido,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .padding(start = 16.dp, top = 4.dp)
                    .fillMaxWidth(),
                horizontalAlignment = androidx.compose.ui.Alignment.Start
            ) {
                function()
            }
        }
    }
}

@Composable
fun BotonTexto(contenido: String, texto: String) {
    var expandido by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = androidx.compose.ui.Alignment.Start
    ) {
        androidx.compose.material3.OutlinedButton(
            onClick = { expandido = !expandido },
            border = androidx.compose.foundation.BorderStroke(1.dp, colorResource(R.color.container))
        ) {
            Text(
                text = if (expandido) stringResource(R.string.txt_ocultar_detalles) else contenido,
                style = androidx.compose.material3.MaterialTheme.typography.bodyLarge
            )
        }

        AnimatedVisibility(
            visible = expandido,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            androidx.compose.material3.Card(
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .fillMaxWidth(0.9f),
                colors = androidx.compose.material3.CardDefaults.cardColors(
                    containerColor = colorResource(R.color.container).copy(alpha = 0.1f)
                )
            ) {
                Text(
                    text = texto,
                    modifier = Modifier.padding(12.dp),
                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
