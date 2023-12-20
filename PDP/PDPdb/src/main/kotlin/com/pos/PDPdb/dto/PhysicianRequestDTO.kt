package com.pos.PDPdb.dto

import com.pos.PDPdb.persistence.model.Physician

data class PhysicianRequestDTO (
    var userID: Int,
    var lastName: String,
    var firstName: String,
    var email: String,
    var phone: String,
    var specialization: String,
)

fun PhysicianRequestDTO.toEntity(id: Int) = Physician(
    physicianId = id,
    userID = userID,
    lastName = lastName,
    firstName = firstName,
    email = email,
    phone = phone,
    specialization = specialization,
    appointments = emptySet()
)