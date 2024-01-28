package com.pos.Gateway.config
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode
import com.pos.Gateway.dto.pdp.PatientResponseDTO
import com.pos.Gateway.dto.pdp.PhysicianResponseDTO
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
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.function.*
import org.springframework.web.servlet.function.RouterFunctions.route
import org.springframework.web.util.ContentCachingResponseWrapper
import java.lang.reflect.Type
import java.net.URI
import java.nio.charset.Charset
import java.util.logging.Logger


@Configuration
class RoutingConfig(
    @GrpcClient("idm-pos")
    private val _authStub : IdentityManagementServiceGrpc.IdentityManagementServiceBlockingStub,
    private val _restTemplate: RestTemplate
){

        val pdpServiceLocation: URI = URI.create("http://pdp-service:8080")
//    val pdpServiceLocation: URI = URI.create("http://localhost:8081")

        val consultServiceLocation: URI = URI.create("http://consult-service:8080")
//    val consultServiceLocation: URI = URI.create("http://localhost:8083")

    private val logger = Logger.getLogger("routingLogger")

    @Bean
    fun routeConfig(): RouterFunction<ServerResponse> {
        return route()
            //consult routes
            .add(
                route(RequestPredicates.path("/api/medical_office/self/patients/*/date/*/consult")
                    .or(RequestPredicates.path("/api/medical_office/self/physicians/*/date/*/consult"))
                    .or(RequestPredicates.path("/api/medical_office/self/patients/*/date/*/consult/investigations"))
                    .or(RequestPredicates.path("/api/medical_office/self/physicians/*/date/*/consult/investigations"))
                    .or(RequestPredicates.path("/api/medical_office/self/patients/*/date/*/consult/investigations/*"))
                    .or(RequestPredicates.path("/api/medical_office/self/physicians/*/date/*/consult/investigations/*")),
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

            logger.info("Routing " + request.uri())

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

            logger.info("Identity assumed " + identity.role)
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
                    val response =  resolvePatientRole(identity, request, next)
                    if(response != null)
                        return response
                }
                "physician" -> {
                    val response =  resolvePhysicianRole(identity, request, next)
                    if(response != null)
                        return response
                }
            }


            //passing the request forward on the chain
            return next.handle(request)
        }
    }

    private fun resolvePatientRole(identity: Auth.IdentityResponse, request: ServerRequest, next: HandlerFunction<ServerResponse>):
            ServerResponse? {
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


            //either way construct the next route
            val patientID =
                kotlin.runCatching {
                    response.body!!.content.elementAt(0)!!.cnp
                }.getOrElse {
                    throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR)
                }

            //pass to services
            var forwardedResponse = next.handle(
                ServerRequest.from(request)
                    .uri(URI(request.uri().parseServerAuthority().toASCIIString()
                        .replace("/self", "/patients/$patientID")))
                    .build())

            //we need to do some changes
            if(forwardedResponse.statusCode().is2xxSuccessful)
            {
                val forwardedResponseContent = ContentCachingResponseWrapper((RequestContextHolder.getRequestAttributes() as ServletRequestAttributes?)!!.response!!)
                forwardedResponse.writeTo(request.servletRequest(), forwardedResponseContent) {
                    mutableListOf(
                        MappingJackson2HttpMessageConverter()
                    ) as List<HttpMessageConverter<*>>
                }

                val objectMapper = ObjectMapper()
                val modified = objectMapper.readValue(forwardedResponseContent.contentAsByteArray, JsonNode::class.java)

                //if the path is only self we must remove the parent link
                if(request.path() == "/api/medical_office/self")
                {
                    (modified["_links"] as ObjectNode).remove("parent")
                }

                modified["_links"].properties().forEach {
                    val node = it.value as ObjectNode
                    var textValue = node.get("href").textValue()
                    textValue = textValue.replace(Regex("/api/medical_office/patients/\\d+"), "/api/medical_office/self")
                    node.remove("href")
                    node.put("href", textValue)
                }

                //if the patient have an active consult
                if(request.path().matches(Regex("/api/medical_office/self/physicians/\\d+/date/(.)+")))
                {
                    val consultPath = consultServiceLocation.toASCIIString() + request.path() + "/consult"
                    kotlin.runCatching {
                        //we do have interest only in status
                    _restTemplate.headForHeaders(consultPath)
                    }.onSuccess {
                        //add the link to hal
                        (modified["_links"] as ObjectNode).putObject("consult").put("href", consultPath)
                    }
                }

                //if the patient is on the consult, show him the parent
                if(request.path().matches(Regex("/api/medical_office/self/physicians/\\d+/date/(.)+/consult")))
                {
                    (modified["_links"] as ObjectNode).putObject("parent").put("href", pdpServiceLocation.toASCIIString() + request.path().split("/consult")[0])
                }

                forwardedResponse = ServerResponse.status(response.statusCode).body(modified)
            }

            return forwardedResponse
        }

        //request was not processed
        return null;
    }

    private fun resolvePhysicianRole(identity: Auth.IdentityResponse, request: ServerRequest, next: HandlerFunction<ServerResponse>):
            ServerResponse?
    {
        //routes for physicians are the pdp routes without the /physicians/{id}, which is implied by the token
        //patient has access on all routes under /physicians/ path, except /physicians/
        if(!request.path().matches(
                //match child routes
                Regex("(/api/medical_office/self(/.*)*)")))
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
                object : ParameterizedTypeReference<CollectionModel<PhysicianResponseDTO?>?>() {
                }

            val response =
                kotlin.runCatching {
                    _restTemplate.exchange("$pdpServiceLocation/api/medical_office/physicians/?userId=" + identity.userId, HttpMethod.GET, HttpEntity(null, headers), responseType);
                }.getOrElse {
                    return if(it is HttpClientErrorException.NotFound)
                        ServerResponse.status(HttpStatus.NOT_FOUND).build()
                    else
                        throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR)
                }


            //either way construct the next route
            val physicianID =
                kotlin.runCatching {
                    response.body!!.content.elementAt(0)!!.physicianId
                }.getOrElse {
                    throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR)
                }


            //check if the physician want to create a consult without an appointment
            if(request.path().matches(Regex("/api/medical_office/self/patients/\\d+/date/(.)+/consult"))
                && request.method() == HttpMethod.PUT)
            {
                kotlin.runCatching {
                    //we do have interest only in status
                    _restTemplate.headForHeaders(pdpServiceLocation.toASCIIString() + request.path().split("/consult")[0].replace("/self", "/physicians/$physicianID"))
                }.onFailure {
                    return ServerResponse.status(HttpStatus.BAD_REQUEST).build()
                }
            }

            //pass to services
            var forwardedResponse = next.handle(
                ServerRequest.from(request)
                    .uri(URI(request.uri().parseServerAuthority().toASCIIString()
                        .replace("/self", "/physicians/$physicianID")))
                    .build())

            //we need to do some changes
            if(forwardedResponse.statusCode().is2xxSuccessful) {

                val forwardedResponseContent =
                    ContentCachingResponseWrapper((RequestContextHolder.getRequestAttributes() as ServletRequestAttributes?)!!.response!!)
                forwardedResponse.writeTo(request.servletRequest(), forwardedResponseContent) {
                    mutableListOf(
                        MappingJackson2HttpMessageConverter()
                    ) as List<HttpMessageConverter<*>>
                }

                val objectMapper = ObjectMapper()
                val modified = objectMapper.readValue(forwardedResponseContent.contentAsByteArray, JsonNode::class.java)

                //if the path is only self we must remove the parent link
                if (request.path() == "/api/medical_office/self") {
                    (modified["_links"] as ObjectNode).remove("parent")
                }

                modified["_links"].properties().forEach {
                    val node = it.value as ObjectNode
                    var textValue = node.get("href").textValue()
                    textValue =
                        textValue.replace(Regex("/api/medical_office/physicians/\\d"), "/api/medical_office/self")
                    node.remove("href")
                    node.put("href", textValue)
                }

                //notify on appointment route the physician, that it's possible to create a consult
                if(request.path().matches(Regex("/api/medical_office/self/patients/\\d+/date/(.)+")))
                {
                    //showing the route to create consult
                    (modified["_links"] as ObjectNode).putObject("consult").put("href", consultServiceLocation.toASCIIString() + request.path() + "/consult")

                }

                //if the physician is on the consult, show him the parent
                if(request.path().matches(Regex("/api/medical_office/self/patients/\\d+/date/(.)+/consult")))
                {
                    (modified["_links"] as ObjectNode).putObject("parent").put("href", pdpServiceLocation.toASCIIString() + request.path().split("/consult")[0])
                }

                forwardedResponse = ServerResponse.status(response.statusCode).body(modified)
            }
            return forwardedResponse
        }


        //request was not processed
        return null;
    }

}