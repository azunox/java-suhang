package com.teamnote.meeting.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdScalarDeserializer;

final class MeetingDateTimeDeserializer extends StdScalarDeserializer<LocalDateTime> {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss").withResolverStyle(ResolverStyle.STRICT);
    private static final String FORMAT_ERROR = "회의 시각은 yyyy-MM-dd'T'HH:mm:ss 형식의 문자열이어야 합니다.";

    MeetingDateTimeDeserializer() {
        super(LocalDateTime.class);
    }

    @Override
    public LocalDateTime deserialize(JsonParser parser, DeserializationContext context) {
        if (!parser.hasToken(JsonToken.VALUE_STRING)) {
            throw context.wrongTokenException(parser, LocalDateTime.class, JsonToken.VALUE_STRING, FORMAT_ERROR);
        }

        String value = parser.getString();
        try {
            return LocalDateTime.parse(value, FORMATTER);
        } catch (DateTimeParseException exception) {
            throw context.weirdStringException(value, LocalDateTime.class, FORMAT_ERROR);
        }
    }
}
