package com.extremum.common.utils;

import lombok.extern.java.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.Locale;

/**
 * @author iPolyakov on 03.02.15.
 */
@Log
public final class DateUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(DateUtils.class);

    public static final String FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    public static final String FORMAT_PATTERN = "[\\d]{4}-[\\d]{2}-[\\d]{2}T[\\d]{2}:[\\d]{2}:[\\d]{2}\\.[\\d]{3}-[\\d]{4}";

    public static final DateTimeFormatter ZONED_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss zzz");

    public static final DateTimeFormatter ISO_8601_ZONED_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(FORMAT);
    public static final ZoneId ZONE = ZoneId.systemDefault();

    public static SimpleDateFormat dateFormat() {
        return new SimpleDateFormat(FORMAT, Locale.US);
    }

    public static String convert(Date from) {
        return from != null ? dateFormat().format(from) : null;
    }

    public static String convert(ZonedDateTime from) {
        return from.format(DateTimeFormatter.ofPattern(FORMAT));
    }

    public static Date convert(String from) {
        if (from != null) {
            try {
                return dateFormat().parse(from);
            } catch (ParseException e) {
                LOGGER.debug("cannot convert Date", e);
            }
        }
        return null;
    }

    /**
     * Parsing string in format "EEE, dd MMM yyyy HH:mm:ss zzz"
     */
    public static ZonedDateTime parseZonedDateTime(String date) {
        return parseZonedDateTime(date, ZONED_DATE_TIME_FORMATTER);
    }

    /**
     * Parsing string in format "yyyy-MM-dd'T'HH:mm:ss.SSSZ"
     */
    public static ZonedDateTime parseZonedDateTimeFromISO_8601(String date) {
        return parseZonedDateTime(date, ISO_8601_ZONED_DATE_TIME_FORMATTER);
    }

    public static String formatZonedDateTimeISO_8601(ZonedDateTime date){
        return date.format(ISO_8601_ZONED_DATE_TIME_FORMATTER);
    }

    public static ZonedDateTime parseZonedDateTime(String date, DateTimeFormatter formatter) {
        if (date != null) {
            try {
                return ZonedDateTime.parse(date, formatter);
            } catch (DateTimeParseException e) {
                LOGGER.error("Cannot parse ZonedDateTime", e);
            }
        }
        return null;
    }

    public static ZonedDateTime now() {
        return ZonedDateTime.now();
    }

    public static ZonedDateTime fromInstant(Instant instant) {
        return fromInstant(instant, ZoneOffset.UTC);
    }

    private static ZonedDateTime fromInstant(Instant instant, ZoneOffset zoneOffset) {
        return ZonedDateTime.ofInstant(instant, zoneOffset);
    }

    private DateUtils() {}
}
