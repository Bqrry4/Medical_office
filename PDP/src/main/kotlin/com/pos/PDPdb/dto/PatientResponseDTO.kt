package com.pos.PDPdb.dto

import com.pos.PDPdb.persistence.model.Patient
import java.sql.Date

data class PatientResponseDTO (
    var cnp: String,
    var userID: Int,
    var lastName: String,
    var firstName: String,
    var email: String,
    var phone: String,
    var birthDay: Date,
    var isActive: Boolean,
)

fun Patient.toDTO() = PatientResponseDTO(
    cnp = cnp,
    userID = userID,
    lastName = lastName,
    firstName = firstName,
    email = email,
    phone = phone,
    birthDay = birthDate,
    isActive = isActive,
)