package cz.petrschopp.skladovysystem.data.model

import com.google.gson.annotations.SerializedName

data class WarehouseLocationDto(
    val id: Int,
    @SerializedName("warehouseId")
    val warehouseId: Int,
    val code: String,
    val name: String,
    val active: Boolean
)