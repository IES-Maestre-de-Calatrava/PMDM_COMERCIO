package es.maestre.juntos.util

import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import android.widget.Toast
import java.util.Calendar

/**
 * Clase de utilidad para interactuar con el Calendar Provider del sistema.
 * La documentacion proporcionada por Santa sugiere que la forma mas sencilla
 * (puesto que implementar un calendario completo requiere de muchos permisos y
 * complejidad) es acceder a una app del calendario y en ella hacer las operaciones
 * de lectura, borrado, insercion y modificacion, para esta primera entrega lo he
 * implementado de esta manera, en un futuro si fuese necesario y el departamento de comercio
 * lo requeriese explicitamente pues habria que implementarlo completamente
 */
object Calendario {

    /**
     * Lanza una Intent para añadir un nuevo evento al calendario del usuario.
     *
     * @param context Contexto de la app
     * @param title Título del evento
     * @param location Ubicación del evento
     * @param description Descripción del evento
     * @param startTimeMillis Tiempo de inicio del evento
     * @param endTimeMillis Tiempo de finalización del evento
     */

    fun addEventToCalendar(
        context: Context,
        title: String,
        location: String,
        description: String,
        startTimeMillis: Long,
        endTimeMillis: Long
    ) {
        // Creo el intent con la accion para insertar
        val intent = Intent(Intent.ACTION_INSERT).apply {
            // hay que asegurarse que la Intent solo sea manejada por la aplicación de calendario
            data = CalendarContract.Events.CONTENT_URI

            // Añadimos los extras del intent
            putExtra(CalendarContract.Events.TITLE, title)
            putExtra(CalendarContract.Events.EVENT_LOCATION, location)
            putExtra(CalendarContract.Events.DESCRIPTION, description)
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startTimeMillis)
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTimeMillis)

            // esto permite que el usuario edite el evento
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        // Verifica si hay alguna aplicación de calendario
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            // Muestra un mensaje si no se encuentra una app de calendario
            Toast.makeText(context, "No hay una app de calendario instalada", Toast.LENGTH_SHORT).show()
        }
    }
}