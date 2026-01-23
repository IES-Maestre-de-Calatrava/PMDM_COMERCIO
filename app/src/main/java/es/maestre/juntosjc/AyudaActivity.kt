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
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import es.maestre.juntosjc.ui.theme.JUNTOSJCTheme

class AyudaActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JUNTOSJCTheme {

            }
        }
    }
}

@Composable
fun AyudaPrincipal(navController: NavController){

}

@Composable
fun AyudaCalendario(navController: NavController){

}

@Composable
fun AyudaTareas(navController: NavController){

}

@Composable
fun AyudaDocumentos(navController: NavController){

}

@Composable
fun BotonDesplegableAnimado() {
    // Estado para controlar si el texto se muestra o no
    var expandido by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(16.dp)) {
        Button(onClick = { expandido = !expandido }) {
            Text(if (expandido) "Ocultar detalles" else "Mostrar detalles")
        }

        // Esta función maneja la animación de entrada y salida
        AnimatedVisibility(
            visible = expandido,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Text(
                text = "¡Este es el texto que se despliega!",
                modifier = Modifier.padding(top = 8.dp),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}