package com.teamnote.meeting.dto;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

public record MeetingSearchRequest(
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
) {
}
