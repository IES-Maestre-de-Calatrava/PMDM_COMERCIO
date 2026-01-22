package es.maestre.juntosjc.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel

class AuthenticationViewModel(application: Application) : AndroidViewModel(application) {

    fun onSignUp(email : String , password : String) : Boolean{
        // TODO implementar
        return false
    }

    fun onSignIn(email : String , password : String) : Boolean{
        // TODO implementar
        return false
    }

}