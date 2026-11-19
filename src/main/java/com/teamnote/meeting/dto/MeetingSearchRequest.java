package com.teamnote.meeting.dto;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

public record MeetingSearchRequest(
        @DateTimeFormat(pattern = "uuuu-MM-dd'T'HH:mm:ss") LocalDateTime from,
        @DateTimeFormat(pattern = "uuuu-MM-dd'T'HH:mm:ss") LocalDateTime to
) {
}
