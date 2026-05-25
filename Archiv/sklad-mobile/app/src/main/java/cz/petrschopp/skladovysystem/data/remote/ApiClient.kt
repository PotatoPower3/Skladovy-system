package cz.petrschopp.skladovysystem.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
//    const val BASE_URL = "http://10.0.2.2:3000/"
    const val BASE_URL = "http://192.168.5.112:3000/"

//      const val BASE_URL = "http://127.0.0.1:3000/"

    val api: WarehouseApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WarehouseApi::class.java)
    }
}