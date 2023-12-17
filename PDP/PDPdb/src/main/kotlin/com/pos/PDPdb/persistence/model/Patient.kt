package com.pos.PDPdb.persistence.model

import jakarta.persistence.*
import java.sql.Date

@Entity
@Table(name = "Patients")
class Patient (
    @Id var cnp: String,
    @Column(name="id_user")
    var userID: Int,
    @Column(name="last_name")
    var lastName: String,
    @Column(name="first_name")
    var firstName: String,
    @Column(unique=true)
    var email: String,
    var phone: String,
    @Column(name="birth_day")
    var birthDay: Date,
    @Column(name="is_active")
    var isActive: Boolean,

    @OneToMany(mappedBy = "patient")
    var appointments: Set<Appointment>
)