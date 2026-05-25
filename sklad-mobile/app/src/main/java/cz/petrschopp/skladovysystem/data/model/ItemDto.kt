package cz.petrschopp.skladovysystem.data.model

import com.google.gson.annotations.SerializedName

data class ItemDto(
    val id: Int,
    val name: String,
    val codes: List<ItemCodeDto> = emptyList(),
    val quantity: String,
    val unit: String,

    @SerializedName("weight_per_unit")
    val weightPerUnit: String?,

    val location: String?,

    @SerializedName("min_quantity")
    val minQuantity: String?,

    @SerializedName("image_filename")
    val imageFilename: String?,

    val note: String?,

    @SerializedName("warehouse_id")
    val warehouseId: Int,

    @SerializedName("warehouse_name")
    val warehouseName: String
)

data class ItemCodeDto(
    val id: Int,
    val code: String,

    @SerializedName("code_type_id")
    val codeTypeId: Int? = null,

    @SerializedName("code_type_code")
    val codeTypeCode: String? = null,

    @SerializedName("code_type_name")
    val codeTypeName: String? = null
)