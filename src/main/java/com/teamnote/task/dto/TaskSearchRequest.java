package com.teamnote.task.dto;

import java.time.LocalDate;

import com.teamnote.task.entity.TaskStatus;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

public record TaskSearchRequest(
        @Size(max = 80, message = "담당자 이름은 80자 이하여야 합니다.") String assigneeName,
        @Pattern(regexp = "TODO|IN_PROGRESS|DONE", message = "업무 상태가 올바르지 않습니다.") String status,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDateFrom,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDateTo,
        Boolean overdue
) {

    public TaskStatus taskStatus() {
        return status == null ? null : TaskStatus.valueOf(status);
    }
}
