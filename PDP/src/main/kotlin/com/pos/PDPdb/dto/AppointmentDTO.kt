package com.pos.PDPdb.dto

import com.fasterxml.jackson.annotation.JsonFormat
import com.pos.PDPdb.persistence.model.Appointment
import com.pos.PDPdb.persistence.model.Status
import java.util.Date

data class AppointmentPatientDTO (
    var physician: PhysicianResponseDTO,
    @JsonFormat(pattern="HH:mm dd-MM-yyyy")
    var date: Date,
    var status: Status
)

data class AppointmentPhysicianDTO (
    var patient: PatientResponseDTO,
    @JsonFormat(pattern="HH:mm dd-MM-yyyy")
    var date: Date,
    var status: Status
)

fun Appointment.toPatientDTO() = AppointmentPatientDTO(
    physician = physician.toDTO(),
    date = id.date,
    status = status,
)

fun Appointment.toPhysicianDTO() = AppointmentPhysicianDTO(
    patient = patient.toDTO(),
    date = id.date,
    status = status,
)

data class AppointmentUpdateDTO(
    var status: Status
)