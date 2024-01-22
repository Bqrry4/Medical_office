package com.pos.consults.controller

import com.pos.consults.component.InvestigationModelAssembler
import com.pos.consults.dto.*
import com.pos.consults.persistance.model.Investigation
import com.pos.consults.persistance.repository.ConsultationRepository
import com.pos.consults.services.ConsultCollectionService
import com.pos.consults.utils.Utils
import org.bson.types.ObjectId
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import org.springframework.web.util.UriComponentsBuilder

@RestController
class InvestigationController(
    private val _consultationService: ConsultCollectionService,
    private val _investigationModelAssembler: InvestigationModelAssembler,
    private val _consultationRepository: ConsultationRepository
) {
//    @GetMapping("/test")
//    fun test() {
//        _consultationRepository.updateConsultInvestigations(1, "1", Utils.parseDatetime("23-12-2023-12:00"), Investigation(ObjectId().toHexString(), "23", 23, "Bro"))
//    }

    @GetMapping("/physicians/{physicianID}/patients/{patientID}/date/{date}/consult/investigations")
    fun getConsultInvestigations(
        @PathVariable(required = true) patientID: String,
        @PathVariable(required = true) physicianID: Int,
        @PathVariable(required = true) date: String
    ): ResponseEntity<Any> {
        val consult = _consultationService.getConsult(physicianID, patientID, Utils.parseDatetime(date))
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

        val investigations = consult.investigations
        return ResponseEntity.status(HttpStatus.OK).body(
            _investigationModelAssembler.toCollectionModel(investigations.map { it.toDTO() })
        )
    }

    @PostMapping("/physicians/{physicianID}/patients/{patientID}/date/{date}/consult/investigations")
    fun createConsultInvestigations(
        @PathVariable(required = true) patientID: String,
        @PathVariable(required = true) physicianID: Int,
        @PathVariable(required = true) date: String,
        @RequestBody investigationDto: InvestigationRequestDTO
    ): ResponseEntity<Any> {

        val newInvestigationId: String = ObjectId().toHexString()
        val investigation = _consultationService.addInvestigationToConsult(
            physicianID,
            patientID,
            Utils.parseDatetime(date),
            investigationDto.toEntity(newInvestigationId)
        ) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

        return ResponseEntity.created(
            ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{id}").buildAndExpand(newInvestigationId).toUri()
        ).body(
            _investigationModelAssembler.toModel(
                investigation.toDTO()
            )
        )
    }

    @GetMapping("/physicians/{physicianID}/patients/{patientID}/date/{date}/consult/investigations/{investigationID}")
    fun getOneInvestigation(
        @PathVariable(required = true) patientID: String,
        @PathVariable(required = true) physicianID: Int,
        @PathVariable(required = true) date: String,
        @PathVariable(required = true) investigationID: String
    ): ResponseEntity<Any> {
        val investigation = _consultationService.getConsultInvestigation(
            physicianID, patientID, Utils.parseDatetime(date), investigationID
        ) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

        return ResponseEntity.status(HttpStatus.OK).body(
            _investigationModelAssembler.toModel(
                investigation.toDTO()
            )
        )
    }

    @PutMapping("/physicians/{physicianID}/patients/{patientID}/date/{date}/consult/investigations/{investigationID}")
    fun updateInvestigation(
        @PathVariable(required = true) patientID: String,
        @PathVariable(required = true) physicianID: Int,
        @PathVariable(required = true) date: String,
        @PathVariable(required = true) investigationID: String,
        @RequestBody investigationDto: InvestigationRequestDTO
    ): ResponseEntity<Any> {

        val status = _consultationService.updateInvestigationFromConsult(
            physicianID,
            patientID,
            Utils.parseDatetime(date),
            investigationDto.toEntity(investigationID)
        )
        if (!status) throw ResponseStatusException(HttpStatus.NOT_FOUND)

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build()
    }
}