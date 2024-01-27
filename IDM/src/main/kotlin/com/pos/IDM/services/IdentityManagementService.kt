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
import io.jsonwebtoken.ExpiredJwtException
import net.devh.boot.grpc.server.service.GrpcService
import org.springframework.scheduling.annotation.Scheduled
import java.util.*
import java.util.logging.Logger


@GrpcService
class IdentityManagementService(
    private val _jwtService: JWTService,
    private val _userRepository: UserRepository,
    private val _invalidTokensRepository: InvalidTokensRepository
) : IdentityManagementServiceGrpcKt.IdentityManagementServiceCoroutineImplBase() {

    private val logger = Logger.getLogger("logger")
    override suspend fun auth(request: Auth.UserAuthDetails): Auth.Token {

        logger.info("Method: auth, with details:" + request.username + " | " + request.password)

        val user = _userRepository.findByUsernameAndPassword(request.username, request.password)
            .orElseThrow {
                val metadata = Metadata()
                metadata.put(
                    ProtoUtils.keyForProto(Auth.ErrorResponse.getDefaultInstance()),
                    Auth.ErrorResponse.newBuilder().setErrorMessage("Not found").build()
                )
                logger.info("Not found")
                Status.NOT_FOUND.asException(metadata)
            }

        val token = _jwtService.generateToken(user.id, user.role)




        return Auth.Token.newBuilder()
            .setToken(token)
            .build()
    }


    override suspend fun register(request: Auth.UserRegisterDetails): Auth.UserId{

        logger.info("Method: register, with details:" + request.username + " | " + request.password)

        var newUser = User(
            username = request.username,
            password = request.password,
            role = enumValueOf<Role>(request.role),
            id = 0
        )

        //the only constraint is the unique username, so it fails only when duplicate
        kotlin.runCatching {
            newUser = _userRepository.save(newUser)
        }.onFailure {
            logger.info("Failed: Already exists")
            throw Status.ALREADY_EXISTS.asException()
        }

        return Auth.UserId.newBuilder()
            .setUserId(newUser.id)
            .build()
    }

    override suspend fun deleteUser(request: Auth.UserId): Empty {

        logger.info("Method: deleteUser, with details:" + request.userId)


        //Delete, i don't think i need to return a status exception there
        //as it is idempotent
        _userRepository.deleteById(request.userId)
        return Empty.newBuilder().build()
    }

    override suspend fun validate(request: Auth.Token): Auth.IdentityResponse {

        logger.info("Method: validate, with details:" + request.token)


        //Parsing token, throw exception when malformed or expired
        val claims = runCatching { _jwtService.parseToken(request.token) }.getOrElse {

            val metadata = Metadata()
            when(it)
            {
                is ExpiredJwtException -> {
                    metadata.put(
                        ProtoUtils.keyForProto(Auth.ErrorResponse.getDefaultInstance()),
                        Auth.ErrorResponse.newBuilder().setErrorMessage("Token expired").build()
                    )
                    logger.info("Token expired")
                }
                else ->
                {
                    metadata.put(
                        ProtoUtils.keyForProto(Auth.ErrorResponse.getDefaultInstance()),
                        Auth.ErrorResponse.newBuilder().setErrorMessage("Token invalid").build()
                    )
                    logger.info("Token malformed")
                }
            }

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
                logger.info("Token is in blacklist")
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

        logger.info("Method: invalidate, with details:" + request.token)

        //In case of exception it will abort the save, meaning the token is already invalid
        runCatching {
            val claims = _jwtService.parseToken(request.token)

            val userID = claims.subject.toLong()
            val invalidTokensEntry =
                _invalidTokensRepository.findById(userID).orElse(InvalidTokensEntry(userID, mutableListOf()))

            if(!invalidTokensEntry.tokens.contains(request.token))
                invalidTokensEntry.tokens.add(request.token)

            _invalidTokensRepository.save(invalidTokensEntry)
        }

        return Empty.newBuilder().build()
    }

    //clear tokens once 1 hr
    @Scheduled(fixedDelay = 60 * 60 * 1000)
    fun clearExpiredInvalidTokens() {

        logger.info("Clearing the expired tokens from redis...")

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