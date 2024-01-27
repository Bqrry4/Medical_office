package com.pos.Gateway.controller

import com.pos.Gateway.dto.ClientResponseErrorMessageDto
import com.pos.Gateway.dto.PatientRegisterDto
import com.pos.Gateway.dto.PhysicianRegisterDto
import com.pos.Gateway.dto.UserLoginDto
import com.pos.Gateway.dto.pdp.PatientRequestDTO
import com.pos.Gateway.dto.pdp.PhysicianRequestDTO
import com.pos.Gateway.service.AuthValidateService
import com.pos.grpc.auth.Auth
import com.pos.grpc.auth.IdentityManagementServiceGrpc
import io.grpc.Status
import io.grpc.StatusRuntimeException
import jakarta.servlet.http.HttpServletRequest
import net.devh.boot.grpc.client.inject.GrpcClient
import org.springframework.http.*
import org.springframework.web.bind.annotation.*
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.exchange


@RestController
@RequestMapping("/api")
class AuthController(
    @GrpcClient("idm-pos")
    private val _authStub : IdentityManagementServiceGrpc.IdentityManagementServiceBlockingStub,
    private val _restTemplate: RestTemplate,
    private val _authValidateService: AuthValidateService
) {

    val pdpServiceLocation = "http://pdp-service:8080"
//    val pdpServiceLocation = "http://localhost:8081"

    @PostMapping("/login")
    fun login(
        @RequestBody userDetails: UserLoginDto
    ) : ResponseEntity<Any>
    {
        val token = kotlin.runCatching {
            _authStub.auth(
                Auth.UserAuthDetails.newBuilder()
                    .setUsername(userDetails.username)
                    .setPassword(userDetails.password)
                    .build()).token
        }.getOrElse {
            return when(it)
            {
                is StatusRuntimeException ->
                {
                     if(it.status == Status.NOT_FOUND)
                        ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                            ClientResponseErrorMessageDto("Invalid credentials"))
                    else
                        ResponseEntity.internalServerError().build()

                }
                else -> ResponseEntity.internalServerError().build()
            }
        }

        return ResponseEntity.ok(token)
    }
    @PutMapping("/logout")
    fun logout(
        @RequestHeader(HttpHeaders.AUTHORIZATION) bearerToken: String
    ): ResponseEntity<Any>
    {
        val token =

            kotlin.runCatching {
                bearerToken.split("Bearer ")[1];
            } .getOrElse {
                return ResponseEntity.badRequest().build()
            }
        _authStub.invalidate(Auth.Token.newBuilder().setToken(token).build());

        return ResponseEntity.ok().build()
    }

    @PostMapping("/register")
    fun registerPatient(
        @RequestBody patientRequestDto: PatientRegisterDto
    ): ResponseEntity<Any>
    {
        //check if there is a patient with given cnp
        //Reason: might overwrite an existing patient
        kotlin.runCatching {
            //we do have interest only in status
            _restTemplate.headForHeaders("$pdpServiceLocation/api/medical_office/patients/" + patientRequestDto.cnp)
        }.fold(
            onSuccess = {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    ClientResponseErrorMessageDto("A patient with given cnp is already registered"))
            },
            onFailure = {
                if(it is HttpClientErrorException.NotFound)
                    return@fold //we can continue
                return ResponseEntity.internalServerError().build()
            }
        )

        //register in idm
        val userId =
            kotlin.runCatching {
                _authStub.register(Auth.UserRegisterDetails.newBuilder()
                .setUsername(patientRequestDto.username)
                .setPassword(patientRequestDto.password)
                .setRole("patient")
                .build()).userId
            }.getOrElse {

                return when(it)
                {
                    is StatusRuntimeException -> {
                        if(it.status == Status.ALREADY_EXISTS)
                            ResponseEntity.status(HttpStatus.CONFLICT).body(
                                ClientResponseErrorMessageDto("Username taken"))
                        else
                            ResponseEntity.badRequest().build()
                    }

                    else -> ResponseEntity.internalServerError().build()
                }
            }

        val request = HttpEntity<PatientRequestDTO>(
            PatientRequestDTO(userId.toInt(),
                patientRequestDto.lastName,
                patientRequestDto.firstName,
                patientRequestDto.email,
                patientRequestDto.phone,
                patientRequestDto.birthDay)
            , null)

        //create resource in pdp
        kotlin.runCatching {
            _restTemplate.put("$pdpServiceLocation/api/medical_office/patients/" + patientRequestDto.cnp, request)
        }.getOrElse {

            //on error revert the register, it should not raise a status exception
            _authStub.deleteUser(Auth.UserId.newBuilder().setUserId(userId).build())

            return when(it)
            {
                is HttpClientErrorException.Conflict -> ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(ClientResponseErrorMessageDto( it.responseBodyAsString))

                is HttpClientErrorException.UnprocessableEntity -> ResponseEntity
                    .status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .body(ClientResponseErrorMessageDto( it.responseBodyAsString))

                else -> ResponseEntity.internalServerError().build()
            }
        }
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/register", headers = ["Authorization"])
    fun registerPhysician(
        @RequestHeader(HttpHeaders.AUTHORIZATION) bearerToken: String,
        @RequestBody physicianRequestDto: PhysicianRegisterDto
    ): ResponseEntity<Any>
    {

        val identity = kotlin.runCatching {
            _authValidateService.validateToken(bearerToken)
        }.getOrElse {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        }

        //Only admin can register physicians
        if(identity.role != "admin")
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()


        //register in idm
        val userId =
            kotlin.runCatching {
                _authStub.register(Auth.UserRegisterDetails.newBuilder()
                    .setUsername(physicianRequestDto.username)
                    .setPassword(physicianRequestDto.password)
                    .setRole("physician")
                    .build()).userId
            }.getOrElse {

                return when(it)
                {
                    is StatusRuntimeException -> {
                        if(it.status == Status.ALREADY_EXISTS)
                            ResponseEntity.status(HttpStatus.CONFLICT).body(
                                ClientResponseErrorMessageDto("Username taken"))
                        else
                            ResponseEntity.internalServerError().build()
                    }

                    else -> ResponseEntity.internalServerError().build()
                }
            }

        val request = HttpEntity<PhysicianRequestDTO>(
            PhysicianRequestDTO(userId.toInt(),
                physicianRequestDto.lastName,
                physicianRequestDto.firstName,
                physicianRequestDto.email,
                physicianRequestDto.phone,
                physicianRequestDto.specialization)
            , null)

        //create resource in pdp
        kotlin.runCatching {
            _restTemplate.postForLocation("$pdpServiceLocation/api/medical_office/patients/", request)
        }.getOrElse {

            //on error revert the register, it should not raise a status exception
            _authStub.deleteUser(Auth.UserId.newBuilder().setUserId(userId).build())

            return when(it)
            {
                is HttpClientErrorException.Conflict -> ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(ClientResponseErrorMessageDto( it.responseBodyAsString))

                is HttpClientErrorException.UnprocessableEntity -> ResponseEntity
                    .status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .body(ClientResponseErrorMessageDto( it.responseBodyAsString))

                else -> ResponseEntity.internalServerError().build()
            }
        }
        return ResponseEntity.noContent().build()
    }

}