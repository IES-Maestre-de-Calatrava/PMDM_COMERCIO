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


import es.maestre.juntosjc.componentes.GenerarComponentesMain
import es.maestre.juntosjc.ui.theme.JUNTOSJCTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JUNTOSJCTheme {
                MyAppMain(modifier = Modifier.fillMaxSize())
                }
            }
        }
    }


@Composable
fun MyAppMain(modifier: Modifier = Modifier) {
    GenerarComponentesMain()
}

