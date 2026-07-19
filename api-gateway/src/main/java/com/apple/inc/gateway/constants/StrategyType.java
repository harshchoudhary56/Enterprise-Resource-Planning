package com.apple.inc.gateway.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum StrategyType {

    FIXED_WINDOW(StrategyBeanNames.FIXED_WINDOW),
    SLIDING_WINDOW(StrategyBeanNames.SLIDING_WINDOW),
    TOKEN_BUCKET(StrategyBeanNames.TOKEN_BUCKET),
    LEAKY_BUCKET(StrategyBeanNames.LEAKY_BUCKET);

    private final String value;

}
