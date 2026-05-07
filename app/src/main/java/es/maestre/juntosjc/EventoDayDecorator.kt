package es.maestre.juntosjc

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import android.os.Parcel
import android.os.Parcelable
import com.google.android.material.datepicker.DayViewDecorator

/**
 * Decorador que pinta un punto debajo de los días que tienen eventos.
 * Usa getCompoundDrawableBottom porque esta versión de Material no tiene shouldDecorate/decorate.
 */
class EventoDayDecorator(private val fechasConEventos: Set<Long>) : DayViewDecorator() {

    // Comprueba si ese día tiene evento
    private fun tieneEvento(year: Int, month: Int, day: Int): Boolean {
        val cal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")).apply {
            set(year, month, day, 0, 0, 0)   // month aquí ya viene 0-11 según el javadoc
            set(java.util.Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis in fechasConEventos
    }

    // Devuelve un drawable de punto si el día tiene evento, null si no
    override fun getCompoundDrawableBottom(
        context: Context,
        year: Int,
        month: Int,
        day: Int,
        valid: Boolean,
        selected: Boolean
    ): Drawable? {
        if (!tieneEvento(year, month, day)) return null
        return PuntoPequenioDrawable()
    }

    // Requerido por Parcelable
    override fun describeContents(): Int = 0
    override fun writeToParcel(dest: Parcel, flags: Int) {}

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<EventoDayDecorator> {
            override fun createFromParcel(source: Parcel) = EventoDayDecorator(emptySet())
            override fun newArray(size: Int) = arrayOfNulls<EventoDayDecorator>(size)
        }
    }
}

/**
 * Drawable que pinta un círculo pequeño (el punto que aparece debajo del día)
 */
class PuntoPequenioDrawable : Drawable() {

    private val paint = Paint().apply {
        color = Color.parseColor("#1565C0") // azul oscuro, cámbialo si quieres
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    override fun draw(canvas: Canvas) {
        val cx = bounds.centerX().toFloat()
        val cy = bounds.centerY().toFloat()
        canvas.drawCircle(cx, cy, 4f, paint)
    }

    override fun setAlpha(alpha: Int) { paint.alpha = alpha }
    override fun setColorFilter(colorFilter: ColorFilter?) { paint.colorFilter = colorFilter }
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT

    // Tamaño del drawable (pequeño, solo el punto)
    override fun getIntrinsicWidth(): Int = 8
    override fun getIntrinsicHeight(): Int = 8
}