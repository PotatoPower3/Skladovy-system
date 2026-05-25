package cz.petrschopp.skladovysystem.utils

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun formatDateTime(value: String): String {
    return try {
        val instant = Instant.parse(value)
        val formatter = DateTimeFormatter
            .ofPattern("d. M. yyyy HH:mm")
            .withZone(ZoneId.systemDefault())

        formatter.format(instant)
    } catch (e: Exception) {
        value
    }
}