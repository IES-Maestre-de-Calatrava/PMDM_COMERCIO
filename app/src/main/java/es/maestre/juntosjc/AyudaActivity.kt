package es.maestre.juntosjc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import es.maestre.juntosjc.ui.theme.JUNTOSJCTheme

class AyudaActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JUNTOSJCTheme {

            }
        }
    }
}

@Composable
fun AyudaPrincipal(navController: NavController){

}

@Composable
fun AyudaCalendario(navController: NavController){

}

@Composable
fun AyudaTareas(navController: NavController){

}

@Composable
fun AyudaDocumentos(navController: NavController){

}

@Composable
fun AyudaRedSocial(navController: NavController){
    
}