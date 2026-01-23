package es.maestre.juntosjc

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import es.maestre.juntosjc.viewModel.ComentarioViewModel
import es.maestre.juntosjc.viewModel.DocumentoViewModel
import es.maestre.juntosjc.ui.theme.JUNTOSJCTheme
import es.maestre.juntosjc.viewModel.ContactoViewModel
import es.maestre.juntosjc.viewModel.EventoViewModel
import es.maestre.juntosjc.viewModel.TareaViewModel
import kotlin.getValue

/**
 * Clase MainActivity: clase principal desde la que se llama a las
 * demás clases por medio de botones
 */
class MainActivity : ComponentActivity() {

    // instancias a los viewModels
    private val comentarioViewModel: ComentarioViewModel by viewModels()
    private val documentosViewModel: DocumentoViewModel by viewModels()
    private val tareasViewModel: TareaViewModel by viewModels()
    private val eventosViewModel: EventoViewModel by viewModels()
    private val contactoViewModel: ContactoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JUNTOSJCTheme {
                MyAppMain(modifier = Modifier.fillMaxSize())
                }
            }
        }
    }


/**
 * Función que llama a la función que crea los componentes de la Activity
 */
@Composable
fun MyAppMain(modifier: Modifier = Modifier) {
    GenerarComponentesMain()
}

/**
 * Funcion que me genera los componentes
 */
@Composable
fun GenerarComponentesMain() {
    val context = LocalContext.current

    // Contenedor principal
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.fondo))
            .padding(24.dp)
    ) {
        // CABECERA
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp, top = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.txt_Juntos),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = colorResource(R.color.azulOscuroLogo)
                )
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // Icono de perfil
                IconButton(onClick = { val intent = Intent(context, PerfilActivity::class.java)
                    context.startActivity(intent) }) {
                    Icon(
                        painter = painterResource(id = R.drawable.perfil),
                        contentDescription = stringResource(R.string.descripcion_btnPerfil_main),
                        modifier = Modifier.size(28.dp),
                        tint = colorResource(R.color.gris)
                    )
                }
                // Icono Ayuda (no implementado)
                IconButton(onClick = {
                    val intent = Intent(context, AyudaActivity::class.java)
                    context.startActivity(intent)
                }) {
                    Icon(
                        painter = painterResource(id = R.drawable.help_question_svgrepo_com),
                        contentDescription = stringResource(R.string.descripcion_btnAyuda_main),
                        modifier = Modifier.size(28.dp),
                        tint = Color.Unspecified
                    )
                }
            }
        }

        // BOTONES
        // Grid vertical de 3 columnas
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // BOTÓN CALENDARIO
            item {
                MenuIconItem(
                    title = stringResource(R.string.txt_calendario),
                    iconRes = R.drawable.calendar_svgrepo_com,
                    iconColor = Color.Unspecified
                ) {
                    val intent = Intent(context, CalendarioActivity::class.java)
                    context.startActivity(intent)
                }
            }

            // BOTÓN TAREAS
            item {
                MenuIconItem(
                    title = stringResource(R.string.txt_tareas),
                    iconRes = R.drawable.information_svgrepo_com,
                    iconColor = Color.Unspecified
                ) {
                    val intent = Intent(context, TareasActivity::class.java)
                    context.startActivity(intent)
                }
            }

            // BOTÓN DOCUMENTOS
            item {
                MenuIconItem(
                    title = stringResource(R.string.txt_btnDocs),
                    iconRes = R.drawable.favorite_file_svgrepo_com,
                    iconColor = Color.Unspecified
                ) {
                    val intent = Intent(context, DocumentoActivity::class.java)
                    context.startActivity(intent)
                }
            }

            // BOTÓN RED SOCIAL
            item {
                MenuIconItem(
                    title = stringResource(R.string.txt_redSocial),
                    iconRes = R.drawable.community_comments_svgrepo_com,
                    iconColor = Color.Unspecified
                ) {
                    val intent = Intent(context, RedSocialActivity::class.java)
                    context.startActivity(intent)
                }
            }

            // BOTÓN INVITAR
            item {
                MenuIconItem(
                    title = stringResource(R.string.txt_invitar),
                    iconRes = R.drawable.invite_friends_svgrepo_com,
                    iconColor = Color.Unspecified
                ) {
                    /*TODO invitar a otros usuarios*/
                }
            }

            // BOTÓN CONTACTOS
            item {
                MenuIconItem(
                    title = stringResource(R.string.txt_contactos),
                    iconRes = R.drawable.recruitment_svgrepo_com,
                    iconColor = Color.Unspecified
                ) {
                    val intent = Intent(context, ContactosActivity::class.java)
                    context.startActivity(intent)
                }
            }

            // BOTÓN FOTOS
            item {
                MenuIconItem(
                    title = stringResource(R.string.txt_fotos),
                    iconRes = R.drawable.photo_album_svgrepo_com,
                    iconColor = Color.Unspecified
                ) {
                    val intent = Intent(context, FotosActivity::class.java)
                    context.startActivity(intent)
                }
            }

            // BOTÓN CONFIGURACIÓN
            item {
                MenuIconItem(
                    title = stringResource(R.string.txt_configuracion),
                    iconRes = R.drawable.configuracion,
                    iconColor = Color.Unspecified
                ) {
                   /*TODO intent al configuracion Activity*/
                }
            }

        }
    }
}

/**
 * Componente individual para cada botón del menú
 */
@Composable
fun MenuIconItem(
    title: String,
    iconRes: Int,
    iconColor: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        // El cuadro blanco redondeado del icono
        Surface(
            modifier = Modifier
                .size(85.dp) // Tamaño del cuadro
                .aspectRatio(1f),
            shape = RoundedCornerShape(24.dp),
            color = colorResource(R.color.white),
            shadowElevation = 2.dp // Sombra sutil
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = title,
                    modifier = Modifier.size(35.dp),
                    tint = iconColor // Aquí aplicamos el color al icono
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Texto debajo del icono
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold,
                color = colorResource(R.color.txt_debajo_icnon_main),
                textAlign = TextAlign.Center
            ),
            maxLines = 2
        )
    }
}
