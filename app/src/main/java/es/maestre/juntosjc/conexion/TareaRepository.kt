package es.maestre.juntosjc.conexion

import androidx.lifecycle.LiveData
import es.maestre.juntosjc.model.Tarea


class TareaRepository(private val tareaDAO: TareaDAO) {

    // Creo una lista con tareas, para el caso de no haber ninguna al iniciar la app
    private val tareas = listOf(
        Tarea(
            tituloTarea = "GESTIÓN DE STOCK",
            descripcionTarea = "Revisar el inventario del almacén central y actualizar las existencias en el sistema.",
            fechaEntrega = "11/11/2026",
            completada = false,
            personaEncargada = "Carlos Gómez Calcerrada"
        ),
        Tarea(
            tituloTarea = "FACTURACIÓN MENSUAL",
            descripcionTarea = "Emitir facturas de enero y enviar a los clientes por correo.",
            fechaEntrega = "7/1/2026",
            completada = true,
            personaEncargada = "Lucía Fernández"
        ),
        Tarea(
            tituloTarea = "MANTENIMIENTO SERVIDORES",
            descripcionTarea = "Optimizar bases de datos y limpiar logs antiguos.",
            fechaEntrega = "24/2/2026",
            completada = false,
            personaEncargada = "Miguel Rivas"
        )
    )

    /**
     * Este es el metodo que me inserta la lista si no hay tareas
     * llamando a los metodos de tareaDAO creados previamente
     */
    suspend fun insertarTareasInicio(){
        if(tareaDAO.contarTareas()==0){
            tareaDAO.insertarTareasSiNoHay(tareas)
        }
    }

    fun getAllTareas(): LiveData<List<Tarea>> {
        return tareaDAO.getAllTareas()
    }

    suspend fun insert(tarea: Tarea) {
        tareaDAO.insert(tarea)
    }

    suspend fun update(tarea: Tarea) {
        tareaDAO.update(tarea)
    }

    suspend fun delete(tarea: Tarea) {
        tareaDAO.delete(tarea)
    }

    fun getTareaById(id: Int): LiveData<Tarea> {
        return tareaDAO.getTareaById(id)
    }

}





