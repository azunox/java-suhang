package com.teamnote.meeting.dto;

import java.time.LocalDateTime;

import com.teamnote.common.dto.StrictStringDeserializer;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import tools.jackson.databind.annotation.JsonDeserialize;

public record MeetingCreateRequest(
        @NotBlank(message = "회의 제목은 필수입니다.")
        @Size(max = 120, message = "회의 제목은 120자 이하여야 합니다.")
        @JsonDeserialize(using = StrictStringDeserializer.class)
        String title,
        @NotNull(message = "회의 시작 시각은 필수입니다.")
        @JsonDeserialize(using = MeetingDateTimeDeserializer.class)
        @Schema(implementation = String.class, description = "오프셋 없는 현지 시각", example = "2026-11-19T10:00:00")
        LocalDateTime startAt,
        @NotNull(message = "회의 종료 시각은 필수입니다.")
        @JsonDeserialize(using = MeetingDateTimeDeserializer.class)
        @Schema(implementation = String.class, description = "오프셋 없는 현지 시각", example = "2026-11-19T11:00:00")
        LocalDateTime endAt,
        @NotNull(message = "회의록은 필수입니다.")
        @JsonDeserialize(using = StrictStringDeserializer.class)
        String minutesMarkdown
) {
}
