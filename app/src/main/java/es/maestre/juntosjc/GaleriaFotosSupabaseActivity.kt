package es.maestre.juntosjc

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import es.maestre.juntosjc.model.FotoCamaraItem
import es.maestre.juntosjc.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest

class GaleriaFotosSupabaseActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GaleriaFotosScreen(
                onVolverAtras = { finish() }
            )
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun GaleriaFotosScreen(onVolverAtras: () -> Unit) {

        var fotos by remember { mutableStateOf<List<FotoCamaraItem>>(emptyList()) }
        var isLoading by remember { mutableStateOf(true) }
        var errorMessage by remember { mutableStateOf<String?>(null) }

        LaunchedEffect(Unit) {
            try {
                val fotosCargadas = SupabaseClient.client.postgrest
                    .from("fotosCamara")
                    .select()
                    .decodeList<FotoCamaraItem>()

                Log.d("GaleriaFotos", "Fotos cargadas: ${fotosCargadas.size}")
                fotos = fotosCargadas
                isLoading = false

            } catch (e: Exception) {
                Log.e("GaleriaFotos", "Error cargando fotos", e)
                errorMessage = e.message
                isLoading = false
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Galería de Fotos",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onVolverAtras) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF6200EE),
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White
                    )
                )
            }
        ) { paddingValues ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color(0xFFF5F5F5))
            ) {

                when {
                    isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    errorMessage != null -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Error al cargar las fotos\n$errorMessage",
                                color = Color.Red
                            )
                        }
                    }

                    fotos.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No hay fotos en la galería",
                                color = Color.Gray
                            )
                        }
                    }

                    else -> {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(fotos) { foto ->
                                FotoGridItem(foto)
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun FotoGridItem(foto: FotoCamaraItem) {
        val context = androidx.compose.ui.platform.LocalContext.current

        Box(
            modifier = Modifier
                .aspectRatio(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.LightGray)
                .clickable {
                    Log.d("GaleriaFotos", "Click: ${foto.urlImagen}")
                }
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(foto.urlImagen)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                onSuccess = {
                    Log.d("GaleriaFotos", "Imagen OK: ${foto.urlImagen}")
                },
                onError = {
                    Log.e("GaleriaFotos", "Error imagen: ${foto.urlImagen}", it.result.throwable)
                }
            )
        }
    }
}
