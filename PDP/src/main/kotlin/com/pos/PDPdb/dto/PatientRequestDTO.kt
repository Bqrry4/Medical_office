package com.pos.PDPdb.dto

import com.pos.PDPdb.persistence.model.Patient
import java.sql.Date

data class PatientRequestDTO (
    var userID: Int,
    var lastName: String,
    var firstName: String,
    var email: String,
    var phone: String,
    var birthDay: Date,
    var isActive: Boolean = true
)

fun PatientRequestDTO.toEntity(cnp: String) = Patient(
    userID = userID,
    lastName = lastName,
    firstName = firstName,
    email = email,
    phone = phone,
    birthDate = birthDay,
    isActive = isActive,
    cnp = cnp,
    appointments = emptySet()
)