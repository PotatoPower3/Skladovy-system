package cz.petrschopp.skladovysystem.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    private const val DEFAULT_BASE_URL = "http://10.0.2.2:3000/"

    private var currentBaseUrl: String = DEFAULT_BASE_URL

    private var retrofit: Retrofit = createRetrofit(currentBaseUrl)

    var api: WarehouseApi = retrofit.create(WarehouseApi::class.java)
        private set

    fun configure(baseUrl: String) {
        val normalizedBaseUrl = normalizeBaseUrl(baseUrl)

        if (normalizedBaseUrl == currentBaseUrl) {
            return
        }

        currentBaseUrl = normalizedBaseUrl
        retrofit = createRetrofit(currentBaseUrl)
        api = retrofit.create(WarehouseApi::class.java)
    }

    fun getBaseUrl(): String {
        return currentBaseUrl
    }

    fun buildBaseUrl(serverIp: String, serverPort: String): String {
        val trimmedIp = serverIp.trim()
        val trimmedPort = serverPort.trim().ifBlank { "3000" }

        return "http://$trimmedIp:$trimmedPort/"
    }

    private fun createRetrofit(baseUrl: String): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private fun normalizeBaseUrl(baseUrl: String): String {
        val trimmed = baseUrl.trim()

        return when {
            trimmed.isBlank() -> DEFAULT_BASE_URL
            trimmed.endsWith("/") -> trimmed
            else -> "$trimmed/"
        }
    }
}