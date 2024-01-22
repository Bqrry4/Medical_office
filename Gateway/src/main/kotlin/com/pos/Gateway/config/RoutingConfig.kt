package com.pos.Gateway.config
import org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions.http
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.function.*
import org.springframework.web.servlet.function.RouterFunctions.route
import java.net.URI


@Configuration
class RoutingConfig {
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
        return { request: ServerRequest, next: HandlerFunction<ServerResponse> ->

            println("REstrict AccesS")
            next.handle(request)

        }
    }

    private fun validateAccess2(): (ServerRequest, HandlerFunction<ServerResponse>) -> ServerResponse {
        return { request: ServerRequest, next: HandlerFunction<ServerResponse> ->

            println("REstrict2 AccesS")
            next.handle(request)

        }
    }


}


