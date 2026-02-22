package es.maestre.juntosjc.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

/**
 * Esquema de colores oscuros personalizado para JUNTOS
 * Basado en la gama cromática del modo claro (azules y violetas)
 */
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF9EAEFF),           // Azul claro para elementos primarios
    onPrimary = Color(0xFF00297A),         // Texto sobre primario
    primaryContainer = Color(0xFF1E3A7A),  // Contenedor primario oscuro
    onPrimaryContainer = Color(0xFFDAE2FF),// Texto sobre contenedor primario
    
    secondary = Color(0xFFBBC6E4),         // Secundario suave
    onSecondary = Color(0xFF253048),       // Texto sobre secundario
    secondaryContainer = Color(0xFF3B4760),// Contenedor secundario
    onSecondaryContainer = Color(0xFFD7E2FF),
    
    tertiary = Color(0xFFE4BADA),          // Rosa/violeta suave (del logo)
    onTertiary = Color(0xFF432740),
    tertiaryContainer = Color(0xFF5B3D57),
    onTertiaryContainer = Color(0xFFFFD7F3),
    
    background = Color(0xFF111318),        // Fondo muy oscuro
    onBackground = Color(0xFFE2E2E9),      // Texto sobre fondo
    
    surface = Color(0xFF1B1B21),           // Superficie ligeramente más clara
    onSurface = Color(0xFFE2E2E9),         // Texto sobre superficie
    surfaceVariant = Color(0xFF44464F),    // Variante de superficie
    onSurfaceVariant = Color(0xFFC5C6D0),  // Texto sobre variante
    
    outline = Color(0xFF8F909A),           // Bordes
    outlineVariant = Color(0xFF44464F),    // Bordes suaves
    
    error = Color(0xFFFFB4AB),             // Error
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    
    inverseSurface = Color(0xFFE2E2E9),
    inverseOnSurface = Color(0xFF2F3036),
    inversePrimary = Color(0xFF3F5AA9)
)

/**
 * Esquema de colores claros personalizado para JUNTOS
 * Mantiene los colores originales de la app
 */
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF3F5AA9),           // Azul corporativo
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDAE2FF),  // container original
    onPrimaryContainer = Color(0xFF001946),
    
    secondary = Color(0xFF585E71),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDCE2F9),
    onSecondaryContainer = Color(0xFF151B2C),
    
    tertiary = Color(0xFF735572),          // Violeta del logo
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFED7F9),
    onTertiaryContainer = Color(0xFF2A132C),
    
    background = Color(0xFFF4F7FD),        // fondo original
    onBackground = Color(0xFF1B1B1F),
    
    surface = Color.White,
    onSurface = Color(0xFF1B1B1F),
    surfaceVariant = Color(0xFFE1E2EC),
    onSurfaceVariant = Color(0xFF44464F),
    
    outline = Color(0xFF757780),
    outlineVariant = Color(0xFFC5C6D0),
    
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    
    inverseSurface = Color(0xFF2F3036),
    inverseOnSurface = Color(0xFFF2F0F4),
    inversePrimary = Color(0xFFB0C6FF)
)

/**
 * Colores personalizados adicionales de JUNTOS que no están en MaterialTheme
 */
data class JuntosColors(
    val azulOscuroLogo: Color,
    val rosita: Color,
    val naranjita: Color,
    val txtDebajo: Color,
    val gris: Color,
    val container: Color,
    val content: Color,
    val fondo: Color,
    val cardBackground: Color,
    val switchTrackOn: Color,
    val switchTrackOff: Color,
    val text: Color
)

val LightJuntosColors = JuntosColors(
    azulOscuroLogo = Color(0xFF333366),
    rosita = Color(0xFFFF6699),
    naranjita = Color(0xFFFF9800),
    txtDebajo = Color(0xFF4A4A68),
    gris = Color(0xFFD3D3D3),
    container = Color(0xFFDAE2FF),
    content = Color(0xFF314578),
    fondo = Color(0xFFF4F7FD),
    cardBackground = Color.White,
    switchTrackOn = Color(0xFF3F5AA9),
    switchTrackOff = Color(0xFFD3D3D3),
    text = Color(0xFF000000)
)

val DarkJuntosColors = JuntosColors(
    azulOscuroLogo = Color(0xFF9EAEFF),
    rosita = Color(0xFFFFB4C6),
    naranjita = Color(0xFFFFB74D),
    txtDebajo = Color(0xFFC5C6D0),
    gris = Color(0xFF5A5A5A),
    container = Color(0xFF1E3A7A),
    content = Color(0xFFDAE2FF),
    fondo = Color(0xFF111318),
    cardBackground = Color(0xFF1B1B21),
    switchTrackOn = Color(0xFF9EAEFF),
    switchTrackOff = Color(0xFF44464F),
    text = Color(0XFFFFFFFF)
)

val LocalJuntosColors = staticCompositionLocalOf { LightJuntosColors }

/**
 * Acceso fácil a los colores personalizados de JUNTOS
 */
object JuntosTheme {
    val colors: JuntosColors
        @Composable
        get() = LocalJuntosColors.current
}

@Composable
fun JUNTOSJCTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color deshabilitado para usar nuestros colores personalizados
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val juntosColors = if (darkTheme) DarkJuntosColors else LightJuntosColors

    CompositionLocalProvider(LocalJuntosColors provides juntosColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}