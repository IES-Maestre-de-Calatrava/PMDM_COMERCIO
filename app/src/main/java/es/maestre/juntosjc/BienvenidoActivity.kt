package es.maestre.juntosjc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import es.maestre.juntosjc.ui.theme.JUNTOSJCTheme

// funciones de la clase BienvenidaApp
import es.maestre.juntosjc.componentes.GenerarComponentesBienvenida

class BienvenidoActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JUNTOSJCTheme {
                MyAppBienvenida(modifier = Modifier.fillMaxSize())
            }
        }
    }
}


@Composable
fun MyAppBienvenida(modifier: Modifier = Modifier,
) {
    GenerarComponentesBienvenida()
}