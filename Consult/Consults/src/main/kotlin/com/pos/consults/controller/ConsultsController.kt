package com.pos.consults.controller

import com.pos.consults.dto.toDTO
import com.pos.consults.persistance.model.Consultation
import com.pos.consults.persistance.model.Diagnostic
import com.pos.consults.persistance.model.Investigation
import com.pos.consults.persistance.repository.ConsultationRepository
import com.pos.consults.persistance.repository.InvestigationRepository
import org.bson.types.ObjectId
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

@RestController
@RequestMapping("/consults")
class ConsultsController (private val _consultationRepository: ConsultationRepository, private val investigationRepository: InvestigationRepository){

    @GetMapping("/")
    fun getConsultations(): ResponseEntity<Any> {
        val consultations = _consultationRepository.findAll()
        return when {
            consultations.isEmpty() -> ResponseEntity.status(HttpStatus.NO_CONTENT).build()
            else -> ResponseEntity.status(HttpStatus.OK).body(
                (consultations.map { it.toDTO() })
            )
        }
    }

    @GetMapping("/{id}")
    fun getConsultation(
        @PathVariable id: String
    ): Consultation? {
        return _consultationRepository.findById(id).orElse(null)
    }

    @PostMapping("/add")
    fun insert(
        @RequestBody consult: Consultation
    ): Consultation {
        print(consult)
        return _consultationRepository.insert(consult)
    }

    @PostMapping("/")
    fun insertI(
        @RequestBody investigation: Investigation
    ): Investigation {
        print(investigation)
        return investigationRepository.insert(investigation)
    }
}