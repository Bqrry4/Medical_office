package com.pos.Gateway.config
import com.pos.grpc.auth.Auth
import com.pos.grpc.auth.IdentityManagementServiceGrpc
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.grpc.protobuf.ProtoUtils
import net.devh.boot.grpc.client.inject.GrpcClient
import org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions.http
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForEntity
import org.springframework.web.servlet.function.*
import org.springframework.web.servlet.function.RouterFunctions.route
import java.net.URI


@Configuration
class RoutingConfig(
    @GrpcClient("idm-pos")
    private val _authStub : IdentityManagementServiceGrpc.IdentityManagementServiceBlockingStub,
    private val _restTemplate: RestTemplate
){
    @Bean
    fun routeConfig(): RouterFunction<ServerResponse> {
        return route()
            .add(
                route(
                    RequestPredicates.path("/api/medical_office/physicians/*/patients/*/date/*/consult"),
                    http(URI.create("http://localhost:8083")))
                    .andRoute(RequestPredicates.path("/api/medical_office/physicians/*/patients/*/date/*/consult/**"),
                        http(URI.create("http://localhost:8083")))
                    .filter(validateAccess2())
            )
            .add(
                route(RequestPredicates.path("/api/medical_office/patients/**"),
                    http(URI.create("http://localhost:8081")))
                    .andRoute(RequestPredicates.path("/api/medical_office/physicians/**"),
                        http(URI.create("http://localhost:8081")))
                    .filter(validateAccess())
            )
            .build()
    }

    private fun validateAccess(): (ServerRequest, HandlerFunction<ServerResponse>) -> ServerResponse {
        return fun ( request: ServerRequest, next: HandlerFunction<ServerResponse> ): ServerResponse {

            //No access without the token
            val token = kotlin.runCatching {
                request.headers().firstHeader("Authorization")!!.split("Bearer ")[1]
            }.getOrElse {
                return ServerResponse.status(HttpStatus.UNAUTHORIZED).build()
            }

            //Validating the token
            val identity = kotlin.runCatching {
                 _authStub.validate(Auth.Token.newBuilder().setToken(token).build())
            }.getOrElse {
                return when(it)
                {
                    is StatusRuntimeException -> {
                        if(it.status == Status.INVALID_ARGUMENT)
                            ServerResponse.status(HttpStatus.UNAUTHORIZED).body(it.trailers
                                ?.get(ProtoUtils.keyForProto(Auth.ErrorResponse.getDefaultInstance()))
                                ?.errorMessage ?: "")
                        else
                            ServerResponse.badRequest().build()
                    }
                    else -> ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
                }
            }

            //Checking the route access for role
            when(identity.role)
            {
                "admin" -> {
                    //admin have rights only on patients/[id], and physician/[id] routes
                    if(!request.path().matches(Regex("/api/medical_office/(patients|physicians)(/\\w*)?\$")))
                    {
                        return ServerResponse.status(HttpStatus.FORBIDDEN).build()
                    }
                }
                "patient" -> {

                    //routes for patients are the pdp routes without the /patients/{id}, which is implied by the token
                    //patient has access on all routes under /patients/ path, except /patients/
                    if(!request.path().matches(Regex("/api/medical_office/physicians/[\\w/]+")))
                    {
                        return ServerResponse.status(HttpStatus.FORBIDDEN).build()
                    }

                    //getting the implied /patients/{id} part
                    val patientID: String = ""
                    if(request.path().matches(Regex("")))
                    {
//                        _restTemplate.getForEntity();
                    }

                    return next.handle(
                        ServerRequest.from(request)
                            .uri(URI(request.uri().scheme
                                    + request.uri().host
                                    + request.uri().port
                                    + "/patients/" + patientID
                                    + request.uri().path)).build())


                }
                "physician" -> {
                    //routes for physicians are the pdp routes without the /physicians/{id}, which is implied by the token
                    //patient has access on all routes under /physicians/ path, except /physicians/
                    if(!request.path().matches(Regex("/api/medical_office/physicians/[\\w/]+")))
                    {
                        return ServerResponse.status(HttpStatus.FORBIDDEN).build()
                    }

                }
            }

            //passing the request forward on the chain
            return next.handle(request)
        }
    }
    private fun validateAccess2(): (ServerRequest, HandlerFunction<ServerResponse>) -> ServerResponse {
        return { request: ServerRequest, next: HandlerFunction<ServerResponse> ->

            println("REstrict2 AccesS")
            next.handle(request)

        }
    }


}


