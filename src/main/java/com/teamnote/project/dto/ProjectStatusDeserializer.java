package com.teamnote.project.dto;

import com.teamnote.project.entity.ProjectStatus;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdScalarDeserializer;

final class ProjectStatusDeserializer extends StdScalarDeserializer<ProjectStatus> {

    private static final String FORMAT_ERROR = "프로젝트 상태는 PLANNED, IN_PROGRESS, COMPLETED 중 하나여야 합니다.";

    ProjectStatusDeserializer() {
        super(ProjectStatus.class);
    }

    @Override
    public ProjectStatus deserialize(JsonParser parser, DeserializationContext context) {
        if (!parser.hasToken(JsonToken.VALUE_STRING)) {
            throw context.wrongTokenException(parser, ProjectStatus.class, JsonToken.VALUE_STRING, FORMAT_ERROR);
        }
        String value = parser.getString();
        try {
            return ProjectStatus.valueOf(value);
        } catch (IllegalArgumentException exception) {
            throw context.weirdStringException(value, ProjectStatus.class, FORMAT_ERROR);
        }
    }
}
