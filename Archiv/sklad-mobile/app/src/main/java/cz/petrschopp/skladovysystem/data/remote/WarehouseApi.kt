package cz.petrschopp.skladovysystem.data.remote

import cz.petrschopp.skladovysystem.data.model.CreateDocumentRequest
import cz.petrschopp.skladovysystem.data.model.DocumentDetailDto
import cz.petrschopp.skladovysystem.data.model.DocumentDto
import cz.petrschopp.skladovysystem.data.model.ItemDto
import cz.petrschopp.skladovysystem.data.model.MovementDto
import cz.petrschopp.skladovysystem.data.model.AddCodeRequest
import cz.petrschopp.skladovysystem.data.model.CreateItemRequest
import cz.petrschopp.skladovysystem.data.model.ItemCodeDto
import cz.petrschopp.skladovysystem.data.model.UpdateItemRequest
import cz.petrschopp.skladovysystem.data.model.LoginRequest
import cz.petrschopp.skladovysystem.data.model.LoggedUserDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.Part

interface WarehouseApi {
    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): LoggedUserDto

    @GET("items")
    suspend fun getItems(
        @Query("warehouseId") warehouseId: Int = 1
    ): List<ItemDto>

    @GET("items/search")
    suspend fun searchItems(
        @Query("warehouseId") warehouseId: Int = 1,
        @Query("q") query: String
    ): List<ItemDto>

    @GET("movements")
    suspend fun getMovements(
        @Query("warehouseId") warehouseId: Int = 1,
        @Query("itemId") itemId: Int? = null,
        @Query("limit") limit: Int = 20
    ): List<MovementDto>

    @GET("documents")
    suspend fun getDocuments(
        @Query("warehouseId") warehouseId: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("movementTypeCode") movementTypeCode: String? = null
    ): List<DocumentDto>

    @GET("documents/{id}")
    suspend fun getDocumentDetail(
        @Path("id") id: Int
    ): DocumentDetailDto

    @POST("documents/in")
    suspend fun createIncomeDocument(
        @Body request: CreateDocumentRequest
    ): DocumentDetailDto

    @POST("documents/out")
    suspend fun createOutcomeDocument(
        @Body request: CreateDocumentRequest
    ): DocumentDetailDto

    @POST("items")
    suspend fun createItem(
        @Body request: CreateItemRequest
    ): ItemDto

    @POST("items/{id}/codes")
    suspend fun addCodeToItem(
        @Path("id") itemId: Int,
        @Body request: AddCodeRequest
    ): ItemCodeDto

    @GET("items/{id}")
    suspend fun getItemDetail(
        @Path("id") id: Int,
        @Query("warehouseId") warehouseId: Int = 1
    ): ItemDto

    @PUT("documents/{id}")
    suspend fun updateDocument(
        @Path("id") documentId: Int,
        @Body request: CreateDocumentRequest
    ): DocumentDetailDto

    @PUT("items/{id}")
    suspend fun updateItem(
        @Path("id") itemId: Int,
        @Body request: UpdateItemRequest
    ): ItemDto

    @Multipart
    @POST("items/{id}/image")
    suspend fun uploadItemImage(
        @Path("id") itemId: Int,
        @Query("warehouseId") warehouseId: Int = 1,
        @Part image: MultipartBody.Part
    ): ItemDto
}