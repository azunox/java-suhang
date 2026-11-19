package com.teamnote.task.dto;

import com.teamnote.task.entity.TaskStatus;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdScalarDeserializer;

final class TaskStatusDeserializer extends StdScalarDeserializer<TaskStatus> {

    private static final String FORMAT_ERROR = "업무 상태는 TODO, IN_PROGRESS, DONE 중 하나여야 합니다.";

    TaskStatusDeserializer() {
        super(TaskStatus.class);
    }

    @Override
    public TaskStatus deserialize(JsonParser parser, DeserializationContext context) {
        if (!parser.hasToken(JsonToken.VALUE_STRING)) {
            throw context.wrongTokenException(parser, TaskStatus.class, JsonToken.VALUE_STRING, FORMAT_ERROR);
        }
        String value = parser.getString();
        try {
            return TaskStatus.valueOf(value);
        } catch (IllegalArgumentException exception) {
            throw context.weirdStringException(value, TaskStatus.class, FORMAT_ERROR);
        }
    }
}
