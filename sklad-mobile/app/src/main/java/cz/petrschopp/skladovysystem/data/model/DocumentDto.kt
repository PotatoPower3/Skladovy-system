package cz.petrschopp.skladovysystem.data.model

import com.google.gson.annotations.SerializedName

data class DocumentDto(
    val id: Int,

    @SerializedName("document_number")
    val documentNumber: String?,

    val status: String,
    val note: String?,

    @SerializedName("created_at")
    val createdAt: String,

    @SerializedName("updated_at")
    val updatedAt: String,

    @SerializedName("movement_type_id")
    val movementTypeId: Int,

    @SerializedName("movement_type_code")
    val movementTypeCode: String,

    @SerializedName("movement_type_name")
    val movementTypeName: String,

    @SerializedName("movement_type_direction")
    val movementTypeDirection: String,

    @SerializedName("created_by_user_id")
    val createdByUserId: Int,

    @SerializedName("created_by_first_name")
    val createdByFirstName: String,

    @SerializedName("created_by_last_name")
    val createdByLastName: String,

    @SerializedName("updated_by_user_id")
    val updatedByUserId: Int,

    @SerializedName("updated_by_first_name")
    val updatedByFirstName: String,

    @SerializedName("updated_by_last_name")
    val updatedByLastName: String,

    @SerializedName("items_count")
    val itemsCount: Int,

    @SerializedName("total_quantity")
    val totalQuantity: String
)

data class DocumentDetailDto(
    val id: Int,

    @SerializedName("document_number")
    val documentNumber: String?,

    val status: String,
    val note: String?,

    @SerializedName("created_at")
    val createdAt: String,

    @SerializedName("updated_at")
    val updatedAt: String,

    @SerializedName("warehouse_id")
    val warehouseId: Int,

    @SerializedName("warehouse_name")
    val warehouseName: String,

    @SerializedName("movement_type_id")
    val movementTypeId: Int,

    @SerializedName("movement_type_code")
    val movementTypeCode: String,

    @SerializedName("movement_type_name")
    val movementTypeName: String,

    @SerializedName("movement_type_direction")
    val movementTypeDirection: String,

    @SerializedName("created_by_user_id")
    val createdByUserId: Int,

    @SerializedName("created_by_first_name")
    val createdByFirstName: String,

    @SerializedName("created_by_last_name")
    val createdByLastName: String,

    @SerializedName("updated_by_user_id")
    val updatedByUserId: Int,

    @SerializedName("updated_by_first_name")
    val updatedByFirstName: String,

    @SerializedName("updated_by_last_name")
    val updatedByLastName: String,

    val items: List<DocumentItemDto>
)

data class DocumentItemDto(
    val id: Int,

    @SerializedName("item_id")
    val itemId: Int,

    @SerializedName("item_name")
    val itemName: String,

    val unit: String,
    val quantity: String,
    val note: String?,

    @SerializedName("created_at")
    val createdAt: String,

    @SerializedName("updated_at")
    val updatedAt: String
)