package es.maestre.juntosjc.supabase

import android.util.Log
import es.maestre.juntosjc.model.UserPreferences
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns

/**
 * Repositorio para gestionar las preferencias de usuario en Supabase
 * Tabla: configuracion
 */
object UserPreferencesRepository {

    private const val TABLE_NAME = "configuracion"
    private const val TAG = "UserPreferencesRepo"

    /**
     * Obtiene el ID del usuario actual autenticado
     */
    fun getCurrentUserId(): String? {
        return SupabaseClient.client.auth.currentUserOrNull()?.id
    }

    /**
     * Obtiene las preferencias del usuario actual
     * Si no existen, las crea con valores por defecto
     */
    suspend fun getUserPreferences(): UserPreferences? {
        val userId = getCurrentUserId() ?: return null
        
        return try {
            val result = SupabaseClient.client.postgrest[TABLE_NAME]
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeSingleOrNull<UserPreferences>()
            
            // Si no existe, crear preferencias por defecto
            if (result == null) {
                val defaultPrefs = UserPreferences(userId = userId)
                createUserPreferences(defaultPrefs)
            } else {
                result
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error obteniendo preferencias: ${e.message}")
            // Si hay error, intentar crear las preferencias
            try {
                val defaultPrefs = UserPreferences(userId = userId)
                createUserPreferences(defaultPrefs)
            } catch (e2: Exception) {
                Log.e(TAG, "Error creando preferencias por defecto: ${e2.message}")
                null
            }
        }
    }

    /**
     * Crea las preferencias del usuario
     */
    suspend fun createUserPreferences(preferences: UserPreferences): UserPreferences? {
        return try {
            SupabaseClient.client.postgrest[TABLE_NAME]
                .insert(preferences) {
                    select()
                }
                .decodeSingle<UserPreferences>()
        } catch (e: Exception) {
            Log.e(TAG, "Error creando preferencias: ${e.message}")
            null
        }
    }

    /**
     * Actualiza el modo de tema del usuario
     */
    suspend fun updateThemeMode(themeMode: String): Boolean {
        val userId = getCurrentUserId() ?: return false
        
        return try {
            SupabaseClient.client.postgrest[TABLE_NAME]
                .update({
                    set("theme_mode", themeMode)
                }) {
                    filter {
                        eq("user_id", userId)
                    }
                }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error actualizando tema: ${e.message}")
            false
        }
    }

    /**
     * Actualiza una funcionalidad específica
     */
    suspend fun updateFeature(featureColumn: String, enabled: Boolean): Boolean {
        val userId = getCurrentUserId() ?: return false
        
        return try {
            SupabaseClient.client.postgrest[TABLE_NAME]
                .update({
                    set(featureColumn, if (enabled) 1 else 0)
                }) {
                    filter {
                        eq("user_id", userId)
                    }
                }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error actualizando feature $featureColumn: ${e.message}")
            false
        }
    }

    /**
     * Actualiza todas las funcionalidades a la vez
     */
    suspend fun updateAllFeatures(preferences: UserPreferences): Boolean {
        val userId = getCurrentUserId() ?: return false
        
        return try {
            SupabaseClient.client.postgrest[TABLE_NAME]
                .update({
                    set("feature_calendario", preferences.featureCalendario)
                    set("feature_tareas", preferences.featureTareas)
                    set("feature_documentos", preferences.featureDocumentos)
                    set("feature_red_social", preferences.featureRedSocial)
                    set("feature_invitar", preferences.featureInvitar)
                    set("feature_contactos", preferences.featureContactos)
                    set("feature_fotos", preferences.featureFotos)
                }) {
                    filter {
                        eq("user_id", userId)
                    }
                }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error actualizando features: ${e.message}")
            false
        }
    }

    /**
     * Mapa de AppFeature a nombre de columna en la base de datos
     */
    fun getFeatureColumnName(feature: es.maestre.juntosjc.model.AppFeature): String {
        return when (feature) {
            es.maestre.juntosjc.model.AppFeature.CALENDARIO -> "feature_calendario"
            es.maestre.juntosjc.model.AppFeature.TAREAS -> "feature_tareas"
            es.maestre.juntosjc.model.AppFeature.DOCUMENTOS -> "feature_documentos"
            es.maestre.juntosjc.model.AppFeature.RED_SOCIAL -> "feature_red_social"
            es.maestre.juntosjc.model.AppFeature.INVITAR -> "feature_invitar"
            es.maestre.juntosjc.model.AppFeature.CONTACTOS -> "feature_contactos"
            es.maestre.juntosjc.model.AppFeature.FOTOS -> "feature_fotos"
        }
    }
}
