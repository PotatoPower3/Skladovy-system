package cz.petrschopp.skladovysystem.ui.documents.model

import cz.petrschopp.skladovysystem.data.model.ItemDto

data class DraftDocumentItem(
    val item: ItemDto,
    val quantity: Int,
    val note: String? = null
)