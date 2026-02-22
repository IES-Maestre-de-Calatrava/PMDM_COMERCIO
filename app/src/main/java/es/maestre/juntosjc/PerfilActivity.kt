package es.maestre.juntosjc

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import es.maestre.juntosjc.model.Ayuda
import es.maestre.juntosjc.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import java.io.InputStream

import es.maestre.juntosjc.viewModel.UserPreferencesViewModel
import es.maestre.juntosjc.ui.theme.JUNTOSJCTheme
import es.maestre.juntosjc.ui.theme.JuntosTheme
import es.maestre.juntosjc.model.AppFeature
import kotlin.getValue

@Serializable
data class PerfilRow(
    val id: String? = null,
    val email: String? = null,
    val nombre: String? = null,
    val apellido: String? = null,
    val edad: Int? = null,
    val curso: String? = null,
    val icono: String? = null
)

class PerfilActivity : ComponentActivity() {

    private val preferencesViewModel: UserPreferencesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isDarkTheme by preferencesViewModel.isDarkTheme.collectAsStateWithLifecycle()
            JUNTOSJCTheme (darkTheme = isDarkTheme) {
                PantallaPerfil( preferencesViewModel = preferencesViewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaPerfil(preferencesViewModel: UserPreferencesViewModel) {
    var showDialog by remember { mutableStateOf(false) }

    // Estados para los datos del perfil
    var nombreUsuario by remember { mutableStateOf("Usuario") }
    var nombre by remember { mutableStateOf("") }
    var apellido by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var edad by remember { mutableStateOf("") }
    var curso by remember { mutableStateOf("") }
    var fotoPerfil by remember { mutableStateOf<String?>(null) }
    var perfilId by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val context = androidx.compose.ui.platform.LocalContext.current
    val dataStoreManager = DataStoreManager(context)

    LaunchedEffect(Unit) {
        isLoading = true
        errorMessage = null
        try {
            val savedEmail = try {
                dataStoreManager.getEmail().first()
            } catch (e: Exception) {
                null
            }

            val currentUser = SupabaseClient.client.auth.currentUserOrNull()
            val userEmail = savedEmail ?: currentUser?.email

            if (userEmail != null) {
                try {
                    val response = SupabaseClient.client.from("perfiles")
                        .select(columns = Columns.list("id", "email", "nombre", "apellido", "edad", "curso", "icono")) {
                            filter { eq("email", userEmail) }
                        }

                    val rows = response.decodeList<PerfilRow>()
                    val row = rows.firstOrNull()
                    
                    if (row != null) {
                        perfilId = row.id
                        nombre = row.nombre.orEmpty()
                        apellido = row.apellido.orEmpty()
                        email = row.email.orEmpty()
                        curso = row.curso.orEmpty()
                        edad = row.edad?.toString().orEmpty()
                        fotoPerfil = row.icono

                        nombreUsuario = listOf(row.nombre.orEmpty(), row.apellido.orEmpty())
                            .filter { it.isNotBlank() }
                            .joinToString(" ")
                            .ifBlank { "Usuario" }

                        errorMessage = null
                    } else {
                        errorMessage = "No se encontró perfil para: $userEmail"
                    }
                } catch (e: Exception) {
                    errorMessage = "Error al procesar datos de Supabase.\n\nVerifica la estructura de la tabla."
                }
            } else {
                errorMessage = "No se pudo obtener el email del usuario.\n\n¿Has iniciado sesión correctamente?"
            }
        } catch (e: Exception) {
            errorMessage = "Error inesperado: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    Scaffold(
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
                    IconButton(onClick = { showDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar perfil",
                            tint = JuntosTheme.colors.content
                        )
                    }
                    IconButton(onClick = {
                        val intent = Intent(context, AyudaActivity::class.java)
                        intent.putExtra("SECCION", Ayuda.PERFIL)
                        context.startActivity(intent)
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.help_question_svgrepo_com),
                            contentDescription = "Ayuda",
                            modifier = Modifier.size(28.dp),
                            tint = Color.Unspecified
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(R.drawable.user_profile),
                    contentDescription = null,
                    modifier = Modifier.size(30.dp),
                    tint = Color.Unspecified
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Perfil",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = JuntosTheme.colors.azulOscuroLogo
                    )
                )
            }
            if (isLoading) {
                // Mostrar indicador de carga
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    androidx.compose.material3.CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Cargando perfil...")
                }
            } else if (errorMessage != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Error",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = errorMessage ?: "",
                        textAlign = TextAlign.Center,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Imagen de perfil circular
                            Surface(
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(CircleShape)
                                    .border(3.dp, Color.White, CircleShape),
                                color = Color.Gray.copy(alpha = 0.3f)
                            ) {
                                if (fotoPerfil != null) {
                                    AsyncImage(
                                        model = fotoPerfil,
                                        contentDescription = "Foto de perfil",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = "Sin foto",
                                            modifier = Modifier.size(50.dp),
                                            tint = Color.White
                                        )
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Información del perfil
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (email.isNotEmpty()) {
                            InfoRow(label = "Email:", value = email)
                        }
                        if (nombre.isNotEmpty()) {
                            InfoRow(label = "Nombre:", value = nombre)
                        }
                        if (apellido.isNotEmpty()) {
                            InfoRow(label = "Apellidos:", value = apellido)
                        }
                        if (edad.isNotEmpty()) {
                            InfoRow(label = "Edad:", value = edad)
                        }
                        if (curso.isNotEmpty()) {
                            InfoRow(label = "Curso:", value = curso)
                        }

                        // Mensaje si no hay datos
                        if (nombre.isEmpty() && apellido.isEmpty() && edad.isEmpty() && curso.isEmpty()) {
                            Spacer(modifier = Modifier.height(32.dp))
                            Text(
                                text = "No hay datos de perfil disponibles.\nCompleta tu perfil editándolo.",
                                textAlign = TextAlign.Center,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal para editar
    if (showDialog) {
        ModalEditarPerfil(
            nombreActual = nombre,
            apellidoActual = apellido,
            edadActual = edad,
            cursoActual = curso,
            fotoActual = fotoPerfil,
            perfilId = perfilId,
            onDismiss = { showDialog = false },
            onGuardar = { nuevoNombre, nuevoApellido, nuevaEdad, nuevoCurso, nuevaFoto ->
                nombre = nuevoNombre
                apellido = nuevoApellido
                edad = nuevaEdad
                curso = nuevoCurso
                fotoPerfil = nuevaFoto

                nombreUsuario = if (nuevoNombre.isNotEmpty() && nuevoApellido.isNotEmpty()) {
                    "$nuevoNombre $nuevoApellido"
                } else if (nuevoNombre.isNotEmpty()) {
                    nuevoNombre
                } else {
                    "Usuario"
                }
                showDialog = false

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val currentUser = SupabaseClient.client.auth.currentUserOrNull()
                        val savedEmail = dataStoreManager.getEmail().first()
                        val userEmail = savedEmail ?: currentUser?.email

                        if (userEmail != null) {
                            SupabaseClient.client.from("perfiles").update({
                                set("nombre", nuevoNombre)
                                set("apellido", nuevoApellido)
                                set("edad", nuevaEdad.toIntOrNull())
                                set("curso", nuevoCurso)
                                nuevaFoto?.let { set("icono", it) }
                            }) {
                                filter { eq("email", userEmail) }
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("PerfilActivity", "Error al actualizar datos: ${e.message}")
                    }
                }
            }
        )
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$label ",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray
        )
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun ModalEditarPerfil(

    nombreActual: String,
    apellidoActual: String,
    edadActual: String,
    cursoActual: String,
    fotoActual: String?,
    perfilId: String?,
    onDismiss: () -> Unit,
    onGuardar: (String, String, String, String, String?) -> Unit
) {
    var nombre by remember { mutableStateOf(nombreActual) }
    var email by remember { mutableStateOf("") }
    var apellido by remember { mutableStateOf(apellidoActual) }
    var edad by remember { mutableStateOf(edadActual) }
    var curso by remember { mutableStateOf(cursoActual) }
    var fotoPerfil by remember { mutableStateOf(fotoActual) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var isUploadingImage by remember { mutableStateOf(false) }
    
    val context = LocalContext.current

    // Launcher para seleccionar imagen de galería
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
        uri?.let {
            isUploadingImage = true
            // Subir imagen a Supabase Storage
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    perfilId?.let { id ->
                        if (fotoActual != null) {
                            try {
                                val fileName = fotoActual.substringAfterLast("/")
                                SupabaseClient.client.storage.from("IconosPerfiles").delete(fileName)
                            } catch (e: Exception) {
                                // Continúa aunque no se pueda borrar la imagen anterior
                            }
                        }

                        val fileName = "perfil_$id.jpg"
                        val inputStream: InputStream = context.contentResolver.openInputStream(uri)!!
                        val bytes = inputStream.readBytes()
                        
                        SupabaseClient.client.storage.from("IconosPerfiles").upload(fileName, bytes) {
                            upsert = true
                        }
                        
                        val publicUrl = SupabaseClient.client.storage.from("IconosPerfiles").publicUrl(fileName)
                        fotoPerfil = publicUrl
                    }
                } catch (e: Exception) {
                    // Error al subir imagen - se mantiene la anterior
                } finally {
                    isUploadingImage = false
                }
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Editar Perfil",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Sección de foto de perfil
                Text(
                    text = "Foto de perfil",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Gray
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box {
                        Surface(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .clickable { imagePickerLauncher.launch("image/*") }
                                .border(2.dp, colorResource(R.color.azul_pastel), CircleShape),
                            color = Color.Gray.copy(alpha = 0.2f)
                        ) {
                            if (imageUri != null || fotoPerfil != null) {
                                AsyncImage(
                                    model = imageUri ?: fotoPerfil,
                                    contentDescription = "Foto de perfil",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PhotoCamera,
                                        contentDescription = "Cambiar foto",
                                        modifier = Modifier.size(30.dp),
                                        tint = colorResource(R.color.azul_pastel)
                                    )
                                }
                            }
                        }
                        
                        if (isUploadingImage) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.5f)),
                                contentAlignment = Alignment.Center
                            ) {
                                androidx.compose.material3.CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Toca la imagen para cambiarla",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false
                )

                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = apellido,
                    onValueChange = { apellido = it },
                    label = { Text("Apellido") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = edad,
                    onValueChange = { edad = it },
                    label = { Text("Edad") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = curso,
                    onValueChange = { curso = it },
                    label = { Text("Curso") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onGuardar(nombre, apellido, edad, curso, fotoPerfil) },
                enabled = !isUploadingImage,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(R.color.azul_pastel)
                )
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

