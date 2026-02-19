package es.maestre.juntosjc

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import es.maestre.juntosjc.model.AppFeature
import es.maestre.juntosjc.model.Ayuda
import es.maestre.juntosjc.model.ThemeMode
import es.maestre.juntosjc.ui.theme.JUNTOSJCTheme
import es.maestre.juntosjc.ui.theme.JuntosTheme
import es.maestre.juntosjc.viewModel.UserPreferencesViewModel

class ConfiguracionActivity : ComponentActivity() {

    private val preferencesViewModel: UserPreferencesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            val isDarkTheme by preferencesViewModel.isDarkTheme.collectAsStateWithLifecycle()
            
            JUNTOSJCTheme(darkTheme = isDarkTheme) {
                ConfiguracionScreen(
                    viewModel = preferencesViewModel,
                    onBack = { 
                        // Al volver, enviamos resultado para que MainActivity recargue
                        setResult(RESULT_OK)
                        finish() 
                    }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Recalcular tema por si ha cambiado la hora
        preferencesViewModel.recalculateTheme()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfiguracionScreen(
    viewModel: UserPreferencesViewModel,
    onBack: () -> Unit
) {
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val enabledFeatures by viewModel.enabledFeatures.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var showThemeDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
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
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.btn_volver)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val intent = Intent(context, AyudaActivity::class.java)
                        intent.putExtra("SECCION", Ayuda.CONFIGURACION)
                        context.startActivity(intent)
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.help_question_svgrepo_com),
                            contentDescription = "Ayuda",
                            modifier = Modifier.size(28.dp),
                            tint = Color.Unspecified
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
            )
        }
    ) { paddingValues ->
        // CAMBIO CLAVE: Usamos Column en lugar de Box para evitar la superposición
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Cabecera de la sección (Icono + CONFIGURACIÓN)
            Row(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.configuracion),
                    contentDescription = null,
                    modifier = Modifier.size(30.dp),
                    tint = Color.Unspecified
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.txt_configuracion),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = JuntosTheme.colors.azulOscuroLogo
                    )
                )
            }

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp))

            // El contenido de la configuración
            Box(modifier = Modifier.weight(1f)) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp), // Padding interno para la lista
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // SECCIÓN: APARIENCIA
                    item {
                        SettingsSection(title = stringResource(R.string.settings_appearance)) {
                            SettingsItem(
                                icon = Icons.Default.Palette,
                                title = stringResource(R.string.settings_theme),
                                subtitle = when (themeMode) {
                                    ThemeMode.LIGHT -> stringResource(R.string.theme_light)
                                    ThemeMode.DARK -> stringResource(R.string.theme_dark)
                                    ThemeMode.AUTO -> stringResource(R.string.theme_auto)
                                },
                                onClick = { showThemeDialog = true }
                            )
                        }
                    }

                    // SECCIÓN: FUNCIONALIDADES
                    item {
                        Text(
                            text = stringResource(R.string.settings_features),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                        Text(
                            text = stringResource(R.string.settings_features_description),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    items(AppFeature.entries) { feature ->
                        FeatureToggleItem(
                            feature = feature,
                            isEnabled = enabledFeatures[feature] ?: true,
                            onToggle = { enabled ->
                                viewModel.setFeatureEnabled(feature, enabled)
                            }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = { viewModel.enableAllFeatures() },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.settings_reset_features))
                        }
                    }

                    item { Spacer(modifier = Modifier.height(32.dp)) }
                }

                // El loading se mantiene en un Box para poder centrarlo sobre la lista
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }

    if (showThemeDialog) {
        ThemeSelectionDialog(
            currentTheme = themeMode,
            onThemeSelected = { mode ->
                viewModel.setThemeMode(mode)
                showThemeDialog = false
            },
            onDismiss = { showThemeDialog = false }
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp
        ) {
            Column {
                content()
            }
        }
    }
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FeatureToggleItem(
    feature: AppFeature,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isEnabled)
            MaterialTheme.colorScheme.surface
        else
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        label = "featureBackground"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        color = backgroundColor,
        tonalElevation = if (isEnabled) 1.dp else 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = getFeatureIcon(feature)),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(feature.displayNameRes),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = if (isEnabled)
                        MaterialTheme.colorScheme.onSurface
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(feature.descriptionRes),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = isEnabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}


@Composable
private fun getFeatureIcon(feature: AppFeature): Int {
    return when (feature) {
        AppFeature.CALENDARIO -> R.drawable.calendar_svgrepo_com
        AppFeature.TAREAS -> R.drawable.information_svgrepo_com
        AppFeature.DOCUMENTOS -> R.drawable.favorite_file_svgrepo_com
        AppFeature.RED_SOCIAL -> R.drawable.community_comments_svgrepo_com
        AppFeature.INVITAR -> R.drawable.invite_friends_svgrepo_com
        AppFeature.CONTACTOS -> R.drawable.recruitment_svgrepo_com
        AppFeature.FOTOS -> R.drawable.photo_album_svgrepo_com
    }
}

@Composable
private fun ThemeSelectionDialog(
    currentTheme: ThemeMode,
    onThemeSelected: (ThemeMode) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.settings_theme),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                ThemeOption(
                    title = stringResource(R.string.theme_light),
                    subtitle = stringResource(R.string.theme_light_desc),
                    icon = Icons.Default.LightMode,
                    isSelected = currentTheme == ThemeMode.LIGHT,
                    onClick = { onThemeSelected(ThemeMode.LIGHT) }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                ThemeOption(
                    title = stringResource(R.string.theme_dark),
                    subtitle = stringResource(R.string.theme_dark_desc),
                    icon = Icons.Default.DarkMode,
                    isSelected = currentTheme == ThemeMode.DARK,
                    onClick = { onThemeSelected(ThemeMode.DARK) }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                ThemeOption(
                    title = stringResource(R.string.theme_auto),
                    subtitle = stringResource(R.string.theme_auto_desc),
                    icon = Icons.Default.BrightnessAuto,
                    isSelected = currentTheme == ThemeMode.AUTO,
                    onClick = { onThemeSelected(ThemeMode.AUTO) }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.btn_Cancelar))
            }
        }
    )
}

@Composable
private fun ThemeOption(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) 
            MaterialTheme.colorScheme.primaryContainer 
        else 
            MaterialTheme.colorScheme.surface,
        tonalElevation = if (isSelected) 2.dp else 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) 
                    MaterialTheme.colorScheme.onPrimaryContainer 
                else 
                    MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected)
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
