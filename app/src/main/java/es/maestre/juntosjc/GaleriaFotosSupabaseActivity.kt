package es.maestre.juntosjc

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import es.maestre.juntosjc.model.Ayuda
import es.maestre.juntosjc.ui.theme.JUNTOSJCTheme
import es.maestre.juntosjc.ui.theme.JuntosTheme
import es.maestre.juntosjc.viewModel.FotoViewModel
import es.maestre.juntosjc.viewModel.UserPreferencesViewModel

class GaleriaFotosSupabaseActivity : ComponentActivity() {

    private val viewModel: FotoViewModel by viewModels()
    private val preferencesViewModel: UserPreferencesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        viewModel.obtenerFotosSupabase()
        viewModel.obtenerCarpetasSupabase()

        setContent {
            val isDarkTheme by preferencesViewModel.isDarkTheme.collectAsStateWithLifecycle()

            JUNTOSJCTheme(darkTheme = isDarkTheme) {
                if (viewModel.mostrarDialogoCrearCarpeta) {
                    DialogoCrearCarpeta(
                        onConfirm = { nombreCarpeta ->
                            viewModel.mostrarDialogoCrearCarpeta = false
                            viewModel.crearCarpetaSupabase(
                                nombreCarpeta = nombreCarpeta,
                                onSuccess = {
                                    runOnUiThread {
                                        Toast.makeText(this, getString(R.string.carpeta_creada_correctamente), Toast.LENGTH_SHORT).show()
                                    }
                                },
                                onError = { e ->
                                    runOnUiThread {
                                        Toast.makeText(this, getString(R.string.error_crear_carpeta, e.message ?: ""), Toast.LENGTH_LONG).show()
                                    }
                                }
                            )
                        },
                        onDismiss = { viewModel.mostrarDialogoCrearCarpeta = false }
                    )
                }

                GaleriaFotosHomeScreen(
                    viewModel = viewModel,
                    onCrearCarpeta = { viewModel.mostrarDialogoCrearCarpeta = true },
                    onHacerFoto = {
                        startActivity(Intent(this, FotosActivity::class.java))
                    }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        preferencesViewModel.loadPreferences()
        preferencesViewModel.recalculateTheme()
        viewModel.obtenerFotosSupabase()
        viewModel.obtenerCarpetasSupabase()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GaleriaFotosHomeScreen(
    viewModel: FotoViewModel,
    onCrearCarpeta: () -> Unit,
    onHacerFoto: () -> Unit
) {
    val context = LocalContext.current
    var textoBusqueda by remember { mutableStateOf("") }
    var carpetaPendienteEliminar by remember { mutableStateOf<es.maestre.juntosjc.model.CarpetaFotoItem?>(null) }

    val carpetasFiltradas = remember(viewModel.listaCarpetasSupabase.toList(), textoBusqueda) {
        viewModel.listaCarpetasSupabase
            .filter { it.nombre_carpeta.contains(textoBusqueda, ignoreCase = true) }
            .sortedBy { it.nombre_carpeta.lowercase() }
    }

    val fotosSinCarpeta = remember(viewModel.listaFotosSupabase.toList()) {
        viewModel.listaFotosSupabase.count { it.carpeta_id == null }
    }

    carpetaPendienteEliminar?.let { carpeta ->
        DialogoConfirmarEliminacion(
            titulo = stringResource(R.string.eliminar_carpeta_titulo),
            mensaje = stringResource(R.string.eliminar_carpeta_mensaje, carpeta.nombre_carpeta),
            onConfirm = {
                carpetaPendienteEliminar = null
                viewModel.eliminarCarpetaSupabase(
                    carpeta = carpeta,
                    onSuccess = {
                        Toast.makeText(context, context.getString(R.string.carpeta_eliminada), Toast.LENGTH_SHORT).show()
                    },
                    onError = { e ->
                        val message = if (e.message == "La carpeta contiene fotos") {
                            context.getString(R.string.error_carpeta_no_vacia_fotos)
                        } else {
                            context.getString(R.string.error_eliminar_carpeta, e.message ?: "")
                        }
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                    }
                )
            },
            onDismiss = { carpetaPendienteEliminar = null }
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(R.drawable.logo_nuevo),
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
                        intent.putExtra("SECCION", Ayuda.FOTOS)
                        context.startActivity(intent)
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.help_question_svgrepo_com),
                            contentDescription = stringResource(R.string.ayuda),
                            tint = Color.Unspecified
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onHacerFoto,
                containerColor = colorResource(R.color.white),
                contentColor = Color.Unspecified
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_hacer_foto),
                    contentDescription = stringResource(R.string.txt_foto)
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
                    painter = painterResource(R.drawable.photo_album_svgrepo_com),
                    contentDescription = null,
                    modifier = Modifier.size(30.dp),
                    tint = Color.Unspecified
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    stringResource(R.string.txt_galeria),
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
                        contador = fotosSinCarpeta,
                        onClick = {
                            val intent = Intent(context, FotosCarpetaActivity::class.java).apply {
                                putExtra(FotosCarpetaActivity.EXTRA_FOLDER_NAME, context.getString(R.string.sin_carpeta))
                                putExtra(FotosCarpetaActivity.EXTRA_IS_UNFILED, true)
                            }
                            context.startActivity(intent)
                        },
                        iconRes = R.drawable.photo_album_svgrepo_com
                    )
                }

                items(carpetasFiltradas, key = { it.id_carpeta ?: it.nombre_carpeta }) { carpeta ->
                    val contador = viewModel.listaFotosSupabase.count { it.carpeta_id == carpeta.id_carpeta }
                    CarpetaGridItem(
                        nombre = carpeta.nombre_carpeta,
                        contador = contador,
                        onClick = {
                            val intent = Intent(context, FotosCarpetaActivity::class.java).apply {
                                putExtra(FotosCarpetaActivity.EXTRA_FOLDER_ID, carpeta.id_carpeta ?: -1L)
                                putExtra(FotosCarpetaActivity.EXTRA_FOLDER_NAME, carpeta.nombre_carpeta)
                            }
                            context.startActivity(intent)
                        },
                        onLongClick = { carpetaPendienteEliminar = carpeta },
                        iconRes = R.drawable.photo_album_svgrepo_com
                    )
                }
            }
        }
    }
}
