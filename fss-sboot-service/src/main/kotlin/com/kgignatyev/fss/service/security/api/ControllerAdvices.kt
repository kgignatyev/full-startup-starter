package com.kgignatyev.fss.service.security.api

import com.kgignatyev.fss.service.UnauthorizedException
import com.kgignatyev.fss.service.ValidationException
import org.postgresql.util.PSQLException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.jdbc.BadSqlGrammarException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.servlet.NoHandlerFoundException
import java.sql.SQLException

@ControllerAdvice
class ControllerAdvices {

    val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    @ExceptionHandler(value = [UnauthorizedException::class])
    fun unauthorizedException(exception: UnauthorizedException): ResponseEntity<Any> {
        logger.error("UnauthorizedException: ${exception.message}", exception)
        return ResponseEntity("${exception.message}", HttpStatus.UNAUTHORIZED)
    }

    @ExceptionHandler(value = [IllegalStateException::class, IllegalArgumentException::class,
        Exception::class, RuntimeException::class, NoHandlerFoundException::class, PSQLException::class,
        ValidationException::class])
    fun ourExceptionsHandler(exception: Exception): ResponseEntity<Any> {
        logger.error("Exception: ${exception.message}", exception)
        val status = when (exception) {
            is IllegalStateException -> HttpStatus.PRECONDITION_FAILED
            is IllegalArgumentException -> HttpStatus.BAD_REQUEST
            is ValidationException -> HttpStatus.BAD_REQUEST
            else -> HttpStatus.INTERNAL_SERVER_ERROR
        }
        val message = when( exception ){
            is PSQLException -> "DB Error"
            is SQLException -> "DB Error"
            is BadSqlGrammarException -> "DB Error"
            else -> {
                if( exception.message == null ) exception.javaClass.name else exception.message!!
            }
        }
        return ResponseEntity(message, status)
    }
}
