package cz.petrschopp.skladovysystem.utils

fun formatQuantity(value: String): String {
    val number = value.replace(",", ".").toDoubleOrNull() ?: return value

    return if (number % 1.0 == 0.0) {
        number.toInt().toString()
    } else {
        value
    }
}

fun formatWeight(value: String): String {
    val number = value.replace(",", ".").toDoubleOrNull() ?: return value
    return String.format(java.util.Locale.US, "%.1f", number)
        .replace(".", ",")
}