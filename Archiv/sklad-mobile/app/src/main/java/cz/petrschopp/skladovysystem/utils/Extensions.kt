package cz.petrschopp.skladovysystem.utils

import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Extension function to convert an Exception into a user-friendly message.
 */
fun Exception.toUserMessage(fallbackMessage: String): String {
    return when (this) {
        is ConnectException, is UnknownHostException, is SocketTimeoutException -> 
            "Nelze se připojit k serveru. Zkontrolujte připojení k síti."
        else -> message?.takeIf { it.isNotBlank() } ?: fallbackMessage
    }
}

/**
 * Extension to check if the exception is related to network connectivity.
 */
fun Exception.isNetworkError(): Boolean {
    return this is ConnectException || this is UnknownHostException || this is SocketTimeoutException
}
