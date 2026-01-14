package es.maestre.juntos.supabase
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
object SupabaseClient {

    // Reemplaza con tus credenciales reales de Supabase
    // postgresql://postgres:[YOUR_PASSWORD]@db.jdgzlrilrarbwzdaxpim.supabase.co:5432/postgres
    private const val SUPABASE_URL = "https://lxmkwegowscwhgrfsqcw.supabase.co"
    private const val SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imx4bWt3ZWdvd3Njd2hncmZzcWN3Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjUwMzQ0NzQsImV4cCI6MjA4MDYxMDQ3NH0.jWjtnyeQmQJHEDQ2fvMtwohb0vNgRPfVeQYhOfySECY"

    val client = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_KEY

    ) {

        // Aquí instalas los plugins que vayas a usar (Auth, Postgrest, Storage, etc.)

        install(Storage.Companion)
        install(Auth) // Si vas a usar autenticación

        // TODO hacer base de datos de usuarios
        install(Postgrest) // Si vas a usar base de datos

    }


}