package com.pos.PDPdb.controller

import com.pos.PDPdb.component.AppointmentModelAssembler
import com.pos.PDPdb.component.PatientModelAssembler
import com.pos.PDPdb.dto.*
import com.pos.PDPdb.persistence.model.Appointment
import com.pos.PDPdb.persistence.model.AppointmentsKey
import com.pos.PDPdb.persistence.repository.AppointmentRepository
import com.pos.PDPdb.persistence.repository.PatientRepository
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.text.SimpleDateFormat
import java.util.*

@RestController
@RequestMapping("/patients")
class PatientsController(
    private val _patientRepository: PatientRepository,
    private val _patientModelAssembler: PatientModelAssembler,
    private val _appointmentRepository: AppointmentRepository,
    private val _appointmentModelAssembler: AppointmentModelAssembler
) {

    @GetMapping("/")
    fun getAll(): ResponseEntity<Any> {
        return ResponseEntity.status(HttpStatus.OK).body(
            _patientModelAssembler.toCollectionModel(_patientRepository.findAll().map { it.toDTO() })
        )
    }

    @GetMapping("/{id}")
    fun getPatient(
        @PathVariable id: String
    ): ResponseEntity<Any> {
        return ResponseEntity.status(HttpStatus.OK).body(
            _patientModelAssembler.toModel(
                _patientRepository.findById(id).orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) }.toDTO()
            )
        )
    }

    @PutMapping("/{id}")
    fun replacePatient(
        @PathVariable id: String, @RequestBody patientDto: PatientRequestDTO
    ): ResponseEntity<Any> = when (val patient = _patientRepository.findById(id).orElse(null)) {
        null -> {
            var newPatient = patientDto.toEntity(id)
            newPatient = _patientRepository.save(newPatient)
            ResponseEntity.status(HttpStatus.CREATED).body(_patientModelAssembler.toModel(newPatient.toDTO()))
        }

        else -> {
            patient.userID = patientDto.userID
            patient.email = patientDto.email
            patient.firstName = patientDto.firstName
            patient.lastName = patientDto.lastName
            patient.phone = patientDto.phone
            patient.birthDate = patientDto.birthDay
            patient.isActive = patientDto.isActive
            val updatedPatient = _patientRepository.save(patient)
            ResponseEntity.status(HttpStatus.OK).body(_patientModelAssembler.toModel(updatedPatient.toDTO()))
        }
    }

    @DeleteMapping("/{id}")
    fun disablePatient(
        @PathVariable id: String
    ): ResponseEntity<Any> {
        return if (_patientRepository.existsById(id)) {
            _patientRepository.setPatientInactive(id)
            ResponseEntity.status(HttpStatus.NO_CONTENT).build()
        } else {
            ResponseEntity.status(HttpStatus.NOT_FOUND).build()
        }
    }

    @GetMapping("/{id}/physicians")
    fun getPatientAppointments(
        @PathVariable id: String,
        @RequestParam(required = false) type: String?,
        @RequestParam(required = false) date: String?
    ): ResponseEntity<Any> {

        val appointments: Iterable<Appointment>
        if (date == null) {
            appointments = _appointmentRepository.findByIdPatientID(id)
        } else when (type) {
            "day" -> {
                val calendar = Calendar.getInstance()
                calendar.set(Calendar.DATE, date.toInt())
                val sqlDate = calendar.time

                appointments = _appointmentRepository.findByPatientIDAndDate(id, sqlDate)
            }

            "month" -> {
                val calendar = Calendar.getInstance()
                calendar.set(Calendar.MONTH, date.toInt() - 1)
                val sqlDate = calendar.time

                appointments = _appointmentRepository.findByIdPatientWithinAMonth(id, sqlDate)
            }

            null -> {
                try {
                    val sqlDate = SimpleDateFormat("dd-MM-yyyy").parse(date)

                    appointments = _appointmentRepository.findByPatientIDAndDate(id, sqlDate)
                } catch (e: IllegalArgumentException) {
                    throw ResponseStatusException(HttpStatus.BAD_REQUEST)
                }
            }

            else -> throw ResponseStatusException(HttpStatus.BAD_REQUEST)
        }

        return ResponseEntity.status(HttpStatus.OK).body(
            _appointmentModelAssembler.toCollectionModel(appointments.map { it.toDTO() }).add(
                WebMvcLinkBuilder.linkTo(
                    WebMvcLinkBuilder.methodOn(PatientsController::class.java)
                        .getPatientAppointments(id, type, date)
                ).withSelfRel()
            )
        )
    }

    @DeleteMapping("/{patientId}/physicians/{physicianId}")
    fun deleteAppointment(
        @PathVariable physicianId: Int, @PathVariable patientId: String, @RequestParam(required = true) date: String
    ): ResponseEntity<Any> {
        val sqlDate = SimpleDateFormat("dd-MM-yyyy-HH:mm").parse(date)
        val apID = AppointmentsKey(patientId, physicianId, sqlDate)
        return if (_appointmentRepository.existsById(apID)) {
            _appointmentRepository.deleteById(apID)
            ResponseEntity.status(HttpStatus.NO_CONTENT).build()
        } else {
            ResponseEntity.status(HttpStatus.NOT_FOUND).build()
        }
    }
}
