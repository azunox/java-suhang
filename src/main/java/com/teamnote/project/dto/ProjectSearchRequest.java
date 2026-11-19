package com.teamnote.project.dto;

import com.teamnote.project.entity.ProjectStatus;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ProjectSearchRequest(
        @Size(max = 120, message = "프로젝트 이름은 120자 이하여야 합니다.") String name,
        @Pattern(regexp = "PLANNED|IN_PROGRESS|COMPLETED", message = "프로젝트 상태가 올바르지 않습니다.") String status
) {

    public ProjectStatus projectStatus() {
        return status == null ? null : ProjectStatus.valueOf(status);
    }
}
