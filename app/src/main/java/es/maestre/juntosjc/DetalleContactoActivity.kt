package es.maestre.juntosjc

import android.os.Bundle
import android.util.Log
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
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
import es.maestre.juntosjc.model.ContactoItem
import es.maestre.juntosjc.viewModel.ContactoViewModel

import es.maestre.juntosjc.viewModel.UserPreferencesViewModel
import es.maestre.juntosjc.ui.theme.JUNTOSJCTheme
import es.maestre.juntosjc.ui.theme.JuntosTheme
import es.maestre.juntosjc.model.AppFeature

/**
 * Clase DetalleContactoActivity: añadir/modificar/borrar contacto
 */
class DetalleContactoActivity : ComponentActivity() {

    private val viewModel: ContactoViewModel by viewModels()

    private val preferencesViewModel: UserPreferencesViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val idContacto = intent.getIntExtra("ID_CONTACTO", -1)
        val nombreBackup = intent.getStringExtra("NOMBRE_CONTACTO") ?: ""
        val telefonoBackup = intent.getStringExtra("TELEFONO_CONTACTO") ?: ""
        val emailBackup = intent.getStringExtra("EMAIL_CONTACTO") ?: ""
        val empresaBackup = intent.getStringExtra("EMPRESA") ?: ""
        val direccionBackup = intent.getStringExtra("DIRECCION_CONTACTO") ?: ""

        enableEdgeToEdge()
        setContent {
            val isDarkTheme by preferencesViewModel.isDarkTheme.collectAsStateWithLifecycle()

            JUNTOSJCTheme (darkTheme = isDarkTheme) {
                val contactoActual = remember(idContacto) {
                    if (idContacto > 0) {
                        viewModel.listaContactosSupabase.find { it.id_contacto == idContacto }
                            ?: ContactoItem(idContacto, nombreBackup, telefonoBackup, emailBackup, empresaBackup, direccionBackup)
                    } else {
                        ContactoItem(null, "", "", "", "", "")
                    }
                }

                MyAppDetalleContacto(viewModel = viewModel, idContacto = idContacto, contactoRecibido = contactoActual,  preferencesViewModel = preferencesViewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyAppDetalleContacto(viewModel: ContactoViewModel, idContacto: Int, contactoRecibido: ContactoItem?,  preferencesViewModel: UserPreferencesViewModel) {
    val context = LocalContext.current

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
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(R.drawable.recruitment_svgrepo_com),
                    contentDescription = null,
                    modifier = Modifier.size(30.dp),
                    tint = Color.Unspecified

                )
                Spacer(modifier = Modifier.width(8.dp)) // Espacio entre texto e icono
                Text(
                    stringResource(R.string.txt_detalle),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = JuntosTheme.colors.azulOscuroLogo
                    )
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            contactoRecibido?.let { contacto ->
                CamposDetalleContacto(
                    contacto = contacto,
                    viewModel = viewModel,
                    esNuevo = idContacto <= 0,
                    onActionDone = { (context as ComponentActivity).finish() }
                )
            }
        }
    }
}

@Composable
fun CamposDetalleContacto(
    contacto: ContactoItem,
    viewModel: ContactoViewModel,
    esNuevo: Boolean,
    onActionDone: () -> Unit
) {
    var nombre by remember { mutableStateOf(contacto.nombre_contacto) }
    var telefono by remember { mutableStateOf(contacto.telefono_contacto) }
    var email by remember { mutableStateOf(contacto.email_contacto) }
    var empresa by remember { mutableStateOf(contacto.empresa.orEmpty()) }
    var direccion by remember { mutableStateOf(contacto.direccion_contacto) }

    Log.d("DetalleContacto", "Cargando contacto: $contacto Empresa: ${contacto.empresa}")

    Text(text = stringResource(R.string.lb_nombreUsuario), fontWeight = FontWeight.Bold)
    OutlinedTextField(
        value = nombre,
        onValueChange = { nombre = it },
        modifier = Modifier.fillMaxWidth(),
        label = { Text(stringResource(R.string.lbNombre)) }
    )

    Text(text = stringResource(R.string.lb_telefono), fontWeight = FontWeight.Bold)
    OutlinedTextField(
        value = telefono,
        onValueChange = { telefono = it },
        modifier = Modifier.fillMaxWidth(),
        label = { Text(stringResource(R.string.lb_telefono)) },
        minLines = 1
    )

    Text(text = stringResource(R.string.lb_email), fontWeight = FontWeight.Bold)
    OutlinedTextField(
        value = email,
        onValueChange = { email = it },
        modifier = Modifier.fillMaxWidth(),
        label = { Text(stringResource(R.string.lb_email)) },
        minLines = 1
    )
    Text(text = stringResource(R.string.lb_empresa), fontWeight = FontWeight.Bold)
    OutlinedTextField(
        value = empresa,
        onValueChange = { empresa = it },
        modifier = Modifier.fillMaxWidth(),
        label = { Text(stringResource(R.string.lb_empresa)) },
        minLines = 1
    )

    Text(text = stringResource(R.string.lb_direccion), fontWeight = FontWeight.Bold)
    OutlinedTextField(
        value = direccion,
        onValueChange = { direccion = it },
        modifier = Modifier.fillMaxWidth(),
        label = { Text(stringResource(R.string.lb_direccion)) },
        minLines = 1
    )

    Button(
        onClick = {
            val nuevoItem = ContactoItem(
                id_contacto = if (esNuevo) null else contacto.id_contacto,
                nombre_contacto = nombre,
                telefono_contacto = telefono,
                email_contacto = email,
                empresa = empresa,
                direccion_contacto = direccion
            )
            Log.d("DetalleContacto", "Guardando contacto: $nuevoItem Empresa: ${nuevoItem.empresa}")
            if (esNuevo) {
                viewModel.insertarContactoSupabase(nuevoItem) { onActionDone() }
            } else {
                viewModel.actualizarContactoSupabase(nuevoItem) { onActionDone() }
            }
        },
        modifier = Modifier.fillMaxWidth().height(50.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = colorResource(R.color.azul_pastel),
            contentColor = colorResource(R.color.white)
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.save_svgrepo_com),
                contentDescription = stringResource(R.string.descripcion_btnGuardar_detalle),
                modifier = Modifier.size(24.dp),
                tint = Color.Unspecified
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(text = stringResource(R.string.btn_Guardar), style = MaterialTheme.typography.titleMedium)
        }
    }

    if (!esNuevo) {
        Button(
            onClick = {
                contacto.id_contacto?.let { idSeguro ->
                    viewModel.borrarContactoSupabase(idSeguro) { onActionDone() }
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorResource(R.color.rojo_pastel),
                contentColor = colorResource(R.color.white)
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.trash_svgrepo_com),
                    contentDescription = stringResource(R.string.descripcion_btnEliminar_detalle),
                    modifier = Modifier.size(24.dp),
                    tint = Color.Unspecified
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(text = stringResource(R.string.btn_Eliminar), style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}