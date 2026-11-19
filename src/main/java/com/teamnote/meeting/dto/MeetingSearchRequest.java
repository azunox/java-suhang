package com.teamnote.meeting.dto;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

public record MeetingSearchRequest(
        @Schema(implementation = String.class, description = "오프셋 없는 현지 시각", example = "2026-11-19T10:00:00")
        LocalDateTime from,
        @Schema(implementation = String.class, description = "오프셋 없는 현지 시각", example = "2026-11-19T11:00:00")
        LocalDateTime to
) {
}
