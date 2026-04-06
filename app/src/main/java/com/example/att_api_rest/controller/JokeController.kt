package com.example.att_api_rest.controller

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import kotlin.random.Random

// 1. Modelos de Dados
data class JokeResponse(
    val error: Boolean,
    val type: String,
    val joke: String? = null,
    val setup: String? = null,
    val delivery: String? = null
)

data class JokeData(
    val id: Int? = null,
    val question: String,
    val answer: String? = null,
    val isLocal: Boolean = false
)

data class UserPost(
    val userId: Int,
    val id: Int? = null,
    val title: String,
    val body: String,
    val language: String
)

// 2. Interface Retrofit
interface ApiService {
    @GET("joke/Any")
    suspend fun getRandomJoke(@Query("lang") lang: String): JokeResponse

    @POST("posts")
    suspend fun createPost(@Body post: UserPost): UserPost
}

// 3. Objeto Retrofit
object RetrofitClient {
    val jokeInstance: ApiService by lazy { createClient("https://v2.jokeapi.dev/") }
    val postInstance: ApiService by lazy { createClient("https://jsonplaceholder.typicode.com/") }

    private fun createClient(baseUrl: String): ApiService = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ApiService::class.java)
}

// 4. Controller com Salvamento Real (em Memória)
class JokeController {
    private val jokeService = RetrofitClient.jokeInstance
    private val postService = RetrofitClient.postInstance

    // Lista onde salvamos as piadas do POST para que apareçam no GET
    private val mySavedJokes = mutableListOf<JokeData>()

    suspend fun getJoke(lang: String): JokeData {
        // 40% de chance de mostrar uma piada que você mesmo criou!
        if (mySavedJokes.isNotEmpty() && Random.nextInt(100) < 40) {
            return mySavedJokes.random()
        }

        return try {
            val res = jokeService.getRandomJoke(lang)
            if (res.type == "single") {
                JokeData(question = res.joke ?: "Nenhuma piada encontrada.")
            } else {
                JokeData(question = res.setup ?: "", answer = res.delivery)
            }
        } catch (e: Exception) {
            if (mySavedJokes.isNotEmpty()) mySavedJokes.random()
            else JokeData(question = "SISTEMA OFFLINE. CONECTE-SE À REDE.")
        }
    }

    suspend fun submitJoke(userId: Int, category: String, q: String, a: String, lang: String): String {
        return try {
            val post = UserPost(userId, null, category, "Q: $q | A: $a", lang)
            val res = postService.createPost(post)
            
            // SALVAMENTO REAL: Adicionamos à nossa lista para que o GET possa buscá-la
            mySavedJokes.add(JokeData(id = res.id, question = q, answer = a, isLocal = true))
            
            "DADOS SINCRONIZADOS COM A NUVEM"
        } catch (e: Exception) {
            "FALHA NO PROTOCOLO DE ENVIO"
        }
    }
}
