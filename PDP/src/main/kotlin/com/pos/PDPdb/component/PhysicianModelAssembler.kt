package com.pos.PDPdb.component

import com.pos.PDPdb.controller.PhysiciansController
import com.pos.PDPdb.dto.PhysicianResponseDTO
import org.springframework.hateoas.CollectionModel
import org.springframework.hateoas.EntityModel
import org.springframework.hateoas.server.RepresentationModelAssembler
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder
import org.springframework.stereotype.Component

@Component
class PhysicianModelAssembler : RepresentationModelAssembler<PhysicianResponseDTO, EntityModel<PhysicianResponseDTO>> {
    override fun toModel(physician: PhysicianResponseDTO): EntityModel<PhysicianResponseDTO> {
        return EntityModel.of(
            physician,
            WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(PhysiciansController::class.java).getPhysician(physician.physicianId))
                .withSelfRel(),
            WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(PhysiciansController::class.java).getAll(null, null, null, null)).withRel("parent")
        )
    }

    override fun toCollectionModel(physicians: Iterable<PhysicianResponseDTO>): CollectionModel<EntityModel<PhysicianResponseDTO>> {
        val models = super.toCollectionModel(physicians)
        return models
    }

}