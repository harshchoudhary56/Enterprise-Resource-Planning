package com.apple.inc.user.exceptions;

import java.util.Map;

public class POBaseException extends RuntimeException {

    public final IBaseError<?> baseError;

    public POBaseException(IBaseError<?> baseError) {
        super(baseError.getErrorMessage());
        this.baseError = baseError;
    }

    public POBaseException(String errorMessage, Throwable t) {
        super(errorMessage, t);
        baseError = new DefaultBaseError<Map<String, String>>(errorMessage);
    }

    public POBaseException(String errorMessage) {
        super(errorMessage);
        baseError = new DefaultBaseError<Map<String, String>>(errorMessage);
    }

    public POBaseException(String errorCode, String errorMessage) {
        super(errorMessage);
        baseError = new DefaultBaseError<Map<String, String>>(errorCode, errorMessage);
    }

    public POBaseException(String errorCode, String errorMessage, Throwable t) {
        super(errorMessage, t);
        baseError = new DefaultBaseError<Map<String, String>>(errorCode, errorMessage);
    }

    public POBaseException(String errorCode, String errorMessage, String userMessage) {
        super(errorMessage);
        baseError = new DefaultBaseError<Map<String, String>>(errorCode, errorMessage, userMessage);
    }

    public POBaseException(IBaseError<?> baseError, Throwable t) {
        super(t);
        this.baseError = baseError;
    }
}

