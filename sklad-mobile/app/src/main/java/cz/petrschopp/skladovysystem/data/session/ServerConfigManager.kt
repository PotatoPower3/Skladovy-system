package cz.petrschopp.skladovysystem.data.session

import android.content.Context
import cz.petrschopp.skladovysystem.data.remote.ApiClient

data class ServerConfig(
    val serverIp: String,
    val serverPort: String
) {
    val baseUrl: String
        get() = ApiClient.buildBaseUrl(serverIp, serverPort)
}

class ServerConfigManager(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(
        "server_config",
        Context.MODE_PRIVATE
    )

    fun getConfig(): ServerConfig {
        return ServerConfig(
            serverIp = prefs.getString(KEY_SERVER_IP, DEFAULT_SERVER_IP) ?: DEFAULT_SERVER_IP,
            serverPort = prefs.getString(KEY_SERVER_PORT, DEFAULT_SERVER_PORT) ?: DEFAULT_SERVER_PORT
        )
    }

    fun saveConfig(serverIp: String, serverPort: String) {
        prefs.edit()
            .putString(KEY_SERVER_IP, serverIp.trim())
            .putString(KEY_SERVER_PORT, serverPort.trim().ifBlank { DEFAULT_SERVER_PORT })
            .apply()
    }

    companion object {
        const val DEFAULT_SERVER_IP = "10.0.2.2"
        const val DEFAULT_SERVER_PORT = "3000"

        private const val KEY_SERVER_IP = "server_ip"
        private const val KEY_SERVER_PORT = "server_port"
    }
}