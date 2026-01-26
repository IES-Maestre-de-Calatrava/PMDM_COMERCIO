package es.maestre.juntosjc.viewModel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.maestre.juntosjc.supabase.SupabaseClient
import es.maestre.juntosjc.supabase.auth.AuthenticationRepository
import es.maestre.juntosjc.supabase.auth.AuthenticationRepositoryImpl
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


// esto es un viewModel que se encarga de la autenticación en la pantalla de login
class AuthenticationViewModel (application: Application): AndroidViewModel(application)  {


    // Repositorio de autenticación
    private val authRepo = AuthenticationRepositoryImpl

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState


    // inicia sesión con email y contraseña  y actualiza el estado.
    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            val success = authRepo.signIn(email, password)
            _loginState.value = if (success) LoginState.Success else LoginState.Error("Error al iniciar sesión")
        }
    }
    // registra un nuevo usuario con email y contraseña y actualiza el estado.
    fun signUp(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            val success = authRepo.signUp(email, password)
            _loginState.value = if (success) LoginState.Success else LoginState.Error("Error al registrarse")
        }
    }

    fun signOut() {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            val success = authRepo.signOut()
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