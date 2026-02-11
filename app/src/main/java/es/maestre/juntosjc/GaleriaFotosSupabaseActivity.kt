package es.maestre.juntosjc

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.SuccessResult
import es.maestre.juntosjc.model.Ayuda
import es.maestre.juntosjc.model.FotoCamaraItem
import es.maestre.juntosjc.supabase.SupabaseClient
import es.maestre.juntosjc.ui.theme.JUNTOSJCTheme
import es.maestre.juntosjc.ui.theme.JuntosTheme
import es.maestre.juntosjc.viewModel.UserPreferencesViewModel
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.OutputStream

class GaleriaFotosSupabaseActivity : ComponentActivity() {

    private val preferencesViewModel: UserPreferencesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isDarkTheme by preferencesViewModel.isDarkTheme.collectAsStateWithLifecycle()

            JUNTOSJCTheme(darkTheme = isDarkTheme) {
                GaleriaFotosScreen()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        preferencesViewModel.loadPreferences()
        preferencesViewModel.recalculateTheme()
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun GaleriaFotosScreen() {

        var fotos by remember { mutableStateOf<List<FotoCamaraItem>>(emptyList()) }
        var isLoading by remember { mutableStateOf(true) }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        var selectedFoto by remember { mutableStateOf<FotoCamaraItem?>(null) }

        val scope = rememberCoroutineScope()
        val context = LocalContext.current

        LaunchedEffect(Unit) {
            try {
                val fotosCargadas = SupabaseClient.client.postgrest
                    .from("fotosCamara")
                    .select()
                    .decodeList<FotoCamaraItem>()
                fotos = fotosCargadas
                isLoading = false
            } catch (e: Exception) {
                errorMessage = e.message
                isLoading = false
            }
        }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(R.string.txt_fotos),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.headlineMedium
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    actions = {
                        IconButton(onClick = {
                            val intent = Intent(context, AyudaActivity::class.java)
                            intent.putExtra("SECCION", Ayuda.FOTOS)
                            context.startActivity(intent)
                        }) {
                            Icon(
                                painter = painterResource(R.drawable.help_question_svgrepo_com),
                                contentDescription = "Ayuda",
                                tint = Color.Unspecified
                            )
                        }
                    }
                )
            }
        ) { paddingValues ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                when {
                    isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = MaterialTheme.colorScheme.primary) }
                    errorMessage != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(errorMessage ?: "Error", color = MaterialTheme.colorScheme.error) }
                    fotos.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No hay fotos", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    else -> {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(fotos) { foto ->
                                FotoGridItem(foto) { selectedFoto = foto }
                            }
                        }
                    }
                }
            }

            // Diálogo de detalle de foto
            if (selectedFoto != null) {
                FotoDetalleDialog(
                    foto = selectedFoto!!,
                    onDismiss = { selectedFoto = null },
                    onGuardar = {
                        scope.launch {
                            guardarImagenEnGaleria(context, selectedFoto!!.urlImagen)
                        }
                    }
                )
            }
        }
    }

    @Composable
    fun FotoGridItem(foto: FotoCamaraItem, onClick: () -> Unit) {
        val context = LocalContext.current
        Box(
            modifier = Modifier
                .aspectRatio(1f)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable { onClick() }
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(foto.urlImagen)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
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
                    .fillMaxHeight(0.60f),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(4.dp, colorResource(R.color.azul_pastel)),
                colors = CardDefaults.cardColors(
                    containerColor = JuntosTheme.colors.cardBackground
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
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
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(8.dp))
                        )
                    }

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
                            elevation = ButtonDefaults.buttonElevation(0.dp),
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
                                style = MaterialTheme.typography.titleMedium,
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
                val bitmap = (result as? BitmapDrawable)?.bitmap

                if (bitmap != null) {
                    saveBitmapToMediaStore(context, bitmap)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Foto guardada en galería", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Error al descargar imagen", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun saveBitmapToMediaStore(context: Context, bitmap: Bitmap) {
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
                fos?.let {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.clear()
                    contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    resolver.update(uri, contentValues, null, null)
                }
            }
        } catch (e: Exception) {
            if (imageUri != null) {
                resolver.delete(imageUri, null, null)
            }
            throw e
        } finally {
            fos?.close()
        }
    }
}