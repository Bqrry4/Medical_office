package com.pos.PDPdb.persistence.model

import jakarta.persistence.*
import java.sql.Date

//enum class Status(val state: String) {
//    APPROVED("onorată"),
//    NOT_PRESENT("neprezentat"),
//    CANCELLED("anulată");
//
//    @JsonValue
//    override fun toString(): String {
//        return this.state
//    }
//}

enum class Status {
    Onorata,
    Neprezentat,
    Anulata
}

@Embeddable
class AppointmentsKey(
    @Column(name ="id_patient")
    var patientID: String,
    @Column(name ="id_physician")
    var physicianID: Int
)


@Entity
@Table(name = "Appointments")
class Appointments (

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

    var data: Date,

    @Column(columnDefinition = "ENUM('onorată', 'neprezentat', 'anulată')")
    @Enumerated(EnumType.STRING)
    var status: Status
)