package com.pos.consults.dto

import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer
import com.pos.consults.persistance.model.Investigation
import org.bson.types.ObjectId

class InvestigationDTO (
    val id: String,
    val name : String,
    val duration: Int,
    val result: String
)
fun Investigation.toDTO() = InvestigationDTO(
    id = id,
    name = name,
    duration = duration,
    result = result
)