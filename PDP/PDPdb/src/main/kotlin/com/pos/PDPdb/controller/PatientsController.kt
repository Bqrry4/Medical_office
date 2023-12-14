package com.pos.PDPdb.controller

import com.pos.PDPdb.persistence.model.Patient
import com.pos.PDPdb.persistence.repository.PatientRepository
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.util.*

@RestController
@RequestMapping("/patients")
class PatientsController(private val patientRepository: PatientRepository) {

    @GetMapping("/{id}")
    fun getPatient(
        @PathVariable id: String
    ): Patient {

        val patient : Patient = patientRepository.findById(id).orElse(null)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
        return patient
    }

//    @PostMapping("/")
//    fun createPatient(
//        @RequestBody patient: Patient
//    ): ResponseEntity<Any> {
//        return try{
//            val newPatient = patientRepository.save(patient)
//            ResponseEntity.status(HttpStatus.CREATED).body(newPatient)
//        }
//        catch (e : Exception)
//        {
//            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.message)
//        }
//    }

    @PutMapping("/{id}")
    fun replacePatient(
        @PathVariable id: String,
        @RequestBody patient: Patient
    ): ResponseEntity<Any> {
        return try{
            val newPatient = patientRepository.save(patient)
            ResponseEntity.status(HttpStatus.CREATED).body(newPatient)
        }
        catch (e : Exception)
        {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).build()
        }
    }

    @DeleteMapping("/{id}")
    fun removePatient(
        @PathVariable id: String
    ) {
            patientRepository.deleteById(id)
    }


    @GetMapping("/{id}/physicians")
    fun getPatients(
        @PathVariable id: String,
        @RequestParam(required = false) date: String?,
        @RequestParam(required = false) type: String?
    ): Optional<Patient> {

        val patient = patientRepository.findById(id)


        return patient
    }


}