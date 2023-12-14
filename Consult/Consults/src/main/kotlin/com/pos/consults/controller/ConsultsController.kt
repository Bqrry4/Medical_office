package com.pos.consults.controller

import com.pos.consults.persistance.model.Consultation
import com.pos.consults.persistance.model.Diagnostic
import com.pos.consults.persistance.model.Investigation
import com.pos.consults.persistance.repository.ConsultationRepository
import com.pos.consults.persistance.repository.InvestigationRepository
import org.bson.types.ObjectId
import org.springframework.web.bind.annotation.*
import java.sql.Date
import java.util.*

@RestController
@RequestMapping("/consults")
class ConsultsController (private val consultationRepository: ConsultationRepository, private val investigationRepository: InvestigationRepository){

    @GetMapping("/")
    fun getConsultations(
    ): MutableList<Consultation> {
        return consultationRepository.findAll()
    }

    @GetMapping("/{id}")
    fun getConsultation(
        @PathVariable id: String
    ): Consultation {
        print(id)
        return consultationRepository.insert(Consultation(ObjectId(), 1, 3, Date(22,9,2023), Diagnostic.ILL, emptyList()))
    }

    @PostMapping("/add")
    fun insert(
        @RequestBody consult: Consultation
    ): Consultation {
        print(consult)
        return consultationRepository.insert(consult)
    }

    @PostMapping("/")
    fun insertI(
        @RequestBody investigation: Investigation
    ): Investigation {
        print(investigation)
        return investigationRepository.insert(investigation)
    }
}