package com.pos.consults.persistance.repository

import com.pos.consults.persistance.model.Consultation
import com.pos.consults.persistance.model.Investigation
import org.springframework.data.mongodb.repository.MongoRepository
import java.util.Date
import java.util.Optional

interface ConsultationRepository : MongoRepository<Consultation, String> {
    fun findByPatientIdAndPhysicianIdAndData(patientId: String, physicianId: Int, date: Date) : Optional<Consultation>
    fun deleteByPatientIdAndPhysicianIdAndData(patientId: String, physicianId: Int, date: Date)

    fun findAllByPhysicianId(physicianId: Int) : List<Consultation>
}

interface InvestigationRepository : MongoRepository<Investigation, String> {
}