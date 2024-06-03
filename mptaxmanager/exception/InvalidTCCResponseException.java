package com.nike.mptaxmanager.exception;

public class InvalidTCCResponseException extends RuntimeException {
    static final long serialVersionUID = 1L;

    public InvalidTCCResponseException(String message, Exception inner) {
        super(message, inner);
    }

    public InvalidTCCResponseException(String message) {
        super(message);
    }
}

