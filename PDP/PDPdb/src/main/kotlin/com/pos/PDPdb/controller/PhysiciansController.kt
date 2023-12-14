package com.pos.PDPdb.controller

import com.pos.PDPdb.persistence.model.Patient
import com.pos.PDPdb.persistence.model.Physician
import com.pos.PDPdb.persistence.repository.PhysicianRepository
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/physicians")
class PhysiciansController(private val physicianRepository: PhysicianRepository) {

    @GetMapping("/{id}")
    fun getPhysician(
        @PathVariable id: Long
    ): Physician {

        val physician: Physician = physicianRepository.findById(id).orElse(null)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
        return physician
    }

    @GetMapping(params = ["specialization"])
    fun getPhysiciansBySpecialization(
        @RequestParam specialization: String,
    ): Iterable<Physician> {
        val physicians = physicianRepository.findBySpecializationStartingWith(specialization)
        if (physicians.none()) throw ResponseStatusException(HttpStatus.NOT_FOUND)
        return physicians
    }

    @GetMapping(params = ["name"])
    fun getPhysiciansByName(
        @RequestParam name: String,
    ): Iterable<Physician> {
        return physicianRepository.findByLastNameStartingWith(name)
    }


    @PostMapping
    fun createPhysician(
        @RequestBody physician: Physician
    ): ResponseEntity<Any> {
        return try {
            val newPhysician = physicianRepository.save(physician)
            ResponseEntity.status(HttpStatus.CREATED).body(newPhysician)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).build()
        }
    }

//    @PutMapping("/{id}")
//    fun replacePhysician(
//        @PathVariable id: Long,
//        @RequestBody physician: Physician
//    ): ResponseEntity<Any> {
//        return try{
//            val newPatient = physicianRepository.save(patient)
//            ResponseEntity.status(HttpStatus.CREATED).body(newPatient)
//        }
//        catch (e : Exception)
//        {
//            ResponseEntity.status(HttpStatus.BAD_REQUEST).build()
//        }
//    }


}