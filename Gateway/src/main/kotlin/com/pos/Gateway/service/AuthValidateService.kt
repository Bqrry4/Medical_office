package com.pos.Gateway.service

import com.pos.grpc.auth.Auth
import com.pos.grpc.auth.IdentityManagementServiceGrpc
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.grpc.protobuf.ProtoUtils
import net.devh.boot.grpc.client.inject.GrpcClient

class AuthValidateService(
    @GrpcClient("idm-pos")
    private val _authStub : IdentityManagementServiceGrpc.IdentityManagementServiceBlockingStub,
){
    fun validateToken(bearerToken: String): Auth.IdentityResponse {
        //No access without the token
        val token = kotlin.runCatching {
            bearerToken.split("Bearer ")[1]
        }.getOrElse {
            throw RuntimeException("Invalid Token format")
        }

        //Validating the token
        val identity = kotlin.runCatching {
            _authStub.validate(Auth.Token.newBuilder().setToken(token).build())
        }.getOrElse {
            when(it)
            {
                is StatusRuntimeException -> {
                    if(it.status == Status.INVALID_ARGUMENT)

                        throw RuntimeException(it.trailers
                            ?.get(ProtoUtils.keyForProto(Auth.ErrorResponse.getDefaultInstance()))
                            ?.errorMessage ?: "Invalid Token format")
                    else
                        throw RuntimeException("Invalid Token format")
                }
                else -> throw RuntimeException("Invalid Token format")
            }
        }
        return identity
    }
}