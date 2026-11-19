package com.teamnote.meeting.dto;

import java.time.LocalDateTime;

public record MeetingSearchRequest(LocalDateTime from, LocalDateTime to) {
}
