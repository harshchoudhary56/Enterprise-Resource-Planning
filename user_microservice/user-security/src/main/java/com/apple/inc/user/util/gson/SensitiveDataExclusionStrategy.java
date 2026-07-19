//package com.apple.inc.user.util.gson;
//
//import com.apple.inc.user.util.annotations.Sensitive;
//import com.google.gson.ExclusionStrategy;
//import com.google.gson.FieldAttributes;
//
//public class SensitiveDataExclusionStrategy implements ExclusionStrategy {
//
//    @Override
//    public boolean shouldSkipField(FieldAttributes fieldAttributes) {
//        return fieldAttributes.getAnnotation(Sensitive.class) != null;
//    }
//
//    @Override
//    public boolean shouldSkipClass(Class<?> aClass) {
//        return false;
//    }
//}
