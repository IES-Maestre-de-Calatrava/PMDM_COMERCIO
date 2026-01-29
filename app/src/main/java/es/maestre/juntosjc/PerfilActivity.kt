package es.maestre.juntosjc

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.maestre.juntosjc.ui.theme.JUNTOSJCTheme
import es.maestre.juntosjc.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
data class PerfilRow(
    val email: String? = null,
    val nombre: String? = null,
    val apellido: String? = null,
    val edad: Int? = null,
    val curso: String? = null
)

class PerfilActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JUNTOSJCTheme {
                PantallaPerfil()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaPerfil() {
    var showDialog by remember { mutableStateOf(false) }

    // Estados para los datos del perfil
    var nombreUsuario by remember { mutableStateOf("Usuario") }
    var nombre by remember { mutableStateOf("") }
    var apellido by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var edad by remember { mutableStateOf("") }
    var curso by remember { mutableStateOf("") }

    // Cargar datos desde Supabase al entrar en la pantalla
    LaunchedEffect(Unit) {
        try {
            val currentUser = SupabaseClient.client.auth.currentUserOrNull()
            val userEmail = currentUser?.email
            val uid = currentUser?.id

            Log.d("PerfilActivity", "Usuario autenticado - Email: $userEmail, UID: $uid")

            if (userEmail != null) {
                Log.d("PerfilActivity", "Buscando perfil con email: $userEmail")

                val response = SupabaseClient.client.from("perfiles")
                    .select {
                        filter { eq("email", userEmail) }
                    }

                Log.d("PerfilActivity", "Respuesta de Supabase recibida")

                val rows = response.decodeList<PerfilRow>()
                Log.d("PerfilActivity", "Número de filas encontradas: ${rows.size}")

                val row = rows.firstOrNull()
                if (row != null) {
                    Log.d("PerfilActivity", "Datos cargados: nombre=${row.nombre}, apellido=${row.apellido}, email=${row.email}, edad=${row.edad}, curso=${row.curso}")

                    nombre = row.nombre.orEmpty()
                    apellido = row.apellido.orEmpty()
                    email = row.email.orEmpty()
                    curso = row.curso.orEmpty()
                    edad = row.edad?.toString().orEmpty()

                    nombreUsuario = listOf(row.nombre.orEmpty(), row.apellido.orEmpty())
                        .filter { it.isNotBlank() }
                        .joinToString(" ")
                        .ifBlank { "Usuario" }

                    Log.d("PerfilActivity", "Datos asignados correctamente a la UI")
                } else {
                    Log.w("PerfilActivity", "No se encontró ningún registro con el email: $userEmail")
                    Log.w("PerfilActivity", "Verifica que existe un registro en la tabla 'perfiles' con este email")
                }
            } else {
                Log.e("PerfilActivity", "No hay usuario autenticado o no tiene email")
            }
        } catch (e: Exception) {
            Log.e("PerfilActivity", "Error al cargar datos del perfil", e)
            Log.e("PerfilActivity", "Tipo de error: ${e.javaClass.simpleName}")
            Log.e("PerfilActivity", "Mensaje de error: ${e.message}")
            e.printStackTrace()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Perfil", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorResource(R.color.container),
                    titleContentColor = colorResource(R.color.content)
                ),
                actions = {
                    IconButton(onClick = { showDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar perfil",
                            tint = colorResource(R.color.content)
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colorResource(R.color.azul_corporativo))
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = nombreUsuario,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
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
                if (nombre.isNotEmpty()) {
                    InfoRow(label = "Nombre:", value = nombre)
                }
                if (apellido.isNotEmpty()) {
                    InfoRow(label = "Apellidos:", value = apellido)
                }
                if (email.isNotEmpty()) {
                    InfoRow(label = "Email:", value = email)
                }
                if (edad.isNotEmpty()) {
                    InfoRow(label = "Edad:", value = edad)
                }
                if (curso.isNotEmpty()) {
                    InfoRow(label = "Curso:", value = curso)
                }
            }
        }
    }

    // Modal para editar
    if (showDialog) {
        ModalEditarPerfil(
            nombreActual = nombre,
            apellidoActual = apellido,
            emailActual = email,
            edadActual = edad,
            cursoActual = curso,
            onDismiss = { showDialog = false },
            onGuardar = { nuevoNombre, nuevoApellido, nuevoEmail, nuevaEdad, nuevoCurso ->
                Log.d("PerfilActivity", "Guardando cambios: nombre=$nuevoNombre, apellido=$nuevoApellido")

                nombre = nuevoNombre
                apellido = nuevoApellido
                email = nuevoEmail
                edad = nuevaEdad
                curso = nuevoCurso

                // Actualizar nombre de usuario con nombre y apellido
                nombreUsuario = if (nuevoNombre.isNotEmpty() && nuevoApellido.isNotEmpty()) {
                    "$nuevoNombre $nuevoApellido"
                } else if (nuevoNombre.isNotEmpty()) {
                    nuevoNombre
                } else {
                    "Usuario"
                }
                showDialog = false

                // Actualizar en Supabase
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val currentUser = SupabaseClient.client.auth.currentUserOrNull()
                        val userEmail = currentUser?.email

                        Log.d("PerfilActivity", "Actualizando en Supabase para email: $userEmail")

                        if (userEmail != null) {
                            SupabaseClient.client.from("perfiles").update({
                                set("nombre", nuevoNombre)
                                set("apellido", nuevoApellido)
                                set("edad", nuevaEdad.toIntOrNull())
                                set("curso", nuevoCurso)
                            }) {
                                filter { eq("email", userEmail) }
                            }
                            Log.d("PerfilActivity", "Datos actualizados correctamente en Supabase")
                        } else {
                            Log.e("PerfilActivity", "No se pudo obtener el email del usuario")
                        }
                    } catch (e: Exception) {
                        Log.e("PerfilActivity", "Error al actualizar datos en Supabase", e)
                        Log.e("PerfilActivity", "Tipo de error: ${e.javaClass.simpleName}")
                        Log.e("PerfilActivity", "Mensaje de error: ${e.message}")
                        e.printStackTrace()
                    }
                }
            }
        )
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ModalEditarPerfil(
    nombreActual: String,
    apellidoActual: String,
    emailActual: String,
    edadActual: String,
    cursoActual: String,
    onDismiss: () -> Unit,
    onGuardar: (String, String, String, String, String) -> Unit
) {
    var nombre by remember { mutableStateOf(nombreActual) }
    var apellido by remember { mutableStateOf(apellidoActual) }
    var email by remember { mutableStateOf(emailActual) }
    var edad by remember { mutableStateOf(edadActual) }
    var curso by remember { mutableStateOf(cursoActual) }

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
                    value = email,
                    onValueChange = { }, // No permitir cambios
                    label = { Text("Email") },
                    enabled = false, // Deshabilitado
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
                onClick = { onGuardar(nombre, apellido, email, edad, curso) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(R.color.azul_corporativo)
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

