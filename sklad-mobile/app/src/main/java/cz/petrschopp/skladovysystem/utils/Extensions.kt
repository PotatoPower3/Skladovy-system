package cz.petrschopp.skladovysystem.utils

import org.json.JSONObject
import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

fun Exception.toUserMessage(fallbackMessage: String): String {
    return when (this) {
        is ConnectException, is UnknownHostException, is SocketTimeoutException ->
            "Nelze se připojit k serveru. Zkontrolujte připojení k síti."

        is HttpException -> {
            val serverMessage = response()
                ?.errorBody()
                ?.string()
                ?.let { body ->
                    runCatching {
                        JSONObject(body).optString("message")
                    }.getOrNull()
                }
                ?.takeIf { it.isNotBlank() }

            serverMessage ?: fallbackMessage
        }

        else -> message?.takeIf { it.isNotBlank() } ?: fallbackMessage
    }
}

fun Exception.isNetworkError(): Boolean {
    return this is ConnectException ||
            this is UnknownHostException ||
            this is SocketTimeoutException
}