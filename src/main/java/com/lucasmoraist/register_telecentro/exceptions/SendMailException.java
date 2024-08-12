package com.lucasmoraist.register_telecentro.exceptions;

public class SendMailException extends RuntimeException {
    public SendMailException() {
        super("Error sending email. Please verify the email address.");
    }
}
