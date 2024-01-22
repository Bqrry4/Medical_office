package com.pos.PDPdb.dto

import com.fasterxml.jackson.annotation.JsonFormat
import com.pos.PDPdb.persistence.model.Appointment
import com.pos.PDPdb.persistence.model.Status
import java.util.Date

data class AppointmentDTO (
    var patient: PatientResponseDTO,
    var physician: PhysicianResponseDTO,
    @JsonFormat(pattern="HH:mm dd-MM-yyyy")
    var date: Date,
    var status: Status
)

fun Appointment.toDTO() = AppointmentDTO(
    patient = patient.toDTO(),
    physician = physician.toDTO(),
    date = id.date,
    status = status,
)

data class AppointmentUpdateDTO(
    var status: Status
)