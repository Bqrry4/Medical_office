package com.pos.PDPdb.controller

import com.pos.PDPdb.component.AppointmentForPatientModelAssembler
import com.pos.PDPdb.component.PatientModelAssembler
import com.pos.PDPdb.dto.*
import com.pos.PDPdb.persistence.model.Appointment
import com.pos.PDPdb.persistence.model.AppointmentsKey
import com.pos.PDPdb.persistence.model.Status
import com.pos.PDPdb.persistence.repository.AppointmentRepository
import com.pos.PDPdb.persistence.repository.PatientRepository
import com.pos.PDPdb.persistence.repository.PhysicianRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.web.PagedResourcesAssembler
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
    private val _physicianRepository: PhysicianRepository,
    private val _patientModelAssembler: PatientModelAssembler,
    private val _appointmentRepository: AppointmentRepository,
    private val _appointmentForPatientModelAssembler: AppointmentForPatientModelAssembler,
    private val _pagedResourcesAssembler: PagedResourcesAssembler<PatientResponseDTO>
) {

    @GetMapping("/")
    fun getAll(
        @RequestParam(required = false, name ="page", defaultValue = "0") page: Int?,
        @RequestParam(required = false, name = "size", defaultValue = "5") count: Int?
    ): ResponseEntity<Any> {
        return ResponseEntity.status(HttpStatus.OK).body(
            _pagedResourcesAssembler.toModel(_patientRepository.findAll(PageRequest.of(page!!, count!!)).map { it.toDTO() }, _patientModelAssembler)
                .add(
                    WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(PatientsController::class.java).getByUserId(null))
                    .withRel("filterByUserId")
                )
        )
    }

    //Returning a list with OK as the /patients/ should return a list, even if the filter is for an unique element
    @GetMapping(value = ["/"], params = ["userId"])
    fun getByUserId(
        @RequestParam(required = true) userId: Int?,
    ): ResponseEntity<Any>
    {
        return ResponseEntity.status(HttpStatus.OK).body(
            _patientModelAssembler.toCollectionModel(_patientRepository.findByUserID(userId!!).map { it.toDTO() })
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
        @PathVariable id: String?,
        @RequestParam(required = false) type: String?,
        @RequestParam(required = false) date: String?
    ): ResponseEntity<Any> {

        val appointments: Iterable<Appointment>
        if (date == null) {
            appointments = _appointmentRepository.findByIdPatientID(id!!)
        } else when (type) {
            "day" -> {
                val calendar = Calendar.getInstance()
                calendar.set(Calendar.DATE, date.toInt())
                val sqlDate = calendar.time

                appointments = _appointmentRepository.findByPatientIDAndDate(id!!, sqlDate)
            }

            "month" -> {
                val calendar = Calendar.getInstance()
                calendar.set(Calendar.MONTH, date.toInt() - 1)
                val sqlDate = calendar.time

                appointments = _appointmentRepository.findByIdPatientWithinAMonth(id!!, sqlDate)
            }

            null -> {
                try {
                    val sqlDate = SimpleDateFormat("dd-MM-yyyy").parse(date)

                    appointments = _appointmentRepository.findByPatientIDAndDate(id!!, sqlDate)
                } catch (e: IllegalArgumentException) {
                    throw ResponseStatusException(HttpStatus.BAD_REQUEST)
                }
            }

            else -> throw ResponseStatusException(HttpStatus.BAD_REQUEST)
        }

        return ResponseEntity.status(HttpStatus.OK).body(
            _appointmentForPatientModelAssembler.toCollectionModel(appointments.map { it.toPatientDTO() }).add(
                WebMvcLinkBuilder.linkTo(
                    WebMvcLinkBuilder.methodOn(PatientsController::class.java)
                        .getPatientAppointments(id, type, date)
                ).withSelfRel()
            )
        )
    }

    @GetMapping("/{patientId}/physicians/{physicianId}/date/{date}")
    fun getAppointment(
        @PathVariable physicianId: Int?, @PathVariable patientId: String?, @PathVariable date: String?
    ): ResponseEntity<Any> {
        return try {
            ResponseEntity.status(HttpStatus.OK).body(
                _appointmentForPatientModelAssembler.toModel(
                    _appointmentRepository.findByIdPatientIDAndIdPhysicianIDAndIdDate(
                        patientId!!, physicianId!!, SimpleDateFormat("dd-MM-yyyy-HH:mm").parse(date)
                    ).orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) }.toPatientDTO()
                )
            )
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).build()
        }
    }

    @PutMapping("/{patientId}/physicians/{physicianId}/date/{date}")
    fun createAppointment(
        @PathVariable physicianId: Int,
        @PathVariable patientId: String,
        @PathVariable date: String
    ): ResponseEntity<Any> {
        try {
            val sqlDate = SimpleDateFormat("dd-MM-yyyy-HH:mm").parse(date)
            val appointment = _appointmentRepository.findByIdPatientIDAndIdPhysicianIDAndIdDate(
                patientId, physicianId, sqlDate
            ).orElse(null)
            return when (appointment) {
                null -> {
                    val patient = _patientRepository.findById(patientId)
                        .orElseThrow { ResponseStatusException(HttpStatus.BAD_REQUEST) }
                    val physician = _physicianRepository.findById(physicianId)
                        .orElseThrow { ResponseStatusException(HttpStatus.BAD_REQUEST) }
                    var newAppointment = Appointment(
                        AppointmentsKey(patientId, physicianId, sqlDate), patient, physician, Status.NOT_COMMITTED
                    )
                    newAppointment = _appointmentRepository.save(newAppointment)
                    ResponseEntity.status(HttpStatus.CREATED).body(
                        _appointmentForPatientModelAssembler.toModel(newAppointment.toPatientDTO())
                    )
                }

                else -> {
                    ResponseEntity.status(HttpStatus.FORBIDDEN).build()
                }
            }
        } catch (e: IllegalArgumentException) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build()
        }
    }


    @DeleteMapping("/{patientId}/physicians/{physicianId}/date/{date}")
    fun deleteAppointment(
        @PathVariable physicianId: Int, @PathVariable patientId: String, @PathVariable date: String
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
