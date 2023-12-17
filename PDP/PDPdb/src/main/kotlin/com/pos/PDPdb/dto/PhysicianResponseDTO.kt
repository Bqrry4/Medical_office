package com.pos.PDPdb.dto

import com.pos.PDPdb.persistence.model.Physician

data class PhysicianResponseDTO (
    var physicianId: Int,
    var userId: Int,
    var lastName: String,
    var firstName: String,
    var email: String,
    var phone: String,
    var specialization: String,
)

fun Physician.toDTO() = PhysicianResponseDTO(
    physicianId = physicianId,
    userId = userId,
    lastName = lastName,
    firstName = firstName,
    email = email,
    phone = phone,
    specialization = specialization,
)