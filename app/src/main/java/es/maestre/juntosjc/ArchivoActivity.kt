package es.maestre.juntosjc

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import es.maestre.juntos.model.Documento
import es.maestre.juntos.viewModel.DocumentoViewModel
import es.maestre.juntosjc.ui.theme.JUNTOSJCTheme

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight

class ArchivoActivity : ComponentActivity() {

    private val viewModel: DocumentoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JUNTOSJCTheme {
                MyAppArchivo(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyAppArchivo(viewModel: DocumentoViewModel) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            // Cabecera con TopAppBar
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.txt_descargar),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingSobrante ->
        // El contenido (la lista) se ajusta debajo de la cabecera gracias a paddingSobrante
        Column(modifier = Modifier.padding(paddingSobrante)) {
            ListaArchivosScreen(viewModel = viewModel)
        }
    }
}

@Composable
fun ListaArchivosScreen(viewModel: DocumentoViewModel) {
    // Observamos los datos del LiveData definido en el ViewModel
    val listaDocumentos by viewModel.data.observeAsState(initial = emptyList())
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Al importar foundation.lazy.items, "documento" se reconoce como tipo Documento
        items(listaDocumentos) { documento ->
            ArchivoItem(
                archivo = documento,
                onClick = {
                    try {
                        // Accedemos a rutaArchivo de la clase Documento
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(documento.rutaArchivo))
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(context, "No se pudo abrir el enlace", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }
    }
}

@Composable
fun ArchivoItem(archivo: Documento, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Description,
                contentDescription = "Icono documento",
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = archivo.nombreArchivo, // nombre del archivo
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = stringResource(R.string.texto_ver_descargar_DESCARGAR),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}