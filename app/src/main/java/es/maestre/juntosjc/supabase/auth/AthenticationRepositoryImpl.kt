package es.maestre.juntosjc.supabase.auth

object AthenticationRepositoryImpl : AuthenticationRepository {

    override suspend fun signIn(email: String, password: String): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun signUp(email: String, password: String): Boolean {
        TODO("Not yet implemented")
    }


}