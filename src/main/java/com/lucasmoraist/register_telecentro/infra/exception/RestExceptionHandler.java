package com.lucasmoraist.register_telecentro.infra.exception;

import com.lucasmoraist.register_telecentro.exceptions.ExceptionDTO;
import com.lucasmoraist.register_telecentro.exceptions.ResourceNotFound;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.io.FileNotFoundException;
import java.io.IOException;

@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ResourceNotFound.class)
    protected ResponseEntity<ExceptionDTO> handleResourceNotFound(ResourceNotFound ex) {
        return ResponseEntity.badRequest().body(
                new ExceptionDTO(ex.getMessage(), HttpStatus.NOT_FOUND));
    }

    @ExceptionHandler(IOException.class)
    protected ResponseEntity<ExceptionDTO> handleIOException(IOException ex) {
        return ResponseEntity.badRequest().body(
                new ExceptionDTO(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @ExceptionHandler(FileNotFoundException.class)
    protected ResponseEntity<ExceptionDTO> handleFileNotFoundException(FileNotFoundException ex) {
        return ResponseEntity.badRequest().body(
                new ExceptionDTO(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ExceptionDTO> handleException(Exception ex) {
        return ResponseEntity.badRequest().body(
                new ExceptionDTO(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR));
    }

}
