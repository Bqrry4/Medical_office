package com.pos.consults.controller

import com.pos.consults.component.ConsultModelAssembler
import com.pos.consults.dto.ConsultationRequestDTO
import com.pos.consults.dto.toDTO
import com.pos.consults.persistance.model.Consultation
import com.pos.consults.persistance.model.Investigation
import com.pos.consults.persistance.repository.ConsultationRepository
import com.pos.consults.services.ConsultCollectionService
import com.pos.consults.utils.Utils
import jakarta.servlet.http.HttpServletRequest
import org.bson.types.ObjectId
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.text.SimpleDateFormat

@RestController
class ConsultsController(
    private val _consultationService: ConsultCollectionService,
    private val _consultModelAssembler: ConsultModelAssembler
) {

    @GetMapping("/")
    fun test() {

        val list = mutableListOf<Investigation>()
        list.add(Investigation(id = ObjectId().toHexString(), description = "cc", duration = 3, result = "c"))
        list.add(Investigation(id = ObjectId().toHexString(), description = "cc", duration = 3, result = "cc"))
        val a = Consultation(
            patientId = "1",
            physicianId = 1,
            diagnostic = null,
            date = Utils.parseDatetime("23-12-2023-12:00"),
            investigations = list
        )
//        _consultationService.insert(a)

    }

    @GetMapping("/physicians/{physicianID}/patients/{patientID}/date/{date}/consult")
    fun getOneConsultation(
        @PathVariable(required = true) physicianID: Int,
        @PathVariable(required = true) patientID: String,
        @PathVariable(required = true) date: String
    ): ResponseEntity<Any> {
        val consult = _consultationService.getConsult(
            physicianID, patientID, Utils.parseDatetime(date)
        ) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

        return ResponseEntity.status(HttpStatus.OK).body(
            _consultModelAssembler.toModel(
                consult.toDTO()
            )
        )
    }

    @PutMapping("/physicians/{physicianID}/patients/{patientID}/date/{date}/consult")
    fun updateConsultation(
        @PathVariable(required = true) physicianID: Int,
        @PathVariable(required = true) patientID: String,
        @PathVariable(required = true) date: String,
        @RequestBody consultationDto: ConsultationRequestDTO
    ): ResponseEntity<Any> {
        val dbDate = Utils.parseDatetime(date)
        val consultation = _consultationService.getConsult(
            physicianID, patientID, dbDate
        )
        return when (consultation) {
            null -> {
                val newConsultation =
                    _consultationService.createConsult(physicianID, patientID, dbDate, consultationDto)
                ResponseEntity.status(HttpStatus.CREATED).body(
                    _consultModelAssembler.toModel(newConsultation.toDTO())
                )
            }

            else -> {
                val updatedConsultation = _consultationService.updateConsult(consultation, consultationDto)
                ResponseEntity.status(HttpStatus.OK).body(
                    _consultModelAssembler.toModel(updatedConsultation.toDTO())
                )
            }
        }
    }

    @DeleteMapping("/physicians/{physicianID}/patients/{patientID}/date/{date}/consult")
    fun deleteAppointment(
        @PathVariable(required = true) patientID: String,
        @PathVariable(required = true) physicianID: Int,
        @PathVariable(required = true) date: String
    ): ResponseEntity<Any> {
        val consult = _consultationService.getConsult(
            physicianID,
            patientID,
            Utils.parseDatetime(date)
        )
        return consult?.let {
            _consultationService.deleteConsult(consult)
            ResponseEntity.status(HttpStatus.NO_CONTENT).build()
        } ?: ResponseEntity.status(HttpStatus.NOT_FOUND).build()

    }
}