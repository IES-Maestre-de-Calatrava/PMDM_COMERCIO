package es.maestre.juntosjc.componentes

import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.maestre.juntosjc.MainActivity
import es.maestre.juntosjc.R

@Composable
fun GenerarComponentesMain() {

    //val context = LocalContext.current

    Card(modifier = Modifier.padding(30.dp)) {
        Row(
            modifier = Modifier.fillMaxSize(), // Ocupa toda la pantalla
        ){
            Column(modifier = Modifier.padding(15.dp)) {
                Text(
                    text = stringResource(R.string.txt_Juntos),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                    style = MaterialTheme.typography.titleLarge
                )
            }

            Column (modifier = Modifier.padding(20.dp)){
                Button(
                    onClick = {
                    // intent al perfil TODO
                    //val intent = Intent(context, PerfilActivity::class.java)
                    //context.startActivity(intent)
                    },
                    modifier = Modifier
                        .height(35.dp)
                ) {
                    Icon(painter = painterResource(id = R.drawable.perfil),
                        contentDescription = stringResource(id=R.string.descripcion_btnPerfil_main),
                        modifier = Modifier.size(24.dp))
                    }
            }

            Column (modifier = Modifier.padding(20.dp)){
                Button(
                    onClick = {
                        // intent al ayuda TODO
                        //val intent = Intent(context, AyudaActivity::class.java)
                        //context.startActivity(intent)
                    },
                    modifier = Modifier
                        .height(35.dp)
                ) {
                    Icon(painter = painterResource(id = R.drawable.ayudar),
                        contentDescription = stringResource(id=R.string.descripcion_btnPerfil_main),
                        modifier = Modifier.size(24.dp))
                }
            }

        }


        // Una imagen
        Row(
            modifier = Modifier.fillMaxSize(), // Ocupa toda la pantalla
        ){}


        // Fila con los botones de red social y calendario
        Row(
            modifier = Modifier.fillMaxSize(), // Ocupa toda la pantalla
        ){}


        // Fila con los botones de tareas y documentos
        Row(
            modifier = Modifier.fillMaxSize(), // Ocupa toda la pantalla
        ){}

        

    }
}