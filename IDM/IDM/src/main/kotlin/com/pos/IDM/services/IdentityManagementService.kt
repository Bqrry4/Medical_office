package com.pos.IDM.services

import com.pos.IDM.persistance.repository.UserRepository
import com.pos.grpc.auth.Auth
import com.pos.grpc.auth.IdentityManagementServiceGrpcKt
import io.grpc.Status
import net.devh.boot.grpc.server.service.GrpcService

@GrpcService
class IdentityManagementService(
    private val _jwtService: JWTService, private val _userRepository: UserRepository
) : IdentityManagementServiceGrpcKt.IdentityManagementServiceCoroutineImplBase() {


    override suspend fun auth(request: Auth.UserDetails): Auth.Token {

        val user = _userRepository.findByUsernameAndPassword(request.username, request.password)
            .orElseThrow { Status.NOT_FOUND.asException() }

        val token = _jwtService.generateToken(user.id, user.role)

        return Auth.Token.newBuilder()
            .setToken(token)
            .build()
    }

    override suspend fun validate(request: Auth.Token): Auth.IdentityResponse {

        //verify for block-list -> return permission denied

        return runCatching {
            val claims = _jwtService.validateToken(request.token)
            Auth.IdentityResponse.newBuilder()
                .setUserId(claims.subject.toLong())
                .setRole(claims["role"].toString())
                .build()
        }.getOrElse { throw Status.INVALID_ARGUMENT.asException() }
    }
}