package com.pos.consults.persistance.model

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field

@Document
data class Investigation (
    @Id
    val id: String,
    @Field(name="name")
    var name : String,
    @Field(name="duration")
    var duration: Int,
    @Field(name="result")
    var result: String
)