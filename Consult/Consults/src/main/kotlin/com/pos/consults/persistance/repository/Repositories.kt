package com.pos.consults.persistance.repository

import com.pos.consults.persistance.model.Consultation
import com.pos.consults.persistance.model.Investigation
import org.springframework.data.mongodb.repository.MongoRepository

interface ConsultationRepository : MongoRepository<Consultation, String> {
}

interface InvestigationRepository : MongoRepository<Investigation, String> {
}