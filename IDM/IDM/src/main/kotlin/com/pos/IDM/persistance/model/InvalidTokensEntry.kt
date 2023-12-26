package com.pos.IDM.persistance.model

import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash
import java.io.Serializable


@RedisHash("InvalidToken")
class InvalidTokensEntry(
    @Id val userID: Long,
    val tokens: MutableList<String>
) : Serializable