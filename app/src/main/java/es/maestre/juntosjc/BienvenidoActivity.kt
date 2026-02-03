package es.maestre.juntosjc

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.maestre.juntosjc.ui.theme.JUNTOSJCTheme

/**
 * Clase de Bienvenida al usuario, sirve de enlace a la MainActivity
 */
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


/**
 * Llama a la funcion de generar componentes
 */
@Composable
fun MyAppBienvenida(modifier: Modifier = Modifier,
) {
    GenerarComponentesBienvenida()
}


/**
 * Esta funcion dibuja el layout de mi ventana
 */
@Composable
fun GenerarComponentesBienvenida() {
    val context = LocalContext.current
    // Scroll para pantallas grandes
    val scrollState = rememberScrollState()

    Card(modifier = Modifier.padding(30.dp)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                // Scroll vertical
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Imagen principal
            Image(
                painter = painterResource(R.drawable.imagen_unidadtrabajo),
                contentDescription = stringResource(R.string.descripcion_imagenWorkUnit),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                contentScale = ContentScale.Crop
            )

            // Espaciado
            Spacer(modifier = Modifier.height(100.dp))

            // Imagen con el logo
            Image(
                painter = painterResource(R.drawable.juntos),
                contentDescription = stringResource(R.string.descripcion_logo),
                modifier = Modifier.size(150.dp),
                contentScale = ContentScale.Crop
            )

            // Espaciado
            Spacer(modifier = Modifier.height(100.dp))

            Text(
                text = stringResource(R.string.txt_pantallaBienvenida),
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                style = MaterialTheme.typography.titleLarge
            )

            Button(
                onClick = {
                    // intent a la LoginActivity
                    val intent = Intent(context, LoginActivity::class.java)
                    context.startActivity(intent)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(30.dp)
                    .height(55.dp)
            ) {
                Text(text = stringResource(R.string.txt_btn_pantallaBienvenida), fontSize = 18.sp)
            }

            // Esto es un pequeño espacio al final para que al hacer scroll
            // el botón no justo borde del card
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
