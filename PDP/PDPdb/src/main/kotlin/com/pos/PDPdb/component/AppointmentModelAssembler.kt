package com.pos.PDPdb.component

import com.pos.PDPdb.controller.PatientsController
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
            WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(PatientsController::class.java).getAppointment(appointment.patient.cnp, appointment.physician.physicianId, SimpleDateFormat("dd-MM-yyyy").format(appointment.date)))
                .withSelfRel(),
            WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(PatientsController::class.java).getPatientAppointments(appointment.patient.cnp, null, null)).withRel("parent")
        )
    }

    override fun toCollectionModel(appointments: Iterable<AppointmentDTO>): CollectionModel<EntityModel<AppointmentDTO>> {

        val models = super.toCollectionModel(appointments)
        return models
    }

}