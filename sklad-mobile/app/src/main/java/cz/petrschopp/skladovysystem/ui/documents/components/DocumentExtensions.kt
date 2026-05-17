package cz.petrschopp.skladovysystem.ui.documents.components

import cz.petrschopp.skladovysystem.data.model.DocumentDetailDto
import cz.petrschopp.skladovysystem.data.model.DocumentDto

fun DocumentDto.wasUpdated(): Boolean {
    return updatedAt.isNotBlank() && updatedAt != createdAt
}

fun DocumentDetailDto.wasUpdated(): Boolean {
    return updatedAt.isNotBlank() && updatedAt != createdAt
}