package com.pos.PDPdb.controller

import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.config.annotation.EnableWebMvc
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import java.sql.SQLException
import java.sql.SQLIntegrityConstraintViolationException
import java.sql.SQLTransientConnectionException
import java.text.ParseException

@RestControllerAdvice
@EnableWebMvc
class GlobalExceptionHandler : ResponseEntityExceptionHandler() {

    @ExceptionHandler(SQLTransientConnectionException::class)
    fun handleSqlTriggerErr(ex: SQLException, request: WebRequest) : ResponseEntity<Any> {
        return handleExceptionInternal(
            ex, ex.message,
            HttpHeaders(), HttpStatus.UNPROCESSABLE_ENTITY, request
        )!!
    }

    @ExceptionHandler(ParseException::class)
    fun handleGenericParseErr(ex: SQLException, request: WebRequest) : ResponseEntity<Any> {
        return handleExceptionInternal(
            ex, ex.message,
            HttpHeaders(), HttpStatus.UNPROCESSABLE_ENTITY, request
        )!!
    }

    @ExceptionHandler(SQLIntegrityConstraintViolationException::class)
    fun handleSqlCheckErr(ex: SQLException, request: WebRequest) : ResponseEntity<Any> {
        return handleExceptionInternal(
            ex, ex.message,
            HttpHeaders(), HttpStatus.CONFLICT, request
        )!!
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleFormatErr(ex: IllegalArgumentException, request: WebRequest) : ResponseEntity<Any> {
        return handleExceptionInternal(
            ex, ex.message,
            HttpHeaders(), HttpStatus.BAD_REQUEST, request
        )!!
    }
}