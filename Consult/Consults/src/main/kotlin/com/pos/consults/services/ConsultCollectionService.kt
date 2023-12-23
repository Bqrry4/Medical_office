package com.pos.consults.services

import com.pos.consults.dto.ConsultationRequestDTO
import com.pos.consults.dto.InvestigationRequestDTO
import com.pos.consults.dto.toEntity
import com.pos.consults.persistance.model.Consultation
import com.pos.consults.persistance.model.Investigation
import com.pos.consults.persistance.repository.ConsultationRepository
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.FindAndModifyOptions
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Service
import java.util.*

@Service
class ConsultCollectionService(
    private val _consultationRepository: ConsultationRepository, private val mongoTemplate: MongoTemplate
) {
    fun getConsult(physicianId: Int, patientId: String, date: Date): Consultation? =
        _consultationRepository.findConsult(physicianId, patientId, date).orElse(null)

    fun createConsult(
        physicianID: Int, patientID: String, date: Date, consultationDto: ConsultationRequestDTO
    ): Consultation {
        return _consultationRepository.save(
            Consultation(
                ObjectId(), patientID, physicianID, date, consultationDto.diagnostic, mutableListOf()
            )
        )
    }

    fun updateConsult(
        consultation: Consultation, consultationDto: ConsultationRequestDTO
    ): Consultation {
        consultation.diagnostic = consultationDto.diagnostic
        return _consultationRepository.save(consultation)
    }


    fun deleteConsult(
        consultation: Consultation
    ) = _consultationRepository.delete(consultation)


    fun getConsultInvestigation(
        physicianID: Int, patientID: String, date: Date, id: String
    ): Investigation? = _consultationRepository.findInvestigationWithID(physicianID, patientID, date, id).orElse(null)


    fun addInvestigationToConsult(
        physicianID: Int,
        patientID: String,
        date: Date,
        investigation: Investigation
    ): Investigation? {
        val query = Query(
            Criteria().andOperator(
                Criteria.where("physicianId").`is`(physicianID),
                Criteria.where("patientId").`is`(patientID),
                Criteria.where("date").`is`(date),
            )
        )
        val update: Update = Update().push("investigations", investigation)
        val options: FindAndModifyOptions = FindAndModifyOptions().returnNew(true).upsert(false)
        return mongoTemplate.findAndModify(
            query,
            update,
            options,
            Consultation::class.java
        )?.investigations?.first { it.id == investigation.id }
    }

    fun updateInvestigationFromConsult(
        physicianID: Int,
        patientID: String,
        date: Date,
        investigation: Investigation
    ): Boolean {
        val query = Query(
            Criteria().andOperator(
                Criteria.where("physicianId").`is`(physicianID),
                Criteria.where("patientId").`is`(patientID),
                Criteria.where("date").`is`(date),
                Criteria.where("investigations._id").`is`(investigation.id),
            )
        )
        val update: Update = Update().set("investigations.$", investigation)
        return mongoTemplate.updateFirst(
            query,
            update,
            Consultation::class.java
        ).matchedCount > 0
    }
}