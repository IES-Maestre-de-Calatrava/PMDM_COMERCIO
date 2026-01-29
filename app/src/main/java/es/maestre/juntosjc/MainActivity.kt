package es.maestre.juntosjc

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import es.maestre.juntosjc.model.AppFeature
import es.maestre.juntosjc.viewModel.ComentarioViewModel
import es.maestre.juntosjc.viewModel.DocumentoViewModel
import es.maestre.juntosjc.ui.theme.JUNTOSJCTheme
import es.maestre.juntosjc.ui.theme.JuntosTheme
import es.maestre.juntosjc.viewModel.EventoViewModel
import es.maestre.juntosjc.viewModel.TareaViewModel
import es.maestre.juntosjc.viewModel.AuthenticationViewModel
import es.maestre.juntosjc.viewModel.UserPreferencesViewModel
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
    private val authViewModel: AuthenticationViewModel by viewModels()
    private val preferencesViewModel: UserPreferencesViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isDarkTheme by preferencesViewModel.isDarkTheme.collectAsStateWithLifecycle()
            
            JUNTOSJCTheme(darkTheme = isDarkTheme) {
                MyAppMain(
                    modifier = Modifier.fillMaxSize(),
                    authViewModel = authViewModel,
                    preferencesViewModel = preferencesViewModel
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Recargar preferencias y recalcular tema al volver a la actividad
        preferencesViewModel.loadPreferences()
        preferencesViewModel.recalculateTheme()
    }
}


/**
 * Función que llama a la función que crea los componentes de la Activity
 */
@Composable
fun MyAppMain(
    modifier: Modifier = Modifier,
    authViewModel: AuthenticationViewModel,
    preferencesViewModel: UserPreferencesViewModel
) {
    GenerarComponentesMain(authViewModel, preferencesViewModel)
}

/**
 * Data class para definir los items del menú
 */
data class MenuItemData(
    val feature: AppFeature?,  // null para items fijos como Configuración
    val titleRes: Int,
    val iconRes: Int,
    val onClick: (android.content.Context) -> Unit
)

/**
 * Funcion que me genera los componentes
 */
@Composable
fun GenerarComponentesMain(
    authViewModel: AuthenticationViewModel,
    preferencesViewModel: UserPreferencesViewModel
) {
    val context = LocalContext.current
    val enabledFeatures by preferencesViewModel.enabledFeatures.collectAsStateWithLifecycle()
    val isLoading by preferencesViewModel.isLoading.collectAsStateWithLifecycle()

    // Launcher para recibir resultado de ConfiguracionActivity
    val configLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            preferencesViewModel.loadPreferences()
        }
    }

    // Definir todos los items del menú
    val allMenuItems = remember {
        listOf(
            MenuItemData(
                feature = AppFeature.CALENDARIO,
                titleRes = R.string.txt_calendario,
                iconRes = R.drawable.calendar_svgrepo_com,
                onClick = { ctx ->
                    ctx.startActivity(Intent(ctx, CalendarioActivity::class.java))
                }
            ),
            MenuItemData(
                feature = AppFeature.TAREAS,
                titleRes = R.string.txt_tareas,
                iconRes = R.drawable.information_svgrepo_com,
                onClick = { ctx ->
                    ctx.startActivity(Intent(ctx, TareasActivity::class.java))
                }
            ),
            MenuItemData(
                feature = AppFeature.DOCUMENTOS,
                titleRes = R.string.txt_btnDocs,
                iconRes = R.drawable.favorite_file_svgrepo_com,
                onClick = { ctx ->
                    ctx.startActivity(Intent(ctx, DocumentoActivity::class.java))
                }
            ),
            MenuItemData(
                feature = AppFeature.RED_SOCIAL,
                titleRes = R.string.txt_redSocial,
                iconRes = R.drawable.community_comments_svgrepo_com,
                onClick = { ctx ->
                    ctx.startActivity(Intent(ctx, RedSocialActivity::class.java))
                }
            ),
            MenuItemData(
                feature = AppFeature.INVITAR,
                titleRes = R.string.txt_invitar,
                iconRes = R.drawable.invite_friends_svgrepo_com,
                onClick = { ctx ->
                    invitar(ctx)
                }
            ),
            MenuItemData(
                feature = AppFeature.CONTACTOS,
                titleRes = R.string.txt_contactos,
                iconRes = R.drawable.recruitment_svgrepo_com,
                onClick = { ctx ->
                    ctx.startActivity(Intent(ctx, ContactosActivity::class.java))
                }
            ),
            MenuItemData(
                feature = AppFeature.FOTOS,
                titleRes = R.string.txt_fotos,
                iconRes = R.drawable.photo_album_svgrepo_com,
                onClick = { ctx ->
                    ctx.startActivity(Intent(ctx, FotosActivity::class.java))
                }
            ),
            // Configuración es FIJA - siempre visible
            MenuItemData(
                feature = null,  // null significa que es fijo
                titleRes = R.string.txt_configuracion,
                iconRes = R.drawable.configuracion,
                onClick = { ctx ->
                    val intent = Intent(ctx, ConfiguracionActivity::class.java)
                    configLauncher.launch(intent)
                }
            )
        )
    }

    // Filtrar items visibles: los fijos (feature == null) siempre, los demás según preferencias
    val visibleMenuItems = allMenuItems.filter { item ->
        item.feature == null || (enabledFeatures[item.feature] ?: true)
    }

    // Contenedor principal
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
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
                        color = JuntosTheme.colors.azulOscuroLogo
                    )
                )

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Icono Perfil (no implementado)
                    IconButton(onClick = { /* TODO */ }) {
                        Icon(
                            painter = painterResource(id = R.drawable.perfil),
                            contentDescription = stringResource(R.string.descripcion_btnPerfil_main),
                            modifier = Modifier.size(28.dp),
                            tint = JuntosTheme.colors.gris
                        )
                    }
                    // Icono Ayuda
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

                    // Icono cerrar sesión
                    IconButton(onClick = {
                        authViewModel.signOut()
                        context.startActivity(Intent(context, LoginActivity::class.java))
                        (context as? android.app.Activity)?.finish()
                    }) {
                        Icon(
                            painter = rememberVectorPainter(Icons.Default.ExitToApp),
                            contentDescription = stringResource(R.string.logout),
                            modifier = Modifier.size(28.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // BOTONES - Grid dinámico según preferencias
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(
                    items = visibleMenuItems,
                    key = { it.titleRes }
                ) { menuItem ->
                    MenuIconItem(
                        title = stringResource(menuItem.titleRes),
                        iconRes = menuItem.iconRes,
                        iconColor = Color.Unspecified
                    ) {
                        menuItem.onClick(context)
                    }
                }
            }
        }

        // Loading overlay
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

private fun invitar(ctx: Context) {
    val sendIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, "This is my text to send.")
        type = "text/plain"
    }

    val shareIntent = Intent.createChooser(sendIntent, null)
    ctx.startActivity(shareIntent)
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
        // El cuadro redondeado del icono - adaptado al tema
        Surface(
            modifier = Modifier
                .size(85.dp)
                .aspectRatio(1f),
            shape = RoundedCornerShape(24.dp),
            color = JuntosTheme.colors.cardBackground,
            shadowElevation = 2.dp
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = title,
                    modifier = Modifier.size(35.dp),
                    tint = iconColor
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Texto debajo del icono
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold,
                color = JuntosTheme.colors.txtDebajo,
                textAlign = TextAlign.Center
            ),
            maxLines = 2
        )
    }
}
