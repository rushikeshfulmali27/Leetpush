package com.leethubai.common.util;

import java.time.*;
import java.time.format.DateTimeFormatter;

/**
 * Utility for date/timezone operations throughout the application.
 */
public final class DateUtils {

    private static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final ZoneOffset UTC = ZoneOffset.UTC;

    private DateUtils() {}

    /** Returns today's date in UTC. */
    public static LocalDate todayUtc() {
        return LocalDate.now(UTC);
    }

    /** Returns current instant in UTC. */
    public static Instant nowUtc() {
        return Instant.now();
    }

    /** Converts an Instant to a LocalDate in UTC. */
    public static LocalDate toLocalDate(Instant instant) {
        if (instant == null) return null;
        return instant.atZone(ZoneId.of("UTC")).toLocalDate();
    }

    /** Converts a LocalDate to the start-of-day Instant in UTC. */
    public static Instant toStartOfDay(LocalDate date) {
        if (date == null) return null;
        return date.atStartOfDay(ZoneId.of("UTC")).toInstant();
    }

    /** Formats a LocalDate as ISO string (yyyy-MM-dd). */
    public static String formatDate(LocalDate date) {
        return date == null ? null : date.format(ISO_DATE);
    }

    /** Gets the first day of a given year. */
    public static LocalDate startOfYear(int year) {
        return LocalDate.of(year, 1, 1);
    }

    /** Gets the last day of a given year. */
    public static LocalDate endOfYear(int year) {
        return LocalDate.of(year, 12, 31);
    }
}
