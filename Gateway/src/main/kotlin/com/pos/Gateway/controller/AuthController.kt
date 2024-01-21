package com.pos.Gateway.controller

import com.pos.Gateway.dto.PatientRequestDto
import com.pos.Gateway.dto.PhysicianRequestDto
import com.pos.Gateway.dto.UserLoginDto
import com.pos.grpc.auth.Auth
import com.pos.grpc.auth.IdentityManagementServiceGrpc
import jakarta.servlet.http.HttpServletRequest
import net.devh.boot.grpc.client.inject.GrpcClient
import org.springframework.http.HttpHeaders
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api")
class AuthController(
    @GrpcClient("idm-pos")
    private val _authStub : IdentityManagementServiceGrpc.IdentityManagementServiceBlockingStub
) {

    @PostMapping("/login")
    fun login(
        @RequestBody userDetails: UserLoginDto
    )
    {
        val token = _authStub.auth(
            Auth.UserAuthDetails.newBuilder()
                .setUsername(userDetails.username)
                .setPassword(userDetails.password)
                .build()).token
        print(token)
    }

    @GetMapping("/logout")
    fun logout(servletRequest: HttpServletRequest)
    {
        val token = servletRequest.getHeader("Authorization")
        _authStub.invalidate(Auth.Token.newBuilder().setToken(token).build());
    }

    @PostMapping("/register")
    fun registerPatient(
        @RequestBody patientRequestDto: PatientRequestDto
    )
    {
        _authStub.register(Auth.UserRegisterDetails.newBuilder()
            .setUsername("")
            .setPassword("")
            .setRole("")
            .build())
    }

    @PostMapping("/register", headers = ["Authorization"])
    fun registerPhysician(
        @RequestHeader(HttpHeaders.AUTHORIZATION) token: String,
        @RequestBody physicianRequestDto: PhysicianRequestDto
    )
    {
        _authStub.register(Auth.UserRegisterDetails.newBuilder()
            .setUsername("")
            .setPassword("")
            .setRole("")
            .build())
    }

}