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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(R.drawable.help_question_svgrepo_com),
                            contentDescription = null,
                            modifier = Modifier.size(30.dp),
                            tint = Color.Unspecified

                        )
                        Spacer(modifier = Modifier.width(8.dp)) // Espacio entre texto e icono
                        Text(
                            stringResource(R.string.txt_ayuda),
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
    val icono = R.drawable.calendar_svgrepo_com

    BotonAyuda(icono,stringResource(R.string.txt_btn_calendario),stringResource(R.string.txt_btn_ocultar_calendario),stringResource(R.string.txt_descripcion_calendario)){
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
    val icono = R.drawable.information_svgrepo_com

    BotonAyuda(icono, stringResource(R.string.txt_btn_tareas), stringResource(R.string.txt_btn_ocultar_tareas),stringResource(R.string.txt_descripcion_tareas)){
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
    val icono = R.drawable.favorite_file_svgrepo_com

    BotonAyuda(icono, stringResource(R.string.txt_btn_documentos), stringResource(R.string.txt_btn_ocultar_documentos),stringResource(R.string.txt_descripcion_documentos)){
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
    val icono = R.drawable.community_comments_svgrepo_com

    BotonAyuda(icono, stringResource(R.string.txt_btn_red_social), stringResource(R.string.txt_btn_ocultar_redsocial),stringResource(R.string.txt_descripcion_redsocial)){
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
    val icono = R.drawable.invite_friends_svgrepo_com
    BotonAyuda(icono , stringResource(R.string.txt_btn_invitar), stringResource(R.string.txt_btn_ocultar_inivitar),stringResource(R.string.txt_contenido_invitar)){
    }
}

@Composable
fun AyudaContactos(){
    val icono = R.drawable.recruitment_svgrepo_com
    BotonAyuda(icono, stringResource(R.string.txt_btn_contactos), stringResource(R.string.txt_btn_ocultar_contactos), stringResource(R.string.txt_descripcion_contactos)){
        BotonTexto(
            stringResource(R.string.txt_crear),
            stringResource(R.string.txt_crear_contacto)
        )
        BotonTexto(
            stringResource(R.string.txt_editar),
            stringResource(R.string.txt_editar_contacto)
        )
        BotonTexto(
            stringResource(R.string.txt_borrar),
            stringResource(R.string.txt_eliminar_contacto)
        )
    }
}

@Composable
fun AyudaFotos(){

    val icono = R.drawable.photo_album_svgrepo_com
    BotonAyuda(icono, stringResource(R.string.txt_btn_fotos), stringResource(R.string.txt_btn_ocultar_fotos),stringResource(R.string.txt_descripcion_fotos)){
        BotonTexto(stringResource(R.string.txt_foto),stringResource(R.string.txt_hacer_fotos) )
        BotonTexto(stringResource(R.string.txt_galeria),stringResource(R.string.txt_entrar_galeria) )
    }
}

@Composable
fun AyudaConfigurar(){
    val icono = R.drawable.help_question_svgrepo_com

    BotonAyuda(icono, stringResource(R.string.txt_btn_configurar), stringResource(R.string.txt_btn_ocultar_configurar),stringResource(R.string.txt_descripcion_configuracion) ){
        BotonTexto(
            stringResource(R.string.txt_apariencia),
            stringResource(R.string.txt_configurar_tema)
        )
        BotonTexto(
            stringResource(R.string.txt_caracteristicas),
            stringResource(R.string.txt_actydesact_carecteristicas)
        )
    }
}

@Composable
fun AyudaPerfil(){
    val icono = R.drawable.perfil

    BotonAyuda(icono, stringResource(R.string.txt_btn_perfil), stringResource(R.string.txt_btn_ocultar_perfil), stringResource(R.string.txt_descripcion_perfil)){
        BotonTexto(stringResource(R.string.txt_añadir_informacion), stringResource(R.string.txt_añadir_informacion_perfil))
    }
}

/**
 * Botón con un título que muestra otros botones, aunque puede ser usado para mostrar cualquier
 * componente o conjunto de componentes que sean una función Composable.
 * @param contenido El texto que va a contener el botón
 * @param function Componentes que mostrará el botón cuando sea pulsado
 */
@Composable
fun BotonAyuda(icono: Int, contenido: String, textoOcultar: String,descripcion: String? = null, function: @Composable () -> Unit) {
    var expandido by remember { mutableStateOf(false) }

    Column(
        modifier = ColumnaPrincipalModifier,
        horizontalAlignment = Alignment.Start
    ) {

        androidx.compose.material3.ElevatedButton(
            onClick = { expandido = !expandido },

            modifier = Modifier.fillMaxWidth(),
            shape = BotonRedondearAyuda,
            colors = botonPrincipalColores()
        ) {
            Icon(
                painter = painterResource(icono),
                contentDescription = null,
                modifier = Modifier.size(30.dp),
                tint = Color.Unspecified

            )
            Text(
                text = if (expandido) textoOcultar else contenido,
                style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 25.dp)
            )
        }

        AnimatedVisibility(
            visible = expandido,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                modifier = ColumnaDetalleModifier,
                horizontalAlignment = Alignment.Start
            ) {
                if (descripcion != null) {
                    Text(
                        text = descripcion,
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                        style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
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
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalAlignment = androidx.compose.ui.Alignment.Start
    ) {
        androidx.compose.material3.OutlinedButton(
            onClick = { expandido = !expandido },
            modifier = Modifier.fillMaxWidth(),
            border = botonSecundarioColores()
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
                    .fillMaxWidth(),
                colors = cardTextoAyudaColores()

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


val ColumnaPrincipalModifier = Modifier
    .fillMaxWidth()
    .padding(horizontal = 16.dp, vertical = 8.dp)

val ColumnaDetalleModifier = Modifier
    .padding(start = 16.dp, top = 4.dp)
    .fillMaxWidth()


val BotonRedondearAyuda = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)

@Composable
fun botonPrincipalColores() = androidx.compose.material3.ButtonDefaults.elevatedButtonColors(
    containerColor = colorResource(R.color.container),
    contentColor = colorResource(R.color.content)
)
@Composable
fun botonSecundarioColores() = androidx.compose.foundation.BorderStroke(
    width = 1.dp,
    color = colorResource(R.color.container)
)
@Composable
fun cardTextoAyudaColores() = androidx.compose.material3.CardDefaults.cardColors(
    containerColor = colorResource(R.color.container).copy(alpha = 0.1f)
)
