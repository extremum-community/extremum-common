package io.extremum.sharedmodels.constants;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class DateConstants {
    public static final String FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSZ";
    public static final DateTimeFormatter ZONED_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss zzz");
    public static final DateTimeFormatter ISO_8601_ZONED_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(FORMAT);
    public static final ZoneId ZONE = ZoneId.systemDefault();

    private DateConstants() {
        throw new UnsupportedOperationException();
    }
}
