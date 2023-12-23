package com.pos.consults.component

import com.pos.consults.controller.ConsultsController
import com.pos.consults.dto.ConsultationResponseDTO
import com.pos.consults.persistance.model.Consultation
import org.springframework.hateoas.EntityModel
import org.springframework.hateoas.Link
import org.springframework.hateoas.server.RepresentationModelAssembler
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn
import org.springframework.stereotype.Component
import java.text.SimpleDateFormat

@Component
class ConsultModelAssembler : RepresentationModelAssembler<ConsultationResponseDTO, EntityModel<ConsultationResponseDTO>> {
    override fun toModel(consult: ConsultationResponseDTO): EntityModel<ConsultationResponseDTO> {
        return EntityModel.of(
            consult,
            linkTo(methodOn(ConsultsController::class.java).getOneConsultation(consult.physicianId, consult.patientId, SimpleDateFormat("dd-MM-yyyy HH:mm").format(consult.date))).withSelfRel(),
        )
    }

//    override fun toCollectionModel(patients: Iterable<PatientResponseDTO>): CollectionModel<EntityModel<PatientResponseDTO>> {
//
//        val models = super.toCollectionModel(patients)
//        models.add(linkTo(methodOn(PatientsController::class.java).getAll()).withSelfRel())
//        return models
//    }

}