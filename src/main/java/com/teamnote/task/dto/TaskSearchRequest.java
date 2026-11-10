package com.teamnote.task.dto;

import java.time.LocalDate;

import com.teamnote.task.entity.TaskStatus;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

public record TaskSearchRequest(
        @Size(max = 80, message = "담당자 이름은 80자 이하여야 합니다.") String assigneeName,
        TaskStatus status,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDateFrom,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDateTo,
        Boolean overdue
) {
}
