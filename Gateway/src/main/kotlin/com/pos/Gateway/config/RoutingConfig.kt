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
import org.springframework.core.ParameterizedTypeReference
import org.springframework.hateoas.CollectionModel
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import org.springframework.web.servlet.function.*
import org.springframework.web.servlet.function.RouterFunctions.route
import java.net.URI
import com.pos.Gateway.dto.pdp.PatientResponseDTO
import org.springframework.web.server.ResponseStatusException


@Configuration
class RoutingConfig(
    @GrpcClient("idm-pos")
    private val _authStub : IdentityManagementServiceGrpc.IdentityManagementServiceBlockingStub,
    private val _restTemplate: RestTemplate
){

    //    val pdpServiceLocation = "http://pdp-service:8080"
    val pdpServiceLocation: URI = URI.create("http://localhost:8081")

    //    val pdpServiceLocation = "http://pdp-service:8080"
    val consultServiceLocation: URI = URI.create("http://localhost:8083")
    @Bean
    fun routeConfig(): RouterFunction<ServerResponse> {
        return route()
            //consult routes
            .add(
                route(
                    RequestPredicates.path("/api/medical_office/physicians/*/patients/*/date/*/consult"),
                    http(consultServiceLocation))
                    .andRoute(RequestPredicates.path("/api/medical_office/physicians/*/patients/*/date/*/consult/**"),
                        http(consultServiceLocation))
                    .filter(validateAccess())
            )
            //pdp routes
            .add(
                //needed for admin
                route(RequestPredicates.path("/api/medical_office/patients/*")
                    .or(RequestPredicates.path("/api/medical_office/physicians/*"))
                    //adding the /self/ "resource" instead of /physicians/id or /patients/id
                    //as the normal user can't see resources under other users
                    //those are inferred from the jwt token
                    .or(RequestPredicates.path("/api/medical_office/self"))
                    //the appointments routes under the self
                    .or(RequestPredicates.path("/api/medical_office/self/patients"))
                    .or(RequestPredicates.path("/api/medical_office/self/physicians"))
                    .or(RequestPredicates.path("/api/medical_office/self/patients/*/date/*"))
                    .or(RequestPredicates.path("/api/medical_office/self/physicians/*/date/*")),
                    http(pdpServiceLocation))
                    .filter(validateAccess())
            )
            .build()
    }

    private fun validateAccess(): (ServerRequest, HandlerFunction<ServerResponse>) -> ServerResponse {
        return fun (request: ServerRequest, next: HandlerFunction<ServerResponse> ): ServerResponse {

            //TODO: For debugging purpose, delete after
            println(request.uri())

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
                    //also has access on /physicians/
                    if(!request.path().matches(
                            //match child routes
                            Regex("(/api/medical_office/self(/.*)*)" +
                                    //match /physicians/ route
                                "|(/api/medical_office/physicians/\$)")))
                    {
                        return ServerResponse.status(HttpStatus.FORBIDDEN).build()
                    }

                    //we must remake the path to the service
                    if(request.path().startsWith("/api/medical_office/self"))
                    {
                        //getting the implied /patients/{id} part
                        val headers = HttpHeaders()
                        headers.add("X-Forwarded-Host", "localhost:8080")
                        headers.add("X-Forwarded-Port", "8080")
                        headers.add("X-Forwarded-Proto", "http")

                        val responseType =
                            object : ParameterizedTypeReference<CollectionModel<PatientResponseDTO?>?>() {
                            }

                        val response =
                            kotlin.runCatching {
                                _restTemplate.exchange("$pdpServiceLocation/api/medical_office/patients/?userId=" + identity.userId, HttpMethod.GET, HttpEntity(null, headers), responseType);
                            }.getOrElse {
                                return if(it is HttpClientErrorException.NotFound)
                                    ServerResponse.status(HttpStatus.NOT_FOUND).build()
                                else
                                    throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR)
                            }

                        //if the path is only self return the response as it is
                        if(request.path() == "/api/medical_office/self")
                            return ServerResponse.status(response.statusCode).body(response.body!!)

                        //either way construct the next route
                        val patientID =
                            kotlin.runCatching {
                                response.body!!.content.elementAt(0)!!.cnp
                            }.getOrElse {
                                throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR)
                            }

                        //pass to services
                        return next.handle(
                            ServerRequest.from(request)
                                .uri(URI(request.uri().parseServerAuthority().toASCIIString()
                                    .replace("/self", "/patients/$patientID")))
                                .build())
                    }
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
}


