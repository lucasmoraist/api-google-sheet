package com.lucasmoraist.register_telecentro.exceptions;

public class RgRegistered extends RuntimeException {
    public RgRegistered() {
        super("This RG is already registered in this course date");
    }
}
