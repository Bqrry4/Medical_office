package com.pos.PDPdb.persistence.repository

import com.pos.PDPdb.persistence.model.Appointment
import com.pos.PDPdb.persistence.model.AppointmentsKey
import com.pos.PDPdb.persistence.model.Patient
import com.pos.PDPdb.persistence.model.Physician
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.sql.Date
import java.util.*

interface PatientRepository : JpaRepository<Patient, String> {
    @Query(value = "update Patients set is_active = 0 where cnp = ?1",
        nativeQuery = true)
    fun setPatientInactive(cnp: String)
}

interface PhysicianRepository : JpaRepository<Physician, Int> {
    fun findBySpecializationStartingWith(specialization: String): Iterable<Physician>
    fun findByLastNameStartingWith(specialization: String): Iterable<Physician>
}

interface AppointmentRepository : JpaRepository<Appointment, AppointmentsKey> {
    fun findByIdPatientID(id: String): Iterable<Appointment>
    fun findByIdPhysicianID(id: Int): Iterable<Appointment>
    fun findByIdPatientIDAndIdDate(id: String, date: Date): Iterable<Appointment>
    fun findByIdPhysicianIDAndIdDate(id: Int, date: Date): Iterable<Appointment>

    @Query(value = "select * from Appointments a where a.id_patient = ?1 and month(a.data) = month(?2)",
        nativeQuery = true)
    fun findByIdPatientWithinAMonth(id: String, date: Date): Iterable<Appointment>
    @Query(value = "select * from Appointments a where a.id_physician = ?1 and month(a.data) = month(?2)",
        nativeQuery = true)
    fun findByIdPhysicianWithinAMonth(id: Int, date: Date): Iterable<Appointment>
    fun findByIdPatientIDAndIdPhysicianIDAndIdDate(patientId: String, physicianId:Int, date: Date): Optional<Appointment>
}