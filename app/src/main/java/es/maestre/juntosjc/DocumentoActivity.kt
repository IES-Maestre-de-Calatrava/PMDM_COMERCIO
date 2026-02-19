package es.maestre.juntosjc

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import es.maestre.juntosjc.model.Ayuda
import es.maestre.juntosjc.viewModel.DocumentoViewModel
import androidx.compose.runtime.*
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle

import es.maestre.juntosjc.viewModel.UserPreferencesViewModel
import es.maestre.juntosjc.ui.theme.JUNTOSJCTheme
import es.maestre.juntosjc.ui.theme.JuntosTheme
import es.maestre.juntosjc.model.AppFeature

class DocumentoActivity : ComponentActivity() {

    private val viewModel: DocumentoViewModel by viewModels()

    private val preferencesViewModel: UserPreferencesViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Cargamos los archivos al iniciar
        viewModel.obtenerArchivosSupabase()

        var tempByteArray: ByteArray? = null
        var tempExtension: String? = null

        val pickImageLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            uri?.let {
                val mimeType = contentResolver.getType(it)
                val inputStream = contentResolver.openInputStream(it)
                tempByteArray = inputStream?.readBytes()
                tempExtension = mimeType?.substringAfterLast('/', "jpg") ?: "jpg"

                if (tempByteArray != null) {
                    // Mostramos el diálogo cambiando un estado en el ViewModel o un estado local
                    viewModel.mostrarDialogoNombre = true
                }
            }
        }

        setContent {
            val isDarkTheme by preferencesViewModel.isDarkTheme.collectAsStateWithLifecycle()

            JUNTOSJCTheme (darkTheme = isDarkTheme){
                // Diálogo para pedir el nombre
                if (viewModel.mostrarDialogoNombre) {
                    DialogoNombreArchivo(
                        onConfirm = { nombreEscrito ->
                            viewModel.mostrarDialogoNombre = false
                            val nombreFinal = "$nombreEscrito.${tempExtension}"

                            // Llamamos a la subida con el nuevo nombre
                            viewModel.subirImagen(
                                byteArray = tempByteArray!!,
                                extension = tempExtension!!,
                                nombrePersonalizado = nombreFinal, // Asegúrate de que tu VM acepte este parámetro
                                onSuccess = { publicUrl ->
                                    // ESTO ES LO IMPORTANTE: Volver al hilo principal para el Toast
                                    runOnUiThread {
                                        Toast.makeText(this, "Subido correctamente", Toast.LENGTH_SHORT).show()
                                    }
                                    viewModel.obtenerArchivosSupabase()
                                },
                                onError = { e ->
                                    runOnUiThread {
                                        Toast.makeText(this, "Error al subir: ${e.message}", Toast.LENGTH_LONG).show()
                                    }
                                }
                            )
                        },
                        onDismiss = { viewModel.mostrarDialogoNombre = false }
                    )
                }

                MyAppDocumentos(
                    viewModel = viewModel,
                    preferencesViewModel = preferencesViewModel,
                            onPickDocument = {
                        pickImageLauncher.launch(arrayOf("image/*", "application/pdf", "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
                    }
                )
            }
        }
    }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyAppDocumentos(viewModel: DocumentoViewModel, preferencesViewModel: UserPreferencesViewModel, onPickDocument: () -> Unit) {
    val context = LocalContext.current

    var textoBusqueda by remember { mutableStateOf("")}

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.icono_tiendacampa_a),
                            contentDescription = null,
                            modifier = Modifier.size(30.dp),
                            tint = Color.Unspecified

                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.txt_Juntos),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = JuntosTheme.colors.azulOscuroLogo
                            )
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = JuntosTheme.colors.container,
                    titleContentColor = JuntosTheme.colors.content
                ),
                actions = {
                    IconButton(onClick = {
                        val intent = Intent(context, AyudaActivity::class.java)
                        intent.putExtra("SECCION", Ayuda.DOCUMENTOS)
                        context.startActivity(intent)
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.help_question_svgrepo_com),
                            contentDescription = "Ayuda",
                            tint = Color.Unspecified
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onPickDocument() },
                containerColor = colorResource(R.color.white),
                contentColor = Color.Unspecified
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.add_to_svgrepo_com),
                    contentDescription = stringResource(R.string.btn_Crear)
                )
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize()
            .padding(16.dp)
        ) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(R.drawable.favorite_file_svgrepo_com),
                    contentDescription = null,
                    modifier = Modifier.size(30.dp),
                    tint = Color.Unspecified

                )
                Spacer(modifier = Modifier.width(8.dp)) // Espacio entre texto e icono
                Text(
                    stringResource(R.string.txt_documentos),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = JuntosTheme.colors.azulOscuroLogo
                    )
                )
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            OutlinedTextField(
                value = textoBusqueda,
                onValueChange = { textoBusqueda = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Buscar por nombre...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = MaterialTheme.shapes.medium
            )

            // Aquí llamamos a la generación de componentes pasando el ViewModel
            GenerarComponentesDocumento(viewModel, textoBusqueda)
        }
    }
}

    @Composable
    fun GenerarComponentesDocumento(viewModel: DocumentoViewModel, consulta: String) {

        val listaDocumentos = viewModel.listaArchivosSupabase
        val context = LocalContext.current


        val listaFiltrada = remember(listaDocumentos.toList(), consulta) {
            listaDocumentos
                .filter { it.nombre_archivo.contains(consulta, ignoreCase = true) } // filtro por el nombre del archivo
                .sortedBy { it.nombre_archivo.lowercase() } // ordenado por nombres
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(listaFiltrada) { documento ->
                FileArchivoItem(
                    archivo = documento,
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(documento.ruta_archivo))
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
fun DialogoNombreArchivo(onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    var nombre by remember { mutableStateOf("") } // Ahora 'remember' funcionará

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nombre del archivo") },
        text = {
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre") },
                singleLine = true
            )
        },
        confirmButton = {
            Button(onClick = { onConfirm(nombre) }) { Text("Subir") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun FileArchivoItem(archivo: es.maestre.juntosjc.model.ArchivoItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = JuntosTheme.colors.container)
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
                tint = JuntosTheme.colors.content
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = archivo.nombre_archivo,
                    style = MaterialTheme.typography.titleMedium,
                    color = JuntosTheme.colors.content
                )
                Text(
                    text = stringResource(R.string.texto_ver_descargar_DESCARGAR),
                    style = MaterialTheme.typography.bodySmall,
                    color = JuntosTheme.colors.content

                )
            }
        }
    }
}}

