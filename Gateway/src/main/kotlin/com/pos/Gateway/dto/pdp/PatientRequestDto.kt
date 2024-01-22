package com.pos.Gateway.dto.pdp

import java.sql.Date

data class PatientRequestDTO (
    var userID: Int,
    var lastName: String,
    var firstName: String,
    var email: String,
    var phone: String,
    var birthDay: String,
    var isActive: Boolean = true
)