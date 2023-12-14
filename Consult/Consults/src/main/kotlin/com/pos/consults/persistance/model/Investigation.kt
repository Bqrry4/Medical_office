package com.pos.consults.persistance.model

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field

@Document
data class Investigation (
    @Id
    val id: ObjectId = ObjectId(),
    @Field(name="name")
    val name : String,
    @Field(name="duration")
    val duration: Int,
    @Field(name="result")
    val result: String
)