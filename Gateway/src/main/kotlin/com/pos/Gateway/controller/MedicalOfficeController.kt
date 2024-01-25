package com.pos.Gateway.controller

import com.pos.Gateway.service.AuthValidateService
import com.pos.grpc.auth.Auth
import com.pos.grpc.auth.IdentityManagementServiceGrpc
import net.devh.boot.grpc.client.inject.GrpcClient
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestTemplate

@RestController
@RequestMapping("/api/medical_office")
class MedicalOfficeController(
    private val _authValidateService: AuthValidateService,
    private val _restTemplate: RestTemplate
) {

    //    val pdpServiceLocation = "http://pdp-service:8080"
    val pdpServiceLocation = "http://localhost:8081"

    @GetMapping("/self")
    fun getSelfInfo(
        @RequestHeader(HttpHeaders.AUTHORIZATION) bearerToken: String
    ): ResponseEntity<Any>
    {
        val identity = kotlin.runCatching {
            _authValidateService.validateToken(bearerToken)
        }.getOrElse {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        }

//        _restTemplate.getForEntity()
    }

}