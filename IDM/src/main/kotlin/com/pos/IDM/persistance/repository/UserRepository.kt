package com.pos.IDM.persistance.repository

import com.pos.IDM.persistance.model.User
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface UserRepository : JpaRepository<User, Long> {

    fun findByUsernameAndPassword(username: String, password: String) : Optional<User>
}