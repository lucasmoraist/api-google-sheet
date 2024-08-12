package com.lucasmoraist.register_telecentro.exceptions;

import org.springframework.http.HttpStatus;

public record ExceptionDTO(String msg, HttpStatus status) {
}
