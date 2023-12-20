package com.pos.consults.controller

import com.pos.consults.dto.ConsultationRequestDTO
import com.pos.consults.dto.InvestigationDTO
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
import org.springframework.web.server.ResponseStatusException
import java.text.SimpleDateFormat
import java.time.LocalDateTime

@RestController
@RequestMapping("/physicians")
class ConsultsController(
    private val _consultationRepository: ConsultationRepository,
    private val investigationRepository: InvestigationRepository
) {

//    @GetMapping("/")
//    fun getConsultations(): ResponseEntity<Any> {
//        val consultations = _consultationRepository.findAll()
//        return when {
//            consultations.isEmpty() -> ResponseEntity.status(HttpStatus.NO_CONTENT).build()
//            else -> ResponseEntity.status(HttpStatus.OK).body(
//                (consultations.map { it.toDTO() })
//            )
//        }
//    }

    @GetMapping("/{physicianID}/patients")
    fun getPhysicianConsultations(
        @PathVariable physicianID: Int
    ): ResponseEntity<Any> {
        val consultations = _consultationRepository.findAllByPhysicianId(physicianID)
        return when {
            consultations.isEmpty() -> ResponseEntity.status(HttpStatus.NO_CONTENT).build()
            else -> ResponseEntity.status(HttpStatus.OK).body(
                (consultations.map { it.toDTO() })
            )
        }
    }

    @GetMapping("/{physicianID}/patients/{physicianID}")
    fun getOneConsultation(
        @PathVariable(required = true) patientID: String,
        @PathVariable(required = true) physicianID: Int,
        @RequestParam(required = true) date: String
    ): ResponseEntity<Any> {
        return try {
            ResponseEntity.status(HttpStatus.OK).body(
                //_appointmentModelAssembler.toModel(
                _consultationRepository.findByPatientIdAndPhysicianIdAndData(
                    patientID, physicianID, SimpleDateFormat("dd-MM-yyyy-HH:mm").parse(date)
                ).orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) }.toDTO()
            )
            //)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).build()
        }
    }

    @PutMapping("/{physicianID}/patients/{physicianID}")
    fun updateConsultation(
        @PathVariable(required = true) patientID: String,
        @PathVariable(required = true) physicianID: Int,
        @RequestParam(required = true) date: String,
        @RequestBody consultationDto: ConsultationRequestDTO
    ): ResponseEntity<Any> {
        try {
            val sqlDate = SimpleDateFormat("dd-MM-yyyy-HH:mm").parse(date)
            val consultation = _consultationRepository.findByPatientIdAndPhysicianIdAndData(
                patientID, physicianID, sqlDate
            ).orElse(null)
            return when (consultation) {
                null -> {
                    var newConsultation = Consultation(
                        ObjectId(), patientID, physicianID, sqlDate, null, emptyList()
                    )
                    newConsultation = _consultationRepository.save(newConsultation)
                    ResponseEntity.status(HttpStatus.CREATED).body(
                        /*_appointmentModelAssembler.toModel*/(newConsultation.toDTO())
                    )
                }
                else -> {
                    consultation.diagnostic = consultationDto.diagnostic
                    val updatedConsultation = _consultationRepository.save(consultation)
                    ResponseEntity.status(HttpStatus.OK).body(
                        /*_appointmentModelAssembler.toModel*/(updatedConsultation.toDTO())
                    )
                }
            }
        } catch (e: IllegalArgumentException) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build()
        }
    }

    @DeleteMapping(value = ["/"], params = ["patientID", "physicianID", "date"])
    fun deleteAppointment(
        @PathVariable(required = true) patientID: String,
        @PathVariable(required = true) physicianID: Int,
        @RequestParam(required = true) date: String
    ) {
        try {
            _consultationRepository.deleteByPatientIdAndPhysicianIdAndData(patientID, physicianID, SimpleDateFormat("dd-MM-yyyy-HH:mm").parse(date))
        } catch (_: IllegalArgumentException) {
        }
    }

    @GetMapping(value = ["/investigation"], params = ["patientID", "physicianID", "date"])
    fun getInvestigations(
        @RequestParam(required = true) patientID: String,
        @RequestParam(required = true) physicianID: Int,
        @RequestParam(required = true) date: String,
    ): ResponseEntity<Any> {
        return try {
            ResponseEntity.status(HttpStatus.OK).body(
                //_appointmentModelAssembler.toModel(
                _consultationRepository.findByPatientIdAndPhysicianIdAndData(
                    patientID, physicianID, SimpleDateFormat("dd-MM-yyyy-HH:mm").parse(date)
                ).orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) }.toDTO()
            )
            //)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).build()
        }
    }

    @GetMapping(value = ["/investigation/{id}"])
    fun getInv(
        @PathVariable id: String
    ): Investigation? {
        val a = investigationRepository.findById(id).orElse(null)
        println(a)
        return a
    }

    @PutMapping(value = ["/investigation/{id}"])
    fun replaceInv(
        @PathVariable id: String,
        @RequestBody body: InvestigationDTO
    ): Investigation? {
        val a = investigationRepository.findById(id).orElse(null)
        a.name = body.name
        investigationRepository.save(a)

        println(a)
        return a
    }



}