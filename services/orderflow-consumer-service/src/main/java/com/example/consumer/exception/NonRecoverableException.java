package com.example.consumer.exception;

public class NonRecoverableException extends RuntimeException {
    public NonRecoverableException(String message) {
        super(message);
    }
}