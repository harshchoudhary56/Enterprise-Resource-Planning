package com.apple.inc.user.exceptions;

import lombok.ToString;

@ToString
public class DefaultBaseError<T> implements IBaseError<T> {

    private String errorCode;
    private String userMessage;
    private String errorMessage;
    private T metaData;
    private boolean displayMsg;

    public DefaultBaseError(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public DefaultBaseError(String errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public DefaultBaseError(String errorCode, String errorMessage, String userMessage) {
        this(errorCode, errorMessage, userMessage, false);
    }

    public DefaultBaseError(String errorCode, String errorMessage, String userMessage, boolean displayMsg) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.userMessage = userMessage;
        this.displayMsg = displayMsg;
    }

    public DefaultBaseError(String errorCode, String errorMessage, String userMessage, T metadata) {
        this.errorCode = errorCode;
        this.userMessage = userMessage;
        this.errorMessage = errorMessage;
        this.metaData = metadata;
    }

    @Override
    public String getErrorCode() {
        return this.errorCode;
    }

    @Override
    public String getErrorMessage() {
        return this.errorMessage;
    }

    @Override
    public String getUserMessage() {
        return this.userMessage;
    }

    @Override
    public T getMetadata() {
        return this.metaData;
    }

    @Override
    public boolean displayMsg() {
        return this.displayMsg;
    }

    @Override
    public void setMetaData(T metaData) {
        this.metaData = metaData;
    }
}

