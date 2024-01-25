package com.pos.Gateway.dto.pdp

data class PhysicianRequestDTO (
    var userID: Int,
    var lastName: String,
    var firstName: String,
    var email: String,
    var phone: String,
    var specialization: String
)