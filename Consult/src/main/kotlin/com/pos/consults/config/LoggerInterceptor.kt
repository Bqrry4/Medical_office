package com.pos.consults.config

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.ModelAndView
import java.util.logging.Logger

class LoggerInterceptor : HandlerInterceptor {

    private val logger = Logger.getLogger("methodLogger")
    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {

        logger.info("IN: { Host:" + request.getHeaders("Host").nextElement() + ", Method: " + request.method + ", Path: " + request.requestURI + ", Query:" + request.queryString +" }");

        return super.preHandle(request, response, handler)
    }

    override fun postHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        modelAndView: ModelAndView?
    ) {
        logger.info("OUT: {Method: " + request.method + ", Path: " + request.requestURI + ", Status: " + response.status + " }");
        super.postHandle(request, response, handler, modelAndView)
    }

}