package es.maestre.juntosjc

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import es.maestre.juntosjc.model.Ayuda
import es.maestre.juntosjc.model.ContactoItem
import es.maestre.juntosjc.viewModel.ContactoViewModel

import es.maestre.juntosjc.viewModel.UserPreferencesViewModel
import es.maestre.juntosjc.ui.theme.JUNTOSJCTheme
import es.maestre.juntosjc.ui.theme.JuntosTheme
import es.maestre.juntosjc.model.AppFeature

/**
 * Clase ContactosActivity: lista de contactos (añadir, modificar, eliminar)
 */
class ContactosActivity : ComponentActivity() {

    private val viewModel: ContactoViewModel by viewModels()

    private val preferencesViewModel: UserPreferencesViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isDarkTheme by preferencesViewModel.isDarkTheme.collectAsStateWithLifecycle()

            JUNTOSJCTheme(darkTheme = isDarkTheme) {
                MyAppContactos(
                    viewModel = viewModel,
                    preferencesViewModel = preferencesViewModel
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.obtenerContactosSupabase()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyAppContactos(viewModel: ContactoViewModel, preferencesViewModel: UserPreferencesViewModel) {
    val context = LocalContext.current

    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.obtenerContactosSupabase()
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
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    IconButton(onClick = {
                        val intent = Intent(context, AyudaActivity::class.java)
                        intent.putExtra("SECCION", Ayuda.CONTACTOS)
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
        },

        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    val intent = Intent(context, DetalleContactoActivity::class.java).apply {
                        putExtra("ID_CONTACTO", 0)
                    }
                    context.startActivity(intent)
                },
                containerColor = colorResource(R.color.white),
                contentColor = Color.Unspecified
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.add_to_svgrepo_com),
                    contentDescription = stringResource(R.string.btn_Crear)
                )
            }
        }

    ) { paddingSobrante ->
        Column(
            modifier = Modifier
                .padding(paddingSobrante)
                .fillMaxSize()
                .padding(16.dp)
        ) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(R.drawable.recruitment_svgrepo_com),
                    contentDescription = null,
                    modifier = Modifier.size(30.dp),
                    tint = Color.Unspecified
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.txt_contactos),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = JuntosTheme.colors.azulOscuroLogo
                    )
                )

            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
            // === BARRA DE BÚSQUEDA ===
            BarraBusquedaContactos(viewModel = viewModel)

            // === LISTA DE CONTACTOS (ya ordenada y filtrada) ===
            ListaContactos(viewModel = viewModel)
        }
    }
}

@Composable
fun BarraBusquedaContactos(viewModel: ContactoViewModel) {
    OutlinedTextField(
        value = viewModel.filtro,
        onValueChange = { viewModel.actualizarFiltro(it) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        placeholder = { Text("Buscar contacto...") },
        shape = MaterialTheme.shapes.medium,
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Buscar"
            )
        },
        trailingIcon = {
            if (viewModel.filtro.isNotEmpty()) {
                IconButton(onClick = { viewModel.actualizarFiltro("") }) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Limpiar búsqueda"
                    )
                }
            }
        },
        singleLine = true
    )
}

@Composable
fun ListaContactos(viewModel: ContactoViewModel) {

    val listaContactos = viewModel.listaContactosSupabase
    val context = LocalContext.current

    if (listaContactos.isEmpty() && viewModel.filtro.isNotEmpty()) {
        Text(
            text = "No se encontraron contactos",
            modifier = Modifier.padding(32.dp),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        items(listaContactos) { contacto ->
            ContactoItemRow(
                contacto = contacto,
                onClick = {
                    val intent = Intent(context, DetalleContactoActivity::class.java).apply {
                        putExtra("ID_CONTACTO", contacto.id_contacto ?: -1)
                        putExtra("NOMBRE_CONTACTO", contacto.nombre_contacto)
                        putExtra("TELEFONO_CONTACTO", contacto.telefono_contacto)
                        putExtra("EMAIL_CONTACTO", contacto.email_contacto)
                        putExtra("DIRECCION_CONTACTO", contacto.direccion_contacto)
                    }
                    context.startActivity(intent)
                }
            )
        }
    }
}

@Composable
fun ContactoItemRow(contacto: ContactoItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = colorResource(R.color.white))
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Contacts,
                contentDescription = "Icono Contacto",
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = contacto.nombre_contacto,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = contacto.telefono_contacto,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}