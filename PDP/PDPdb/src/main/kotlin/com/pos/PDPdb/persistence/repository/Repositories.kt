package com.pos.PDPdb.persistence.repository

import com.pos.PDPdb.persistence.model.Patient
import com.pos.PDPdb.persistence.model.Physician
import org.springframework.data.jpa.repository.JpaRepository

interface PatientRepository : JpaRepository<Patient, String> {
}

interface PhysicianRepository : JpaRepository<Physician, Long> {
}