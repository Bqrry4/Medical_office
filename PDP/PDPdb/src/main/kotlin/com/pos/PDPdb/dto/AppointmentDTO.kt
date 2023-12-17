package com.pos.PDPdb.dto

import com.pos.PDPdb.persistence.model.*
import java.sql.Date

data class AppointmentDTO (
    var patient: PatientResponseDTO,
    var physician: PhysicianResponseDTO,
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