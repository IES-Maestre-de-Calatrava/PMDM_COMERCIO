package es.maestre.juntosjc.viewModel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import es.maestre.juntosjc.model.AppFeature
import es.maestre.juntosjc.model.ThemeMode
import es.maestre.juntosjc.model.UserPreferences
import es.maestre.juntosjc.model.getEnabledFeatures
import es.maestre.juntosjc.model.getThemeMode
import es.maestre.juntosjc.supabase.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * ViewModel para gestionar las preferencias de usuario
 */
class UserPreferencesViewModel(application: Application) : AndroidViewModel(application) {

    private val TAG = "UserPreferencesVM"

    // Estado de las preferencias
    private val _preferences = MutableStateFlow<UserPreferences?>(null)
    val preferences: StateFlow<UserPreferences?> = _preferences.asStateFlow()

    // Estado del tema actual
    private val _isDarkTheme = MutableStateFlow(false)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    // Estado de carga
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Estado de error
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Mapa de funcionalidades habilitadas
    private val _enabledFeatures = MutableStateFlow<Map<AppFeature, Boolean>>(
        AppFeature.entries.associateWith { true }
    )
    val enabledFeatures: StateFlow<Map<AppFeature, Boolean>> = _enabledFeatures.asStateFlow()

    // Modo de tema actual
    private val _themeMode = MutableStateFlow(ThemeMode.AUTO)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    init {
        loadPreferences()
    }

    /**
     * Carga las preferencias del usuario desde Supabase
     */
    fun loadPreferences() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val prefs = UserPreferencesRepository.getUserPreferences()
                _preferences.value = prefs
                
                if (prefs != null) {
                    _enabledFeatures.value = prefs.getEnabledFeatures()
                    _themeMode.value = prefs.getThemeMode()
                    updateDarkTheme(prefs.getThemeMode())
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error cargando preferencias: ${e.message}")
                _error.value = "Error al cargar preferencias"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Actualiza el modo de tema
     */
    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            _isLoading.value = true
            
            val success = UserPreferencesRepository.updateThemeMode(mode.name)
            if (success) {
                _themeMode.value = mode
                updateDarkTheme(mode)
                
                // Actualizar preferencias locales
                _preferences.value = _preferences.value?.copy(themeMode = mode.name)
            } else {
                _error.value = "Error al actualizar tema"
            }
            
            _isLoading.value = false
        }
    }

    /**
     * Actualiza si se debe usar tema oscuro según el modo y la hora
     */
    private fun updateDarkTheme(mode: ThemeMode) {
        _isDarkTheme.value = when (mode) {
            ThemeMode.LIGHT -> false
            ThemeMode.DARK -> true
            ThemeMode.AUTO -> isNightTime()
        }
    }

    /**
     * Comprueba si es de noche (entre 20:00 y 6:00)
     */
    private fun isNightTime(): Boolean {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return hour < 6 || hour >= 20
    }

    /**
     * Recalcula el tema (útil cuando la app vuelve al primer plano)
     */
    fun recalculateTheme() {
        updateDarkTheme(_themeMode.value)
    }

    /**
     * Actualiza una funcionalidad específica
     */
    fun setFeatureEnabled(feature: AppFeature, enabled: Boolean) {
        viewModelScope.launch {
            _isLoading.value = true
            
            val columnName = UserPreferencesRepository.getFeatureColumnName(feature)
            val success = UserPreferencesRepository.updateFeature(columnName, enabled)
            
            if (success) {
                // Actualizar mapa local
                val updatedFeatures = _enabledFeatures.value.toMutableMap()
                updatedFeatures[feature] = enabled
                _enabledFeatures.value = updatedFeatures
                
                // Actualizar preferencias locales
                _preferences.value = _preferences.value?.let { prefs ->
                    when (feature) {
                        AppFeature.CALENDARIO -> prefs.copy(featureCalendario = if (enabled) 1 else 0)
                        AppFeature.TAREAS -> prefs.copy(featureTareas = if (enabled) 1 else 0)
                        AppFeature.DOCUMENTOS -> prefs.copy(featureDocumentos = if (enabled) 1 else 0)
                        AppFeature.RED_SOCIAL -> prefs.copy(featureRedSocial = if (enabled) 1 else 0)
                        AppFeature.INVITAR -> prefs.copy(featureInvitar = if (enabled) 1 else 0)
                        AppFeature.CONTACTOS -> prefs.copy(featureContactos = if (enabled) 1 else 0)
                        AppFeature.FOTOS -> prefs.copy(featureFotos = if (enabled) 1 else 0)
                    }
                }
            } else {
                _error.value = "Error al actualizar funcionalidad"
            }
            
            _isLoading.value = false
        }
    }

    /**
     * Habilita todas las funcionalidades
     */
    fun enableAllFeatures() {
        viewModelScope.launch {
            _isLoading.value = true
            
            val userId = UserPreferencesRepository.getCurrentUserId()
            if (userId != null) {
                val allEnabled = UserPreferences(
                    userId = userId,
                    featureCalendario = 1,
                    featureTareas = 1,
                    featureDocumentos = 1,
                    featureRedSocial = 1,
                    featureInvitar = 1,
                    featureContactos = 1,
                    featureFotos = 1
                )
                
                val success = UserPreferencesRepository.updateAllFeatures(allEnabled)
                if (success) {
                    _enabledFeatures.value = AppFeature.entries.associateWith { true }
                    loadPreferences() // Recargar para sincronizar
                }
            }
            
            _isLoading.value = false
        }
    }

    /**
     * Limpia el mensaje de error
     */
    fun clearError() {
        _error.value = null
    }
}
