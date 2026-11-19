package com.teamnote.task.dto;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoField;

import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdScalarDeserializer;

final class TaskDateDeserializer extends StdScalarDeserializer<LocalDate> {

    private static final DateTimeFormatter FORMATTER = new DateTimeFormatterBuilder()
            .appendValue(ChronoField.YEAR, 4)
            .appendPattern("-MM-dd")
            .toFormatter()
            .withResolverStyle(ResolverStyle.STRICT);
    private static final String FORMAT_ERROR = "업무 마감일는 yyyy-MM-dd 형식의 문자열이어야 합니다.";

    TaskDateDeserializer() {
        super(LocalDate.class);
    }

    @Override
    public LocalDate deserialize(JsonParser parser, DeserializationContext context) {
        if (!parser.hasToken(JsonToken.VALUE_STRING)) {
            throw context.wrongTokenException(parser, LocalDate.class, JsonToken.VALUE_STRING, FORMAT_ERROR);
        }

        String value = parser.getString();
        try {
            return LocalDate.parse(value, FORMATTER);
        } catch (DateTimeParseException exception) {
            throw context.weirdStringException(value, LocalDate.class, FORMAT_ERROR);
        }
    }
}
