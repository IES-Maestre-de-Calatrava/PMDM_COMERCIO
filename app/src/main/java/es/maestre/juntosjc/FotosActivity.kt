package es.maestre.juntosjc

import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import okio.IOException
import es.maestre.juntosjc.model.FotoCamaraItem
import es.maestre.juntosjc.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import io.github.jan.supabase.storage.upload
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

/**
 * Clase FotosActivity: en esta clase se muestran dos botones, uno que permite abrir la cámara y echar una foto,
 * y el otro que te permite abrir la galería y visualizar las imágenes que has añadido anteriormente
 */
class FotosActivity : ComponentActivity() {
    // Estados
    private val imagenBitmap = mutableStateOf<Bitmap?>(null)
    private val nombreArchivo = mutableStateOf("")
    private val pantallaActual = mutableStateOf(PantallaActual.CAMARA)

    // Lista de fotos
    private val fotosGuardadas = mutableStateListOf<Bitmap>()
    // Launchers
    private lateinit var abrirCamara: ActivityResultLauncher<Intent>
    private lateinit var pedirPermiso: ActivityResultLauncher<String>
    private lateinit var escoger: ActivityResultLauncher<PickVisualMediaRequest>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Abrir camara
        abrirCamara = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if(result.resultCode == RESULT_OK){
                val data = result.data!!
                val bitmap = data.extras!!.get("data") as Bitmap
                imagenBitmap.value = bitmap
                pantallaActual.value = PantallaActual.PREVIEW
            }
        }

        // Pedir permiso
        pedirPermiso = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if(isGranted){
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                abrirCamara.launch(intent)
            } else{
                Log.e("SAR", "Permiso de camara no concedido")
            }
        }

        // Abrir galeria
        escoger = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if(uri != null){
                imagenBitmap.value = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                pantallaActual.value = PantallaActual.PREVIEW
            }
        }

        setContent {
            when (pantallaActual.value){
                PantallaActual.CAMARA -> PantallaCamara(
                    onAbrirCamara = {
                        pedirPermiso.launch(android.Manifest.permission.CAMERA)
                    },
                    onAbrirGaleria = {
                        pantallaActual.value = PantallaActual.GALERIA
                    }
                )
                PantallaActual.GALERIA -> PantallaGaleria(
                    fotos = fotosGuardadas,
                    onVolver = { pantallaActual.value = PantallaActual.CAMARA },
                    onSeleccionar = { bitmap ->
                        imagenBitmap.value = bitmap
                        pantallaActual.value = PantallaActual.PREVIEW
                    }
                )
                PantallaActual.PREVIEW -> PantallaPreview(
                    imagenBitmap = imagenBitmap.value,
                    nombreArchivo = nombreArchivo.value,
                    onNombreChange = { nombreArchivo.value = it },
                    onGuardar = {
                        guardarImagen(imagenBitmap.value, nombreArchivo.value)
                    },
                    onVolver = {
                        pantallaActual.value = PantallaActual.CAMARA
                    }
                )
            }
        }
    }

    // Guardar imagen
    private fun guardarImagen(bitmap: Bitmap?, nombreArchivo: String) {
        if(bitmap != null && nombreArchivo.isNotBlank()){
            // Subir directamente a Supabase sin guardar en lista local
            CoroutineScope(Dispatchers.IO).launch {
                subirASupabase(bitmap, nombreArchivo)
            }

            Toast.makeText(this, "Subiendo imagen al servidor...", Toast.LENGTH_LONG).show()
        } else {
            val mensaje = if (bitmap == null){
                "No hay ninguna imagen para guardar"
            } else {
                "Introduce un nombre para la imagen"
            }
            Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show()
        }
    }

    private fun guardarEnGaleria(bitmap: Bitmap?, nombreArchivo: String) {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, nombreArchivo)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
        }

        val uri = contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues
        )

        try{
            val outputStream = contentResolver.openOutputStream(uri!!)
            outputStream?.let {
                bitmap?.compress(Bitmap.CompressFormat.PNG, 100, it)
                it.close()
                Toast.makeText(this, "Imagen guardada en la galería", Toast.LENGTH_LONG).show()
            }
        } catch(e: IOException){
            e.printStackTrace()
            Toast.makeText(this, "Error al guardar la imagen", Toast.LENGTH_LONG).show()
        }
    }

    @Composable
    fun PantallaCamara(
        onAbrirCamara: () -> Unit,
        onAbrirGaleria: () -> Unit
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement =  Arrangement.Center
        ) {
            Button(onClick = onAbrirCamara){
                Text("Tomar foto")
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(onClick = onAbrirGaleria){
                Text("Abrir galería")
            }
        }
    }

    @Composable
    fun PantallaGaleria (
        fotos: List<Bitmap>,
        onVolver: () -> Unit,
        onSeleccionar: (Bitmap) -> Unit
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(10.dp)
        ) {
            Button(onClick = onVolver) {
                Text("Volver")
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text("Fotos")

            Spacer(modifier = Modifier.height(10.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxSize()
            ) {
                items(fotos) { img ->
                    Image(
                        bitmap = img.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.size(100.dp).padding(4.dp).clickable { onSeleccionar(img) }
                    )
                }
            }
        }
    }

    @Composable
    fun PantallaPreview(
        imagenBitmap: Bitmap?,
        nombreArchivo: String,
        onNombreChange: (String) -> Unit,
        onGuardar: () -> Unit,
        onVolver: () -> Unit
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(onClick = onVolver) {
                Text("Volver")
            }

            Spacer(modifier = Modifier.height(10.dp))

            imagenBitmap?.let {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.size(300.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = nombreArchivo,
                onValueChange = onNombreChange,
                label = { Text("Nombre del archivo") }
            )

            Spacer(modifier = Modifier.height(10.dp))

            Button(onClick = onGuardar) {
                Text("Guardar")
            }
        }
    }

    private suspend fun subirASupabase(bitmap: Bitmap, nombreArchivo: String) {
        try {
            // Convertir bitmap a archivo
            val file = convertBitmapToFile(bitmap, nombreArchivo)

            // Generar nombre único para el archivo
            val nombreUnico = "${UUID.randomUUID()}_$nombreArchivo.png"

            // Subir a Supabase Storage
            val urlImagen = subirAlBucket(file, nombreUnico)

            if (urlImagen != null) {
                // Guardar la URL en la base de datos
                guardarEnBaseDatos(urlImagen)
                Log.d("FotosActivity", "Imagen subida exitosamente: $urlImagen")

                // Ejecutar en Main para actualizar UI
                CoroutineScope(Dispatchers.Main).launch {
                    Toast.makeText(this@FotosActivity, "Imagen subida al servidor", Toast.LENGTH_LONG).show()
                    // Resetear estados
                    imagenBitmap.value = null
                    pantallaActual.value = PantallaActual.CAMARA
                }
            } else {
                Log.e("FotosActivity", "Error al subir la imagen")
                CoroutineScope(Dispatchers.Main).launch {
                    Toast.makeText(this@FotosActivity, "Error al subir la imagen", Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: Exception) {
            Log.e("FotosActivity", "Exception al subir imagen: ${e.message}", e)
            CoroutineScope(Dispatchers.Main).launch {
                Toast.makeText(this@FotosActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
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
            bucket.upload(nombreArchivo, file)

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
        } catch (e: Exception) {
            Log.e("FotosActivity", "Error guardando en BD: ${e.message}", e)
            throw e
        }
    }

    // Enum para controlar las pantallas
    enum class PantallaActual {
        CAMARA, GALERIA, PREVIEW
    }
}
