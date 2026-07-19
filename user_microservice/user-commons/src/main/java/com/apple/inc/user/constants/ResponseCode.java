package com.apple.inc.user.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ResponseCode {

    SUCCESS("SPG-0000", "SUCCESS", "SUCCESS"),
    FAILED("SPG-0001", "FAILED", "FAILED"),
    PENDING("SPG-0002", "PENDING", "PENDING");

    private final String code;

    private final String message;

    private final String userMessage;
}
