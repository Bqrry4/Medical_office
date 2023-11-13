package com.pos.PDPdb.persistence.model

import jakarta.persistence.*

@Entity
@Table(name = "Physicians")
class Physician (
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id var id_physician: Int,
    var id_user: Int,
    var last_name: String,
    var first_name: String,
    @Column(unique=true)
    var email: String,
    var phone: String,
    var specialization: String,

    @OneToMany(mappedBy = "physician")
    var appointments: Set<Appointments>
)
