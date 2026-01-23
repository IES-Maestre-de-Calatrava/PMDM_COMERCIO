package es.maestre.juntosjc

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import es.maestre.juntosjc.viewModel.DocumentoViewModel
import es.maestre.juntosjc.ui.theme.JUNTOSJCTheme

/**
 * Clase DocumentoActivity: en esta clase se muestra dos botones, uno para subir
 * documentos y otro par descargarlos. Subir elementos, guarda el archivo en supabase
 * Storage y recupera la ruta en un objeto documento que se guarda en la BBDD. Descargar documentos,
 * muestra la LazyColumn con los documentos, para descargarlos.
 */
class DocumentoActivity: ComponentActivity(){

    // instancio mi viewModel para el acceso a BBDD
    private val viewModel: DocumentoViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()


        // El launcher se debe declarar en la activity, no en una funcion
        val pickImageLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->

            uri?.let {
                // Obtener el tipo MIME del archivo seleccionado
                val mimeType = contentResolver.getType(it)

                // Convertir URI a ByteArray para subirlo
                val inputStream = contentResolver.openInputStream(it)
                val byteArray = inputStream?.readBytes()

                if (byteArray != null) {

                    val extension = mimeType?.substringAfterLast('/', "jpg") ?: "jpg"

                    // Llamar a la función de subida (puedes mostrar un ProgressBar aquí)
                    viewModel.subirImagen(
                        byteArray = byteArray,
                        extension = extension, // extension del archivo
                        onSuccess = { url ->

                            Toast.makeText(this, "Archivo subido correctamente", Toast.LENGTH_SHORT).show()

                        },

                        onError = { e ->

                            Toast.makeText(this, "Error al subir: ${e.message}", Toast.LENGTH_LONG).show()

                        }

                    )

                }

            }

        }




        setContent {
            JUNTOSJCTheme {
                MyAppDocumentos(
                        onPickDocument = {
                            pickImageLauncher.launch(
                                arrayOf(
                                    "image/*",
                                    "application/pdf",
                                    "application/msword",
                                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                                ) // array con los tipos que se van a poder subir al supabase
                            )
                        }
                )
            }
        }
    }
}

/**
 * Funcion que me genera la cabecera de la Activity con su nombre correspondiente
 * y me llama a la funcion que me genera los componentes del Activity
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable fun MyAppDocumentos(onPickDocument: () -> Unit) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            // Cabecera
            TopAppBar(
                title = {
                    Text(
                        text =  stringResource(R.string.txt_documentos),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorResource(R.color.container),
                    titleContentColor = colorResource(R.color.content)
                )
            )
        }
    ) { innerPadding ->
        // Aplicamos innerPadding para que el contenido empiece debajo de la TopAppBar
        Column(modifier = Modifier.padding(innerPadding)) {
            GenerarComponentesDocumento(onPickDocument)
        }
    }
}

/**
 * Funcion que me genera el layout de la Activity
 */
@Composable
fun GenerarComponentesDocumento(
    onPickDocument: () -> Unit
){

    val context = LocalContext.current

    Card(
        modifier = Modifier.padding(30.dp).fillMaxWidth()
    ) {

        Column(
            modifier = Modifier.padding(20.dp).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // Boton de subir documento
            Button(
                onClick = { onPickDocument() },
                modifier = Modifier.fillMaxWidth().height(45.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.upload_minimalistic_svgrepo_com),
                    contentDescription = stringResource(R.string.descripcion_subirDocumento),
                    modifier = Modifier.size(24.dp),
                    tint = Color.Unspecified
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = stringResource(R.string.btn_subirDoc),
                    style = MaterialTheme.typography.titleMedium
                )
            }

            // Boton para visualizar las descargas , hace un intent a una nueva actividad llamada ArchivoActivity
            Button(
                onClick = {
                    val intent = Intent(context, ArchivoActivity::class.java)
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth().height(45.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.descargar),
                    contentDescription = stringResource(R.string.descripcion_descargarDocumento),
                    modifier = Modifier.size(24.dp),
                    tint = Color.Unspecified
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = stringResource(R.string.btn_descargarDoc),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}
