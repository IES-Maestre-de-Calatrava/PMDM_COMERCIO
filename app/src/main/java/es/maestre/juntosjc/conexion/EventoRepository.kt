package es.maestre.juntosjc.conexion

import androidx.lifecycle.LiveData
import es.maestre.juntosjc.model.Evento
import java.util.Calendar

class EventoRepository(private val eventoDAO: EventoDAO) {

    // Creo una lista con eventos, para el caso de no haber ninguno al iniciar la app

    // Le paso un tipo long al datepicker porque funciona asi, para ello uso la clase calendar para elegir el dia
    // y lo paso a long al crear el objeto con .timeMillis
    val calendar = Calendar.getInstance().apply {
        set(2026, Calendar.JANUARY, 26, 0, 0, 0)
        set(Calendar.MILLISECOND, 0)
    }

    val calendar1 = Calendar.getInstance().apply {
        set(2026, Calendar.FEBRUARY, 24, 0, 0, 0)
        set(Calendar.MILLISECOND, 0)
    }

    private val eventos = listOf(
        Evento(
            tituloEvento = "Fotos de la Orla",
            descripcionEvento = "Traer una camiseta blanca para la foto de la orla",
            fechaEvento = calendar.timeInMillis
        ),
        Evento(tituloEvento="Prácticas", descripcionEvento = "Empieza el periodo de prácticas laborales de segundo", fechaEvento=calendar1.timeInMillis)
    )

    /**
     * Este es el metodo que me inserta la lista si no hay comentarios
     * llamando a los metodos de comentarioDAO creados previamente
     */
    suspend fun insertarEventosInicio(){
        if(eventoDAO.contarEventos()==0){
            eventoDAO.insertarEventosSiNoHay(eventos)
        }
    }

    fun getAllEventos(): LiveData<List<Evento>> {
        return eventoDAO.getAllEventos()
    }
    suspend fun insert(evento: Evento) {
        eventoDAO.insert(evento)
    }

    suspend fun update(evento: Evento) {
        eventoDAO.update(evento)
    }

    suspend fun delete(evento: Evento) {
        eventoDAO.delete(evento)
    }

    fun getEventosByFecha(fechaSeleccionada: Long): LiveData<List<Evento>> {
        return eventoDAO.getEventosByFecha(fechaSeleccionada)
    }

}