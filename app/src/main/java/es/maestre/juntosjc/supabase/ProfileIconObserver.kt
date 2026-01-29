package es.maestre.juntosjc.supabase

import android.content.Context
import android.net.Uri
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class PerfilIcono(
    val icono: String? = null
)

class ProfileIconObserver(
    private val supabase: SupabaseClient,
    private val bucketName: String = "IconosPerfiles" // bucket público
) {
    suspend fun updateIcon(
        context: Context,
        uid: UUID,
        newImageUri: Uri
    ): String = withContext(Dispatchers.IO) {
        // 1) Leer icono actual
        val current = supabase.from("perfiles").select(columns = Columns.list("icono")) {
            filter {
                eq("uid", uid.toString())
            }
            single()
        }.decodeAs<PerfilIcono>()
        val oldUrl = current.icono

        // 2) Borrar anterior (si existe y está en el bucket)
        oldUrl?.let { url ->
            parsePathOrNull(url)?.let { oldPath ->
                runCatching {
                    supabase.storage.from(bucketName).delete(listOf(oldPath))
                }
            }
        }

        // 3) Subir nuevo
        val bytes = context.contentResolver.openInputStream(newImageUri)?.use { it.readBytes() }
            ?: error("No se pudo leer la imagen")
        val ext = guessExtension(context, newImageUri) ?: "jpg"
        val newPath = "${uid}/avatar-${System.currentTimeMillis()}.$ext"

        supabase.storage.from(bucketName).upload(
            path = newPath,
            data = bytes
        ) {
            upsert = true
        }

        // 4) URL pública y actualización en la tabla
        val publicUrl = supabase.storage.from(bucketName).publicUrl(newPath)
        supabase.from("perfiles").update({
            set("icono", publicUrl)
        }) {
            filter {
                eq("uid", uid.toString())
            }
        }

        publicUrl
    }

    private fun parsePathOrNull(url: String): String? {
        // Soporta URLs públicas y firmadas
        val idx = url.indexOf("/storage/v1/object/")
        if (idx == -1) return null
        val tail = url.substring(idx).removePrefix("/storage/v1/object/")
        val parts = tail.split("/", limit = 3)
        if (parts.size < 3) return null
        val bucket = parts[1]
        if (bucket != bucketName) return null
        return parts[2].substringBefore("?")
    }

    private fun guessExtension(context: Context, uri: Uri): String? {
        val type = context.contentResolver.getType(uri) ?: return null
        return when (type.lowercase()) {
            "image/png" -> "png"
            "image/jpeg", "image/jpg" -> "jpg"
            "image/webp" -> "webp"
            else -> null
        }
    }

    private fun contentTypeForExt(ext: String): String =
        when (ext.lowercase()) {
            "png" -> "image/png"
            "jpg", "jpeg" -> "image/jpeg"
            "webp" -> "image/webp"
            else -> "application/octet-stream"
        }
}
