package com.pos.consults.dto

import com.pos.consults.persistance.model.Consultation
import com.pos.consults.persistance.model.Diagnostic
import java.util.*

data class ConsultationDTO (
    val patientId : Int,
    val physicianId: Int,
    val data: Date,
    val diagnostic : Diagnostic?,
    val investigations: List<InvestigationDTO>
)

fun Consultation.toDTO() = ConsultationDTO(
    patientId = patientId,
    physicianId = physicianId,
    data = data,
    diagnostic = diagnostic,
    investigations = investigations.map { x -> x.toDTO()}
)