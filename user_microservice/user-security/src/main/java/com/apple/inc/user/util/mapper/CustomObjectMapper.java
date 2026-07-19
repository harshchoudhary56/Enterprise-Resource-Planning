//package com.apple.inc.user.util.mapper;
//
//import com.apple.inc.user.util.gson.SensitiveDataExclusionStrategy;
//import com.google.gson.Gson;
//import com.google.gson.GsonBuilder;
//import lombok.experimental.UtilityClass;
//import lombok.extern.slf4j.Slf4j;
//
//@Slf4j
//@UtilityClass
//public class CustomObjectMapper {
//
//    private static final Gson GSON = new GsonBuilder()
//            .serializeNulls()
//            .setExclusionStrategies(new SensitiveDataExclusionStrategy())
//            .create();
//
//    public static <R> String _toString(R request) {
//        return GSON.toJson(request);
//    }
//
//    public static <R> R _toObject(String request, Class<R> clazz) {
//        return GSON.fromJson(request, clazz);
//    }
//}
