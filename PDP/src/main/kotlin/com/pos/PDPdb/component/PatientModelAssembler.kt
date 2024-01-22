package com.pos.PDPdb.component

import com.pos.PDPdb.controller.PatientsController
import com.pos.PDPdb.dto.PatientResponseDTO
import org.springframework.hateoas.CollectionModel
import org.springframework.hateoas.EntityModel
import org.springframework.hateoas.server.RepresentationModelAssembler
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn
import org.springframework.stereotype.Component


@Component
class PatientModelAssembler : RepresentationModelAssembler<PatientResponseDTO, EntityModel<PatientResponseDTO>> {
    override fun toModel(patient: PatientResponseDTO): EntityModel<PatientResponseDTO> {
        return EntityModel.of(
            patient,
            linkTo(methodOn(PatientsController::class.java).getPatient(patient.cnp)).withSelfRel(),
            linkTo(methodOn(PatientsController::class.java).getAll()).withRel("parent")
        )
    }

    override fun toCollectionModel(patients: Iterable<PatientResponseDTO>): CollectionModel<EntityModel<PatientResponseDTO>> {

        val models = super.toCollectionModel(patients)
        models.add(linkTo(methodOn(PatientsController::class.java).getAll()).withSelfRel())
        return models
    }

}