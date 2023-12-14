package com.pos.PDPdb.persistence.model

import jakarta.persistence.*

@Entity
@Table(name = "Physicians")
class Physician (
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id_physician")
    @Id var physicianId: Int,
    @Column(name="id_user")
    var userId: Int,
    @Column(name="last_name")
    var lastName: String,
    @Column(name="first_name")
    var firstName: String,
    @Column(unique=true)
    var email: String,
    var phone: String,
    var specialization: String,

    @OneToMany(mappedBy = "physician")
    var appointments: Set<Appointments>
)
