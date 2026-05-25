package cz.petrschopp.skladovysystem.utils

import java.text.Normalizer

object SearchUtils {
    fun normalize(text: String): String {
        return Normalizer.normalize(text.lowercase(), Normalizer.Form.NFD)
            .replace("\\p{Mn}+".toRegex(), "")
    }

    fun <T> filter(items: List<T>, query: String, selector: (T) -> String): List<T> {
        val normalizedQuery = normalize(query.trim())
        if (normalizedQuery.isBlank()) return items
        return items.filter { normalize(selector(it)).contains(normalizedQuery) }
    }
}
