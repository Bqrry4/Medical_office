package com.pos.consults.dto

import com.pos.consults.persistance.model.Investigation

class InvestigationResponseDTO (
    val id: String,
    val description : String,
    val duration: Int,
    val result: String
)
fun Investigation.toDTO() = InvestigationResponseDTO(
    id = id,
    description = description,
    duration = duration,
    result = result
)

class InvestigationRequestDTO (
    val description : String,
    val duration: Int,
    val result: String
)

fun InvestigationRequestDTO.toEntity(id: String) = Investigation(
    id = id,
    description = description,
    duration = duration,
    result = result
)
