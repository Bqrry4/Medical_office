package com.pos.consults.dto

import com.pos.consults.persistance.model.Investigation

data class InvestigationDTO (
    val name : String,
    val duration: Int,
    val result: String
)

fun Investigation.toDTO() = InvestigationDTO(
    name = name,
    duration = duration,
    result = result
)