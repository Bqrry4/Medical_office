package com.pos.Gateway.dto.pdp

data class PhysicianResponseDTO (
    var physicianId: Int,
    var userID: Int,
    var lastName: String,
    var firstName: String,
    var email: String,
    var phone: String,
    var specialization: String,
)