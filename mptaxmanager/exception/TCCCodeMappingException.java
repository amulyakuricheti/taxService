package com.nike.mptaxmanager.exception;

/**
 * Copyright © 2021 Nike. All rights reserved.
 *
 * @Created by Sumant on 27/9/2021
 * Project Name : MPTaxManagerService
 * File Name : TCCCodeMappingException
 * Description : This class is to handle the exception occurred while binding tcc in taxManager Response
 */

public class TCCCodeMappingException extends RuntimeException {

    static final long serialVersionUID = 1L;

    public TCCCodeMappingException(String message) {
        super(message);
    }

    public TCCCodeMappingException(String message, Exception ex) {
        super(message, ex);
    }
}

