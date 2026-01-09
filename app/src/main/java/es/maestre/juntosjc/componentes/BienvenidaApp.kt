package es.maestre.juntosjc.componentes

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.maestre.juntosjc.MainActivity
import es.maestre.juntosjc.R

@Composable
fun GenerarComponentes(){

    val context = LocalContext.current

    Card(modifier = Modifier.padding(30.dp)){
        Column(
            modifier = Modifier.fillMaxSize(), // Ocupa toda la pantalla
            horizontalAlignment = Alignment.CenterHorizontally // centra todos los componentes que esten dentro
        ) {

            // Imagen principal
            Image(
                painter = painterResource(R.drawable.imagen_unidadtrabajo),
                contentDescription = stringResource(R.string.descripcion_imagenWorkUnit),
                modifier = Modifier.fillMaxWidth().height(300.dp),
                contentScale = ContentScale.Crop
            )

            // Esto pone un espacio entre los componentes
            Spacer(modifier = Modifier.height(130.dp))


            // Imagen con el logo
            Image(
                painter = painterResource(R.drawable.icono_tiendacampa_a),
                contentDescription = stringResource(R.string.descripcion_logo),
                modifier = Modifier.height(50.dp),
                contentScale = ContentScale.Crop
            )

            Text(
                text = stringResource(R.string.txt_Juntos),
                modifier = Modifier.padding(horizontal = 5.dp, vertical = 5.dp),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(100.dp))

            Text(
                text = stringResource(R.string.txt_pantallaBienvenida),
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                style = MaterialTheme.typography.titleLarge
            )


            Button(
                onClick = {
                    // intent a la mainActivity
                    val intent = Intent(context, MainActivity::class.java)
                    context.startActivity(intent) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(30.dp)
                    .height(55.dp)
            ) {
                Text(text = stringResource(R.string.txt_btn_pantallaBienvenida), fontSize = 18.sp)
            }
        }


    }

}