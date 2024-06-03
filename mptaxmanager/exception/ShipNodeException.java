package com.nike.mptaxmanager.exception;

public class ShipNodeException extends RuntimeException {

    static final long serialVersionUID = 1L;

    public ShipNodeException(String message) {
        super(message);
    }

    public ShipNodeException(String message, Exception inner) {
        super(message, inner);
    }

}

