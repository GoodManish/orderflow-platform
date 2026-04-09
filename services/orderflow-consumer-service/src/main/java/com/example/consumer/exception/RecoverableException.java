package com.example.consumer.exception;

public class RecoverableException extends RuntimeException {
    public RecoverableException(String message) {
        super(message);
    }
}