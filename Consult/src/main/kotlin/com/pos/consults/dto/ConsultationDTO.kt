package com.pos.consults.dto

import com.fasterxml.jackson.annotation.JsonFormat
import com.pos.consults.persistance.model.Consultation
import com.pos.consults.persistance.model.Diagnostic
import java.util.*

data class ConsultationResponseDTO (
    val patientId : String,
    val physicianId: Int,
    @JsonFormat(pattern="HH:mm dd-MM-yyyy")
    val date: Date,
    val diagnostic : Diagnostic?,
    val investigations: List<InvestigationResponseDTO>
)

fun Consultation.toDTO() = ConsultationResponseDTO(
    patientId = patientId,
    physicianId = physicianId,
    date = date,
    diagnostic = diagnostic,
    investigations = investigations.map { x -> x.toDTO()}
)

data class ConsultationRequestDTO (
    val diagnostic : Diagnostic?
)