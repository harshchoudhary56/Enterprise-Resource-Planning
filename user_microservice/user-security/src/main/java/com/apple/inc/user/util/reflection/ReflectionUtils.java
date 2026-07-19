//package com.apple.inc.user.util.reflection;
//
//import com.apple.inc.user.util.annotations.ChecksumExclude;
//import com.apple.inc.user.dto.FieldParam;
//import lombok.experimental.UtilityClass;
//import org.apache.commons.collections4.CollectionUtils;
//import org.apache.commons.lang3.StringUtils;
//
//import java.lang.annotation.Annotation;
//import java.lang.reflect.AccessibleObject;
//import java.lang.reflect.Field;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Objects;
//
//@UtilityClass
//public class ReflectionUtils {
//
//    public static final String JAVA_LANG_PACKAGE = "java.lang";
//
//    public static List<FieldParam> getAllFields(Object object) throws IllegalAccessException {
//        if (Objects.isNull(object)) {
//            return Collections.emptyList();
//        }
//        if (object.getClass().isEnum()) {
//            return Collections.emptyList();
//        }
//        List<FieldParam> fieldParams = new ArrayList<>();
//        List<FieldParam> firstLevelAndParentFields = getFirstLevelAndParentFields(object);
//        if (!CollectionUtils.isEmpty(firstLevelAndParentFields)) {
//            for (FieldParam fieldParam : firstLevelAndParentFields) {
//                Field field = fieldParam.getField();
//                AccessibleObject.setAccessible(new AccessibleObject[] {field}, true);
//                if (!field.getType().getPackageName().contains(JAVA_LANG_PACKAGE) && !field.isAnnotationPresent(ChecksumExclude.class)) {
//                    List<FieldParam> nestedFields = getAllFields(field.get(object));
//                    if (!CollectionUtils.isEmpty(nestedFields)) {
//                        fieldParams.addAll(nestedFields);
//                    }
//                }
//            }
//        }
//        if (!CollectionUtils.isEmpty(firstLevelAndParentFields)) {
//            fieldParams.addAll(firstLevelAndParentFields);
//        }
//        return fieldParams;
//    }
//
//    private static List<FieldParam> getFirstLevelAndParentFields(Object object) {
//        if (Objects.isNull(object)) {
//            return Collections.emptyList();
//        }
//        if (object.getClass().isEnum()) {
//            return Collections.emptyList();
//        }
//        List<FieldParam> firstLevelAndParentFields = new ArrayList<>();
//        Class<?> currentClass = object.getClass();
//        while (currentClass != null && currentClass.getSuperclass() != null) {
//            List<Field> fields = new ArrayList<>(Arrays.asList(currentClass.getDeclaredFields()));
//            fields.forEach(field -> {
//                AccessibleObject.setAccessible(new AccessibleObject[] {field}, true);
//                firstLevelAndParentFields.add(new FieldParam(field, object));
//            });
//            currentClass = currentClass.getSuperclass();
//        }
//        return firstLevelAndParentFields;
//    }
//
//    public static String getValueByAnnotation(List<FieldParam> fieldParams, Class<? extends Annotation> annotation) throws IllegalAccessException {
//        for (FieldParam fieldParam : fieldParams) {
//            Field field = fieldParam.getField();
//            Object object = fieldParam.getObject();
//            if (Objects.nonNull(field.get(object)) &&
//                    field.isAnnotationPresent(annotation) &&
//                    !StringUtils.isEmpty(String.valueOf(field.get(object)).trim()) &&
//                    (field.get(object).getClass().isEnum() || field.getType().getPackageName()
//                            .contains(JAVA_LANG_PACKAGE))) {
//                return String.valueOf(field.get(object));
//            }
//        }
//        return null;
//    }
//
//    public static Map<Class<? extends Annotation>, String> annotatedMap(List<FieldParam> fieldParams)
//            throws IllegalAccessException {
//        Map<Class<? extends Annotation>, String> annotatedValueMap = new HashMap<>();
//        for (FieldParam fieldParam : fieldParams) {
//            Field field = fieldParam.getField();
//            Object object = fieldParam.getObject();
//            if (Objects.nonNull(field.get(object)) &&
//                    !StringUtils.isEmpty(String.valueOf(field.get(object)).trim()) &&
//                    (field.get(object).getClass().isEnum() || field.getType().getPackageName()
//                            .contains(JAVA_LANG_PACKAGE))) {
//                Annotation[] annotations = field.getAnnotations();
//                for (Annotation annotation : annotations) {
//                    annotatedValueMap.put(annotation.annotationType(),
//                            String.valueOf(field.get(object)));
//                }
//            }
//        }
//        return annotatedValueMap;
//    }
//}
