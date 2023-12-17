package com.pos.PDPdb.controller

import com.pos.PDPdb.component.AppointmentModelAssembler
import com.pos.PDPdb.component.PatientModelAssembler
import com.pos.PDPdb.dto.*
import com.pos.PDPdb.persistence.model.Appointment
import com.pos.PDPdb.persistence.model.AppointmentsKey
import com.pos.PDPdb.persistence.repository.AppointmentRepository
import com.pos.PDPdb.persistence.repository.PatientRepository
import com.pos.PDPdb.persistence.repository.PhysicianRepository
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.sql.Date
import java.text.SimpleDateFormat
import java.util.*

@RestController
@RequestMapping("/patients")
class PatientsController(
    private val _patientRepository: PatientRepository,
    private val _patientModelAssembler: PatientModelAssembler,
    private val _appointmentRepository: AppointmentRepository,
    private val _appointmentModelAssembler: AppointmentModelAssembler,

    private val _physicianRepository: PhysicianRepository
) {

    @GetMapping("/")
    fun getAll(): ResponseEntity<Any> {
        val patients = _patientRepository.findAll()
        return when {
            patients.isEmpty() -> ResponseEntity.status(HttpStatus.NO_CONTENT).build()
            else -> ResponseEntity.status(HttpStatus.OK).body(
                _patientModelAssembler.toCollectionModel(patients.map { it.toDTO() })
            )
        }
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
            patient.birthDay = patientDto.birthDay
            patient.isActive = patientDto.isActive
            val updatedPatient = _patientRepository.save(patient)
            ResponseEntity.status(HttpStatus.OK).body(_patientModelAssembler.toModel(updatedPatient.toDTO()))
        }
    }

    @DeleteMapping("/{id}")
    fun disablePatient(
        @PathVariable id: String
    ) {
        _patientRepository.setPatientInactive(id)
    }

    @GetMapping("/{id}/physicians")
    fun getPatientAppointments(
        @PathVariable id: String,
        @RequestParam(required = false) type: String?,
        @RequestParam(required = false) date: String?
    ): ResponseEntity<Any> {

        if (date == null) {
            return ResponseEntity.status(HttpStatus.OK)
                .body(
                    _appointmentModelAssembler.toCollectionModel(
                        _appointmentRepository.findByIdPatientID(id).map { it.toDTO() }).add(
                        WebMvcLinkBuilder.linkTo(
                            WebMvcLinkBuilder.methodOn(PatientsController::class.java)
                                .getPatientAppointments(id, type, date)
                        ).withSelfRel()
                    )
                )
        } else return when (type) {
            "day" -> {
                val calendar = Calendar.getInstance()
                calendar.set(Calendar.DATE, date.toInt())
                val sqlDate = Date(calendar.time.time)

                ResponseEntity.status(HttpStatus.OK).body(
                    _appointmentModelAssembler.toCollectionModel(
                        _appointmentRepository.findByIdPatientIDAndIdDate(id, sqlDate)
                            .map { it.toDTO() }).add(
                        WebMvcLinkBuilder.linkTo(
                            WebMvcLinkBuilder.methodOn(PatientsController::class.java)
                                .getPatientAppointments(id, type, date)
                        ).withSelfRel()
                    )
                )

            }

            "month" -> {
                val calendar = Calendar.getInstance()
                calendar.set(Calendar.MONTH, date.toInt() - 1)
                val sqlDate = Date(calendar.time.time)
                print(calendar.toString())
                ResponseEntity.status(HttpStatus.OK)
                    .body(
                        _appointmentModelAssembler.toCollectionModel(
                            _appointmentRepository.findByIdPatientWithinAMonth(id, sqlDate).map { it.toDTO() }).add(
                            WebMvcLinkBuilder.linkTo(
                                WebMvcLinkBuilder.methodOn(PatientsController::class.java)
                                    .getPatientAppointments(id, type, date)
                            ).withSelfRel()
                        )
                    )
            }

            null -> {
                try {
                    val sqlDate = Date(SimpleDateFormat("dd-MM-yyyy").parse(date).time)

                    ResponseEntity.status(HttpStatus.OK)
                        .body(
                            _appointmentModelAssembler.toCollectionModel(
                                _appointmentRepository.findByIdPatientIDAndIdDate(id, sqlDate).map { it.toDTO() }).add(
                                WebMvcLinkBuilder.linkTo(
                                    WebMvcLinkBuilder.methodOn(PatientsController::class.java)
                                        .getPatientAppointments(id, type, date)
                                ).withSelfRel()
                            )
                        )
                } catch (e: IllegalArgumentException) {
                    ResponseEntity.status(HttpStatus.BAD_REQUEST).build()
                }
            }

            else -> ResponseEntity.status(HttpStatus.BAD_REQUEST).build()
        }
    }

    @GetMapping("/{patientId}/physicians/{physicianId}")
    fun getAppointment(
        @PathVariable patientId: String, @PathVariable physicianId: Int, @RequestParam(required = true) date: String
    ): ResponseEntity<Any> {
        return try {
            ResponseEntity.status(HttpStatus.OK).body(
                _appointmentModelAssembler.toModel(
                    _appointmentRepository.findByIdPatientIDAndIdPhysicianIDAndIdDate(
                        patientId, physicianId, Date(SimpleDateFormat("dd-MM-yyyy").parse(date).time)
                    ).orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) }.toDTO()
                )
            )
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).build()
        }
    }

    @PutMapping("/{patientId}/physicians/{physicianId}")
    fun updateAppointment(
        @PathVariable patientId: String,
        @PathVariable physicianId: Int,
        @RequestParam(required = true) date: String,
        @RequestBody appointmentDto: AppointmentUpdateDTO
    ): ResponseEntity<Any> {
        try {
            val sqlDate = Date(SimpleDateFormat("dd-MM-yyyy").parse(date).time)
            val appointment = _appointmentRepository.findByIdPatientIDAndIdPhysicianIDAndIdDate(
                patientId, physicianId, sqlDate
            ).orElse(null)
            return when (appointment) {
                null -> {
                    val patient = _patientRepository.findById(patientId)
                        .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) }
                    val physician = _physicianRepository.findById(physicianId)
                        .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) }
                    var newAppointment = Appointment(
                        AppointmentsKey(patientId, physicianId, sqlDate), patient, physician, appointmentDto.status
                    )
                    newAppointment = _appointmentRepository.save(newAppointment)
                    ResponseEntity.status(HttpStatus.CREATED).body(
                        _appointmentModelAssembler.toModel(newAppointment.toDTO())
                    )
                }

                else -> {
                    appointment.status = appointmentDto.status
                    val updatedAppointment = _appointmentRepository.save(appointment)
                    ResponseEntity.status(HttpStatus.OK).body(
                        _appointmentModelAssembler.toModel(updatedAppointment.toDTO()))
                }
            }
        } catch (e: IllegalArgumentException) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build()
        }
    }
}
