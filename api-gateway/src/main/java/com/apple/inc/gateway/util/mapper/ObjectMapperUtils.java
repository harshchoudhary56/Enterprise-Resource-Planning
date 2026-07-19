package com.apple.inc.gateway.util.mapper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class ObjectMapperUtils {

    private static final Gson GSON = new GsonBuilder()
            .serializeNulls()
            .create();

    public static <R> String _toString(R request) {
        return GSON.toJson(request);
    }

    public static <R> R _toObject(String request, Class<R> clazz) {
        return GSON.fromJson(request, clazz);
    }
}
