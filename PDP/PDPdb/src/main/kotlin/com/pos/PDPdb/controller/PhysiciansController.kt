package com.pos.PDPdb.controller

import com.pos.PDPdb.component.PhysicianModelAssembler
import com.pos.PDPdb.dto.*
import com.pos.PDPdb.persistence.model.Appointment
import com.pos.PDPdb.persistence.model.AppointmentsKey
import com.pos.PDPdb.persistence.model.Physician
import com.pos.PDPdb.persistence.repository.AppointmentRepository
import com.pos.PDPdb.persistence.repository.PhysicianRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.web.PagedResourcesAssembler
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.sql.Date
import java.text.SimpleDateFormat
import java.util.*

@RestController
@RequestMapping("/physicians")
class PhysiciansController(
    private val _physicianRepository: PhysicianRepository,
    private val _physicianModelAssembler: PhysicianModelAssembler,
    private val _pagedResourcesAssembler: PagedResourcesAssembler<PhysicianResponseDTO>,
    private val _appointmentRepository: AppointmentRepository,
) {
    @GetMapping("/")
    fun getAll(): ResponseEntity<Any> {
        val physicians = _physicianRepository.findAll()
        return when {
            physicians.isEmpty() -> ResponseEntity.status(HttpStatus.NO_CONTENT).build()
            else -> ResponseEntity.status(HttpStatus.OK).body(
                _physicianModelAssembler.toCollectionModel(physicians.map { it.toDTO() }).add(
                    WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(PhysiciansController::class.java).getAll())
                        .withSelfRel()
                )
            )
        }
    }

    @GetMapping("")
    fun getPaged(
        @RequestParam(required = true) page: Int,
        @RequestParam(required = false, name = "items_per_page", defaultValue = "5") count: Int
    ): ResponseEntity<Any> {
        val physicians = _physicianRepository.findAll(PageRequest.of(page, count))
        return when {
            physicians.isEmpty -> ResponseEntity.status(HttpStatus.NO_CONTENT).build()
            else -> ResponseEntity.status(HttpStatus.OK).body(
                _pagedResourcesAssembler.toModel(physicians.map { it.toDTO() }, _physicianModelAssembler)
            )
        }
    }

    @GetMapping("/{id}")
    fun getPhysician(
        @PathVariable id: Int
    ): ResponseEntity<Any> {
        return ResponseEntity.status(HttpStatus.OK).body(
                _physicianModelAssembler.toModel(
                    _physicianRepository.findById(id).orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) }
                        .toDTO()
                )
            )
    }

    @GetMapping(params = ["specialization"])
    fun getPhysiciansBySpecialization(
        @RequestParam specialization: String,
    ): ResponseEntity<Any> {
        val physicians = _physicianRepository.findBySpecializationStartingWith(specialization)
        return when {
            physicians.none() -> ResponseEntity.status(HttpStatus.NO_CONTENT).build()
            else -> ResponseEntity.status(HttpStatus.OK).body(
                _physicianModelAssembler.toCollectionModel(physicians.map { it.toDTO() }).add(
                    WebMvcLinkBuilder.linkTo(
                        WebMvcLinkBuilder.methodOn(PhysiciansController::class.java)
                            .getPhysiciansBySpecialization(specialization)
                    ).withSelfRel()
                )

            )
        }
    }

    @GetMapping(params = ["name"])
    fun getPhysiciansByName(
        @RequestParam name: String,
    ): ResponseEntity<Any> {
        val physicians = _physicianRepository.findByLastNameStartingWith(name)
        return when {
            physicians.none() -> ResponseEntity.status(HttpStatus.NO_CONTENT).build()
            else -> ResponseEntity.status(HttpStatus.OK).body(
                _physicianModelAssembler.toCollectionModel(physicians.map { it.toDTO() }).add(
                    WebMvcLinkBuilder.linkTo(
                        WebMvcLinkBuilder.methodOn(PhysiciansController::class.java).getPhysiciansBySpecialization(name)
                    ).withSelfRel()
                )
            )
        }
    }

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
                physician.userId = physicianDto.userId
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
    ) {
        _physicianRepository.deleteById(id)
    }

    @GetMapping("/{id}/patients")
    fun getPhysicianAppointments(
        @PathVariable id: Int,
        @RequestParam(required = false) type: String?,
        @RequestParam(required = false) date: String?
    ): ResponseEntity<Any> {

        if (date == null) {
            return ResponseEntity.status(HttpStatus.OK)
                .body(_appointmentRepository.findByIdPhysicianID(id).map { it.toDTO() })
        } else return when (type) {
            "day" -> {
                val calendar = Calendar.getInstance()
                calendar.set(Calendar.DATE, date.toInt())
                val sqlDate = Date(calendar.time.time)

                ResponseEntity.status(HttpStatus.OK)
                    .body(_appointmentRepository.findByIdPhysicianIDAndIdDate(id, sqlDate).map { it.toDTO() })
            }

            "month" -> {
                val calendar = Calendar.getInstance()
                calendar.set(Calendar.MONTH, date.toInt() - 1)
                val sqlDate = Date(calendar.time.time)
                print(calendar.toString())
                ResponseEntity.status(HttpStatus.OK)
                    .body(_appointmentRepository.findByIdPhysicianWithinAMonth(id, sqlDate).map { it.toDTO() })
            }

            null -> {
                try {
                    val sqlDate = Date(SimpleDateFormat("dd-MM-yyyy").parse(date).time)

                    ResponseEntity.status(HttpStatus.OK)
                        .body(_appointmentRepository.findByIdPhysicianIDAndIdDate(id, sqlDate).map { it.toDTO() })
                } catch (e: IllegalArgumentException) {
                    ResponseEntity.status(HttpStatus.BAD_REQUEST).build()
                }
            }

            else -> ResponseEntity.status(HttpStatus.BAD_REQUEST).build()
        }
    }
}