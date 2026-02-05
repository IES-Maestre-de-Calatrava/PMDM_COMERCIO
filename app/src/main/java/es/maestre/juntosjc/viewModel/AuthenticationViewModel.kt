package es.maestre.juntosjc.viewModel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import es.maestre.juntosjc.DataStoreManager
import es.maestre.juntosjc.supabase.auth.AuthenticationRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


// esto es un viewModel que se encarga de la autenticación en la pantalla de login
class AuthenticationViewModel (application: Application): AndroidViewModel(application)  {


    // Repositorio de autenticación
    private val authRepo = AuthenticationRepositoryImpl
    private val dataStoreManager = DataStoreManager(application.applicationContext)

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState


    // inicia sesión con email y contraseña  y actualiza el estado.
    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            val success = authRepo.signIn(email, password)
            if (success) {
                // guardar email en DataStore
                try {
                    dataStoreManager.saveEmail(email)
                } catch (e: Exception) {
                    Log.e("AuthViewModel", "Error guardando email en DataStore: ${e.message}")
                }
            }
            _loginState.value = if (success) LoginState.Success else LoginState.Error("Error al iniciar sesión")
        }
    }
    // registra un nuevo usuario con email y contraseña y actualiza el estado.
    fun signUp(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            val success = authRepo.signUp(email, password)
            if (success) {
                // guardar email en DataStore
                try {
                    dataStoreManager.saveEmail(email)
                } catch (e: Exception) {
                    Log.e("AuthViewModel", "Error guardando email en DataStore: ${e.message}")
                }
            }
            _loginState.value = if (success) LoginState.Success else LoginState.Error("Error al registrarse")
        }
    }

    fun signOut() {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            val success = authRepo.signOut()
            if (success) {
                try {
                    dataStoreManager.clearEmail()
                } catch (e: Exception) {
                    Log.e("AuthViewModel", "Error limpiando email en DataStore: ${e.message}")
                }
            }
            _loginState.value = if (success) LoginState.Idle else LoginState.Error("Error al cerrar sesión")
        }
    }

    fun resetState() {
        _loginState.value = LoginState.Idle
    }


}
// estos son los estados en los que puede estar el login
sealed class LoginState {
    object Idle : LoginState() // sin accion
    object Loading : LoginState()// cargando
    object Success : LoginState()// completado con exito
    data class Error(val message: String) : LoginState() // y finalizada con error
}