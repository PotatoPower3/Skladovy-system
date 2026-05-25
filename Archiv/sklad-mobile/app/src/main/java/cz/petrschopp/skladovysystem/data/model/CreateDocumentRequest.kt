package cz.petrschopp.skladovysystem.data.model

data class CreateDocumentRequest(
    val warehouseId: Int = 1,
    val userId: Int = 1,
    val note: String?,
    val items: List<CreateDocumentItemRequest>
)

data class CreateDocumentItemRequest(
    val itemId: Int,
    val quantity: Double,
    val note: String? = null
)