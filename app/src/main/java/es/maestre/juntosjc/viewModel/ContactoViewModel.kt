package es.maestre.juntosjc.viewModel

import android.app.Application
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import es.maestre.juntosjc.model.ContactoItem
import es.maestre.juntosjc.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel de los contactos
 */
class ContactoViewModel(application: Application) : AndroidViewModel(application) {

    private val _listaContactosCompleta = mutableStateListOf<ContactoItem>()
    val listaContactosSupabase = mutableStateListOf<ContactoItem>()

    var filtro by mutableStateOf("")
        private set

    fun actualizarFiltro(nuevoFiltro: String) {
        filtro = nuevoFiltro
        aplicarFiltroYOrden()
    }

    /** Aplica el filtro de texto y la ordenación alfabética, filtra tanto por nombre, telefono, email y direccion de contacto */
    private fun aplicarFiltroYOrden() {
        val filtrados = if (filtro.isBlank()) {
            _listaContactosCompleta.toList()
        } else {
            _listaContactosCompleta.filter { contacto ->
                contacto.nombre_contacto.contains(filtro, ignoreCase = true) ||
                        contacto.telefono_contacto.contains(filtro, ignoreCase = true) ||
                        contacto.email_contacto.contains(filtro, ignoreCase = true) ||
                        contacto.direccion_contacto.contains(filtro, ignoreCase = true)
            }
        }

        val ordenados = filtrados.sortedBy { it.nombre_contacto.lowercase() }

        listaContactosSupabase.clear()
        listaContactosSupabase.addAll(ordenados)
    }

    fun obtenerContactosSupabase() {
        viewModelScope.launch {
            try {
                val resultado = withContext(Dispatchers.IO) {
                    SupabaseClient.client.from("contacto")
                        .select().decodeList<ContactoItem>()
                }

                _listaContactosCompleta.clear()
                _listaContactosCompleta.addAll(resultado)
                aplicarFiltroYOrden()

                Log.d("Supabase.Fetch", "Contactos traídos: ${resultado.size}")
            } catch (e: Exception) {
                Log.e("Supabase.Fetch", "Error al obtener contactos", e)
            }
        }
    }

    fun insertarContactoSupabase(item: ContactoItem, onDone: () -> Unit) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    SupabaseClient.client.from("contacto").insert(item)
                }

                obtenerContactosSupabase()
                onDone()
                Log.d("Supabase.Insert", "Insert pedido enviado para: ${item.nombre_contacto}")
            } catch (e: Exception) {
                Log.e("Supabase.Insert", "Error al insertar contacto", e)
            }
        }
    }

    fun actualizarContactoSupabase(item: ContactoItem, onDone: () -> Unit) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    SupabaseClient.client.from("contacto").update(item) {
                        filter { eq("id_contacto", item.id_contacto ?: 0) }
                    }
                }

                obtenerContactosSupabase()
                onDone()
                Log.d("Supabase.Update", "Update pedido para id: ${item.id_contacto}")
            } catch (e: Exception) {
                Log.e("Supabase.Update", "Error al actualizar contacto", e)
            }
        }
    }

    fun borrarContactoSupabase(id: Int?, onDone: () -> Unit) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    SupabaseClient.client.from("contacto").delete {
                        filter { eq("id_contacto", id as Any) }
                    }
                }

                obtenerContactosSupabase()
                onDone()
                Log.d("Supabase.Delete", "Delete pedido para id: $id")
            } catch (e: Exception) {
                Log.e("Supabase.Delete", "Error al borrar contacto", e)
            }
        }
    }
}