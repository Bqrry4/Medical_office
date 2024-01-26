package com.pos.PDPdb.controller

import com.pos.PDPdb.component.AppointmentForPatientModelAssembler
import com.pos.PDPdb.component.AppointmentForPhysicianModelAssembler
import com.pos.PDPdb.component.PhysicianModelAssembler
import com.pos.PDPdb.dto.*
import com.pos.PDPdb.persistence.model.Appointment
import com.pos.PDPdb.persistence.model.Physician
import com.pos.PDPdb.persistence.repository.AppointmentRepository
import com.pos.PDPdb.persistence.repository.PatientRepository
import com.pos.PDPdb.persistence.repository.PhysicianRepository
import org.springframework.data.domain.Page
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
@RequestMapping("/physicians")
class PhysiciansController(
    private val _physicianRepository: PhysicianRepository,
    private val _physicianModelAssembler: PhysicianModelAssembler,
    private val _pagedResourcesAssembler: PagedResourcesAssembler<PhysicianResponseDTO>,
    private val _appointmentRepository: AppointmentRepository,
    private val _appointmentModelAssembler: AppointmentForPhysicianModelAssembler,
    private val _patientRepository: PatientRepository,
) {
//    @GetMapping("/")
//    fun getAll(): ResponseEntity<Any> {
//        val physicians = _physicianRepository.findAll()
//        return ResponseEntity.status(HttpStatus.OK).body(
//            _physicianModelAssembler.toCollectionModel(physicians.map { it.toDTO() }).add(
//                WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(PhysiciansController::class.java).getAll())
//                    .withSelfRel()
//
//            ).add(
//                WebMvcLinkBuilder.linkTo(
//                    WebMvcLinkBuilder.methodOn(PhysiciansController::class.java).getPhysiciansBySpecialization(null)
//                )
//                    .withRel("filterBySpecialization")
//            ).add(
//                WebMvcLinkBuilder.linkTo(
//                    WebMvcLinkBuilder.methodOn(PhysiciansController::class.java).getPhysiciansByName(null)
//                )
//                    .withRel("filterByName")
//            )
//        )
//    }

    @GetMapping("/")
    fun getAll(
        @RequestParam(required = false) page: Int?,
        @RequestParam(required = false, name = "size", defaultValue = "5") count: Int?,
        @RequestParam(required = true) name: String?,
        @RequestParam(required = true) specialization: String?

    ): ResponseEntity<Any> {
        val physicians: Page<Physician>
        val pageRequest = PageRequest.of(page!!, count!!)
        when
        {
            //if no one is selected
            name == null && specialization == null -> {
                physicians = _physicianRepository.findAll(pageRequest)
            }
            //if name is not selected
            name == null -> {
                physicians = _physicianRepository.findBySpecializationStartingWith(specialization!!, pageRequest)
            }
            //if specialization is not selected
            specialization == null -> {
                physicians = _physicianRepository.findByLastNameStartingWith(name, pageRequest)
            }
            //both are selected
            else -> {
                physicians = _physicianRepository.findByLastNameStartingWithAndSpecializationStartingWith(name,
                    specialization, pageRequest)
            }
        }

        return ResponseEntity.status(HttpStatus.OK).body(
            _pagedResourcesAssembler.toModel(physicians.map { it.toDTO() }, _physicianModelAssembler)
                .add(
                    WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(PhysiciansController::class.java).getByUserId(null))
                        .withRel("filterByUserId")
                )
        )
    }

    //Returning a list with OK as the /physicians/ should return a list, even if the filter is for an unique element
    @GetMapping(value = ["/"], params = ["userId"])
    fun getByUserId(
        @RequestParam(required = true) userId: Int?,
    ): ResponseEntity<Any>
    {
        return ResponseEntity.status(HttpStatus.OK).body(
            _physicianModelAssembler.toCollectionModel(_physicianRepository.findByUserID(userId!!).map { it.toDTO() })
        )
    }

    @GetMapping("/{id}")
    fun getPhysician(
        @PathVariable id: Int
    ): ResponseEntity<Any> {
        return ResponseEntity.status(HttpStatus.OK)
            .body(
                _physicianModelAssembler.toModel(
                    _physicianRepository.findById(id)
                        .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) }.toDTO()
                )
            )
    }

//    @GetMapping(value = ["/"], params = ["specialization"])
//    fun getPhysiciansBySpecialization(
//        @RequestParam(required = true) specialization: String?,
//    ): ResponseEntity<Any> {
//        val physicians = _physicianRepository.findBySpecializationStartingWith(specialization!!)
//        return when {
//            physicians.none() -> ResponseEntity.status(HttpStatus.NO_CONTENT).build()
//            else -> ResponseEntity.status(HttpStatus.OK).body(
//                _physicianModelAssembler.toCollectionModel(physicians.map { it.toDTO() }).add(
//                    WebMvcLinkBuilder.linkTo(
//                        WebMvcLinkBuilder.methodOn(PhysiciansController::class.java)
//                            .getPhysiciansBySpecialization(specialization)
//                    ).withSelfRel()
//                )
//
//            )
//        }
//    }

//    @GetMapping(value = ["/"], params = ["name"])
//    fun getPhysiciansByName(
//        @RequestParam(required = true) name: String?,
//    ): ResponseEntity<Any> {
//        val physicians = _physicianRepository.findByLastNameStartingWith(name!!)
//        return when {
//            physicians.none() -> ResponseEntity.status(HttpStatus.NO_CONTENT).build()
//            else -> ResponseEntity.status(HttpStatus.OK).body(
//                _physicianModelAssembler.toCollectionModel(physicians.map { it.toDTO() }).add(
//                    WebMvcLinkBuilder.linkTo(
//                        WebMvcLinkBuilder.methodOn(PhysiciansController::class.java).getPhysiciansByName(name)
//                    ).withSelfRel()
//                )
//            )
//        }
//    }

    @PostMapping("/")
    fun createPhysician(
        @RequestBody physician: PhysicianRequestDTO
    ): ResponseEntity<Any> {
        return try {
            val newPhysician = _physicianRepository.save(physician.toEntity(0))
            ResponseEntity.status(HttpStatus.CREATED).body(_physicianModelAssembler.toModel(newPhysician.toDTO()))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).build()
        }
    }

    @PutMapping("/{id}")
    fun replacePhysician(
        @PathVariable id: Int, @RequestBody physicianDto: PhysicianRequestDTO
    ): ResponseEntity<Any> {
        return when (val physician = _physicianRepository.findById(id).orElse(null)) {
            null -> ResponseEntity.status(HttpStatus.NOT_FOUND).build()
            else -> {
                physician.userID = physicianDto.userID
                physician.email = physicianDto.email
                physician.firstName = physicianDto.firstName
                physician.lastName = physicianDto.lastName
                physician.phone = physicianDto.phone
                physician.specialization = physicianDto.specialization
                val updatedPhysician = _physicianRepository.save(physician)
                ResponseEntity.status(HttpStatus.OK).body(_physicianModelAssembler.toModel(updatedPhysician.toDTO()))
            }
        }
    }

    @DeleteMapping("/{id}")
    fun removePhysician(
        @PathVariable id: Int
    ): ResponseEntity<Any> {
        return if (_physicianRepository.existsById(id)) {
            _physicianRepository.deleteById(id)
            ResponseEntity.status(HttpStatus.NO_CONTENT).build()
        } else {
            ResponseEntity.status(HttpStatus.NOT_FOUND).build()
        }
    }

    @GetMapping("/{id}/patients")
    fun getPhysicianAppointments(
        @PathVariable id: Int?,
        @RequestParam(required = false) type: String?,
        @RequestParam(required = false) date: String?
    ): ResponseEntity<Any> {

        val appointments: Iterable<Appointment>
        if (date == null) {
            appointments = _appointmentRepository.findByIdPhysicianID(id!!)
        } else when (type) {
            "day" -> {
                val calendar = Calendar.getInstance()
                calendar.set(Calendar.DATE, date.toInt())
                val sqlDate = calendar.time

                appointments = _appointmentRepository.findByPhysicianIDAndDate(id!!, sqlDate)
            }

            "month" -> {
                val calendar = Calendar.getInstance()
                calendar.set(Calendar.MONTH, date.toInt() - 1)
                val sqlDate = calendar.time

                appointments = _appointmentRepository.findByIdPhysicianWithinAMonth(id!!, sqlDate)
            }

            null -> {
                try {
                    val sqlDate = SimpleDateFormat("dd-MM-yyyy").parse(date)

                    appointments = _appointmentRepository.findByPhysicianIDAndDate(id!!, sqlDate)
                } catch (e: IllegalArgumentException) {
                    throw ResponseStatusException(HttpStatus.BAD_REQUEST)
                }
            }

            else -> throw ResponseStatusException(HttpStatus.BAD_REQUEST)
        }

        return when {
            !appointments.none() -> ResponseEntity.status(HttpStatus.OK).body(
                _appointmentModelAssembler.toCollectionModel(appointments.map { it.toPhysicianDTO() }).add(
                    WebMvcLinkBuilder.linkTo(
                        WebMvcLinkBuilder.methodOn(PhysiciansController::class.java)
                            .getPhysicianAppointments(id, type, date)
                    ).withSelfRel()
                )
            )

            else -> ResponseEntity.status(HttpStatus.NO_CONTENT).build()
        }
    }

    @GetMapping("/{physicianId}/patients/{patientId}/date/{date}")
    fun getAppointment(
        @PathVariable physicianId: Int?, @PathVariable patientId: String?, @PathVariable date: String?
    ): ResponseEntity<Any> {
        return try {
            ResponseEntity.status(HttpStatus.OK).body(
                _appointmentModelAssembler.toModel(
                    _appointmentRepository.findByIdPatientIDAndIdPhysicianIDAndIdDate(
                        patientId!!, physicianId!!, SimpleDateFormat("dd-MM-yyyy-HH:mm").parse(date)
                    ).orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) }.toPhysicianDTO()
                )
            )
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).build()
        }
    }

    @PutMapping("/{physicianId}/patients/{patientId}/date/{date}")
    fun updateAppointment(
        @PathVariable physicianId: Int,
        @PathVariable patientId: String,
        @PathVariable date: String,
        @RequestBody appointmentDto: AppointmentUpdateDTO
    ): ResponseEntity<Any> {
        try {
            val sqlDate = SimpleDateFormat("dd-MM-yyyy-HH:mm").parse(date)
            val appointment = _appointmentRepository.findByIdPatientIDAndIdPhysicianIDAndIdDate(
                patientId, physicianId, sqlDate
            ).orElse(null)
            return when (appointment) {
                null -> {
                    ResponseEntity.status(HttpStatus.FORBIDDEN).build()
                }
                else -> {
                    appointment.status = appointmentDto.status
                    val updatedAppointment = _appointmentRepository.save(appointment)
                    ResponseEntity.status(HttpStatus.OK).body(
                        _appointmentModelAssembler.toModel(updatedAppointment.toPhysicianDTO())
                    )
                }
            }
        } catch (e: IllegalArgumentException) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build()
        }
    }

}