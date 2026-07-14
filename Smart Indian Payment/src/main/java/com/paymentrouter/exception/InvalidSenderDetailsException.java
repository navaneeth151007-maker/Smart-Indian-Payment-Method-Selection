package com.paymentrouter.exception;

public class InvalidSenderDetailsException extends RuntimeException {

    public InvalidSenderDetailsException(String message) {
        super(message);
    }
}
