package com.lucasmoraist.register_telecentro.infra.exception;

import com.lucasmoraist.register_telecentro.exceptions.ExceptionDTO;
import com.lucasmoraist.register_telecentro.exceptions.ResourceNotFound;
import com.lucasmoraist.register_telecentro.exceptions.RgRegistered;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.io.FileNotFoundException;
import java.io.IOException;

@Slf4j
@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * Handles ResourceNotFound exceptions.
     *
     * @param ex the ResourceNotFound exception
     * @return a ResponseEntity containing the exception details
     */
    @ExceptionHandler(ResourceNotFound.class)
    protected ResponseEntity<ExceptionDTO> handleResourceNotFound(ResourceNotFound ex) {
        log.error("Resource not found: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(
                new ExceptionDTO(ex.getMessage(), HttpStatus.NOT_FOUND));
    }

    /**
     * Handles IOException exceptions.
     *
     * @param ex the IOException
     * @return a ResponseEntity containing the exception details
     */
    @ExceptionHandler(IOException.class)
    protected ResponseEntity<ExceptionDTO> handleIOException(IOException ex) {
        log.error("IO error occurred: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(
                new ExceptionDTO(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR));
    }

    /**
     * Handles FileNotFoundException exceptions.
     *
     * @param ex the FileNotFoundException
     * @return a ResponseEntity containing the exception details
     */
    @ExceptionHandler(FileNotFoundException.class)
    protected ResponseEntity<ExceptionDTO> handleFileNotFoundException(FileNotFoundException ex) {
        log.error("File not found: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(
                new ExceptionDTO(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR));
    }

    /**
     * Handles MailAuthenticationException exceptions.
     *
     * @param e the MailAuthenticationException
     * @return a ResponseEntity containing the exception details
     */
    @ExceptionHandler(MailAuthenticationException.class)
    protected ResponseEntity<ExceptionDTO> sendMailException(MailAuthenticationException e) {
        log.error("Mail authentication error: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                new ExceptionDTO(e.getMessage(), HttpStatus.BAD_REQUEST)
        );
    }

    /**
     * Handles RgRegistered exceptions.
     * @param ex the RgRegistered exception
     * @return a ResponseEntity containing the exception details
     */
    @ExceptionHandler(RgRegistered.class)
    protected ResponseEntity<ExceptionDTO> handleRgRegistered(RgRegistered ex) {
        log.error("RG already registered: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(
                new ExceptionDTO(ex.getMessage(), HttpStatus.BAD_REQUEST));
    }

    /**
     * Handles generic exceptions.
     *
     * @param ex the generic exception
     * @return a ResponseEntity containing the exception details
     */
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ExceptionDTO> handleException(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        return ResponseEntity.badRequest().body(
                new ExceptionDTO(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @ExceptionHandler(NullPointerException.class)
    protected ResponseEntity<ExceptionDTO> handleNullPointerException(NullPointerException ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        return ResponseEntity.badRequest().body(
                new ExceptionDTO(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR));
    }

}
