package cz.petrschopp.skladovysystem.data.remote

object ApiUrls {

    fun itemImageUrl(imageFilename: String?): String? {
        val value = imageFilename
            ?.trim()
            ?.replace("\\", "/")
            ?: return null

        if (value.isBlank()) return null

        val baseUrl = ApiClient.BASE_URL.trimEnd('/')
        val normalizedPath = value.trimStart('/')

        return when {
            value.startsWith("http://") || value.startsWith("https://") -> value
            normalizedPath.startsWith("uploads/") -> "$baseUrl/$normalizedPath"
            else -> "$baseUrl/uploads/items/$normalizedPath"
        }
    }
}