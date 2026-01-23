package es.maestre.juntosjc.viewModel

import android.app.Application
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
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

    val listaContactosSupabase = mutableStateListOf<ContactoItem>()

    fun obtenerContactosSupabase() {
        viewModelScope.launch {
            try {
                // Ejecutar en IO por seguridad
                val resultado = withContext(Dispatchers.IO) {
                    SupabaseClient.client.from("contacto")
                        .select().decodeList<ContactoItem>()
                }

                listaContactosSupabase.clear()
                listaContactosSupabase.addAll(resultado)

                Log.d("Supabase.Fetch", "Contactos traídos: ${resultado.size}")
            } catch (e: Exception) {
                // Mostrar stacktrace completo para depurar
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

                // Refrescar la lista y avisar
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