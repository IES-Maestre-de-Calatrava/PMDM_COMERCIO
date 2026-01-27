package es.maestre.juntosjc

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.lifecycleScope
import es.maestre.juntosjc.model.FotoCamaraItem
import es.maestre.juntosjc.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.launch
import okio.IOException
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Clase FotosActivity: en esta clase se muestran dos botones, uno que permite abrir la cámara y echar una foto,
 * y el otro que te permite abrir la galería y visualizar las imágenes que has añadido anteriormente
 */
class FotosActivity : ComponentActivity() {
    // Estado de la imagen
    private var imagenBitmap by mutableStateOf<Bitmap?>(null)

    // Launcher para escoger la imagen Nombre del archivo escrito por el usuario
    private lateinit var pickMediaLauncher: ActivityResultLauncher<PickVisualMediaRequest>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Abrir la galería
        pickMediaLauncher =
            registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
                if(uri != null) {
                    imagenBitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                }
            }

        setContent {
            FullCameraScreen(
                onFotoTomada = { bitmap ->
                    imagenBitmap = bitmap
                    guardarEnGaleria(bitmap)
                },
                onAbrirGaleria = {
                    pickMediaLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
            )
        }
    }

    // Guardar imagen en galeria y subir a Supabase
    private fun guardarEnGaleria(bitmap: Bitmap) {
        val nombre = "foto_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(System.currentTimeMillis())}"

        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, nombre)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
        }

        val uri = contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        )

        try {
            val outputStream = contentResolver.openOutputStream(uri!!)
            outputStream?.let {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
                it.close()
                Toast.makeText(this, "Imagen guardada en la galería", Toast.LENGTH_LONG).show()

                // Convertir bitmap a archivo temporal y subir a Supabase
                val archivoTemporal = convertBitmapToFile(bitmap, nombre)
                lifecycleScope.launch {
                    val urlImagen = subirAlBucket(archivoTemporal, "$nombre.png")
                    if (urlImagen != null) {
                        guardarEnBaseDatos(urlImagen)
                    } else {
                        Toast.makeText(this@FotosActivity, "Error al subir la imagen a Supabase", Toast.LENGTH_LONG).show()
                    }
                }
            }
        } catch(e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Error al guardar la imagen", Toast.LENGTH_LONG).show()
        }
    }

    @Composable
    fun FullCameraScreen(
        onFotoTomada: (Bitmap) -> Unit,
        onAbrirGaleria: () -> Unit
    ) {
        val context = LocalContext.current
        val cameraPermissionGranted = remember { mutableStateOf(false) }

        // Pedir permiso de camara
        val permissionLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            cameraPermissionGranted.value = isGranted
        }

        LaunchedEffect(Unit) {
            val permiso = Manifest.permission.CAMERA
            if (ContextCompat.checkSelfPermission(context, permiso)
                == PackageManager.PERMISSION_GRANTED
            ) {
                cameraPermissionGranted.value = true
            } else {
                permissionLauncher.launch(permiso)
            }
        }

        if (!cameraPermissionGranted.value) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                Text("Permiso de cámara denegado", color = Color.White)
            }
            return
        }

        val imageCapture = remember { ImageCapture.Builder().build() }

        Box(
            modifier = Modifier.fillMaxSize().background(Color.Black)
        ) {
            CameraPreviewView(
                imageCapture = imageCapture,
                modifier = Modifier.fillMaxSize()
            )

            Box(
                modifier = Modifier.fillMaxSize().padding(bottom = 80.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 40.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Botón de la galería
                    IconButton(
                        onClick = onAbrirGaleria,
                        modifier = Modifier.size(56.dp).background(Color.Black.copy(alpha = 0.4f), CircleShape)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_galeria),
                            contentDescription = "Ver Galería",
                            tint = Color.White
                        )
                    }

                    // Botón de hacer foto
                    Box(
                        modifier = Modifier.size(80.dp).border(5.dp,
                        Color.White, CircleShape) .padding(5.dp)
                    ) {
                        Button(
                            onClick = {
                                echarFoto(context, imageCapture, onFotoTomada)
                            },
                            modifier = Modifier.fillMaxSize(),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White.copy(alpha = 0.8f)
                            )
                        ) { }
                    }

                    Spacer(modifier = Modifier.size(56.dp))
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
        imageCapture: ImageCapture,
        onFotoTomada: (Bitmap) -> Unit
    ) {
        val archivo = File(context.cacheDir, "foto_temp.png")
        val outputOptions = ImageCapture.OutputFileOptions.Builder(archivo).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback  {
                override fun onError(exc: ImageCaptureException) {
                    Toast.makeText(context, "Error al tomar foto", Toast.LENGTH_SHORT).show()
                }

                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val bitmap = BitmapFactory.decodeFile(archivo.path)
                    onFotoTomada(bitmap)
                }
            }
        )
    }

    private fun convertBitmapToFile(bitmap: Bitmap, nombreArchivo: String): File {
        val file = File(cacheDir, "${nombreArchivo}_${System.currentTimeMillis()}.png")
        file.outputStream().use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        }
        return file
    }

    private suspend fun subirAlBucket(file: File, nombreArchivo: String): String? {
        return try {
            val bucket = SupabaseClient.client.storage.from("CameraPhotos")
            val fileBytes = file.readBytes()
            bucket.upload(nombreArchivo, fileBytes)

            // Construir la URL pública del archivo
            val publicUrl = "https://lxmkwegowscwhgrfsqcw.supabase.co/storage/v1/object/public/CameraPhotos/$nombreArchivo"
            publicUrl
        } catch (e: Exception) {
            Log.e("FotosActivity", "Error subiendo al bucket: ${e.message}", e)
            null
        }
    }

    private suspend fun guardarEnBaseDatos(urlImagen: String) {
        try {
            val fotoCamara = FotoCamaraItem(
                urlImagen = urlImagen
            )

            SupabaseClient.client.postgrest
                .from("fotosCamara")
                .insert(fotoCamara)

            Log.d("FotosActivity", "Registro guardado en BD")
            Toast.makeText(this, "Foto guardada en Supabase", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Log.e("FotosActivity", "Error guardando en BD: ${e.message}", e)
            Toast.makeText(this, "Error al guardar en base de datos: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}

