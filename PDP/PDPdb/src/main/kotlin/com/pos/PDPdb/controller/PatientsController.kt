package com.pos.PDPdb.controller

import com.pos.PDPdb.persistence.model.Patient
import com.pos.PDPdb.persistence.repository.PatientRepository
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/patients")
class PatientsController(private val patientRepository: PatientRepository) {

    @GetMapping("/{id}")
    fun getPatient(
        @PathVariable id: String,
        @RequestParam(required = false) date: String?,
        @RequestParam(required = false) type: String?
    ): Optional<Patient> {

        return patientRepository.findById(id)

    }

}