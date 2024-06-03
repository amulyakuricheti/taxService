package com.nike.mptaxmanager.exception;

public class InvalidRulesEngineResponseException extends RuntimeException {
    static final long serialVersionUID = 1L;

    public InvalidRulesEngineResponseException(String message, Exception inner) {
        super(message, inner);
    }

    public InvalidRulesEngineResponseException(String message) {
        super(message);
    }
}

