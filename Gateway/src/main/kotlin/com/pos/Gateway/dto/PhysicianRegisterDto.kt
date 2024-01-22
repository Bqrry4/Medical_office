package com.pos.Gateway.dto

data class PhysicianRegisterDto (
    val username: String,
    val password: String,
    var lastName: String,
    var firstName: String,
    var email: String,
    var phone: String,
    var specialization: String
)