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
                route(RequestPredicates.path("/api/medical_office/patients/*"),
                    http(URI.create("")))
                    .filter(validateAccess())
            )
            .add(
                route(RequestPredicates.path("/api/medical_office/physicians/*"),
                    http(URI.create("")))
                    .filter(validateAccess())
            )
            .add(
                route(
                    RequestPredicates.path("/api/medical_office/physicians/*/patients/*/date/*/consult"),
                    http(URI.create(""))
                )
                    .andRoute(RequestPredicates.path("/api/medical_office/physicians/*/patients/*/date/*/consult/**"),
                        http(URI.create("")))
                    .filter(validateAccess())
            )
            .build()

    }

    private fun validateAccess(): (ServerRequest?, HandlerFunction<ServerResponse?>?) -> ServerResponse {
        return { request: ServerRequest?, next: HandlerFunction<ServerResponse?>? ->

            println("asfhasfoasi")
            ServerResponse.ok().build()
        }
    }


}


