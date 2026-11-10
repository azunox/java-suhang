package com.teamnote.meeting.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import tools.jackson.databind.annotation.JsonDeserialize;

public record MeetingCreateRequest(
        @NotBlank(message = "회의 제목은 필수입니다.")
        @Size(max = 120, message = "회의 제목은 120자 이하여야 합니다.")
        String title,
        @NotNull(message = "회의 시작 시각은 필수입니다.")
        @JsonDeserialize(using = MeetingDateTimeDeserializer.class)
        LocalDateTime startAt,
        @NotNull(message = "회의 종료 시각은 필수입니다.")
        @JsonDeserialize(using = MeetingDateTimeDeserializer.class)
        LocalDateTime endAt,
        @NotNull(message = "회의록은 필수입니다.")
        String minutesMarkdown
) {
}
