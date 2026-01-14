package es.maestre.juntosjc

import android.content.Intent
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.maestre.juntos.viewModel.ComentarioViewModel
import es.maestre.juntos.viewModel.DocumentoViewModel


import es.maestre.juntosjc.ui.theme.JUNTOSJCTheme
import kotlin.getValue

class MainActivity : ComponentActivity() {

    // instancias a los viewModels
    private val comentarioViewModel: ComentarioViewModel by viewModels()
    private val documentosViewModel: DocumentoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Llamada para insertar datos al iniciar en caso de no haber
        comentarioViewModel.insertarComentariosInicio()
        documentosViewModel.insertarDocumentosInicio()

        setContent {
            JUNTOSJCTheme {
                MyAppMain(modifier = Modifier.fillMaxSize())
                }
            }
        }
    }


@Composable
fun MyAppMain(modifier: Modifier = Modifier) {
    GenerarComponentesMain()
}


@Composable
fun GenerarComponentesMain() {

    val context = LocalContext.current

    Card(
        modifier = Modifier.padding(20.dp).fillMaxWidth(),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {

        Column(
            modifier = Modifier.padding(20.dp).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {


            // CABECERA: contiene el titulo de la app y los botones del perfil y de ayuda
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically // centramos el titulo
            ) {

                // TITULO
                Text(
                    text = stringResource(R.string.txt_Juntos),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    style = MaterialTheme.typography.headlineSmall.copy( fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {

                    // PERFIL
                    IconButton(onClick = { /* TODO Perfil */ }) {
                        Icon(
                            painter = painterResource(id = R.drawable.perfil),
                            contentDescription = stringResource(R.string.descripcion_btnPerfil_main),
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // AYUDA
                    IconButton(onClick = { /* TODO Ayuda */ }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ayudar),
                            contentDescription = stringResource(R.string.descripcion_btnPerfil_main),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

            HorizontalDivider() // una linea para dividir los botones, ahora pasamos a los de las funcionalidades

            // BOTÓN DOCUMENTOS
            Button(
                onClick = {
                    val intent = Intent(context, DocumentoActivity::class.java)
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.documento),
                        contentDescription = stringResource(R.string.descripcion_btnDocumentos_main),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = stringResource(R.string.txt_btnDocs),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}
