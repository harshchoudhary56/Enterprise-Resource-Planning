package com.apple.inc.user.util.date;

import lombok.experimental.UtilityClass;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@UtilityClass
public class DateUtils {

    private static final String DB_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    private static final DateTimeFormatter DB_DATE_FORMATTER = DateTimeFormatter.ofPattern(DB_DATE_FORMAT);

    public static String now() {
        return LocalDateTime.now().format(DB_DATE_FORMATTER);
    }

}
