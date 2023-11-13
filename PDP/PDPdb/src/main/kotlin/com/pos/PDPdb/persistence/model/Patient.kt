package com.pos.PDPdb.persistence.model

import jakarta.persistence.*
import lombok.Getter
import lombok.Setter
import java.sql.Date

@Entity
@Table(name = "Patients")
@Getter
@Setter
class Patient (
    @Id var cnp: String,
    var id_user: Int,
    var last_name: String,
    var first_name: String,
    @Column(unique=true)
    var email: String,
    var phone: String,
    var birth_day: Date,
    var is_active: Boolean,

    @OneToMany(mappedBy = "patient")
    var appointments: Set<Appointments>
)