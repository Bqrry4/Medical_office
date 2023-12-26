package com.pos.IDM.services

import com.google.protobuf.Empty
import com.pos.IDM.persistance.model.InvalidTokensEntry
import com.pos.IDM.persistance.model.Role
import com.pos.IDM.persistance.model.User
import com.pos.IDM.persistance.repository.InvalidTokensRepository
import com.pos.IDM.persistance.repository.UserRepository
import com.pos.grpc.auth.Auth
import com.pos.grpc.auth.IdentityManagementServiceGrpcKt
import io.grpc.Metadata
import io.grpc.Status
import io.grpc.protobuf.ProtoUtils
import net.devh.boot.grpc.server.service.GrpcService
import org.springframework.scheduling.annotation.Scheduled
import java.util.*


@GrpcService
class IdentityManagementService(
    private val _jwtService: JWTService,
    private val _userRepository: UserRepository,
    private val _invalidTokensRepository: InvalidTokensRepository
) : IdentityManagementServiceGrpcKt.IdentityManagementServiceCoroutineImplBase() {

    override suspend fun auth(request: Auth.UserAuthDetails): Auth.Token {

        //TODO: check for username and password validity

        val user = _userRepository.findByUsernameAndPassword(request.username, request.password)
            .orElseThrow {
                val metadata = Metadata()
                metadata.put(
                    ProtoUtils.keyForProto(Auth.ErrorResponse.getDefaultInstance()),
                    Auth.ErrorResponse.newBuilder().setErrorMessage("Not found").build()
                )
                Status.NOT_FOUND.asException(metadata)
            }

        val token = _jwtService.generateToken(user.id, user.role)




        return Auth.Token.newBuilder()
            .setToken(token)
            .build()
    }


    override suspend fun register(request: Auth.UserRegisterDetails): Auth.UserId{

        //TODO: check for username and password validity

        var newUser = User(
            username = request.username,
            password = request.password,
            role = enumValueOf<Role>(request.role),
            id = 0
        )

        newUser = _userRepository.save(newUser)

        return Auth.UserId.newBuilder()
            .setUserId(newUser.id)
            .build()
    }

    override suspend fun deleteUser(request: Auth.UserId): Empty {
        //Delete, i don't think i need to return a status exception there
        _userRepository.deleteById(request.userId)
        return super.deleteUser(request)
    }

    override suspend fun validate(request: Auth.Token): Auth.IdentityResponse {

        //Parsing token, throw exception when malformed or expired
        val claims = runCatching { _jwtService.parseToken(request.token) }.getOrElse {
            val metadata = Metadata()
            metadata.put(
                ProtoUtils.keyForProto(Auth.ErrorResponse.getDefaultInstance()),
                Auth.ErrorResponse.newBuilder().setErrorMessage("Token invalid").build()
            )
            throw Status.INVALID_ARGUMENT.asException(metadata)
        }

        //Searching for invalidated tokens
        _invalidTokensRepository.findById(claims.subject.toLong()).orElse(null)?.let {
            it.tokens.find { token -> token == request.token }?.let {
                val metadata = Metadata()
                metadata.put(
                    ProtoUtils.keyForProto(Auth.ErrorResponse.getDefaultInstance()),
                    Auth.ErrorResponse.newBuilder().setErrorMessage("Token invalidated").build()
                )
                throw Status.INVALID_ARGUMENT.asException(metadata)
            }
        }

        return runCatching {
            Auth.IdentityResponse.newBuilder()
                .setUserId(claims.subject.toLong())
                .setRole(claims["role"].toString())
                .build()
        }.getOrElse { throw Status.INVALID_ARGUMENT.asException() }
    }

    override suspend fun invalidate(request: Auth.Token): Empty {
        //In case of exception it will abort the save, meaning the token is already invalid
        runCatching {
            val claims = _jwtService.parseToken(request.token)

            val userID = claims.subject.toLong()
            val invalidTokensEntry =
                _invalidTokensRepository.findById(userID).orElse(InvalidTokensEntry(userID, mutableListOf()))

            invalidTokensEntry.tokens.add(request.token)

            _invalidTokensRepository.save(invalidTokensEntry)
        }

        return Empty.newBuilder().build()
    }

    //clear tokens once 1 hr
    @Scheduled(fixedDelay = 60 * 60 * 1000)
    fun clearExpiredInvalidTokens() {
        _invalidTokensRepository.findAll().forEach { entry ->
            entry.tokens.forEach { token ->
                //try to parse
                kotlin.runCatching {
                    _jwtService.parseToken(token)
                }.getOrElse { _ ->
                    //its invalid
                    entry.tokens.remove(token)
                }
                if (entry.tokens.isEmpty()) {
                    _invalidTokensRepository.delete(entry)
                } else {
                    _invalidTokensRepository.save(entry)
                }
            }
        }
    }

}