package com.pos.IDM.persistance.model

import jakarta.persistence.*


enum class Role {
    admin,
    patient,
    physician
}

@Entity
@Table(name = "Users")
class User(
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id val id: Long,
    var username: String,
    var password: String,
    @Enumerated(EnumType.STRING)
    var role: Role
)