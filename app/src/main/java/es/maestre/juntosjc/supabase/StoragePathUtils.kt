package es.maestre.juntosjc.supabase

import java.net.URLDecoder
import java.nio.charset.StandardCharsets

fun parseStoragePathOrNull(url: String, bucketName: String): String? {
    val markers = listOf(
        "/storage/v1/object/",
        "/storage/v1/render/image/"
    )

    for (marker in markers) {
        val idx = url.indexOf(marker)
        if (idx != -1) {
            val tail = url.substring(idx).removePrefix(marker)
            val parts = tail.split("/", limit = 3)
            if (parts.size >= 3 && parts[1] == bucketName) {
                return parts[2]
                    .substringBefore('?')
                    .let(::decodeStoragePath)
                    .ifBlank { null }
            }
        }
    }

    val fileName = url.substringAfterLast('/').substringBefore('?').trim()
    return decodeStoragePath(fileName).ifBlank { null }
}

private fun decodeStoragePath(path: String): String {
    return URLDecoder.decode(path, StandardCharsets.UTF_8.name())
}
