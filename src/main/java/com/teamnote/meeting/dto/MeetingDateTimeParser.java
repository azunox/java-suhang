package com.teamnote.meeting.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoField;

public final class MeetingDateTimeParser {

    private static final DateTimeFormatter FORMATTER = new DateTimeFormatterBuilder()
            .appendValue(ChronoField.YEAR, 4)
            .appendPattern("-MM-dd'T'HH:mm:ss")
            .toFormatter()
            .withResolverStyle(ResolverStyle.STRICT);

    private MeetingDateTimeParser() {
    }

    public static LocalDateTime parse(String value) {
        return LocalDateTime.parse(value, FORMATTER);
    }
}
