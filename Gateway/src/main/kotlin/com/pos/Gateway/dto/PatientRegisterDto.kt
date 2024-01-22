package com.pos.Gateway.dto

import java.sql.Date

data class PatientRegisterDto (
    val username: String,
    val password: String,
    val cnp: String,
    var lastName: String,
    var firstName: String,
    var email: String,
    var phone: String,
    var birthDay: String,
)