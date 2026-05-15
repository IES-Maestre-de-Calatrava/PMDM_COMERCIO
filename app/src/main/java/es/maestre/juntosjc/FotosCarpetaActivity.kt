package es.maestre.juntosjc

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.SuccessResult
import es.maestre.juntosjc.model.FotoCamaraItem
import es.maestre.juntosjc.ui.theme.JUNTOSJCTheme
import es.maestre.juntosjc.ui.theme.JuntosTheme
import es.maestre.juntosjc.viewModel.FotoViewModel
import es.maestre.juntosjc.viewModel.UserPreferencesViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.OutputStream

class FotosCarpetaActivity : ComponentActivity() {

    companion object {
        const val EXTRA_FOLDER_ID = "extra_folder_id"
        const val EXTRA_FOLDER_NAME = "extra_folder_name"
        const val EXTRA_IS_UNFILED = "extra_is_unfiled"
    }

    private val viewModel: FotoViewModel by viewModels()
    private val preferencesViewModel: UserPreferencesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        viewModel.obtenerFotosSupabase()

        val folderId = intent.getLongExtra(EXTRA_FOLDER_ID, -1L)
        val folderName = intent.getStringExtra(EXTRA_FOLDER_NAME).orEmpty().ifBlank { getString(R.string.txt_galeria) }
        val isUnfiled = intent.getBooleanExtra(EXTRA_IS_UNFILED, false)

        setContent {
            val isDarkTheme by preferencesViewModel.isDarkTheme.collectAsStateWithLifecycle()

            JUNTOSJCTheme(darkTheme = isDarkTheme) {
                FotosCarpetaScreen(
                    viewModel = viewModel,
                    folderId = folderId,
                    folderName = folderName,
                    isUnfiled = isUnfiled
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FotosCarpetaScreen(
    viewModel: FotoViewModel,
    folderId: Long,
    folderName: String,
    isUnfiled: Boolean
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedFoto by remember { mutableStateOf<FotoCamaraItem?>(null) }
    var fotoPendienteEliminar by remember { mutableStateOf<FotoCamaraItem?>(null) }

    val fotos = remember(viewModel.listaFotosSupabase.toList(), folderId, isUnfiled) {
        viewModel.listaFotosSupabase
            .filter {
                if (isUnfiled) it.carpeta_id == null else it.carpeta_id == folderId
            }
            .sortedBy { it.nombreOrdenable }
    }

    // Dialogo de borrado
    fotoPendienteEliminar?.let { foto ->
        DialogoConfirmarEliminacion(
            titulo = stringResource(R.string.eliminar_foto_titulo),
            mensaje = stringResource(R.string.eliminar_foto_mensaje, foto.nombreVisible),
            onConfirm = {
                fotoPendienteEliminar = null
                if (selectedFoto?.id == foto.id) {
                    selectedFoto = null
                }
                viewModel.eliminarFotoSupabase(
                    foto = foto,
                    onSuccess = {
                        Toast.makeText(context, context.getString(R.string.foto_eliminada), Toast.LENGTH_SHORT).show()
                    },
                    onError = { e ->
                        Toast.makeText(
                            context,
                            context.getString(R.string.error_eliminar_foto, e.message ?: ""),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                )
            },
            onDismiss = { fotoPendienteEliminar = null }
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(R.drawable.photo_album_svgrepo_com),
                            contentDescription = null,
                            modifier = Modifier.size(28.dp),
                            tint = Color.Unspecified
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = folderName,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = JuntosTheme.colors.azulOscuroLogo
                            )
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = JuntosTheme.colors.container,
                    titleContentColor = JuntosTheme.colors.content
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.fotos_carpeta),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = JuntosTheme.colors.azulOscuroLogo
                )
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            if (fotos.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = JuntosTheme.colors.container)
                ) {
                    Text(
                        text = stringResource(R.string.no_fotos),
                        modifier = Modifier.padding(16.dp),
                        color = JuntosTheme.colors.content
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(fotos, key = { it.id ?: it.urlImagen }) { foto ->
                        FotoCarpetaGridItem(
                            foto = foto,
                            onClick = { selectedFoto = foto },
                            onLongClick = { fotoPendienteEliminar = foto }
                        )
                    }
                }
            }
        }
    }

    if (selectedFoto != null) {
        FotoDetalleDialog(
            foto = selectedFoto!!,
            onDismiss = { selectedFoto = null },
            onGuardar = {
                selectedFoto?.let { foto ->
                    scope.launch {
                        guardarImagenEnGaleria(context, foto.urlImagen)
                    }
                }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FotoCarpetaGridItem(
    foto: FotoCamaraItem,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        colors = CardDefaults.cardColors(containerColor = JuntosTheme.colors.container),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(foto.urlImagen)
                        .crossfade(true)
                        .build(),
                    contentDescription = foto.nombreVisible,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Text(
                text = foto.nombreVisible,
                modifier = Modifier.padding(12.dp),
                color = JuntosTheme.colors.content,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun FotoDetalleDialog(
    foto: FotoCamaraItem,
    onDismiss: () -> Unit,
    onGuardar: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .fillMaxHeight(0.65f),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(4.dp, colorResource(R.color.azul_pastel)),
            colors = CardDefaults.cardColors(
                containerColor = JuntosTheme.colors.cardBackground
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(JuntosTheme.colors.cardBackground)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(foto.urlImagen)
                            .crossfade(true)
                            .build(),
                        contentDescription = foto.nombreVisible,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(8.dp))
                    )
                }

                Text(
                    text = foto.nombreVisible,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    textAlign = TextAlign.Center,
                    color = JuntosTheme.colors.content,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(70.dp)
                        .background(colorResource(R.color.azul_pastel)),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = onGuardar,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorResource(R.color.azul_pastel),
                            contentColor = colorResource(R.color.black)
                        ),
                        elevation = ButtonDefaults.buttonElevation(0.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.txt_descargar),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }
    }
}

suspend fun guardarImagenEnGaleria(context: Context, imageUrl: String) {
    withContext(Dispatchers.IO) {
        try {
            val loader = ImageLoader(context)
            val request = ImageRequest.Builder(context)
                .data(imageUrl)
                .allowHardware(false)
                .build()

            val result = (loader.execute(request) as? SuccessResult)?.drawable
            val bitmap = (result as? android.graphics.drawable.BitmapDrawable)?.bitmap

            if (bitmap != null) {
                saveBitmapToMediaStore(context, bitmap)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, context.getString(R.string.foto_guardada_galeria), Toast.LENGTH_SHORT).show()
                }
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, context.getString(R.string.error_descargar_imagen), Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    context,
                    context.getString(R.string.error_generico, e.message ?: ""),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}

private fun saveBitmapToMediaStore(context: Context, bitmap: android.graphics.Bitmap) {
    val filename = "IMG_${System.currentTimeMillis()}.jpg"
    var fos: OutputStream? = null
    var imageUri: Uri? = null

    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/JuntosJC")
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }
    }

    val resolver = context.contentResolver

    try {
        imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        imageUri?.let { uri ->
            fos = resolver.openOutputStream(uri)
            fos?.use {
                bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 100, it)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(uri, contentValues, null, null)
            }
        }
    } finally {
        fos?.close()
    }
}
