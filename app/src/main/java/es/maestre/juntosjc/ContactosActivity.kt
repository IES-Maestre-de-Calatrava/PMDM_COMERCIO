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
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import es.maestre.juntosjc.model.ContactoItem
import es.maestre.juntosjc.ui.theme.JUNTOSJCTheme
import es.maestre.juntosjc.viewModel.ContactoViewModel

/**
 * Clase ContactosActivity: lista de contactos (añadir, modificar, eliminar)
 */
class ContactosActivity : ComponentActivity() {

    private val viewModel: ContactoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JUNTOSJCTheme {
                MyAppContactos(viewModel = viewModel)
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
fun MyAppContactos(viewModel: ContactoViewModel) {
    val context = LocalContext.current

    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.obtenerContactosSupabase()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.txt_contactos),
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
    ) { paddingSobrante ->
        Column(
            modifier = Modifier.padding(paddingSobrante),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = {
                    val intent = Intent(context, DetalleContactoActivity::class.java).apply {
                        putExtra("ID_CONTACTO", 0)
                    }
                    context.startActivity(intent)
                },
                modifier = Modifier.height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(R.color.verde_esmeralda),
                    contentColor = Color.Unspecified
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.add_to_svgrepo_com),
                        contentDescription = stringResource(R.string.descripcion_btnCrear_detalle),
                        modifier = Modifier.size(24.dp),
                        tint = Color.Unspecified
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = stringResource(R.string.btn_Crear),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            ListaContactos(viewModel = viewModel)
        }
    }
}

@Composable
fun ListaContactos(viewModel: ContactoViewModel) {

    val listaContactos = viewModel.listaContactosSupabase
    val context = LocalContext.current

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
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Description,
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