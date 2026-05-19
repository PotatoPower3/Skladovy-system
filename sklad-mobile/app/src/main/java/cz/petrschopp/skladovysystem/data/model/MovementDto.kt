package cz.petrschopp.skladovysystem.data.model

import com.google.gson.annotations.SerializedName

data class MovementDto(
    val id: Int,
    val type: String,

    @SerializedName("type_name")
    val typeName: String? = null,

    val quantity: String,
    val note: String?,

    @SerializedName("created_at")
    val createdAt: String,

    @SerializedName("item_id")
    val itemId: Int,

    @SerializedName("item_name")
    val itemName: String,

    @SerializedName("item_code")
    val itemCode: String?,

    @SerializedName("user_id")
    val userId: Int,

    @SerializedName("first_name")
    val firstName: String,

    @SerializedName("last_name")
    val lastName: String,

    @SerializedName("warehouse_id")
    val warehouseId: Int,

    @SerializedName("warehouse_name")
    val warehouseName: String
)