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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import okio.IOException

class FotosActivity : ComponentActivity() {
    // Estados
    private val imagenBitmap = mutableStateOf<Bitmap?>(null)
    private val nombreArchivo = mutableStateOf("")

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
            }
        }
        setContent {
            PantallaCamara(
                imagenBitmap = imagenBitmap.value,
                nombreArchivo = nombreArchivo.value,
                onNombreChange = { nombreArchivo.value = it },
                onAbrirCamara = {
                    pedirPermiso.launch(android.Manifest.permission.CAMERA)
                },
                onAbrirGaleria = {
                    escoger.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                onGuardar = {
                    guardarImagen(imagenBitmap.value, nombreArchivo.value)
                }
            )
        }
    }

    // Guardar imagen
    private fun guardarImagen(bitmap: Bitmap?, nombreArchivo: String) {
        if(bitmap != null && nombreArchivo.isNotBlank()){
            guardarEnGaleria(bitmap, nombreArchivo)
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
        imagenBitmap: Bitmap?,
        nombreArchivo: String,
        onNombreChange: (String) -> Unit,
        onAbrirCamara: () -> Unit,
        onAbrirGaleria: () -> Unit,
        onGuardar: () -> Unit
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement =  Arrangement.Top
        ) {
            Button(onClick = onAbrirCamara){
                Text("Abrir cámara")
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(onClick = onAbrirGaleria){
                Text("Abrir galería")
            }

            Spacer(modifier = Modifier.height(10.dp))

            imagenBitmap?.let {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = "Imagen seleccionada",
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
                Text("Guardar imagen")
            }
        }
    }
}
