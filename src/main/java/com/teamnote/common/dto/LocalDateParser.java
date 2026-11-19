package com.teamnote.common.dto;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoField;

public final class LocalDateParser {

    private static final DateTimeFormatter FORMATTER = new DateTimeFormatterBuilder()
            .appendValue(ChronoField.YEAR, 4)
            .appendPattern("-MM-dd")
            .toFormatter()
            .withResolverStyle(ResolverStyle.STRICT);

    private LocalDateParser() {
    }

    public static LocalDate parse(String value) {
        return LocalDate.parse(value, FORMATTER);
    }
}
