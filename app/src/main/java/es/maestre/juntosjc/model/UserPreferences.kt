package es.maestre.juntosjc.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Modelo de datos para las preferencias de usuario almacenadas en Supabase
 * Tabla: configuracion
 */
@Serializable
data class UserPreferences(
    @SerialName("id")
    val id: String? = null,
    
    @SerialName("user_id")
    val userId: String,
    
    @SerialName("theme_mode")
    val themeMode: String = ThemeMode.AUTO.name,
    
    @SerialName("feature_calendario")
    val featureCalendario: Int = 1,
    
    @SerialName("feature_tareas")
    val featureTareas: Int = 1,
    
    @SerialName("feature_documentos")
    val featureDocumentos: Int = 1,
    
    @SerialName("feature_red_social")
    val featureRedSocial: Int = 1,
    
    @SerialName("feature_invitar")
    val featureInvitar: Int = 1,
    
    @SerialName("feature_contactos")
    val featureContactos: Int = 1,
    
    @SerialName("feature_fotos")
    val featureFotos: Int = 1,
    
    @SerialName("created_at")
    val createdAt: String? = null,
    
    @SerialName("updated_at")
    val updatedAt: String? = null
)

/**
 * Enum para los modos de tema disponibles
 */
enum class ThemeMode {
    LIGHT,  // Siempre claro
    DARK,   // Siempre oscuro
    AUTO    // Automático según hora del día (6:00-20:00 claro, resto oscuro)
}

/**
 * Enum para identificar las funcionalidades de la app
 */
enum class AppFeature(val displayNameRes: Int, val descriptionRes: Int) {
    CALENDARIO(es.maestre.juntosjc.R.string.txt_calendario, es.maestre.juntosjc.R.string.feature_calendario_desc),
    TAREAS(es.maestre.juntosjc.R.string.txt_tareas, es.maestre.juntosjc.R.string.feature_tareas_desc),
    DOCUMENTOS(es.maestre.juntosjc.R.string.txt_btnDocs, es.maestre.juntosjc.R.string.feature_documentos_desc),
    RED_SOCIAL(es.maestre.juntosjc.R.string.txt_redSocial, es.maestre.juntosjc.R.string.feature_red_social_desc),
    INVITAR(es.maestre.juntosjc.R.string.txt_invitar, es.maestre.juntosjc.R.string.feature_invitar_desc),
    CONTACTOS(es.maestre.juntosjc.R.string.txt_contactos, es.maestre.juntosjc.R.string.feature_contactos_desc),
    FOTOS(es.maestre.juntosjc.R.string.txt_fotos, es.maestre.juntosjc.R.string.feature_fotos_desc)
}

/**
 * Extension function para convertir UserPreferences a un mapa de features habilitadas
 */
fun UserPreferences.getEnabledFeatures(): Map<AppFeature, Boolean> {
    return mapOf(
        AppFeature.CALENDARIO to (featureCalendario == 1),
        AppFeature.TAREAS to (featureTareas == 1),
        AppFeature.DOCUMENTOS to (featureDocumentos == 1),
        AppFeature.RED_SOCIAL to (featureRedSocial == 1),
        AppFeature.INVITAR to (featureInvitar == 1),
        AppFeature.CONTACTOS to (featureContactos == 1),
        AppFeature.FOTOS to (featureFotos == 1)
    )
}

/**
 * Extension function para obtener el ThemeMode desde el string
 */
fun UserPreferences.getThemeMode(): ThemeMode {
    return try {
        ThemeMode.valueOf(themeMode)
    } catch (e: Exception) {
        ThemeMode.AUTO
    }
}
