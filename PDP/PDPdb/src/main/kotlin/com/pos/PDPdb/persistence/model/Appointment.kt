package com.pos.PDPdb.persistence.model

import com.fasterxml.jackson.annotation.JsonValue
import jakarta.persistence.*
import java.util.Date


enum class Status(val state: String) {
    APPROVED("onorată"),
    NOT_COMMITTED("neprezentat"),
    CANCELLED("anulată");

    @JsonValue
    override fun toString(): String {
        return this.state
    }
}

@Embeddable
class AppointmentsKey(
    @Column(name = "id_patient")
    var patientID: String,
    @Column(name = "id_physician")
    var physicianID: Int,
    @Column(name = "data")
    var date: Date
)

@Converter(autoApply = true)
class StatusConverter : AttributeConverter<Status, String> {
    override fun convertToDatabaseColumn(status: Status) = status.state

    override fun convertToEntityAttribute(dbData: String) =
        Status.values().find { statusType -> statusType.state == dbData }
            ?: throw IllegalArgumentException("Unknown database value:$dbData")
}

@Entity
@Table(name = "Appointments")
class Appointment(

    @EmbeddedId
    var id: AppointmentsKey,

    @ManyToOne
    @MapsId("patientID")
    @JoinColumn(name = "id_patient")
    var patient: Patient,

    @ManyToOne
    @MapsId("physicianID")
    @JoinColumn(name = "id_physician")
    var physician: Physician,

    var status: Status
)

