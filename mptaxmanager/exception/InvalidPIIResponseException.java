package com.nike.mptaxmanager.exception;

public class InvalidPIIResponseException extends RuntimeException {
    static final long serialVersionUID = 1L;

    public InvalidPIIResponseException(String message, Exception inner) {
        super(message, inner);
    }

    public InvalidPIIResponseException(String message) {
        super(message);
    }
}

