package com.pos.consults.persistance.repository

import com.pos.consults.persistance.model.Consultation
import com.pos.consults.persistance.model.Investigation
import org.springframework.data.mongodb.repository.Aggregation
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.data.mongodb.repository.Update
import java.util.*

interface ConsultationRepository : MongoRepository<Consultation, String> {
    @Query(value = "{ physicianId: ?0, patientId: ?1, date: ?2 }")
    fun findConsult(physicianId: Int, patientId: String, date: Date): Optional<Consultation>
    @Aggregation(pipeline = [
        "{'\$match': { 'physicianId': ?0, 'patientId': ?1, 'date': ?2 }}",
        "{'\$unwind': '\$investigations'}",
        "{'\$match': { 'investigations._id': ?3}}",
        "{'\$replaceRoot': {'newRoot': '\$investigations'}}"
    ])
    fun findInvestigationWithID(physicianId: Int, patientId: String, date: Date, id: String): Optional<Investigation>

}