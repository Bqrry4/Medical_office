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

@Component
class InvestigationModelAssembler : RepresentationModelAssembler<InvestigationResponseDTO, EntityModel<InvestigationResponseDTO>> {
     override fun toModel(consult: InvestigationResponseDTO): EntityModel<InvestigationResponseDTO> {
        return EntityModel.of(
            consult
        )
    }

     override fun toCollectionModel(consults: Iterable<InvestigationResponseDTO>): CollectionModel<EntityModel<InvestigationResponseDTO>> {

        val models = super.toCollectionModel(consults)
        return models
    }

}