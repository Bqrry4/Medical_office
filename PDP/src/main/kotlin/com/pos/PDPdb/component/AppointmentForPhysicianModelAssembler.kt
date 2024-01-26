package com.pos.PDPdb.component

import com.pos.PDPdb.controller.PatientsController
import com.pos.PDPdb.controller.PhysiciansController
import com.pos.PDPdb.dto.AppointmentPatientDTO
import com.pos.PDPdb.dto.AppointmentPhysicianDTO
import com.pos.PDPdb.persistence.model.Physician
import org.springframework.hateoas.CollectionModel
import org.springframework.hateoas.EntityModel
import org.springframework.hateoas.server.RepresentationModelAssembler
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder
import org.springframework.stereotype.Component
@Component
class AppointmentForPhysicianModelAssembler :
    RepresentationModelAssembler<AppointmentPhysicianDTO, EntityModel<AppointmentPhysicianDTO>> {
    override fun toModel(appointment: AppointmentPhysicianDTO): EntityModel<AppointmentPhysicianDTO> {
        return EntityModel.of(
            appointment,
            WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(PhysiciansController::class.java).getAppointment(null, null, null))
                .withSelfRel(),
            WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(PhysiciansController::class.java).getPhysicianAppointments(null, null, null)).withRel("parent")
        )
    }

    override fun toCollectionModel(appointments: Iterable<AppointmentPhysicianDTO>): CollectionModel<EntityModel<AppointmentPhysicianDTO>> {

        val models = super.toCollectionModel(appointments)
        return models
    }

}