package com.pos.IDM.persistance.repository

import com.pos.IDM.persistance.model.InvalidTokensEntry
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface InvalidTokensRepository : CrudRepository<InvalidTokensEntry, Long>