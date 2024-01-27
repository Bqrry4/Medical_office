package com.pos.consults.component

import com.pos.consults.controller.ConsultsController
import com.pos.consults.controller.InvestigationController
import com.pos.consults.dto.ConsultationResponseDTO
import com.pos.consults.dto.InvestigationResponseDTO
import org.springframework.hateoas.CollectionModel
import org.springframework.hateoas.EntityModel
import org.springframework.hateoas.server.RepresentationModelAssembler
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder
import org.springframework.stereotype.Component
import java.text.SimpleDateFormat
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn

@Component
class InvestigationModelAssembler : RepresentationModelAssembler<InvestigationResponseDTO, EntityModel<InvestigationResponseDTO>> {
     override fun toModel(investigation: InvestigationResponseDTO): EntityModel<InvestigationResponseDTO> {
        return EntityModel.of(
            investigation,
            linkTo(methodOn(InvestigationController::class.java).getOneInvestigation(null, null, null, investigation.id)).withSelfRel(),
            linkTo(methodOn(InvestigationController::class.java).getConsultInvestigations(null, null, null)).withRel("parent")
        )
    }

     override fun toCollectionModel(investigation: Iterable<InvestigationResponseDTO>): CollectionModel<EntityModel<InvestigationResponseDTO>> {

        val models = super.toCollectionModel(investigation)
         models.add(linkTo(methodOn(InvestigationController::class.java).getConsultInvestigations(null, null, null)).withSelfRel())

        return models
    }

}