package cz.petrschopp.skladovysystem.data.model

data class CreateItemRequest(
    val name: String,
    val code: String? = null,
    val codeType: String = "UNKNOWN",
    val unit: String = "ks",
    val weightPerUnit: Double = 0.0,
    val imageFilename: String? = null,
    val note: String? = null,
    val warehouseId: Int = 1,
    val quantity: Double = 0.0,
    val location: String? = null,
    val minQuantity: Double = 0.0
)

data class UpdateItemRequest(
    val name: String? = null,
    val unit: String? = null,
    val weightPerUnit: Double? = null,
    val imageFilename: String? = null,
    val note: String? = null,
    val active: Boolean? = null,
    val warehouseId: Int = 1,
    val location: String? = null,
    val minQuantity: Double? = null
)

data class AddCodeRequest(
    val code: String,
    val codeType: String = "UNKNOWN"
)