package com.teamnote.task.dto;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import com.teamnote.common.dto.LocalDateParser;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdScalarDeserializer;

final class TaskDateDeserializer extends StdScalarDeserializer<LocalDate> {

    private static final String FORMAT_ERROR = "업무 마감일은 yyyy-MM-dd 형식의 문자열이어야 합니다.";

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
            return LocalDateParser.parse(value);
        } catch (DateTimeParseException exception) {
            throw context.weirdStringException(value, LocalDate.class, FORMAT_ERROR);
        }
    }
}
