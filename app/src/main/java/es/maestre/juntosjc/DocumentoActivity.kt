package es.maestre.juntosjc

import android.content.Intent
import android.os.Bundle
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import es.maestre.juntosjc.model.Ayuda
import es.maestre.juntosjc.model.CarpetaDocumentoItem
import es.maestre.juntosjc.ui.theme.JUNTOSJCTheme
import es.maestre.juntosjc.ui.theme.JuntosTheme
import es.maestre.juntosjc.viewModel.DocumentoViewModel
import es.maestre.juntosjc.viewModel.UserPreferencesViewModel

class DocumentoActivity : ComponentActivity() {

    private val viewModel: DocumentoViewModel by viewModels()
    private val preferencesViewModel: UserPreferencesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        viewModel.obtenerArchivosSupabase()
        viewModel.obtenerCarpetasSupabase()

        var tempByteArray: ByteArray? = null
        var tempExtension: String? = null

        val pickDocumentLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            uri?.let {
                val mimeType = contentResolver.getType(it)
                val inputStream = contentResolver.openInputStream(it)
                tempByteArray = inputStream?.use { stream -> stream.readBytes() }
                tempExtension = obtenerExtensionDesdeMimeType(mimeType)

                if (tempByteArray != null) {
                    viewModel.mostrarDialogoSubida = true
                }
            }
        }

        setContent {
            val isDarkTheme by preferencesViewModel.isDarkTheme.collectAsStateWithLifecycle()

            JUNTOSJCTheme(darkTheme = isDarkTheme) {
                if (viewModel.mostrarDialogoSubida) {
                    DialogoSubirDocumento(
                        carpetas = viewModel.listaCarpetasSupabase,
                        extension = tempExtension.orEmpty(),
                        onConfirm = { nombreEscrito, carpetaId ->
                            val extensionLimpia = tempExtension.orEmpty().trim('.').ifBlank { "bin" }
                            val nombreBase = nombreEscrito.trim()
                            val nombreFinal = if (nombreBase.endsWith(".$extensionLimpia", ignoreCase = true)) {
                                nombreBase
                            } else {
                                "$nombreBase.$extensionLimpia"
                            }

                            viewModel.mostrarDialogoSubida = false

                            val bytes = tempByteArray
                            if (bytes == null) {
                                Toast.makeText(this, "No se pudo leer el archivo seleccionado", Toast.LENGTH_LONG).show()
                                return@DialogoSubirDocumento
                            }

                            viewModel.subirDocumento(
                                byteArray = bytes,
                                nombrePersonalizado = nombreFinal,
                                carpetaId = carpetaId,
                                onSuccess = {
                                    runOnUiThread {
                                        Toast.makeText(this, "Documento subido correctamente", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                onError = { e ->
                                    runOnUiThread {
                                        Toast.makeText(this, "Error al subir: ${e.message}", Toast.LENGTH_LONG).show()
                                    }
                                }
                            )
                        },
                        onDismiss = { viewModel.mostrarDialogoSubida = false }
                    )
                }

                if (viewModel.mostrarDialogoCrearCarpeta) {
                    DialogoCrearCarpeta(
                        onConfirm = { nombreCarpeta ->
                            viewModel.mostrarDialogoCrearCarpeta = false
                            viewModel.crearCarpetaSupabase(
                                nombreCarpeta = nombreCarpeta,
                                onSuccess = {
                                    runOnUiThread {
                                        Toast.makeText(this, "Carpeta creada correctamente", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                onError = { e ->
                                    runOnUiThread {
                                        Toast.makeText(this, "Error al crear carpeta: ${e.message}", Toast.LENGTH_LONG).show()
                                    }
                                }
                            )
                        },
                        onDismiss = { viewModel.mostrarDialogoCrearCarpeta = false }
                    )
                }

                DocumentosHomeScreen(
                    viewModel = viewModel,
                    onPickDocument = {
                        pickDocumentLauncher.launch(
                            arrayOf(
                                "image/*",
                                "application/pdf",
                                "application/msword",
                                "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                            )
                        )
                    },
                    onCrearCarpeta = {
                        viewModel.mostrarDialogoCrearCarpeta = true
                    }
                )
            }
        }
    }

    private fun obtenerExtensionDesdeMimeType(mimeType: String?): String {
        if (mimeType.isNullOrBlank()) return "bin"
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
            ?: mimeType.substringAfterLast('/', "bin")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentosHomeScreen(
    viewModel: DocumentoViewModel,
    onPickDocument: () -> Unit,
    onCrearCarpeta: () -> Unit
) {
    val context = LocalContext.current
    var textoBusqueda by remember { mutableStateOf("") }

    val carpetasFiltradas = remember(viewModel.listaCarpetasSupabase.toList(), textoBusqueda) {
        viewModel.listaCarpetasSupabase
            .filter { it.nombre_carpeta.contains(textoBusqueda, ignoreCase = true) }
            .sortedBy { it.nombre_carpeta.lowercase() }
    }

    val documentosSinCarpeta = remember(viewModel.listaArchivosSupabase.toList()) {
        viewModel.listaArchivosSupabase.count { it.carpeta_id == null }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
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
                onClick = onPickDocument,
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
        Column(
            modifier = Modifier
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
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    stringResource(R.string.txt_documentos),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = JuntosTheme.colors.azulOscuroLogo
                    )
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(onClick = onCrearCarpeta) {
                    Icon(imageVector = Icons.Default.CreateNewFolder, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.crear_carpeta))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = textoBusqueda,
                onValueChange = { textoBusqueda = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(R.string.buscar_carpeta)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = MaterialTheme.shapes.medium
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                item {
                    CarpetaGridItem(
                        nombre = stringResource(R.string.sin_carpeta),
                        contador = documentosSinCarpeta,
                        onClick = {
                            val intent = Intent(context, DocumentosCarpetaActivity::class.java).apply {
                                putExtra(DocumentosCarpetaActivity.EXTRA_FOLDER_NAME, "Sin carpeta")
                                putExtra(DocumentosCarpetaActivity.EXTRA_IS_UNFILED, true)
                            }
                            context.startActivity(intent)
                        }
                    )
                }

                items(carpetasFiltradas, key = { it.id_carpeta ?: it.nombre_carpeta }) { carpeta ->
                    val contador = viewModel.listaArchivosSupabase.count { it.carpeta_id == carpeta.id_carpeta }
                    CarpetaGridItem(
                        nombre = carpeta.nombre_carpeta,
                        contador = contador,
                        onClick = {
                            val intent = Intent(context, DocumentosCarpetaActivity::class.java).apply {
                                putExtra(DocumentosCarpetaActivity.EXTRA_FOLDER_ID, carpeta.id_carpeta ?: -1L)
                                putExtra(DocumentosCarpetaActivity.EXTRA_FOLDER_NAME, carpeta.nombre_carpeta)
                            }
                            context.startActivity(intent)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CarpetaGridItem(
    nombre: String,
    contador: Int,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Card(
            modifier = Modifier.size(85.dp),
            colors = CardDefaults.cardColors(containerColor = JuntosTheme.colors.cardBackground),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(JuntosTheme.colors.cardBackground),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.favorite_file_svgrepo_com),
                    contentDescription = nombre,
                    modifier = Modifier.size(35.dp),
                    tint = Color.Unspecified
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = nombre,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold,
                color = JuntosTheme.colors.txtDebajo,
                textAlign = TextAlign.Center
            ),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = "$contador item${if (contador == 1) "" else "s"}",
            style = MaterialTheme.typography.bodySmall,
            color = JuntosTheme.colors.content,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogoSubirDocumento(
    carpetas: List<CarpetaDocumentoItem>,
    extension: String,
    onConfirm: (String, Long?) -> Unit,
    onDismiss: () -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var carpetaSeleccionada by remember { mutableStateOf<CarpetaDocumentoItem?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.subir_documento)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text(stringResource(R.string.nombre_archivo)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Extension: .${extension.trim('.').ifBlank { "bin" }}",
                    style = MaterialTheme.typography.bodySmall,
                    color = JuntosTheme.colors.content
                )

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = carpetaSeleccionada?.nombre_carpeta ?: "Sin carpeta",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.carpeta)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Sin carpeta") },
                            onClick = {
                                carpetaSeleccionada = null
                                expanded = false
                            }
                        )

                        carpetas.forEach { carpeta ->
                            DropdownMenuItem(
                                text = { Text(carpeta.nombre_carpeta) },
                                onClick = {
                                    carpetaSeleccionada = carpeta
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(nombre.trim(), carpetaSeleccionada?.id_carpeta) },
                enabled = nombre.trim().isNotEmpty()
            ) {
                Text(stringResource(R.string.subir))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun DialogoCrearCarpeta(onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    var nombreCarpeta by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.crear_carpeta)) },
        text = {
            OutlinedTextField(
                value = nombreCarpeta,
                onValueChange = { nombreCarpeta = it },
                label = { Text(stringResource(R.string.nombre_carpeta)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(nombreCarpeta.trim()) },
                enabled = nombreCarpeta.trim().isNotEmpty()
            ) {
                Text(stringResource(R.string.crear))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancelar)) }
        }
    )
}
