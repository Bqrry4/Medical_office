package com.pos.Gateway.controller

import com.pos.Gateway.dto.UserLoginDto
import com.pos.grpc.auth.Auth
import com.pos.grpc.auth.IdentityManagementServiceGrpc.IdentityManagementServiceBlockingStub
import com.pos.grpc.auth.IdentityManagementServiceGrpc.IdentityManagementServiceStub
import jakarta.servlet.ServletRequest
import jakarta.servlet.http.HttpServletRequest
import net.devh.boot.grpc.client.inject.GrpcClient
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

@Controller
class AuthController(
    @GrpcClient("gRPC server name")
    private val _authStub : IdentityManagementServiceBlockingStub
) {
    @PostMapping("/login")
    fun login(
        @RequestBody userDetails: UserLoginDto
    )
    {
        val token = _authStub.auth(Auth.UserAuthDetails.newBuilder()
            .setUsername(userDetails.username)
            .setPassword(userDetails.password)
            .build()).token
    }

    @GetMapping("/logout")
    fun logout(servletRequest: HttpServletRequest)
    {
        val token = servletRequest.getHeader("Authorization")
        _authStub.invalidate(Auth.Token.newBuilder().setToken(token).build());
    }

    //register user and doctor should make a difference
    @PostMapping("/register")
    fun register()
    {
        _authStub.register(Auth.UserRegisterDetails.newBuilder()
            .setUsername("")
            .setPassword("")
            .setRole("")
            .build())
    }

}