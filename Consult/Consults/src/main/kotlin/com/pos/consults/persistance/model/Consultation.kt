package com.pos.consults.persistance.model

import com.fasterxml.jackson.annotation.JsonValue
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import java.util.*

enum class Diagnostic(val state: String) {
    HEALTHY("sănătos"),
    ILL("bolnav");

    @JsonValue
    override fun toString(): String {
        return this.state
    }
}

@Document(collection = "Consultations")
data class Consultation (
    @Id
    val id: ObjectId = ObjectId(),
    @Field(name="patientId")
    val patientId : String,
    @Field(name="physicianId")
    val physicianId: Int,
    @Field(name="date")
    val date: Date,
    @Field(name="diagnostic")
    var diagnostic : Diagnostic?,
    @Field(name="investigations")
    val investigations: MutableList<Investigation>
)