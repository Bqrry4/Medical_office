package com.pos.PDPdb.component

import com.pos.PDPdb.controller.PatientsController
import com.pos.PDPdb.controller.PhysiciansController
import com.pos.PDPdb.dto.AppointmentDTO
import com.pos.PDPdb.dto.PatientResponseDTO
import org.springframework.hateoas.CollectionModel
import org.springframework.hateoas.EntityModel
import org.springframework.hateoas.server.RepresentationModelAssembler
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder
import org.springframework.stereotype.Component
import java.text.SimpleDateFormat

@Component
class AppointmentModelAssembler : RepresentationModelAssembler<AppointmentDTO, EntityModel<AppointmentDTO>> {
    override fun toModel(appointment: AppointmentDTO): EntityModel<AppointmentDTO> {
        return EntityModel.of(
            appointment,
            WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(PhysiciansController::class.java).getAppointment(appointment.physician.physicianId, appointment.patient.cnp, SimpleDateFormat("dd-MM-yyyy HH:mm").format(appointment.date)))
                .withSelfRel(),
            WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(PhysiciansController::class.java).getPhysicianAppointments(appointment.physician.physicianId, null, null)).withRel("parent")
        )
    }

    override fun toCollectionModel(appointments: Iterable<AppointmentDTO>): CollectionModel<EntityModel<AppointmentDTO>> {

        val models = super.toCollectionModel(appointments)
        return models
    }

}