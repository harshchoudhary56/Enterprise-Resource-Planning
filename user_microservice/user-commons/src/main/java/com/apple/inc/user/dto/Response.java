package com.apple.inc.user.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

import static com.apple.inc.user.constants.ResponseCode.SUCCESS;

@Data
public class Response<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 4677408507953208206L;

    private String statusCode = SUCCESS.getCode();

    private String statusMessage = SUCCESS.getMessage();

    private T data;

    public Response(T data) {
        this.data = data;
    }

    public Response(String statusCode, String statusMessage) {
        this.statusCode = statusCode;
        this.statusMessage = statusMessage;
    }
}

