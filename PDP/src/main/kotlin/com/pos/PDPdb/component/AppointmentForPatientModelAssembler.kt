package com.pos.PDPdb.component

import com.pos.PDPdb.controller.PatientsController
import com.pos.PDPdb.controller.PhysiciansController
import com.pos.PDPdb.dto.AppointmentPatientDTO
import org.springframework.hateoas.CollectionModel
import org.springframework.hateoas.EntityModel
import org.springframework.hateoas.server.RepresentationModelAssembler
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder
import org.springframework.stereotype.Component
import java.text.SimpleDateFormat

@Component
class AppointmentForPatientModelAssembler : RepresentationModelAssembler<AppointmentPatientDTO, EntityModel<AppointmentPatientDTO>> {
    override fun toModel(appointment: AppointmentPatientDTO): EntityModel<AppointmentPatientDTO> {
        return EntityModel.of(
            appointment,
            WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(PatientsController::class.java).getAppointment(null, null, null))
                .withSelfRel(),
            WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(PatientsController::class.java).getPatientAppointments(null, null, null)).withRel("parent")
        )
    }

    override fun toCollectionModel(appointments: Iterable<AppointmentPatientDTO>): CollectionModel<EntityModel<AppointmentPatientDTO>> {

        val models = super.toCollectionModel(appointments)
        return models
    }

}