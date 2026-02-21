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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import coil.compose.AsyncImage
import es.maestre.juntosjc.model.Ayuda
import es.maestre.juntosjc.viewModel.ComentarioViewModel
import es.maestre.juntosjc.model.ComentarioItem
import kotlin.getValue
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.lazy.items
import coil.compose.AsyncImage
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle

import es.maestre.juntosjc.viewModel.UserPreferencesViewModel
import es.maestre.juntosjc.ui.theme.JUNTOSJCTheme
import es.maestre.juntosjc.ui.theme.JuntosTheme
import es.maestre.juntosjc.model.AppFeature


/**
 * Clase RedSocialActivity: en esta clase se muestra una LazyColumn con
 * los comentarios. Se puede añadir comentarios, visualizarlos, modificarlo y borrarlos
 */
class RedSocialActivity : ComponentActivity() {

    // instancio mi viewModel para el acceso a BBDD
    private val viewModel: ComentarioViewModel by viewModels()

    private val preferencesViewModel: UserPreferencesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Pido los datos al abrir
        viewModel.obtenerComentariosSupabase()

        setContent {
            val isDarkTheme by preferencesViewModel.isDarkTheme.collectAsStateWithLifecycle()

            JUNTOSJCTheme (darkTheme = isDarkTheme) {
                MyAppRedSocial(
                    viewModel = viewModel,
                    preferencesViewModel = preferencesViewModel
                )
            }
        }
    }
    override fun onResume() {
        super.onResume()
        // Cada vez que la pantalla vuelve a estar visible, pedimos los datos
        viewModel.obtenerComentariosSupabase()
    }
}

/**
 * Funcion que me genera la cabecera de la Activity con su nombre correspondiente
 * y me llama a la funcion que me lista los datos
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyAppRedSocial(viewModel: ComentarioViewModel, preferencesViewModel: UserPreferencesViewModel) {
    val context = LocalContext.current

    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.obtenerComentariosSupabase()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
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
                    IconButton(onClick = {
                        val intent = Intent(context, AyudaActivity::class.java)
                        intent.putExtra("SECCION", Ayuda.SOCIAL)
                        context.startActivity(intent)
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.help_question_svgrepo_com),
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
                    val intent = Intent(context, DetalleComentarioActivity::class.java).apply {
                        putExtra("ID_COMENTARIO", 0)
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
                    painter = painterResource(R.drawable.community_comments_svgrepo_com),
                    contentDescription = null,
                    modifier = Modifier.size(30.dp),
                    tint = Color.Unspecified

                )
                Spacer(modifier = Modifier.width(8.dp)) // Espacio entre texto e icono
                Text(
                    stringResource(R.string.txt_redSocial),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = JuntosTheme.colors.azulOscuroLogo
                    )
                )
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            ListaComentarios(viewModel = viewModel)
        }
    }
}

/**
 * Funcion que carga los items de la BBDD en el LazyColumn
 */
@Composable
fun ListaComentarios(viewModel: ComentarioViewModel) {
    val listaComentarios = viewModel.listaComentariosSupabase
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(listaComentarios) { comentario ->
            ComentarioItem(
                comentario = comentario,
                onClick = {
                    val intent = Intent(context, DetalleComentarioActivity::class.java).apply {
                        putExtra("ID_COMENTARIO", comentario.id_comentario ?: -1)
                        putExtra("NOMBRE_USUARIO", comentario.nombre_usuario)
                        putExtra("TEXTO", comentario.texto)
                        putExtra("TITULO", comentario.titulo)
                        putExtra("HORA", comentario.hora)
                        putExtra("EMAIL_USUARIO", comentario.email_usuario)
                        putExtra("FECHA_PUBLICACION", comentario.fecha_publicacion)
                    }
                    context.startActivity(intent)
                }
            )
        }
    }
}


/**
 * Funcion que establece la estructura de cada uno de los items que
 * se cargan en el LazyColumn
 */
@Composable
fun ComentarioItem(comentario: ComentarioItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = JuntosTheme.colors.container)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Imagen de perfil circular
            androidx.compose.material3.Surface(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                color = colorResource(R.color.grisOscuro)
            ) {
                // Comprobamos si el modelo tiene icono, si no, ponemos el de Person
                if (!comentario.icono_usuario.isNullOrEmpty()) {
                    AsyncImage(
                        model = comentario.icono_usuario,
                        contentDescription = "Foto de perfil",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Sin foto",
                        modifier = Modifier.padding(8.dp),
                        tint = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = comentario.nombre_usuario,
                    style = MaterialTheme.typography.titleLarge,
                    color = JuntosTheme.colors.content
                )
                Text(
                    text = comentario.texto,
                    style = MaterialTheme.typography.titleSmall,
                    color = JuntosTheme.colors.content
                )
                Text(
                    text = " ${comentario.fecha_publicacion}  Hora: ${comentario.hora}",
                    style = MaterialTheme.typography.titleSmall,
                    color = JuntosTheme.colors.content
                )
            }
        }
    }
}