package es.maestre.juntosjc

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import es.maestre.juntosjc.model.CarpetaFotoItem
import es.maestre.juntosjc.ui.theme.JUNTOSJCTheme
import es.maestre.juntosjc.viewModel.FotoViewModel
import es.maestre.juntosjc.viewModel.UserPreferencesViewModel
import kotlinx.coroutines.launch
import okio.IOException
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

class FotosActivity : ComponentActivity() {
    private val fotoViewModel: FotoViewModel by viewModels()
    private val preferencesViewModel: UserPreferencesViewModel by viewModels()

    private var imagenBitmap by mutableStateOf<Bitmap?>(null)
    private var nombreSugerido by mutableStateOf("")
    
    // ImageCapture se guarda como propiedad para poder liberarlo correctamente
    private var imageCapture: ImageCapture? = null

    // Executor para procesamiento de imágenes en background
    private val imageProcessingExecutor = java.util.concurrent.Executors.newSingleThreadExecutor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imageCapture = ImageCapture.Builder().build()
        enableEdgeToEdge()

        fotoViewModel.obtenerCarpetasSupabase()

        setContent {
            val isDarkTheme by preferencesViewModel.isDarkTheme.collectAsStateWithLifecycle()

            JUNTOSJCTheme(darkTheme = isDarkTheme) {
                if (fotoViewModel.mostrarDialogoGuardarFoto && imagenBitmap != null) {
                    DialogoGuardarFoto(
                        carpetas = fotoViewModel.listaCarpetasSupabase,
                        nombreInicial = nombreSugerido,
                        onConfirm = { nombreEscrito, carpetaId ->
                            val nombreBase = nombreEscrito.trim().ifBlank { nombreSugerido }
                            val nombreFinal = if (nombreBase.endsWith(".png", ignoreCase = true)) {
                                nombreBase
                            } else {
                                "$nombreBase.png"
                            }

                            fotoViewModel.mostrarDialogoGuardarFoto = false
                            imagenBitmap?.let { bitmap ->
                                guardarEnGaleriaYSubir(bitmap, nombreFinal, carpetaId)
                            }
                            imagenBitmap = null
                        },
                        onDismiss = {
                            fotoViewModel.mostrarDialogoGuardarFoto = false
                            imagenBitmap = null
                        }
                    )
                }

                FullCameraScreen(
                    onFotoTomada = { bitmap ->
                        imagenBitmap = bitmap
                        nombreSugerido = "foto_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(System.currentTimeMillis())}"
                        fotoViewModel.mostrarDialogoGuardarFoto = true
                    }
                )
            }
        }
    }

    override fun onStop() {
        super.onStop()
        // Desvinculamos la cámara del ciclo de vida para liberar recursos
        try {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                cameraProvider.unbindAll()
            }, ContextCompat.getMainExecutor(this))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        imageCapture = null
        imageProcessingExecutor.shutdown()
        super.onDestroy()
    }

    private fun guardarEnGaleriaYSubir(bitmap: Bitmap, nombreArchivo: String, carpetaId: Long?) {
        val displayName = nombreArchivo.removeSuffix(".png")

        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, displayName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/JuntosJC")
        }

        val uri = contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        )

        try {
            val outputStream = uri?.let { contentResolver.openOutputStream(it) }
            outputStream?.use {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
            }

            if (uri == null) {
                Toast.makeText(this, getString(R.string.error_guardar_imagen), Toast.LENGTH_LONG).show()
                return
            }

            Toast.makeText(this, getString(R.string.foto_guardada_galeria), Toast.LENGTH_LONG).show()

            val bytes = bitmapToPngByteArray(bitmap)
            lifecycleScope.launch {
                fotoViewModel.subirFoto(
                    byteArray = bytes,
                    nombrePersonalizado = nombreArchivo,
                    carpetaId = carpetaId,
                    onSuccess = {
                        runOnUiThread {
                            Toast.makeText(this@FotosActivity, getString(R.string.foto_guardada_supabase), Toast.LENGTH_LONG).show()
                        }
                    },
                    onError = { e ->
                        runOnUiThread {
                            Toast.makeText(
                                this@FotosActivity,
                                getString(R.string.error_subir_imagen_supabase, e.message ?: ""),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                )
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, getString(R.string.error_guardar_imagen), Toast.LENGTH_LONG).show()
        }
    }

    private fun bitmapToPngByteArray(bitmap: Bitmap): ByteArray {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        return outputStream.toByteArray()
    }

    @Composable
    fun FullCameraScreen(
        onFotoTomada: (Bitmap) -> Unit
    ) {
        val context = LocalContext.current
        val cameraPermissionGranted = remember { mutableStateOf(false) }

        val permissionLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            cameraPermissionGranted.value = isGranted
        }

        LaunchedEffect(Unit) {
            val permiso = Manifest.permission.CAMERA
            if (ContextCompat.checkSelfPermission(context, permiso) == PackageManager.PERMISSION_GRANTED) {
                cameraPermissionGranted.value = true
            } else {
                permissionLauncher.launch(permiso)
            }
        }

        if (!cameraPermissionGranted.value) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                Text(stringResource(R.string.permiso_camara_denegado), color = Color.White)
            }
            return
        }

        // Usamos la instancia de ImageCapture almacenada para poder liberarla después
        val imageCapture = imageCapture ?: ImageCapture.Builder().build()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            CameraPreviewView(
                imageCapture = imageCapture,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 80.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = {
                            val intent = Intent(context, GaleriaFotosSupabaseActivity::class.java)
                            context.startActivity(intent)
                        },
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .size(48.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_galeria),
                            contentDescription = stringResource(R.string.ver_galeria),
                            tint = Color.White
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .border(5.dp, Color.White, CircleShape)
                            .padding(2.dp)
                    ) {
                        Button(
                            onClick = { echarFoto(context, onFotoTomada) },
                            modifier = Modifier.fillMaxSize(),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White.copy(alpha = 0.8f)
                            )
                        ) {}
                    }
                }
            }
        }
    }

    @Composable
    fun CameraPreviewView(
        imageCapture: ImageCapture,
        modifier: Modifier = Modifier
    ) {
        val context = LocalContext.current
        val lifecycleOwner = LocalLifecycleOwner.current
        val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx).apply {
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                }

                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()

                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageCapture
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }, ContextCompat.getMainExecutor(context))

                previewView
            },
            modifier = modifier
        )
    }

    fun echarFoto(
        context: Context,
        onFotoTomada: (Bitmap) -> Unit
    ) {
        val capture = imageCapture ?: ImageCapture.Builder().build()
        val archivo = File(context.cacheDir, "foto_temp.png")
        val outputOptions = ImageCapture.OutputFileOptions.Builder(archivo).build()

        capture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Toast.makeText(context, context.getString(R.string.error_tomar_foto), Toast.LENGTH_SHORT).show()
                }

                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    // Procesar imagen en background para evitar ANR
                    imageProcessingExecutor.execute {
                        var bitmap = BitmapFactory.decodeFile(archivo.path)
                        
                        // Escalar bitmap si es muy grande (>2048px en el lado más largo)
                        val maxSize = 2048
                        if (bitmap.width > maxSize || bitmap.height > maxSize) {
                            val scale = maxSize.toFloat() / maxOf(bitmap.width, bitmap.height)
                            val scaled = Bitmap.createScaledBitmap(
                                bitmap,
                                (bitmap.width * scale).toInt(),
                                (bitmap.height * scale).toInt(),
                                true
                            )
                            if (scaled != bitmap) {
                                bitmap.recycle()
                                bitmap = scaled
                            }
                        }

                        try {
                            val exif = ExifInterface(archivo.path)
                            val orientation = exif.getAttributeInt(
                                ExifInterface.TAG_ORIENTATION,
                                ExifInterface.ORIENTATION_UNDEFINED
                            )

                            val matrix = Matrix()
                            when (orientation) {
                                ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                                ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                                ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                            }

                            if (orientation != ExifInterface.ORIENTATION_NORMAL && orientation != ExifInterface.ORIENTATION_UNDEFINED) {
                                val rotated = Bitmap.createBitmap(
                                    bitmap,
                                    0,
                                    0,
                                    bitmap.width,
                                    bitmap.height,
                                    matrix,
                                    true
                                )
                                if (rotated != bitmap) {
                                    bitmap.recycle()
                                    bitmap = rotated
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                        // Devolver al hilo principal el bitmap procesado
                        runOnUiThread {
                            archivo.delete() // Limpiar archivo temporal
                            onFotoTomada(bitmap)
                        }
                    }
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogoGuardarFoto(
    carpetas: List<CarpetaFotoItem>,
    nombreInicial: String,
    onConfirm: (String, Long?) -> Unit,
    onDismiss: () -> Unit
) {
    var nombre by remember(nombreInicial) { mutableStateOf(nombreInicial) }
    var expanded by remember { mutableStateOf(false) }
    var carpetaSeleccionada by remember { mutableStateOf<CarpetaFotoItem?>(null) }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.subir_foto)) },
        text = {
            Column {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text(stringResource(R.string.nombre_foto)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = carpetaSeleccionada?.nombre_carpeta ?: stringResource(R.string.sin_carpeta),
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
                            text = { Text(stringResource(R.string.sin_carpeta)) },
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
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancelar))
            }
        }
    )
}
