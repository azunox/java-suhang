package com.teamnote.meeting.dto;

import java.time.LocalDateTime;

public record MeetingResponse(
        Long id,
        Long projectId,
        String title,
        LocalDateTime startAt,
        LocalDateTime endAt,
        String minutesMarkdown
) {
}
