package com.apple.inc.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.lang.reflect.Field;

@Data
@AllArgsConstructor
public class FieldParam {

    private Field field;
    private Object object;
}
