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
import androidx.compose.foundation.lazy.LazyColumn
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
import es.maestre.juntosjc.model.Ayuda
import es.maestre.juntosjc.ui.theme.JUNTOSJCTheme

class AyudaActivity : ComponentActivity() {
    val modifier: Modifier = Modifier.fillMaxWidth()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        //para filtrar, usaremos una variables seccion, recuperamos la seccion desde intent, por defecto es la general
        //Esta implementación de getSerializableExtra está deprecada a partir de API 33,
        //el proyecto apunta a una versión anterior, así que se usa la implementación anterior
        val seccion = intent.getSerializableExtra("SECCION") ?: Ayuda.GENERAL

        setContent {
            JUNTOSJCTheme {
                AyudaPrincipal(seccion as Ayuda)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AyudaPrincipal(seccion: Ayuda){
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
        LazyColumn(
            modifier = Modifier.padding(it)
        ) {
            item {
                // Usamos un when para llamar a las funciones dependiendo de la seccion
                when (seccion) {
                    Ayuda.CALENDARIO -> AyudaCalendario()
                    Ayuda.CONFIGURACION -> AyudaConfigurar()
                    Ayuda.CONTACTOS -> AyudaContactos()
                    Ayuda.DOCUMENTOS -> AyudaDocumentos()
                    Ayuda.FOTOS -> AyudaFotos()
                    Ayuda.INVITAR -> AyudaInvitar()
                    Ayuda.PERFIL -> AyudaPerfil()
                    Ayuda.SOCIAL -> AyudaRedSocial()
                    Ayuda.TAREAS -> AyudaTareas()
                    // si llamamos desde la main, se muestran todos
                    else -> {
                        AyudaCalendario()
                        AyudaConfigurar()
                        AyudaContactos()
                        AyudaDocumentos()
                        AyudaFotos()
                        AyudaInvitar()
                        AyudaPerfil()
                        AyudaRedSocial()
                        AyudaTareas()
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun AyudaPrincipalPreview(){
    AyudaPrincipal(Ayuda.GENERAL)
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
            stringResource(R.string.txt_eliminar_comentario)
        )
    }
}

//TODO Establecer textos correctamente
@Composable
fun AyudaInvitar(){
    BotonAyuda("Invitar") {
        BotonTexto("Invitar", "Invitar")
    }
}

@Composable
fun AyudaContactos(){
    BotonAyuda("Contactos") {
        BotonTexto("Contactos", "Contactos")
    }
}

@Composable
fun AyudaFotos(){
    BotonAyuda("Fotos") {
        BotonTexto("Fotos", "Fotos")
    }
}

@Composable
fun AyudaConfigurar(){
    BotonAyuda("Configuración") {
        BotonTexto("Configuración", "Configuración")
    }
}

@Composable
fun AyudaPerfil(){
    BotonAyuda("Perfil"){
        BotonTexto("Perfil", "Perfil")
    }
}

/**
 * Botón con un título que muestra otros botones, aunque puede ser usado para mostrar cualquier
 * componente o conjunto de componentes que sean una función Composable.
 * @param contenido El texto que va a contener el botón
 * @param function Componentes que mostrará el botón cuando sea pulsado
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

/**
 * Botón que muestra un texto al ser pulsado, esta función está pensada para ser pasada como
 * parámetro a un BotonAyuda
 * @param contenido El texto que se mostrará en el botón
 * @param texto El texto que se mostrará después de pulsar el botón
 */
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
