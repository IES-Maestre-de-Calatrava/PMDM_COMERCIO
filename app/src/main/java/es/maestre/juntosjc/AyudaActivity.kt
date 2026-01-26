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

@Composable
fun AyudaCalendario(){
    BotonAyuda(onClick = {}, stringResource(R.string.txt_btn_calendario)){
        BotonTexto(
            onClick = {},
            stringResource(R.string.txt_eventos),
            stringResource(R.string.txt_eventos_content))
        BotonTexto(
            onClick = {},
            stringResource(R.string.txt_add),
            stringResource(R.string.txt_add_evento_content))
        BotonTexto(
            onClick = {},
            stringResource(R.string.txt_eliminar),
            stringResource(R.string.txt_eliminar_evento))
    }
}

@Composable
fun AyudaTareas(){
    BotonAyuda(onClick = {}, stringResource(R.string.txt_btn_tareas)){
        BotonTexto(
            onClick = {},
            stringResource(R.string.txt_add),
            stringResource(R.string.txt_add_tarea_content))
        BotonTexto(
            onClick = {},
            stringResource(R.string.txt_editar),
            stringResource(R.string.txt_editar_tarea_content))
        BotonTexto(
            onClick = {},
            stringResource(R.string.txt_eliminar),
            stringResource(R.string.txt_eliminar_tarea_content))
    }
}

@Composable
fun AyudaDocumentos(){

}

@Composable
fun AyudaRedSocial(){

}

@Composable
fun BotonAyuda(onClick: () -> Unit, contenido: String, function: @Composable () -> Unit) {
    var expandido by remember { mutableStateOf(false) }
    Column(modifier = Modifier.padding()) {
        Button(onClick = { expandido = !expandido }) {
            Text(if (expandido) stringResource(R.string.txt_ocultar_detalles) else contenido)
        }
    }

    AnimatedVisibility(
        visible = expandido,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
        content = {
            Column {
                function()
            }
        }
    )
}

@Composable
fun BotonTexto(onClick: () -> Unit, contenido: String, texto: String) {
    var expandido by remember { mutableStateOf(false) }
    Column(modifier = Modifier.padding()) {
        Button(onClick = { expandido = !expandido }) {
            Text(if (expandido) stringResource(R.string.txt_ocultar_detalles) else contenido)
        }

        AnimatedVisibility(
            visible = expandido,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut(),
            content = {
                Text(texto)
            }
        )
    }
}